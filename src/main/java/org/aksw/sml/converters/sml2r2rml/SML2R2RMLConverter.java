package org.aksw.sml.converters.sml2r2rml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sml.converters.errors.SMLVocabException;
import org.aksw.sml.converters.r2rml2sml.QuadPatternInfo;
import org.aksw.sml.converters.vocabs.RR;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;

// just needed because I know no better way to let a method return multiple values
class PredicateAndObject {
    Property predicate;
    RDFNode object;

    PredicateAndObject(Property predicate, RDFNode object) {
        this.predicate = predicate;
        this.object = object;
    }
}

public class SML2R2RMLConverter {

    private static int triplesMapIdCounter = 1;
    private static final BigDecimal BNODE = new BigDecimal(0);
    private static final BigDecimal URI = new BigDecimal(1);
    private static final BigDecimal PLAINLITERAL = new BigDecimal(2);
    private static final BigDecimal TYPEDLITERAL = new BigDecimal(3);


    /**
     * The actual export method returning an RDF model
     *
     * @return a com.hp.hpl.jena.rdf.model.Model representing the R2RML
     *     structure
     * @throws SMLVocabException
     */
    public static Model convert(final Collection<ViewDefinition> viewDefs) throws SMLVocabException {
        Model r2rml = ModelFactory.createDefaultModel();

        for (ViewDefinition viewDef : viewDefs) {
            exportViewDef(viewDef, r2rml);
        }
        return r2rml;
    }

    /**
     * Derives an R2RML graph from the given SML view definition input
     *
     * @param viewDef a SML view definition
     * @param r2rml a jena model representing the R2RML structure that will be built up
     * @throws SMLVocabException
     */
    private static void exportViewDef(ViewDefinition viewDef, Model r2rml) throws SMLVocabException {
        SqlOpBase relation = (SqlOpBase) viewDef.getMapping().getSqlOp();
        List<Quad> patterns = viewDef.getTemplate().getList();
        VarDefinition varDefs = viewDef.getMapping().getVarDefinition();

        for (Quad pattern : patterns) {
            exportPattern(pattern, relation, varDefs, r2rml);
        }
    }

