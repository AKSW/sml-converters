package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class R2RMLSpecTest {
    
    private final String prefix = "http://ex.org/";

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
    public void test_getTriplesMaps_1() {
        Model r2rml = readR2RML(r2rml1);
        Resource expectedTMResource = ResourceFactory
                .createResource(prefix + "TriplesMap1");
        int expectedNumTMResources = 1;

        R2RMLSpec spec = new R2RMLSpec(r2rml);
        Set<TriplesMap> triplesMaps = spec.getTriplesMaps();
        Set<Resource> tmResources = new HashSet<Resource>();
        for (TriplesMap tm : triplesMaps) {
            tmResources.add(tm.getResource());
        }

        assertEquals(expectedNumTMResources, triplesMaps.size());
        assertEquals(expectedNumTMResources, tmResources.size());
        assertTrue(tmResources.contains(expectedTMResource));
    }

    @Test
    public void test_getTriplesMaps_2() {
        Model r2rml = readR2RML(r2rml2);
        List<Resource> expectedResources = new ArrayList<Resource>();
        expectedResources.add(ResourceFactory.createResource(prefix + "TriplesMap2"));
        expectedResources.add(ResourceFactory.createResource(prefix + "TriplesMap3"));
        int expectedNumTMResources = 2;
        
        R2RMLSpec spec = new R2RMLSpec(r2rml);
        Set<TriplesMap> triplesMaps = spec.getTriplesMaps();
        Set<Resource> tmResources = new HashSet<Resource>();
        for (TriplesMap tm : triplesMaps) {
            tmResources.add(tm.getResource());
        }

        assertEquals(expectedNumTMResources, triplesMaps.size());
        assertEquals(expectedNumTMResources, tmResources.size());
        for (Resource expctdRes : expectedResources) {
            assertTrue(tmResources.contains(expctdRes));
        }
    }
    
    @Test
    public void test_getTriplesMaps_3() {
        Model r2rml = readR2RML(r2rml3);
        List<Resource> expectedResources = new ArrayList<Resource>();
        expectedResources.add(ResourceFactory.createResource(prefix + "TriplesMap4"));
        expectedResources.add(ResourceFactory.createResource(prefix + "TriplesMap5"));
        int expectedNumTMResources = 3;

        R2RMLSpec spec = new R2RMLSpec(r2rml);
        Set<TriplesMap> triplesMaps = spec.getTriplesMaps();
        Set<Resource> tmResources = new HashSet<Resource>();
        for (TriplesMap tm : triplesMaps) {
            tmResources.add(tm.getResource());
        }

        assertEquals(expectedNumTMResources, triplesMaps.size());
        assertEquals(expectedNumTMResources, tmResources.size());
        for (Resource expctdRes : expectedResources) {
            assertTrue(tmResources.contains(expctdRes));
        }
    }
}
