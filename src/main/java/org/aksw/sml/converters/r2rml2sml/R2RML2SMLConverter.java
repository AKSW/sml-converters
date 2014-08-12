package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.domain.input.ViewReference;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_BNode;
import com.hp.hpl.jena.sparql.expr.E_Datatype;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrDatatype;
import com.hp.hpl.jena.sparql.expr.E_StrLang;
import com.hp.hpl.jena.sparql.expr.E_URI;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * TODO: handle graphs  #6
 * TODO: normalize rr:graph --> rr:graphMap  #3
 * TODO: normalize rr:subject --> rr:subjectMap  #3
 * TODO: normalize rr:predicate --> rr:predicateMap  #3
 * TODO: normalize rr:object --> rr:objectMap  #3
 * @author patrick
 *
 */
public class R2RML2SMLConverter {
    private static ViewDefNameService viewDefNameService = new ViewDefNameService();
    // will be used to generate SML quad pattern variables like ?s1, ?s2 etc
    private static final String subjectVarNamePrefix = "s";
    private static final String predicateVarNamePrefix = "p";
    private static final String objectvarNamePrefix = "o";

    protected static Multimap<LogicalTable, TriplesMap> buildTblToTM(R2RMLSpec r2rmlSpec) {
        Multimap<LogicalTable, TriplesMap> tbl2TM = HashMultimap.create();

        // Group triples maps by their logical table
        for (TriplesMap triplesMap : r2rmlSpec.getTriplesMaps()) {
            tbl2TM.put(triplesMap.getLogicalTable(), triplesMap);
        }

        return tbl2TM;
    }

    /**
     * Extracts the following information from the triples map's logical table:
     * - the table type (table or query)
     * - the actual table expression (table name or query expression)
     * and adds this information to the view definition info container object
     * 
     * @param tbl the triples map's logical table object
     * @param viewDefInfo a container holding gathered information to
     *      instantiate a ViewDefinition object later
     */
    protected static void extractTblInfoFromLogicalTbl(LogicalTable tbl, ViewDefinitionInfo viewDefInfo) {
        SqlOp logSMLTbl;
        if (tbl.isTable()) {
            // TODO: see if this null schema approach works
            logSMLTbl = new SqlOpTable(null, tbl.getTableExpression());
        } else {
            // can only be table or query --> if not an exception would have
            // been raised during initialization of the LogicalTable object
            // TODO: see if this null schema approach works
            logSMLTbl = new SqlOpQuery(null, tbl.getTableExpression());
        }
        viewDefInfo.from = logSMLTbl;
    }

    protected static Expr buildTermConstructor(TermMap termMap) {
        Expr inner = null;
        ExprFunction outer = null;
        if (termMap.isTemplateTermMap()) {
            String templateString = termMap.getTemplateTerm().getLexicalForm();
            inner = RRUtils.parseTemplate(templateString);

        } else if (termMap.isColumnTermMap()) {
            inner = NodeValue.makeString("?" + termMap.getColumnTerm());
        }

        // URI term constructor
        if (termMap.getTermType().equals(RR.IRI)) {
            outer = new E_URI(inner);

        // bNode term constructor
        } else if (termMap.getTermType().equals(RR.BlankNode)) {
            outer = new E_BNode(inner);

        // plainLiteral / typedLiteral term constructor
        } else if (termMap.getTermType().equals(RR.Literal)) {
            // plain literal with language tag
            if (termMap.hasLanguage()) {
                // FIXME: what about variable language tags???
                E_Lang lang = new E_Lang(NodeValue.makeString(termMap
                        .getLanguage().getLexicalForm()));
                outer = new E_StrLang(inner, lang);

            // typed literal
            } else if (termMap.hasDatatype()) {
                // FIXME: what about variable datatypes???
                E_Datatype type = new E_Datatype(
                        NodeValue.makeString(termMap.getDatatype().getURI()));
                outer = new E_StrDatatype(inner, type);

            // plain literal wo language tag
            } else {
                outer = new E_Str(inner);
            }
        }
        return outer;
    }

    /**
     * Extracts information from a given subject map and feeds view definition
     * info object (which is used to create a view definition later)
     * @param subjectMap the considered subject map
     * @param varName a variable name that can be used in case the subject map
     *      is variable
     * @param viewDefInfo a container holding gathered information to
     *      instantiate a ViewDefinition object later
     */
    protected static void extractInfoFromSubjectMap(TermMap subjectMap,
            String varName, ViewDefinitionInfo viewDefInfo) {
        Node subject = createNodeFromTermMap(subjectMap, varName);
        viewDefInfo.quadPatternInfo.addSubject(subject);
        if (subject.isVariable()) {
            viewDefInfo.termConstructors.put(subject, buildTermConstructor(subjectMap));
        }
    }