    /**
     * Builds up the RDF model "r2ml" representing the R2RML structure of the
     * given SML definitions. At this point only parts of the SML view
     * definitions are considered. These are  quads (graph, subject, predicate,
     * object) that can be valid resources or variables with certain
     * restrictions derived from SML expressions like
     * "uri(concat("http://panlex.org/plx/", ?li))" meaning that the actual
     * value is constructed by creating a URI based on the concatenation of
     * "http://panlex.org/plx/" and the value of the "li" column in the current
     * line of the table and database at hand.
     *
     * @param r2rml the target jena model
     * @param quad a quad that may contain variables in the subject,
     *      predicate or object position
     * @param relation the considered database table or constructed logical
     *      relation defined by a query or view
     * @param varDefs the construction definition of the target value based in
     *      the actual database value and some additional data like prefix
     *      strings or certain functions like uri( ... )
     * @throws SMLVocabException
     */
    private static void exportPattern(Quad quad, SqlOpBase relation,
            VarDefinition varDefs, Model r2rml) throws SMLVocabException {
        /*
         * Just some hints concerning the variable names: I will try to be as
         * consistent as possible obeying the following rules:
         * - there is always a scope of a triple that defines the current
         *   subject predicate and object
         * - since triples can be nested it may be ambiguous what the
         *   current subject, predicate or object is
         * - since triples in R2RML refer to a subject ("rr:subjectMap"),
         *   predicate or object ("predicateObjectMap") things become even
         *   more unclear
         * - I will determine a scope based on the its subject, so if the
         *   subject is "foo", "fooSubject" is the subject in the "foo" scope.
         *   "fooPredicate" is the predicate in the "foo" scope and so on.
         * - since there may be several predicates and objects I will append a
         *   hint stating the special use of the considered part of a triple,
         *   so "fooPredicate_bar" is the predicate in the "foo" scope to
         *   define "bar"
         * - statements are named the same way: <scope>Statement_<use>
         */

        // create the triples map subject
        String triplesMapId = String.format("#TriplesMap%d", triplesMapIdCounter++);
        Resource triplesMapSubject = ResourceFactory.createResource(triplesMapId);

        /*
         * logical table
         */
        Property triplesMapPredicate_logicalTable = RR.logicalTable;

        Statement logicalTblStatement_tblDefinition = buildLogicalTableTriple(relation, r2rml);
        r2rml.add(logicalTblStatement_tblDefinition);

        Statement triplesMapStatement_logicalTable = r2rml.createStatement(
                triplesMapSubject, triplesMapPredicate_logicalTable,
                logicalTblStatement_tblDefinition.getSubject());
        r2rml.add(triplesMapStatement_logicalTable);

        /*
         * subject map
         */
        Node subjectMapObject_templColOrConst = quad.getSubject();
        List<Statement> triplesMapStatement_subjectMaps = buildTermMapStatements(
                subjectMapObject_templColOrConst, varDefs, r2rml);

        // there may be more than one entry, e.g.
        // [] rr:template "http://data.example.com/department/{DEPTNO}" and
        // [] rr:class ex:Department
        for (Statement statement : triplesMapStatement_subjectMaps) {
            r2rml.add(statement);
        }

        // build up the subject map triple that looks sth. like this:
        // <#TriplesMap2> rr:subjectMap []
        if (triplesMapStatement_subjectMaps.size() > 0) {
            // rr:subjectMap
            Property triplesMapPredicate_subjectMap = RR.subjectMap;
            // []
            RDFNode triplesMapObject_subjectMap =
                    triplesMapStatement_subjectMaps.get(0).getSubject();
            // <#TriplesMap2> rr:subjectMap []
            Statement subjectMapTriple = r2rml.createStatement(
                    triplesMapSubject, triplesMapPredicate_subjectMap,
                    triplesMapObject_subjectMap);

            // add graph definitions
            Node graph = quad.getGraph();
            if (!graph.equals(QuadPatternInfo.defaultGraph)) {
                // build the actual graph map
                List<Statement> graphStatements = buildTermMapStatements(graph,
                        varDefs, r2rml);
                r2rml.add(graphStatements);
                // connect it to the subject
                Resource graphMapSubject = graphStatements.get(0).getSubject();

                Statement graphMapStatement = ResourceFactory.createStatement(
                        triplesMapObject_subjectMap.asResource(), RR.graphMap,
                        graphMapSubject);

                r2rml.add(graphMapStatement);
            }
            r2rml.add(subjectMapTriple);
        }

        /*
         * predicate map
         */
        Node predicateMap_templColOrConst = quad.getPredicate();
        List<Statement> prediacteMapStatements = buildTermMapStatements(
                predicateMap_templColOrConst, varDefs, r2rml);

        // there may be more than one entry, e.g.
        // [] rr:template "http://data.example.com/department/{DEPTNO}" and
        // [] rr:class ex:Department
        for (Statement statement : prediacteMapStatements) {
            r2rml.add(statement);
        }

        /*
         * object map
         */
        Node objectMap_templColOrConst = quad.getObject();
        List<Statement> objectMapStatements = buildTermMapStatements(
                objectMap_templColOrConst, varDefs, r2rml);

        // there may be more than one entry, e.g.
        // [] rr:template "http://data.example.com/department/{DEPTNO}" and
        // [] rr:class ex:Department
        for (Statement statement : objectMapStatements) {
            r2rml.add(statement);
        }

        /*
         * predicate object map
         */
        // build the predicate-object map triple that may look like this:
        // <#TriplesMap2> rr:prediacteObjectMap [
        //         rr:predicateMap [
        //                 rr:constant ex:name ] ;
        //         rr:objectMap [
        //                 rr:column "ENAME" ];
        //     ]

        // [#1]
        Resource triplesMapObject_predicateObjectMap =
                ResourceFactory.createResource();

        // 1) the statement for [#1] rr:predicateMap [#2]
        Property predicateObjectMapPredicate_predicateMap = RR.predicateMap;
        // [#2]
        RDFNode predicateObjectMapObject_predicateMap =
                prediacteMapStatements.get(0).getSubject();

        Statement predicateObjectMapStatement_predicateMap = r2rml.createStatement(
                triplesMapObject_predicateObjectMap,
                predicateObjectMapPredicate_predicateMap,
                predicateObjectMapObject_predicateMap);

        r2rml.add(predicateObjectMapStatement_predicateMap);

        // 2) the statement for [#1] rr:objectMap [#3]
        Property prediacteObjectMapPrediacte_objectMap = RR.objectMap;
        // [#3]
        RDFNode prediacteObjectMapObject_objectMap =
                objectMapStatements.get(0).getSubject();

        Statement predicateObjectMapStatement_objectMap = r2rml.createStatement(
                triplesMapObject_predicateObjectMap,
                prediacteObjectMapPrediacte_objectMap,
                prediacteObjectMapObject_objectMap);

        r2rml.add(predicateObjectMapStatement_objectMap);

        // 3) the statement for <#TriplesMap2> rr:prediacteObjectMap [#1]
        Property triplesMapPredicate_predicateObjectMap = RR.predicateObjectMap;

        Statement triplesMapStatement_predicateObjectMap = r2rml.createStatement(
                triplesMapSubject,
                triplesMapPredicate_predicateObjectMap,
                triplesMapObject_predicateObjectMap);

        r2rml.add(triplesMapStatement_predicateObjectMap);
    }

