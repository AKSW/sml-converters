package org.aksw.sml.converters.vocabs;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class RR {
    public static final String prefix = "http://www.w3.org/ns/r2rml#";

    protected static Resource resource(String local) {
        return ResourceFactory.createResource( prefix + local );
    }

    protected static Property property(String local) {
        return ResourceFactory.createProperty(prefix + local);
    }

    // R2RML classes
    public static final Resource BaseTableOrView = resource("BaseTableOrView");
    public static final Resource GraphMap = resource("GraphMap");
    public static final Resource Join = resource("Join");
    public static final Resource LogicalTable = resource("LogicalTable");
    public static final Resource ObjectMap = resource("ObjectMap");
    public static final Resource PredicateMap = resource("PredicateMap");
    public static final Resource PredicateObjectMap = resource("PredicateObjectMap");
    public static final Resource R2RMLView = resource("R2RMLView");
    public static final Resource RefObjectMap = resource("RefObjectMap");
    public static final Resource SubjectMap = resource("SubjectMap");
    public static final Resource TermMap = resource("TermMap");
    public static final Resource TriplesMap = resource("TriplesMap");

    // R2RML properties
    public static final Property child = property("child");
    public static final Property class_ = property("class");
    public static final Property column = property("column");
    public static final Property datatype = property("datatype");
    public static final Property constant = property("constant");
    public static final Property graph = property("graph");
    public static final Property graphMap = property("graphMap");
    public static final Property inverseExpression = property("inverseExpression");
    public static final Property joinCondition = property("joinCondition");
    public static final Property language = property("language");
    public static final Property logicalTable = property("logicalTable");
    public static final Property object = property("object");
    public static final Property objectMap = property("objectMap");
    public static final Property parent = property("parent");
    public static final Property parentTriplesMap = property("parentTriplesMap");
    public static final Property predicate = property("predicate");
    public static final Property predicateMap = property("predicateMap");
    public static final Property predicateObjectMap = property("predicateObjectMap");
    public static final Property sqlQuery = property("sqlQuery");
    public static final Property sqlVersion = property("sqlVersion");
    public static final Property subject = property("subject");
    public static final Property subjectMap = property("subjectMap");
    public static final Property tableName = property("tableName");
    public static final Property template = property("template");
    public static final Property termType = property("termType");

    // other terms
    public static final Resource defaultGraph = resource("defaultGraph");
    public static final Resource SQL2008 = resource("SQL2008");
    public static final Resource IRI = resource("IRI");
    public static final Resource BlankNode = resource("BlankNode");
    public static final Resource Literal = resource("Literal");
}
