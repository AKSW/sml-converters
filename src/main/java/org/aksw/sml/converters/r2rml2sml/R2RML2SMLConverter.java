package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_URI;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDF;

public class R2RML2SMLConverter {

    public static List<ViewDefinition> convert(Model r2rmlMappings) {
        R2RMLSpec r2rmlSpecs = new R2RMLSpec(r2rmlMappings);
        Multimap<LogicalTable, TriplesMap> tableToTm = HashMultimap.create();

        // Group triple maps by their logical table
        for (TriplesMap triplesMap : r2rmlSpecs.getTripleMaps()) {
            LogicalTable logicalTable = triplesMap.getLogicalTable();

            tableToTm.put(logicalTable, triplesMap);
        }

        List<ViewDefinition> result = new ArrayList<ViewDefinition>();

        // Let's create view definitions
        for (Entry<LogicalTable, Collection<TriplesMap>> entry : tableToTm.asMap().entrySet()) {
            LogicalTable logicalTable = entry.getKey();
            Collection<TriplesMap> triplesMaps = entry.getValue();
            String name = logicalTable + "" + triplesMaps;
            QuadPattern template = new QuadPattern();

            for (TriplesMap triplesMap : triplesMaps) {
                SubjectMap subjectMap = triplesMap.getSubjectMap();
                RDFNode rrClass = subjectMap.getRrClass();
                Multimap<Var, RestrictedExpr> varToExprs = HashMultimap.create();

                String templateString = subjectMap.getTemplate();
                E_StrConcatPermissive e = RRUtils.parseTemplate(templateString);
                Expr pkExpr = new E_URI(e);
                RestrictedExpr restExpr = new RestrictedExpr(pkExpr);
                Generator genS = Gensym.create("S");
                Var varS = Var.alloc(genS.next());
                Var subjectVar = varS;
                varToExprs.put(varS, restExpr);

                VarDefinition varDef = new VarDefinition(varToExprs);

                if (rrClass != null) {
                    template.add(new Quad(Quad.defaultGraphNodeGenerated, varS,
                            RDF.type.asNode(), rrClass.asNode()));
                }
                Generator genO = Gensym.create("O");

                for (PredicateObjectMap pom : triplesMap.getPredicateObjectMaps()) {
                    // FIXME: the actual evaluation of the predicate and object
                    // map is missing here...
                    for (ObjectMap om : pom.getObjectMap()) {

                        varDef = getVarDefinition(template, varToExprs,
                                subjectVar, varDef, pom, om, genO);
                    }
                }

                SqlOp op;
                // Create the table node
                if (logicalTable.isTableName()) {
                    String tableName = logicalTable.getTableName();
                    op = new SqlOpTable(new SchemaImpl(), tableName);

                } else {
                    throw new RuntimeException("Not implemented");
                }

                Mapping mapping = new Mapping(varDef, op);
                ViewDefinition viewDef = new ViewDefinition(name,template, null, mapping, entry);

                result.add(viewDef);
            }
        }

        return result;
    }

    /** @author sherif */
    public static VarDefinition getVarDefinition(QuadPattern template,
            Multimap<Var, RestrictedExpr> varToExprs, Var subjectVar,
            VarDefinition varDef, PredicateObjectMap pom, ObjectMap om, Generator genO) {

        ExprList tableExprList = new ExprList();
        E_Function ef;
        RDFNode datatype = om.getDatatype();
        RDFNode languageTag = om.getLanguageTag();
        String term = getObjectMapTerm(om);

        if (term != null){
            tableExprList.add(new ExprVar(Var.alloc(term)));

            if (datatype != null) {
                tableExprList.add(NodeValue.makeNode(datatype.asNode()));
                ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);

            } else if (languageTag != null) {
                tableExprList.add(NodeValue.makeNode(languageTag.asNode())); 
                ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);

            } else {
                ef = new E_Function(SparqlifyConstants.plainLiteralLabel,tableExprList);
            }

            RestrictedExpr tableRestExpr = new RestrictedExpr(ef);
            Var varO = Var.alloc(genO.next());
            varToExprs.put(varO, tableRestExpr);
            varDef = new VarDefinition(varToExprs);
            template.add(new Quad(Quad.defaultGraphNodeGenerated , subjectVar,pom.getPredicate().asNode() , varO));
        }
        return varDef;
    }

    public static String getObjectMapTerm(ObjectMap om) {

        switch (om.getTermSpec()) {
            case COLUMN: {
                return om.getColumnName();
            }
            case CONSTANT: {
                return om.getConstant();
            }
            case TEMPLATE: {
                return om.getTemplate();
            }
            case JOIN: {
                return null;
            } // NOT SUPPORTED YET
            default: {
                throw new RuntimeException("Not supported TermSpec");
            }
        }
    }
}