    /**
     * Builds up the one triple that states where the actual data for the target
     * mapping comes from. Such a source can be simply a database table or an
     * SQL query.
     *
     * @param relation the data source (table name or SQL query)
     * @param r2rml the target Jena model
     * @return the whole Statement stating where the data comes from,
     *     e.g. '[] rr:tableName "EMP"'
     * @throws SMLVocabException
     */
    private static Statement buildLogicalTableTriple(SqlOpBase relation, Model r2rml) throws SMLVocabException {
        // subject (a blank node [])
        Resource logicalTableSubject = ResourceFactory.createResource();
        // predicate (rr:tableName or rr:sqlQuery)
        Property logicalTablePredicate;
        // object (a Literal like "SELECT DEPTNO FROM DEPT WHERE DEPTNO > 23" or
        // simply a table name like "DEPTNO"
        Literal logicalTableObject;

        if (relation instanceof SqlOpTable) {
            // it's a table
            SqlOpTable tbl = (SqlOpTable) relation;
            logicalTablePredicate = RR.tableName;
            logicalTableObject = ResourceFactory.createPlainLiteral(tbl.getTableName());

        } else if (relation instanceof SqlOpQuery) {
            // it's a query
            SqlOpQuery query = (SqlOpQuery) relation;
            logicalTablePredicate = RR.sqlQuery;
            logicalTableObject = ResourceFactory.createPlainLiteral(query.getQueryString());

        } else {
            // it's not possible
            throw new SMLVocabException();
        }

        Statement logicalTblStatement = r2rml.createStatement(
                logicalTableSubject, logicalTablePredicate, logicalTableObject);

        return logicalTblStatement;
    }

    /**
     * Builds up statements like
     * [] rr:template "http://data.example.com/department/{DEPTNO}" or
     * [] rr:class ex:Department and returns them as a Statement List.
     *
     * @param mappingData the target that should be mapped to relational
     *     structures (subject, predicate or object)
     * @param varDefs the construction definition of the target value based in
     *     the actual database value and some additional data like prefix
     *     strings or certain functions like uri( ... )
     * @param r2rml the target Jena model
     * @return a List<Statement> containing all the term map statements
     * @throws SMLVocabException
     */
    private static List<Statement> buildTermMapStatements(Node mappingData,
            VarDefinition varDefs, Model r2rml) throws SMLVocabException {
        List<Statement> results = new ArrayList<Statement>();

        // a blank node []
        Resource mapSubject = ResourceFactory.createResource();
        // rr:template or rr:column or rr:constant
        Property mapPredicate;
        // a literal like "http://data.example.com/department/{DEPTNO}" or
        // simply "DEPTNO" (column name) or a constant "Foo bar!!"
        // (or in rare cases a URI, which is handled separately)
        Literal mapObject;

        // template or column or constant
        if (mappingData.isVariable()) {
            Collection<RestrictedExpr> restrictions =
                    varDefs.getDefinitions((Var) mappingData);
            List<PredicateAndObject> mapPredicateAndObjects = processRestrictions(restrictions);

            for (PredicateAndObject result : mapPredicateAndObjects) {

                mapPredicate = result.predicate;
                Statement resultStatement;
                RDFNode object = result.object;

                if (object.isLiteral()) {
                    // object is literal
                    mapObject = object.asLiteral();
                    resultStatement = r2rml.createStatement(mapSubject, mapPredicate, mapObject);

                } else if (object.isAnon()) {
                    // object is blank node
                    Resource mapResObject = object.asResource();
                    resultStatement = r2rml.createStatement(mapSubject, mapPredicate, mapResObject);

                } else {
                    // object is resource
                    Resource mapResObject = object.asResource();
                    resultStatement = r2rml.createStatement(mapSubject, mapPredicate, mapResObject);
                }

                results.add(resultStatement);
            }

        // everything that is not a variable is handled as a constant
        } else if (mappingData.isConcrete()) {
            // URIs and Literals have to be handled separately since the methods
            // to retrieve the respective value are different

            Statement resultStatement;

            if (mappingData.isURI()) {
                // URI
                /*
                 * This case is somewhat special since the mapObject is not a
                 * Literal. So, this needs some special handling:
                 * - the Literal mapObject will not be used
                 * - a special mapObject_uri Resource will be created
                 * - the result will be created, appended to the List and
                 *   returned to not go through any further ordinary processing
                 */

                Resource mapObject_uri = ResourceFactory.createResource(mappingData.getURI());
                mapPredicate = RR.constant;

                resultStatement = r2rml.createStatement(mapSubject,
                        mapPredicate, mapObject_uri);
                results.add(resultStatement);

                return results;

            } else if (mappingData.isLiteral()) {
                // Literal

                mapObject = ResourceFactory.createPlainLiteral(mappingData.getLiteral().toString(false));

            } else {
                // blank node
                /*
                 * According to the current (2014-07-26) Jena javadoc a
                 * concrete node can either be a URI, literal or a blank node.
                 * So what's left here is the blank node case. This is handled
                 * using the blank node id as actual term map value and adding
                 * the rr:termType rr:BlankNode
                 */
                // first add the term type statement -- the actual term map
                // value statement will be added generically
                Statement termTypeStatement = r2rml.createStatement(
                        mapSubject, RR.termType, RR.BlankNode);

                results.add(termTypeStatement);

                String bNodeId = ((Node_Blank) mappingData).getBlankNodeId().toString();
                mapObject = ResourceFactory.createPlainLiteral(bNodeId);
            }

            resultStatement = r2rml.createStatement(mapSubject, RR.constant, mapObject);
            results.add(resultStatement);
        }

        return results;
    }

