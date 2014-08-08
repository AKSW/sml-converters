package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sml.converters.vocabs.RR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class LogicalTableTest {
    private final String tblExpression1 = "employee";
    private final String tblExpression2 = "SELECT foo FROM bar";

    private final String r2rmlStr1 =
            "@prefix rr: <http://www.w3.org/ns/r2rml#> . " +
            "@prefix : <http://ex.org/> . " +
            ":TriplesMap1 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:tableName \"" + tblExpression1 +"\" ] ; " +
                "rr:subject  :sth ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred1 ; " +
                    "rr:object :sthElse " +
                "] .";
    private final String r2rmlStr2 =
            "@prefix rr: <http://www.w3.org/ns/r2rml#> . " +
            "@prefix : <http://ex.org/> . " +
            ":TriplesMap1 " +
                "a rr:TriplesMap ; " +
                "rr:logicalTable [ rr:sqlQuery \"" + tblExpression2 + "\" ] ; " +
                "rr:subject  :sth ; " +
                "rr:predicateObjectMap [ " +
                    "rr:predicate :pred1 ; " +
                    "rr:object :sthElse " +
                "] .";
    private Model r2rml1;
    private LogicalTable logTbl1;
    private Model r2rml2;
    private LogicalTable logTbl2;

    @Before
    public void setUp() throws Exception {
        r2rml1 = ModelFactory.createDefaultModel();
        Reader strRead1 = new StringReader(r2rmlStr1);
        r2rml1.read(strRead1, null, "TURTLE");

        Resource logTblResource1 =
                RRUtils.getResourceFromSet(r2rml1.listObjectsOfProperty(RR.logicalTable).toSet());
        logTbl1= new LogicalTable(r2rml1, logTblResource1);

        r2rml2 = ModelFactory.createDefaultModel();
        Reader strRead2 = new StringReader(r2rmlStr2);
        r2rml2.read(strRead2, null, "TURTLE");
        
        Resource logTblResource2 =
                RRUtils.getResourceFromSet(r2rml2.listObjectsOfProperty(RR.logicalTable).toSet());
        logTbl2 = new LogicalTable(r2rml2, logTblResource2);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsTable() {
        assertTrue(logTbl1.isTable());
        assertFalse(logTbl2.isTable());
    }

    @Test
    public void testIsQuery() {
        assertFalse(logTbl1.isQuery());
        assertTrue(logTbl2.isQuery());
    }

    @Test
    public void testGetTableExpression() {
        assertEquals(logTbl1.getTableExpression(), tblExpression1);
        assertEquals(logTbl2.getTableExpression(), tblExpression2);
    }

}
