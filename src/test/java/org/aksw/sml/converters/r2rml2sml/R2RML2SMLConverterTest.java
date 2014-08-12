package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sml.converters.vocabs.RR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.XSD;

public class R2RML2SMLConverterTest {

    private final String prefix = "http://ex.org/";
    private final String example = "foo";
    private final String exampleBlankNodeId = "23";

    private final String r2rml1 =
            "@prefix rr: <http://www.w3.org/ns/r2rml#> . " +
            "@prefix : <http://ex.org/> . " +
            ":TriplesMap1 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"employee\" ] ; " +
                "rr:subject  :sth ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred1 ; " +
                    "rr:object :sthElse " +
                "] .";

    private final String r2rml2 =
            "@prefix rr: <http://www.w3.org/ns/r2rml#> . " +
            "@prefix : <http://ex.org/> . " +
            ":TriplesMap2 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"employee\" ] ; " +
                "rr:subject  :sth ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred1 ; " +
                    "rr:object :sthElse " +
                "] . " +
            ":TriplesMap3 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"dept\" ] ; " +
                "rr:subject  :dept123 ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred2 ; " +
                    "rr:object :sthDifferent " +
                "] .";

    private final String r2rml3 =
            "@prefix rr: <http://www.w3.org/ns/r2rml#> . " +
            "@prefix : <http://ex.org/> . " +
            ":TriplesMap4 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"employee\" ] ; " +
                "rr:subject  :sth ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred1 ; " +
                    "rr:object :sthElse " +
                "] . " +
            ":TriplesMap5 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"dept\" ] ; " +
                "rr:subject  :dept123 ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred2 ; " +
                    "rr:object :sthDifferent " +
                "] ." +
            ":TriplesMap6 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"empl2dept\" ] ; " +
                "rr:subject  :blah ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred3 ; " +
                    "rr:object :sthCompletelyDifferent " +
                "] .";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private Model readR2RML(String r2rmlString) {
        Reader r2rmlRead = new StringReader(r2rmlString);
        Model r2rml = ModelFactory.createDefaultModel();
        r2rml.read(r2rmlRead, null, "TURTLE");

        return r2rml;
    }

