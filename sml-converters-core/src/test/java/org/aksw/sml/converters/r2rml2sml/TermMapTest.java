package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;

import org.aksw.sml.converters.vocabs.RR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class TermMapTest {

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
    public void test_getTermType() {
        // term type set explicitly --> rr:IRI
        Model model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.IRI + "> " +
                "]");
        Resource modelSubject = model.listSubjects().toList().get(0);
        TermMap termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.IRI, termMap.getTermType());

        // term type set explicitly --> rr:BlankNode
        model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.BlankNode + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.BlankNode, termMap.getTermType());

        // term type set explicitly --> rr:Literal
        model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" ; " +
                    "<" + RR.termType + "> <" + RR.Literal + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.Literal, termMap.getTermType());

        // column-based term map --> rr:Literal
        model = readR2RML(
                "[ " +
                    "<" + RR.column + "> \"foo\" " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.Literal, termMap.getTermType());

        // rr:language property set --> rr:Literal
        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"foo{id}\" ; " +
                    "<" + RR.language + "> \"en\" " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.Literal, termMap.getTermType());

        // rr:datatype property --> rr:Literal
        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"23{id}\" ; " +
                    "<" + RR.datatype + "> <" + XSD.integer + "> " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.Literal, termMap.getTermType());

        // rr:template (wo/ language or datatype) --> rr:IRI
        model = readR2RML(
                "[ " +
                    "<" + RR.template + "> \"http://ex.org/{id}\" ; " +
                "]");
        modelSubject = model.listSubjects().toList().get(0);
        termMap = new SubjectMap(model, modelSubject);
        assertEquals(RR.IRI, termMap.getTermType());
    }
}
