package org.aksw.sml.converters.r2rml2sml;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


public class ObjectMap extends TermMap {

    public ObjectMap(Model model, Resource termMapResource) {
        super(model, termMapResource);
    }
}
