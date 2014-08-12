package org.aksw.sml.converters.r2rml2sml;

import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SubjectMap extends TermMap {

    public SubjectMap(Model model, Resource subjectMapResource) {
        super(model, subjectMapResource);
    }

    public RDFNode getRrClass() {
        Set<RDFNode> objects = model.listObjectsOfProperty(resource,RR.class_).toSet();

        if(objects.isEmpty()) {
            return null;
        }

        RDFNode node = RRUtils.getFirst(objects);

        return node;
    }
}
