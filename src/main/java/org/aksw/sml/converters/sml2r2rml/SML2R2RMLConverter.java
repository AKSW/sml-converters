package org.aksw.sml.converters.sml2r2rml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sml.converters.errors.SMLVocabException;
import org.aksw.sml.converters.vocabs.RR;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_URI;
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
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;

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
    private static final String fnSymbolConcat = "concat";


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
     * Derives an R2RML graph from the given Sparqlify-ML view definition input
     * 
     * @param viewDef a Sparqlify-ML view definition
     * @param r2rml a jena model representing the R2RML structure that will be built up
     * @throws SMLVocabException 
     */
    private static void exportViewDef(ViewDefinition viewDef, Model r2rml) throws SMLVocabException {
        SqlOpBase relation = (SqlOpBase) viewDef.getMapping().getSqlOp();
        List<Quad> patterns = (List<Quad>) viewDef.getTemplate().getList();
        VarDefinition varDefs = viewDef.getMapping().getVarDefinition();

        for (Quad pattern : patterns) {
            exportPattern(pattern, relation, varDefs, r2rml);
        }
    }

    /**
     * Builds up the RDF model "r2ml" representing the R2RML structure of the
     * given SML definitions. At this point only parts of the SML view
     * definitions are considered. These are so called patterns representing
     * quads (graph, subject, predicate, object) that can be valid resources
     * or variables with certain restrictions derived from SML expressions like
     * "uri(concat("http://panlex.org/plx/", ?li))" meaning that the actual
     * value is constructed by creating a URI based on the concatenation of
     * "http://panlex.org/plx/" and the value of the "li" column in the current
     * line of the table and database at hand.
     * 
     * @param r2rml the target jena model
     * @param pattern a quad that may contain variables in the subject,
     *      predicate or object position
     * @param relation the considered database table or constructed logical
     *      relation defined by a query or view
     * @param varDefs the construction definition of the target value based in
     *      the actual database value and some additional data like prefix
     *      strings or certain functions like uri( ... )
     * @throws SMLVocabException 
     */
    private static void exportPattern(Quad pattern, SqlOpBase relation,
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
         * - since there may be several predicates and objects I will prefix a
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
        Property triplesMapPredicate_logicalTable =
                ResourceFactory.createProperty(RR.logicalTable);

        Statement logicalTblStatement_tblDefinition = buildLogicalTableTriple(relation, r2rml);
        r2rml.add(logicalTblStatement_tblDefinition);

        Statement triplesMapStatement_logicalTable = r2rml.createStatement(
                triplesMapSubject, triplesMapPredicate_logicalTable,
                logicalTblStatement_tblDefinition.getSubject());
        r2rml.add(triplesMapStatement_logicalTable);

        /*
         * subject map
         */
        Node subjectMapObject_templColOrConst = pattern.getSubject();
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
            Property triplesMapPredicate_subjectMap =
                    ResourceFactory.createProperty(RR.subjectMap);
            // []
            RDFNode triplesMapObject_subjectMap =
                    (RDFNode) triplesMapStatement_subjectMaps.get(0).getSubject();
            // <#TriplesMap2> rr:subjectMap []
            Statement subjectMapTriple = r2rml.createStatement(
                    triplesMapSubject, triplesMapPredicate_subjectMap,
                    triplesMapObject_subjectMap);
            r2rml.add(subjectMapTriple);
        }

        /*
         * predicate map
         */
        Node predicateMap_templColOrConst = pattern.getPredicate();
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
        Node objectMap_templColOrConst = pattern.getObject();
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
        Property predicateObjectMapPredicate_predicateMap =
                ResourceFactory.createProperty(RR.predicateMap);
        // [#2]
        RDFNode predicateObjectMapObject_predicateMap =
                (RDFNode) prediacteMapStatements.get(0).getSubject();

        Statement predicateObjectMapStatement_predicateMap = r2rml.createStatement(
                triplesMapObject_predicateObjectMap,
                predicateObjectMapPredicate_predicateMap,
                predicateObjectMapObject_predicateMap);

        r2rml.add(predicateObjectMapStatement_predicateMap);

        // 2) the statement for [#1] rr:objectMap [#3]
        Property prediacteObjectMapPrediacte_objectMap =
                ResourceFactory.createProperty(RR.objectMap);
        // [#3]
        RDFNode prediacteObjectMapObject_objectMap =
                (RDFNode) objectMapStatements.get(0).getSubject();

        Statement predicateObjectMapStatement_objectMap = r2rml.createStatement(
                triplesMapObject_predicateObjectMap,
                prediacteObjectMapPrediacte_objectMap,
                prediacteObjectMapObject_objectMap);

        r2rml.add(predicateObjectMapStatement_objectMap);

        // 3) the statement for <#TriplesMap2> rr:prediacteObjectMap [#1]
        Property triplesMapPredicate_predicateObjectMap =
                ResourceFactory.createProperty(RR.predicateObjectMap);

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
            logicalTablePredicate = ResourceFactory.createProperty(RR.tableName);
            logicalTableObject = ResourceFactory.createPlainLiteral(tbl.getTableName());

        } else if (relation instanceof SqlOpQuery) {
            // it's a query
            SqlOpQuery query = (SqlOpQuery) relation;
            logicalTablePredicate = ResourceFactory.createProperty(RR.sqlQuery);
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
                mapPredicate = ResourceFactory.createProperty(RR.constant);

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
                        mapSubject,
                        ResourceFactory.createProperty(RR.termType),
                        ResourceFactory.createResource(RR.BlankNode));
                results.add(termTypeStatement);

                String bNodeId = ((Node_Blank) mappingData).getBlankNodeId().toString();
                mapObject = ResourceFactory.createPlainLiteral(bNodeId);
            }

            resultStatement = r2rml.createStatement(
                    mapSubject,
                    ResourceFactory.createProperty(RR.constant),
                    mapObject);
            results.add(resultStatement);
        }

        return results;
    }

    /**
     * Builds up an R2RML literal string -- the actual mapping value that can be
     * constructed like this: "http://data.example.com/department/{DEPTNO}" .
     * This would correspond to the following Sparqlify-ML expression:
     * uri(concat("http://data.example.com/department/", ?DEPTNO))
     * 
     * So the given restrictions Collection encompass the following nested
     * expressions (from outer to inner):
     * - uri function
     * - concat function
     * - String "http://data.example.com/department/", variable DEPTNO
     * 
     * This method can be called for all kinds of mappings: rr:subjectMap,
     * rr:predicateMap and rr:objectMap (but not for rr:predicateObjectMap which
     * is kind of a container for the rr:predicateMap and rr:objectMap).
     * 
     * @param restrictions collection of RestrictedExpr objects containing
     *      restriction expressions
     * @return a list of predicates and objects
     * @throws SMLVocabException 
	 */
	private static List<PredicateAndObject> processRestrictions(
	        Collection<RestrictedExpr> restrictions) throws SMLVocabException {
        String exprStr = "";
        String langTag = null;
        Node_URI type = null;
        String termType = null;

        List<PredicateAndObject> results = new ArrayList<PredicateAndObject>();

        for (int i = 0; i < restrictions.size(); i++) {

            Property mapPredicate;
            RestrictedExpr restriction = (RestrictedExpr) restrictions.toArray()[i];
            Expr expression = restriction.getExpr();

            /*
             * handle functions:
             * - plainLiteral (explicitly)
             * - typedLiteral (explicitly)
             * - blankNode (explicitly)
             * - other (generic)
             */
            if (expression.isFunction()) {
                exprStr += processRestrExpr(expression);

                // get first argument of the function
                Expr firstArg = expression.getFunction().getArgs().get(0);

                /*
                 * plainLiteral
                 */
                if (expression.getFunction().getFunctionSymbol().equals(
                        new FunctionLabel(SparqlifyConstants.plainLiteralLabel))) {
                    // if the outermost function is plainLiteral( ... ) with...
                    termType = RR.Literal;

                    if (firstArg.isVariable()) {
                        // ...a variable as first argument --> rr:column
                        mapPredicate = ResourceFactory.createProperty(RR.column);

                        // Yes, this is  a bit goofy, but I have to strip off the
                        // curly braces added in the processRestrExpr method before.
                        // This is necessary because down there I could not check
                        // if the variable would end up in a rr:template or
                        // rr:column literal
                        int strlength = exprStr.length();
                        exprStr = exprStr.substring(1, strlength - 1);

                    } else if (firstArg.isFunction()) {
                        // ...a function as first argument --> rr:template

                        /*
                         * Since the value is defined by the function and is not
                         * the pure column value, this has to be rr:template.
                         * This inner function could also work with constants
                         * which would be OK since rr:template would also fit
                         * here being more general than rr:constant, which
                         * would be the clean choice here
                         */
                        mapPredicate = ResourceFactory.createProperty(RR.template);

                    } else {
                        // ...a constant --> rr:constant
                        mapPredicate = ResourceFactory.createProperty(RR.constant);
                    }

                    // get language tag (if set)
                    List<Expr> funcArgs = expression.getFunction().getArgs();

                    if (funcArgs.size() > 1) {
                        // there is more than one argument, like in
                        // plainLiteral(?foo, 'en')
                        // TODO: check if this is still valid (after adding implicit concat, e.g. plainLiteral('foo', 'bar', 'en'))

                        if (funcArgs.get(1).isConstant() && 
                                funcArgs.get(1).getConstant().isString()) {
                            // looks like this could be a language tag
                            langTag = funcArgs.get(1).getConstant().asString();
                        }
                    }

                /*
                 * typed literal
                 */
                } else if (expression.getFunction().getFunctionSymbol().equals(
                        new FunctionLabel(SparqlifyConstants.typedLiteralLabel))) {
                    termType = RR.Literal;

                    if (firstArg.isVariable()) {
                        // rr:column
                        mapPredicate = ResourceFactory.createProperty(RR.column);

                        // Yes, this is  a bit goofy, but I have to strip off the
                        // curly braces added in the processRestrExpr method before.
                        // This is necessary because down there I could not check
                        // if the variable would end up in a rr:template or
                        // rr:column literal
                        int strlength = exprStr.length();
                        exprStr = exprStr.substring(1, strlength - 1);

                    } else if (firstArg.isFunction()) {
                        // rr:template
                        mapPredicate = ResourceFactory.createProperty(RR.template);

                    } else {
                        // rr:constant
                        mapPredicate = ResourceFactory.createProperty(RR.constant);
                    }

                    // get type
                    List<Expr> funcArgs = expression.getFunction().getArgs();

                    if (funcArgs.size() > 1) {
                        // there is more than one argument, like in
                        // typedLiteral(?foo, xsd:date)
                        if (funcArgs.get(1).isConstant() &&
                                funcArgs.get(1).getConstant().isIRI()) {
                            // looks like this could be a type declaration
                            type = (Node_URI) funcArgs.get(1).getConstant()
                                    .getNode();
                        }
                    }

                /*
                 * blank node
                 */
                } else if (expression.getFunction().getFunctionSymbol().equals(
                        new FunctionLabel(SparqlifyConstants.blankNodeLabel))) {
                    termType = RR.BlankNode;

                    mapPredicate = ResourceFactory.createProperty(RR.constant);
                    Resource mapObject = ResourceFactory.createResource();
                    PredicateAndObject result = new PredicateAndObject(mapPredicate, mapObject);
                    results.add(result);

                    PredicateAndObject bNodeTypePredAndObj = buildTermTypePredAndObj(termType);

                    results.add(bNodeTypePredAndObj);

                    /*
                     * It would be
                     * - wrong going through the build-up process of the literal
                     *   object
                     * - useless to go through the type or language tag detection
                     *   process
                     * so we get out of this loop here.  
                     */
                    continue;

                } else {
                    // rr:template
                    if (expression.getFunction().getFunctionSymbol().equals(
                            new FunctionLabel(SparqlifyConstants.uriLabel))) {
                        termType = RR.IRI;
                    }
                    mapPredicate = ResourceFactory.createProperty(RR.template);
                }

            // There is just a variable or given. (The case of a constand was
            // already handled above.) Since such expressions
            // (e.g. ?variable_name=?COUMN) would violate the SML, this branch
            // should never be reached.
            } else {
                throw new SMLVocabException();
            }

            Literal mapObject = ResourceFactory.createPlainLiteral(exprStr);
            PredicateAndObject result = new PredicateAndObject(mapPredicate, mapObject);
            results.add(result);

            if (termType != null) {
                PredicateAndObject termTypePredAndObj = buildTermTypePredAndObj(termType);
                results.add(termTypePredAndObj);
            }

            if (langTag != null) {
                PredicateAndObject rrlanguage = buildLangPredAndObj(langTag);
                results.add(rrlanguage);

            } else if (type != null) {
                PredicateAndObject rrDataType = buildDataTypePredAndObj(type);
                results.add(rrDataType);
            }
        }

        return results;
    }

    /**
     * Builds the predicate and object of the rr:termType definition, e.g
     * [] rr:termType rr:Literal
     * 
     * @param termType one of literalType, bNodeType and uriType
     * @return a PredicateAndObject object containing the predicate
     *      (rr:termType) and the object (e.g. rr:Literal) of the term type
     *      definition 
     */
    private static PredicateAndObject buildTermTypePredAndObj(String rrTermType) {
        Property termTypePredicate = ResourceFactory.createProperty(RR.termType);
        Resource termTypeObjct = ResourceFactory.createResource(rrTermType);

        return new PredicateAndObject(termTypePredicate, termTypeObjct);
    }

    /**
     * Builds the predicate and object of a data type definition like
     * [] rr:dataType xsd:date
     * 
     * @param type a Node_URI object containing the type resource URI
     * @return a PredicateAndObject object containing the predicate
     *      (rr:dataType) and the object (e.g. xsd:string) of the rr:dataType
     *      expression
     */
    private static PredicateAndObject buildDataTypePredAndObj(Node_URI type) {
        Property rrDataTypePredicate = ResourceFactory.createProperty(RR.datatype);
        Resource rrDataTypeObject = ResourceFactory.createResource(type.toString());

        return new PredicateAndObject(rrDataTypePredicate, rrDataTypeObject);
    }

    /**
     * Builds the predicate and object of a language restriction like
     * [] rr:language "en" based on a Sparqlify-ML plainLiteral setting like in
     * plainLiteral(?foo, 'en')
     * 
     * @param langTag
     *            a String containing a language abbreviation like 'en' or 'de'
     * @return a PredicateAndObject object containing the predicate
     *         (rr:language) and object (e.g. "en") of the rr:language
     *         expression
     */
    private static PredicateAndObject buildLangPredAndObj(String langTag) {
        Property rrLangPredicate = ResourceFactory.createProperty(RR.language);
        Literal rrLangObject = ResourceFactory.createPlainLiteral(langTag);

        return new PredicateAndObject(rrLangPredicate, rrLangObject);
    }

    /**
     * Processes (at the time of writing some) known functions available in the
     * Sparqlify-ML, variables and constant strings. Since arguments of the
     * considered function can be functions, variables or constant strings as
     * well, this method is called recursively to get to the most inner
     * expression and build up the rest based on that.
     * 
     * @param expr
     *            a restriction expression (com.hp.hpl.jena.sparql.expr.Expr)
     *            like a function (concat( ... ), uri( ... ), ...) or a variable
     * @return a String containing the R2RML counterpart of these Sparqlify-ML
     *         expressions
     */
    private static String processRestrExpr(Expr expr) {
        String exprStr = "";

        /*
         * functions
         */
        if (expr.isFunction()) {
            ExprFunction func = expr.getFunction();
            if (func.getFunctionSymbol().equals(
                    new FunctionLabel(SparqlifyConstants.rdfTermLabel))) {

                func = func.getArg(2).getFunction();
            }

            // concat( ... )
            if (func.getFunctionSymbol().equals(new FunctionLabel(fnSymbolConcat))) {
                List<Expr> args = func.getArgs();

                for (Expr arg : args) {
                    // explicitely use string representation of IRIs get strings
                    // without leading and trailing angle brackets
                    if (arg.isConstant() && arg.toString().startsWith("<")) {
                        exprStr += arg.getConstant().asString();
                        continue;
                    }
                    exprStr += processRestrExpr(arg);
                }

            // uri( ... )
            } else if (func.getFunctionSymbol().equals(
                    new FunctionLabel(SparqlifyConstants.uriLabel))) {
                // there should be just one argument here
                Expr subExpr = func.getArgs().get(0);
                exprStr += processRestrExpr(subExpr);

            // plainLiteral( ... )
            } else if (func.getFunctionSymbol().equals(
                    new FunctionLabel(SparqlifyConstants.plainLiteralLabel))) {
                // I am only interested in the first argument here, since a
                // possible second argument containing a language tag is only
                // of interest, if the plainLiteral is the most outer function
                // which is handled in a different place.
                Expr subExpr = func.getArgs().get(0);
                exprStr += processRestrExpr(subExpr);

            // typedLiteral
            } else if (func.getFunctionSymbol().equals(
                    new FunctionLabel(SparqlifyConstants.typedLiteralLabel))) {
                // As above I am only interested in the first argument here
                // since the second argument stating which type the first
                // argument has, should be processed in a different place.
                Expr subExpr = func.getArgs().get(0);
                exprStr += processRestrExpr(subExpr);
            } else {
                // URL encode
                // FIXME: no URL encoding is done here!!
                Expr subExpr = func.getArgs().get(0);
                exprStr += processRestrExpr(subExpr);
            }

        /*
         * variables and strings
         */
        } else {

            // strings
            if (expr.isConstant()) {
                String constStr = expr.toString();

                if (constStr.startsWith("\"")) {
                    // strip off the leading and trailing quote
                    constStr = constStr.substring(1);

                    if (constStr.endsWith("\"")) {
                        int strLength = constStr.length();
                        constStr = constStr.substring(0, strLength - 1);
                    }
                }
                exprStr += constStr;

            // variables
            } else if (expr.isVariable()) {
                String varStr = expr.toString();
                // strip off the leading question mark...
                varStr = varStr.substring(1);
                // ...and put the value in curly braces
                varStr = "{" + varStr + "}";
                exprStr += varStr;
            }
        }

        return exprStr;
    }
}
