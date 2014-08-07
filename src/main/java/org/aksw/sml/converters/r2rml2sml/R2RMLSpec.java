package org.aksw.sml.converters.r2rml2sml;

import java.util.HashSet;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class R2RMLSpec {
    private Model model;

    /** @author sherif */
    public R2RMLSpec(Model model) {
        super();
        this.model = model;
    }

    public Set<TriplesMap> getTriplesMaps() {
        Set<TriplesMap> result = new HashSet<TriplesMap>();
        Set<Resource> tripleMaps = model.listSubjectsWithProperty(RR.logicalTable).toSet();

        for(Resource resource : tripleMaps) {
            TriplesMap item = new TriplesMap(model, resource);

            result.add(item);
        }

        return result;
    }
}