    /**
     * Returns a node used in the SML view definition's quad pattern. Such a
     * node can be either a variable, a URI, a blank node or a literal node:
     * - variable --> if rr:constant is used in the term map 
     * - URI --> if rr:constant is not used and term type is URI
     * - blank node --> if rr:constant is not used and term type is rr:BlankNode
     * - literal --> if rr:constant is not used and term 
     */
    protected static Node createNodeFromTermMap(TermMap termMap, String varName) {
        Node node = null;

        // var
        if (!termMap.isConstantTermMap()) {
            node = new Node_Variable(varName);

        } else {
        // URI
            if (termMap.getTermType().equals(RR.IRI)) {
                RDFNode term = termMap.getTerm();
                if (term.isURIResource()) {
                    node = NodeFactory.createURI(term.asResource().getURI());

                } else if (term.isLiteral()) {
                    node = NodeFactory.createURI(term.asLiteral().getLexicalForm());
                }

        // blank node
            } else if (termMap.getTermType().equals(RR.BlankNode)) {
                String bNodeId = termMap.getTerm().asLiteral().getLexicalForm();
                node = NodeFactory.createAnon(new AnonId(bNodeId));

        // literal
            } else if (termMap.getTermType().equals(RR.Literal)) {
                String termValue = termMap.getConstantTerm().asLiteral().getLexicalForm();

                // typed with datatype
                if (termMap.hasDatatype()) {
                    RDFDatatype datatype = XSDUtils.getDatatype(termMap.getDatatype());
                    node = NodeFactory.createLiteral(termValue, datatype);

                // plain with language tag
                } else if (termMap.hasLanguage()) {
                    String lang = termMap.getLanguage().getLexicalForm();
                    node = NodeFactory.createLiteral(termValue, lang, false);
                
                // plain without language tag
                } else {
                    node = NodeFactory.createLiteral(termValue);
                }
            }
        }

        return node;
    }

    protected static QuadPattern buildQuadPattern(QuadPatternInfo quadPatternInfo) {
        QuadPattern quadPattern = new QuadPattern();

        for (Node graph : quadPatternInfo.getGraphs()) {
            for (Node subject : quadPatternInfo.getSubjectsOfGraph(graph)) {
                for (Node predicate : quadPatternInfo.getPredicateOfGraphSubject(graph, subject)) {
                    for (Node object : quadPatternInfo.getObjectOfGraphSubjectPredicate(graph, subject, predicate)) {
                        quadPattern.add(new Quad(graph, subject, predicate, object));
                    }
                }
            }
        }

        return quadPattern;
    }

    protected static ViewDefinition buildViewDefFromInfo(ViewDefinitionInfo viewDefInfo) {
        Multimap<Var, RestrictedExpr> varToExprs = HashMultimap.create();
        
        for (Node node : viewDefInfo.termConstructors.keySet()) {
            Var var = Var.alloc(((Node_Variable) node).getName());
            RestrictedExpr restr = new RestrictedExpr(viewDefInfo.termConstructors.get(node));
            varToExprs.put(var, restr);
        }

        VarDefinition varDef = new VarDefinition(varToExprs);

        QuadPattern quadPattern = buildQuadPattern(viewDefInfo.quadPatternInfo);
        Map<String, ViewReference> viewReferences = null;
        Mapping mapping = new Mapping(varDef, viewDefInfo.from);
        RestrictionManagerImpl varRestrictions = new RestrictionManagerImpl();

        return new ViewDefinition(viewDefInfo.name, quadPattern, null, mapping, null);
    }

