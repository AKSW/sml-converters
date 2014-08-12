package org.aksw.sml.converters.r2rml2sml;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class PredicateMap extends TermMap {

    public PredicateMap(Model model, Resource termMapResource) {
        super(model, termMapResource);
    }
}
