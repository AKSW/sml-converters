package org.aksw.sml.converters.sml2r2rml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sml.converters.errors.SMLVocabException;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class Run {
    private final static String viewDefFilePath = "src/main/resources/views.sml";
    private final static String typeAliasFile = "src/main/resources/type-map.h2.tsv";
    private final static Logger logger = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) throws IOException, RecognitionException, SMLVocabException {
        // read SML view definitions
        File viewDefsFile = new File(viewDefFilePath);
        if (!viewDefsFile.exists()) {
            logger.error("SML view definitions file does not exist. " +
                    "Exiting...");
            System.exit(1);
        }
        FileInputStream viewStream = new FileInputStream(viewDefsFile);
        ConfigParser viewDefParser = new ConfigParser();
        Config views;
        try {
            views = viewDefParser.parse(viewStream, logger);
        } finally {
            viewStream.close();
        }
        List<org.aksw.sparqlify.config.syntax.ViewDefinition> syntaxViewDefs =
                views.getViewDefinitions();
        
        TypeSystem datatypeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        Map<String, String> aliasMap = MapReader.read(new File(typeAliasFile));
        SchemaProvider schemaProvider = new SchemaProviderDummy(datatypeSystem, aliasMap);
        SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
        Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
        
        for (org.aksw.sparqlify.config.syntax.ViewDefinition sViewDef : syntaxViewDefs) {
            viewDefs.add(syntaxBridge.create(sViewDef));
        }

        System.out.println(viewDefs);
        // call exporter
        Model r2rml = SML2R2RMLConverter.convert(viewDefs);

        r2rml.write(System.out, "TURTLE", null);
    }
}