    private static TermConstructorConverter buildTermConstructorConverter(Expr expr) {
        TermConstructorConverter tc;
        E_RdfTerm func = (E_RdfTerm) expr.getFunction();
        TermConstructorType tcType;

        NodeValueDecimal typeIntNode = (NodeValueDecimal) func.getArgs().get(0);
        BigDecimal typeInt = typeIntNode.getDecimal();

        if (typeInt.equals(BNODE)) {
            tcType = TermConstructorType.bNode;
        } else if (typeInt.equals(URI)) {
            tcType = TermConstructorType.uri;
        } else if (typeInt.equals(PLAINLITERAL)) {
            tcType = TermConstructorType.plainLiteral;
        } else if (typeInt.equals(TYPEDLITERAL)) {
            tcType = TermConstructorType.typedLiteral;
        } else {
            // fall back
            tcType = TermConstructorType.plainLiteral;
        }

        List<Expr> args = func.getArgs();
        int numArgs = args.size();
        List<Expr> tcArgs = args.subList(1, numArgs);
        tc = new TermConstructorConverter(tcType, tcArgs);

        return tc;
    }

    private static List<PredicateAndObject> processRestrictions(
            Collection<RestrictedExpr> restrictions) {

        List<PredicateAndObject> results = new ArrayList<PredicateAndObject>();

        for (int i = 0; i < restrictions.size(); i++) {
            RestrictedExpr restriction = (RestrictedExpr) restrictions.toArray()[i];
            Expr expression = restriction.getExpr();
            // TODO: create term type statement

            if (expression.isFunction() &&
                    expression
                            .getFunction()
                            .getFunctionSymbol()
                            .equals(new FunctionLabel(
                                    SparqlifyConstants.rdfTermLabel))) {
                TermConstructorConverter tcc = buildTermConstructorConverter(expression);
                Property mapPredicate = tcc.getMapPredicate();
                Literal mapObject = tcc.getMapObject();
                results.add(new PredicateAndObject(mapPredicate, mapObject));

                // add rr:language if set
                Literal lang = tcc.getLang();
                if (lang != null ) {
                    results.add(new PredicateAndObject(RR.language, lang));
                }
                // add rr:datatype if set
                Resource dtype = tcc.getDatatype();
                if (dtype != null) {
                    results.add(new PredicateAndObject(RR.datatype, dtype));
                }
            }
            /* else: The most outer function *must* be such a generic term
             * constructor function already handled above. So there is no else
             * (at least at the moment)
             */
        }
        return results;
    }
}
