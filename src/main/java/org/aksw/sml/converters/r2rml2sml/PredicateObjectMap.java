package org.aksw.sml.converters.r2rml2sml;

import java.util.HashSet;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
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
}
