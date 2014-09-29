package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class PredicateObjectMap {
    private Model model;
    private Resource resource;

    /** @author sherif */
    public PredicateObjectMap(Model model, Resource resource) {
        super();
        this.model = model;
        this.resource = resource;
    }

    public Set<PredicateMap> getPredicateMaps() {
        Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
        Set<RDFNode> nodes = model.listObjectsOfProperty(resource, RR.predicateMap).toSet();

        for (RDFNode res : nodes) {
            predicateMaps.add(new PredicateMap(model, (Resource) res));
        }

        return predicateMaps;
    }

    public Set<ObjectMap> getObjectMaps() {
        Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
        Set<RDFNode> nodes = model.listObjectsOfProperty(resource, RR.objectMap).toSet();

        for (RDFNode res : nodes) {
            objectMaps.add(new ObjectMap(model, (Resource) res));
        }

        return objectMaps;
    }

    public List<TermMap> getGraphMaps() {
        List<TermMap> graphMaps = new ArrayList<TermMap>();
        NodeIterator graphMapNodesIt = model.listObjectsOfProperty(resource, RR.graphMap);

        while (graphMapNodesIt.hasNext()) {
            RDFNode graphMapNode = graphMapNodesIt.next();
            TermMap graphMap = new TermMap(model, graphMapNode.asResource());
            graphMaps.add(graphMap);
        }

        return graphMaps;
    }
}
