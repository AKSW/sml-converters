package org.aksw.sml.converters.r2rml2sml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Run {
    private static final String r2rmlFilePath = "src/main/resources/triples_maps.ttl";

    public static void main(String[] args) throws FileNotFoundException {
        // read R2ML mappings
        Reader r2rmlMappingsReader = new FileReader(new File(r2rmlFilePath));
        Model r2rmlMappings = ModelFactory.createDefaultModel();
        r2rmlMappings.read(r2rmlMappingsReader, null, "TURTLE");

        // call importer
        List<ViewDefinition> viewDefs = R2RML2SMLConverter.convert(r2rmlMappings);
        System.out.println(viewDefs);
    }
}