    protected static ViewDefinition buildViewDef(LogicalTable tbl, Collection<TriplesMap> trplsMaps) {
        String viewDefName = "";

        int subjVarNameCounter = 1;
        ViewDefinitionInfo viewDefInfo = null;

        for (TriplesMap triplesMap : trplsMaps) {
            /*
             *  build view definition name; sth. like "triplesMap1" or
             *  "triplesMap2_triplesMap3" (in case two triples maps use the
             *  same table and can therefore be consolidated in one view
             *  definition)
             */
            if (viewDefName != "") viewDefName = viewDefName + "_";
            viewDefName = viewDefName +
                    viewDefNameService.getNameFromUri(triplesMap.getResource().getURI());
            
            /* create a new view definition info object which is fed with all
             * the information needed to instantiate a ViewDefinition object
             * later
             */
            viewDefInfo = new ViewDefinitionInfo();
            viewDefInfo.name = viewDefName;
            
            /*
             * The plan:
             * 1) Extract all information from
             *    a) the table
             *    b) the subject map
             *    c) the predicate object maps
             *    and feed it to the viewDefInfo object.
             * 2) Generate view definition based on the viewDefInfo object
             */

            // 1a) get table information
            extractTblInfoFromLogicalTbl(tbl, viewDefInfo);

            // 1b) get subject map information
            String subjVarName = subjectVarNamePrefix + subjVarNameCounter;
            subjVarNameCounter++;

            SubjectMap subjectMap = triplesMap.getSubjectMap();
            Node subject = createNodeFromTermMap(subjectMap, subjVarName);
            viewDefInfo.quadPatternInfo.addSubject(subject);

            if (subject.isVariable()) {
                viewDefInfo.termConstructors.put(subject, buildTermConstructor(subjectMap));
            }

            // 1c)
            for (PredicateObjectMap predObjMap : triplesMap.getPredicateObjectMaps()) {
                /*
                 * Just to remember (http://www.w3.org/TR/r2rml/#dfn-predicate-object-map):
                 * A predicate-object map is represented by a resource that
                 * references the following other resources:
                 * 
                 * - One or more predicate maps. Each of them may be specified
                 *   in one of two ways:
                 *   1) using the rr:predicateMap property, whose value MUST be
                 *      a predicate map, or
                 *   2) using the constant shortcut property rr:predicate.
                 * 
                 * - One or more object maps or referencing object maps. Each
                 *   of them may be specified in one of two ways:
                 *   1) using the rr:objectMap property, whose value MUST be
                 *      either an object map, or a referencing object map.
                 *   2) using the constant shortcut property rr:object.
                 */
                List<PredicateMap> predicateMaps =
                        new ArrayList<PredicateMap>(predObjMap.getPredicateMaps());

                List<ObjectMap> objectMaps =
                        new ArrayList<ObjectMap>(predObjMap.getObjectMaps());

                int predVarNameCounter = 1;

                for (PredicateMap predicateMap : predicateMaps) {
                    String predVarName = predicateVarNamePrefix + predVarNameCounter;
                    predVarNameCounter++;

                    Node predicate = createNodeFromTermMap(predicateMap, predVarName);
                    viewDefInfo.quadPatternInfo.addPredicateToSubject(subject, predicate);

                    if (predicate.isVariable()) {
                        viewDefInfo.termConstructors.put(predicate, buildTermConstructor(predicateMap));
                    }
                    

                    int objVarNameCounter = 1;

                    for (ObjectMap objectMap : objectMaps) {
                        String objectVarName = objectvarNamePrefix + objVarNameCounter;
                        objVarNameCounter++;

                        Node object = createNodeFromTermMap(objectMap, objectVarName);
                        viewDefInfo.quadPatternInfo.addObjectToSubjectPredicate(subject, predicate, object);

                        if (object.isVariable()) {
                            viewDefInfo.termConstructors.put(object, buildTermConstructor(objectMap));
                        }
                    }
                }
            }
        }

        // 2)
        ViewDefinition viewDef = buildViewDefFromInfo(viewDefInfo);

        return viewDef;
    }

    public static List<ViewDefinition> convert(Model r2rmlMappings) {
        R2RMLSpec r2rmlSpec = new R2RMLSpec(r2rmlMappings);
        Multimap<LogicalTable, TriplesMap> tbl2TM = buildTblToTM(r2rmlSpec);

        List<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();

        Set<LogicalTable> referencedTables = tbl2TM.keySet();
        for (LogicalTable tbl : referencedTables) {
            Collection<TriplesMap> tMaps = tbl2TM.get(tbl);
            
            ViewDefinition viewDef = buildViewDef(tbl, tMaps);
            viewDefs.add(viewDef);
        }

        return viewDefs;
    }

    @Deprecated
    public static Map<String, ViewDefinition> convert2NameHash(Model r2rmlMappings) {
        Map<String, ViewDefinition> nameViewDefs = new HashMap<String, ViewDefinition>();
        
        List<ViewDefinition> viewDefs = convert(r2rmlMappings);
        
        for (ViewDefinition viewDef : viewDefs) {
            nameViewDefs.put(viewDef.getName(), viewDef);
        }
        
        return nameViewDefs;
    }
}
