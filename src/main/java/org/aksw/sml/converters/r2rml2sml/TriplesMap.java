package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;
import org.apache.jena.riot.Lang;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class TriplesMap {
    private Model model;
    private Resource triplesMapResource;
    private final Set<Property> shortcutPredicates = new HashSet<Property>(Arrays.asList(
            RR.graph, RR.subject, RR.predicate, RR.object));
    private final Map<Property, Property> short2general = new HashMap<Property, Property>() {
        private static final long serialVersionUID = 1800478415936356282L;
        {
            put(RR.graph, RR.graphMap);
            put(RR.subject, RR.subjectMap);
            put(RR.predicate, RR.predicateMap);
            put(RR.object, RR.objectMap);
        }
    };

    /**
     * @author sherif
     */
    public TriplesMap(Model model, Resource triplesMapResource) {
        super();
        this.model = normalize(model, triplesMapResource);
        this.triplesMapResource = triplesMapResource;
    }

    private Model normalize(Model triplesMap, Resource triplesMapResource) {
        Model normalizedTriplesMap = ModelFactory.createDefaultModel();

        StmtIterator sttmntIt = triplesMap.listStatements();
        while (sttmntIt.hasNext()) {
            Statement statement = sttmntIt.next();
            Property pred = statement.getPredicate();

            if (shortcutPredicates.contains(pred)) {
                List<Statement> normalizedStatements =
                        getNormalizedTermMapStatements(triplesMap, statement);
                normalizedTriplesMap.add(normalizedStatements);

            } else if (pred.equals(RR.class_)) {
                List<Statement> normalizedStatements =
                        getNormalizedClassStatements(triplesMap, statement);
                normalizedTriplesMap.add(normalizedStatements);

            } else {
                normalizedTriplesMap.add(statement);
            }
        }

        return normalizedTriplesMap;
    }

    /**
     * Given a statement like
     * 
     *  [ rr:subject :someResource ;
     *    rr:class :SomeClass ;
     *    ...                           ]
     * 
     * that types a subject of a subject map, this method introduces a new
     * predicate object map like so
     * 
     *  [ rr:subject <:someResource> ;
     *    rr:predicateObjectMap [
     *      rr:predicateMap [ rr:constant rdf:type ] ;
     *      rr:objectMap [ rr:constant :SomeClass ] ] ;
     *    ...                                            ]
     * 
     * @param triplesMap
     * @param statement
     * @return
     */
    private List<Statement> getNormalizedClassStatements(Model triplesMap,
            Statement statement) {

        List<Statement> normalizedStatements = new ArrayList<Statement>();
        // input statement may look like this: _:subjMap rr:class :SomeClass

        // build and add _:0 rr:predicateObjectMap _:1 (with _:0 being the
        // blank node of the statement _:0 subjectMap _:subjMap
        Resource bNode_subjMap = statement.getSubject();

        Resource bNode_0 = triplesMap
                .listStatements(null, RR.subjectMap, bNode_subjMap).next()
                .getSubject();

        Resource bNode_1 = ResourceFactory.createResource();
        normalizedStatements.add(ResourceFactory.createStatement(
                bNode_0, RR.predicateObjectMap, bNode_1));

        // build and add _:1 rr:predicateMap _:2
        Resource bNode_2 = ResourceFactory.createResource();
        normalizedStatements.add(ResourceFactory.createStatement(
                bNode_1, RR.predicateMap, bNode_2));

        // build and add _:2 rr:constant rdf:type
        normalizedStatements.add(ResourceFactory.createStatement(
                bNode_2, RR.constant, RDF.type.asResource()));

        // build and add _:1 rr:objectMap _:3
        Resource bNode_3 = ResourceFactory.createResource();
        normalizedStatements.add(ResourceFactory.createStatement(
                bNode_1, RR.objectMap, bNode_3));

        // build and add _:3 rr:constant :SomeClass
        RDFNode class_ = statement.getObject();
        normalizedStatements.add(ResourceFactory.createStatement(
                bNode_3, RR.constant, class_));

        return normalizedStatements;
    }

    private List<Statement> getNormalizedTermMapStatements(Model triplesMap,
            Statement statement) {

        List<Statement> normalizedStatements = new ArrayList<Statement>();

        Resource subject = statement.getSubject();
        Property shortCutPredicate = statement.getPredicate();
        Property generalProperty = short2general.get(shortCutPredicate);
        RDFNode object = statement.getObject();
        Resource termMapBNode = ResourceFactory.createResource();

        normalizedStatements.add(ResourceFactory.createStatement(
                subject, generalProperty, termMapBNode));

        normalizedStatements.add(ResourceFactory.createStatement(
                termMapBNode, RR.constant, object));

        return normalizedStatements;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return the subject
     */
    @Deprecated
    public Resource getSubject() {
        return triplesMapResource;
    }

    /**
     * Returns the actual triples map resource
     * @return the actual triples map resource
     */
    public Resource getResource() {
        return triplesMapResource;
    }

    public Set<PredicateObjectMap> getPredicateObjectMaps() {
        Set<PredicateObjectMap> result = new HashSet<PredicateObjectMap>();

        // list all predicate object maps
        Set<RDFNode> objects = model.listObjectsOfProperty(triplesMapResource, RR.predicateObjectMap).toSet();

        for(RDFNode object : objects) {
            Resource r = (Resource)object;
            PredicateObjectMap item = new PredicateObjectMap(model, r); 

            result.add(item);
        }
        return result;
    }

    public LogicalTable getLogicalTable() {
        Set<RDFNode> objects = model.listObjectsOfProperty(triplesMapResource, RR.logicalTable).toSet();

        Resource resource = RRUtils.getResourceFromSet(objects);
        LogicalTable result = new LogicalTable(model, resource);

        return result;
    }

    public SubjectMap getSubjectMap() {
        Set<RDFNode> objects = model.listObjectsOfProperty(triplesMapResource, RR.subjectMap).toSet();

        Resource resource = RRUtils.getResourceFromSet(objects);
        SubjectMap result = new SubjectMap(model, resource);

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((triplesMapResource == null) ? 0 : triplesMapResource.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        TriplesMap other = (TriplesMap) obj;

        if (model == null) {
            if (other.model != null) return false;

        } else if (!model.equals(other.model)) return false;

        if (triplesMapResource == null) {
            if (other.triplesMapResource != null) return false;

        } else if (!triplesMapResource.equals(other.triplesMapResource)) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + triplesMapResource;
    }
}
