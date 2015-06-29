package org.aksw.sml.converters.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.aksw.commons.util.MapReader;
import org.aksw.sml.converters.errors.SMLVocabException;
import org.aksw.sml.converters.r2rml2sml.R2RML2SMLConverter;
import org.aksw.sml.converters.sml2r2rml.SML2R2RMLConverter;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

enum Format {
    FORMAT_SML,
    FORMAT_R2RML
}

interface Processor
{
    void process(InputStream in, OutputStream out);
}

abstract class ProcessorBase
    implements Processor
{
    private boolean closeOut = false;

    @Override
    public void process(InputStream in, OutputStream out) {
        try {
            try {
                doProcess(in, out);
            }
            finally {
                out.flush();
                if(closeOut) {
                    out.close();
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void doProcess(InputStream in, OutputStream out) throws Exception;
}


class ProcessorR2rmlToSml
    extends ProcessorBase
{
    @Override
    public void doProcess(InputStream in, OutputStream out) throws Exception {
        PrintWriter w = new PrintWriter(out);

        Model r2rmlModel = ModelFactory.createDefaultModel();
        r2rmlModel.read(in, null, "TURTLE");

        List<ViewDefinition> viewDefs = R2RML2SMLConverter.convert(r2rmlModel);
        for(ViewDefinition viewDef : viewDefs) {
            w.println(viewDef);
            w.flush();
        }

        //System.out.println(viewDefs);
    }
}


class ProcessorSmlToR2rml
    extends ProcessorBase
{
    private Logger logger;

    public ProcessorSmlToR2rml(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void doProcess(InputStream in, OutputStream out) throws Exception {

      ConfigParser configParser = new ConfigParser();
      Config views;
      LoggerCount log = new LoggerCount(logger);
      try {
          views = configParser.parse(in, log);
      } finally {
          in.close();
      }

      if(log.getErrorCount() != 0) {
          throw new RuntimeException();
      }

      List<org.aksw.sparqlify.config.syntax.ViewDefinition> syntaxViewDefs =
          views.getViewDefinitions();


      TypeSystem datatypeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
      Map<String, String> aliasMap = MapReader.read(new File("typeAliasFile"));
      SchemaProvider schemaProvider = new SchemaProviderDummy(datatypeSystem, aliasMap);
      SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
      Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();

      for (org.aksw.sparqlify.config.syntax.ViewDefinition sViewDef : syntaxViewDefs) {
          viewDefs.add(syntaxBridge.create(sViewDef));
      }

      // call exporter
      Model r2rml = SML2R2RMLConverter.convert(viewDefs);

      r2rml.write(System.out, "TURTLE", null);
    }
}


public class MainSmlConverters {

    private static final Logger logger = LoggerFactory
            .getLogger(MainSmlConverters.class);

    public static final Map<String, Map<String, Processor>> converters = new HashMap<String, Map<String, Processor>>();


    public static void register(Map<String, Map<String, Processor>> map, String k, String v, Processor processor) {
        Map<String, Processor> sub = map.get(k);
        if(sub == null) {
            sub = new HashMap<String, Processor>();
            map.put(k, sub);
        }

        sub.put(v, processor);
    }

    public static void register(String k, String v, Processor processor) {
        register(converters, k, v, processor);
    }

    public static Processor get(Map<String, Map<String, Processor>> map, String k, String v) {
        Processor result = null;

        Map<String, Processor> sub = map.get(k);
        if(sub != null) {
            result = sub.get(v);
        }

        return result;
    }

    public static Processor get(String k, String v) {
        Processor result = get(converters, k, v);

        return result;
    }


    static {
        register("r2rml", "sml", new ProcessorR2rmlToSml());
        register("sml", "r2rml", new ProcessorSmlToR2rml(logger));
    }


    public static void main(String[] args) throws IOException, RecognitionException, SMLVocabException {

        /*
         * Command Line Parsing
         */

        OptionParser parser = new OptionParser();
        parser.accepts("f").withRequiredArg().ofType(File.class);
        parser.accepts("i").withRequiredArg().ofType(String.class).defaultsTo("sml");
        parser.accepts("o").withRequiredArg().ofType(String.class).defaultsTo("r2rml");

        OptionSet optionSet = parser.parse(args);

        // Get input file
        boolean hasFile = optionSet.has("f");
        //Reader reader;
        InputStream in;
        if(hasFile) {
            File file = (File)optionSet.valueOf("f");
            System.out.println(file.getAbsolutePath());
            in = new FileInputStream(file);
            //reader = new InputStreamReader(in);

        } else {
            in = System.in;
            //reader = new InputStreamReader(System.in);
        }


        String inputFormat = (String)optionSet.valueOf("i");
        String outputFormat = (String)optionSet.valueOf("o");


        Processor processor = get(inputFormat, outputFormat);
        if(processor == null) {
            throw new RuntimeException("No processor found for conversion from " + inputFormat + " -> " + outputFormat);
        }

        processor.process(in, System.out);
    }
}