    @Test
    public void test_buildTblToTM_1() {
        Model r2rml = readR2RML(r2rml1);
        R2RMLSpec spec = new R2RMLSpec(r2rml);

        Resource expectedTMSubject = ResourceFactory.createResource(prefix + "TriplesMap1");
        NodeIterator tmpRes = r2rml.listObjectsOfProperty(expectedTMSubject, RR.logicalTable);
        Resource expectedTblSubject = tmpRes.next().asResource();
        Pair<LogicalTable, TriplesMap> expectedLtTm =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject),
                        new TriplesMap(r2rml, expectedTMSubject));
        int expectedNumLtTmEntries = 1;

        Multimap<LogicalTable, TriplesMap> tableToTmMultiMap = R2RML2SMLConverter.buildTblToTM(spec);
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keys().size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keySet().size());
        Set<LogicalTable> tbls = tableToTmMultiMap.keySet();
        assertTrue(tbls.contains(expectedLtTm.first));
        Collection<TriplesMap> triplesMaps = tableToTmMultiMap.values();
        assertTrue(triplesMaps.contains(expectedLtTm.second));
    }

    @Test
    public void test_buildTblToTM_2() {
        Model r2rml = readR2RML(r2rml2);
        R2RMLSpec spec = new R2RMLSpec(r2rml);

        List<Pair<LogicalTable, TriplesMap>> expctdLtTmEntries =
                new ArrayList<Pair<LogicalTable,TriplesMap>>();

        // entry 1
        Resource expectedTMSubject1 = ResourceFactory.createResource(prefix + "TriplesMap2");
        NodeIterator tmpRes1 = r2rml.listObjectsOfProperty(expectedTMSubject1, RR.logicalTable);
        Resource expectedTblSubject1 = tmpRes1.next().asResource();
        tmpRes1.close();
        Pair<LogicalTable, TriplesMap> expectedLtTm1 =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject1),
                        new TriplesMap(r2rml, expectedTMSubject1));
        expctdLtTmEntries.add(expectedLtTm1);
        // entry 2
        Resource expectedTMSubject2 = ResourceFactory.createResource(prefix + "TriplesMap3");
        NodeIterator tmpRes2 = r2rml.listObjectsOfProperty(expectedTMSubject2, RR.logicalTable);
        Resource expectedTblSubject2 = tmpRes2.next().asResource();
        tmpRes2.close();
        Pair<LogicalTable, TriplesMap> expectedLtTm2 =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject2),
                        new TriplesMap(r2rml, expectedTMSubject2));
        expctdLtTmEntries.add(expectedLtTm2);

        int expectedNumLtTmEntries = 2;

        Multimap<LogicalTable, TriplesMap> tableToTmMultiMap = R2RML2SMLConverter.buildTblToTM(spec);
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keys().size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keySet().size());
        Set<LogicalTable> tbls = tableToTmMultiMap.keySet();
        Collection<TriplesMap> triplesMaps = tableToTmMultiMap.values();
        for (Pair<LogicalTable, TriplesMap> expctdVal : expctdLtTmEntries) {
            assertTrue(tbls.contains(expctdVal.first));
            assertTrue(triplesMaps.contains(expctdVal.second));
        }
    }

    @Test
    public void test_buildTblToTM_3() {
        Model r2rml = readR2RML(r2rml3);
        R2RMLSpec spec = new R2RMLSpec(r2rml);

        List<Pair<LogicalTable, TriplesMap>> expctdLtTmEntries =
                new ArrayList<Pair<LogicalTable,TriplesMap>>();

        // entry 1
        Resource expectedTMSubject1 = ResourceFactory.createResource(prefix + "TriplesMap4");
        NodeIterator tmpRes1 = r2rml.listObjectsOfProperty(expectedTMSubject1, RR.logicalTable);
        Resource expectedTblSubject1 = tmpRes1.next().asResource();
        tmpRes1.close();
        Pair<LogicalTable, TriplesMap> expectedLtTm1 =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject1),
                        new TriplesMap(r2rml, expectedTMSubject1));
        expctdLtTmEntries.add(expectedLtTm1);
        // entry 2
        Resource expectedTMSubject2 = ResourceFactory.createResource(prefix + "TriplesMap5");
        NodeIterator tmpRes2 = r2rml.listObjectsOfProperty(expectedTMSubject2, RR.logicalTable);
        Resource expectedTblSubject2 = tmpRes2.next().asResource();
        tmpRes2.close();
        Pair<LogicalTable, TriplesMap> expectedLtTm2 =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject2),
                        new TriplesMap(r2rml, expectedTMSubject2));
        expctdLtTmEntries.add(expectedLtTm2);
        // entry 3
        Resource expectedTMSubject3 = ResourceFactory.createResource(prefix + "TriplesMap6");
        NodeIterator tmpRes3 = r2rml.listObjectsOfProperty(expectedTMSubject2, RR.logicalTable);
        Resource expectedTblSubject3 = tmpRes3.next().asResource();
        tmpRes2.close();
        Pair<LogicalTable, TriplesMap> expectedLtTm3 =
                new Pair<LogicalTable, TriplesMap>(
                        new LogicalTable(r2rml, expectedTblSubject3),
                        new TriplesMap(r2rml, expectedTMSubject3));
        expctdLtTmEntries.add(expectedLtTm3);

        int expectedNumLtTmEntries = 3;

        Multimap<LogicalTable, TriplesMap> tableToTmMultiMap = R2RML2SMLConverter.buildTblToTM(spec);
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keys().size());
        assertEquals(expectedNumLtTmEntries, tableToTmMultiMap.keySet().size());
        Set<LogicalTable> tbls = tableToTmMultiMap.keySet();
        Collection<TriplesMap> triplesMaps = tableToTmMultiMap.values();
        for (Pair<LogicalTable, TriplesMap> expctdVal : expctdLtTmEntries) {
            assertTrue(tbls.contains(expctdVal.first));
            assertTrue(triplesMaps.contains(expctdVal.second));
        }
    }
    

    
    /*
     * Considered combinations:
     * - rr:column
     * - rr:template
     * - rr:constant
     *   - term type rr:IRI
     *   - term type rr:BlankNoe
     *   - term type rr:Literal
     */
    @Test
    public void test_getNodeFromTermMap() {
        String varName = "s1";

        /*
         * rr:column
         */
        Model model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.IRI + "> " +
                "]");
        Resource modelSubject = model.listSubjects().toList().get(0);
        TermMap termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.Literal + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.BlankNode + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        /*
         * rr:template
         */
        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.IRI + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.BlankNode + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.Literal + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createVariable(varName),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        /*
         * rr:constant
         */
        // Node_URI
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> <" + prefix + example + "> ; " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createURI(prefix + example),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));
        
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> <" + prefix + example + "> ; " +
                    "<" + RR.termType + "> <" + RR.IRI + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createURI(prefix + example),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> \"" + prefix + example + "\" ; " +
                    "<" + RR.termType + "> <" + RR.IRI + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createURI(prefix + example),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        // blank node
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> \"" + exampleBlankNodeId + "\" ; " +
                    "<" + RR.termType + "> <" + RR.BlankNode + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createAnon(new AnonId(exampleBlankNodeId)),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));

        // literal (plain wo/ language tag)
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> \"" + example + "\" ; " +
                    "<" + RR.termType + "> <" + RR.Literal + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createLiteral(example),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));
        
        // literal (plain w/ language tag)
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> \"" + example + "\" ; " +
                    "<" + RR.language + "> \"en\" " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createLiteral(example, "en", false),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));
        
        // literal (typed w/ type)
        model = readR2RML(
                "[ " +
                    "<" + RR.constant + "> \"" + example + "\" ; " +
                    "<" + RR.datatype + "> <" + XSD.xstring + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(
                NodeFactory.createLiteral(example, XSDDatatype.XSDstring),
                R2RML2SMLConverter.createNodeFromTermMap(termMap, varName));
    }
}
