package org.aksw.sml.converters.r2rml2sml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
@Deprecated
public class OfficialTests {
    private final static String typeAliasFile = "src/main/resources/type-map.h2.tsv";
    private final Logger logger = LoggerFactory.getLogger(OfficialTests.class);

    @Test
    public void runTests()
        throws Exception
    {
        
        String fileNames[] = {
                "R2RMLTC0000",
        }; 
        
        for(String name : fileNames) {
            InputStream inActual = this.getClass().getResourceAsStream("/" + name + ".ttl");
            Model inModel = ModelFactory.createDefaultModel();;
            inModel.read(inActual, null, "TURTLE");
            Map<String, ViewDefinition> actuals = R2RML2SMLConverter.convert2NameHash(inModel);
            

            InputStream inExpected = this.getClass().getResourceAsStream("/" + name + ".sparqlify");
            Map<String, ViewDefinition> expecteds = readSML(inExpected);

            Set<String> actualViewNames = actuals.keySet();
            Set<String> expectedViewNames = expecteds.keySet();
            logger.debug(expecteds.keySet().toString());
            logger.debug(actuals.keySet().toString());

            Assert.assertEquals(expectedViewNames, actualViewNames);

            for(String viewName : expectedViewNames) {
                ViewDefinition expected = expecteds.get(viewName);
                ViewDefinition actual = actuals.get(viewName);

//                Assert.assertEquals(expected, actual);  // java.lang.RuntimeException: Don't compare views with equal - use their name instead
            }
        }
    }

    private Map<String, ViewDefinition> readSML(InputStream in) throws IOException, RecognitionException {
        
        Map<String, ViewDefinition> nameViewDefs = new HashMap<String, ViewDefinition>();
        ConfigParser viewDefParser = new ConfigParser();
        Config views;
        try {
            views = viewDefParser.parse(in, logger);
        } finally {
            in.close();
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

        for (ViewDefinition viewDef : viewDefs) {
            nameViewDefs.put(viewDef.getName(), viewDef);
        }
        logger.debug(nameViewDefs.toString());
        return nameViewDefs;
    }

}
