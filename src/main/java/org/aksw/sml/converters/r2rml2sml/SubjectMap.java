package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SubjectMap extends TermMap {

    public SubjectMap(Model model, Resource subjectMapResource) {
        super(model, subjectMapResource);
    }

    /**
     * Should not be needed anymore since the triples maps are normalized, i.e.
     * rr:class statements should already be transformed to predicate object
     * maps assigning the type explicitly.
     */
    @Deprecated
    public RDFNode getRrClass() {
        Set<RDFNode> objects = model.listObjectsOfProperty(resource,RR.class_).toSet();

        if(objects.isEmpty()) {
            return null;
        }

        RDFNode node = RRUtils.getFirst(objects);

        return node;
    }
}
