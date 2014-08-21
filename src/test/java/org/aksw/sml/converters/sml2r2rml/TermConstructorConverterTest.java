package org.aksw.sml.converters.sml2r2rml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class TermConstructorConverterTest {
    private static ViewDefinitionFactory vdf;
    @Before
    public void setUp() throws Exception {
        Map<String, String> typeAlias = MapReader.read(
                new File("src/main/resources/type-map.h2.tsv"));
        vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
    }

    private Boolean[] createNewPropArray() {
        Boolean[] props = new Boolean[3];
        props[0] = false;
        props[1] = false;
        props[2] = false;

        return props;
    }

    private TermConstructorConverter createTCConverter(ViewDefinition viewDef, String varName) {
        VarDefinition varDefs = viewDef.getVarDefinition();
        Collection<RestrictedExpr> varDef = varDefs.getDefinitions(Var.alloc(varName));
        RestrictedExpr restr = (RestrictedExpr) varDef.toArray()[0];
        List<Expr> exprs = Arrays.asList(restr.getExpr());
        // unwrap inner exprs from outer term constructor function
        E_RdfTerm tc = (E_RdfTerm) exprs.get(0).getFunction();
        exprs = tc.getArgs();
        exprs.remove(0);

        return new TermConstructorConverter(TermConstructorType.uri, exprs);
    }

    /*
     * - single variable
     * - no constants
     */
    @Test
    public void testCollectProperties01() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Prefix ex: <http://ex.org/> " +
                "Create View test01 As " +
                    "Construct {" +
                        "?empl a ex:Employee " +
                    "} " +
                    "With " +
                        "?empl = uri(?empl_uri) " +
                    "From " +
                        "empl");

        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertTrue(props[0]);  // there is a variable
        assertFalse(props[1]);  // there are no multiples variables
        assertFalse(props[2]);  // there are no constants
    }

    /*
     * - multiple variables
     * - no constants
     */
    @Test
    public void testCollectProperties02() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Create View test02 As " +
                    "Construct { " +
                        "?empl a ex:Employee " +
                    "} " +
                    "With " +
                        "?empl = uri(?empl_uri, ?dept_id) " +
                    "From " +
                        "empl");
        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertTrue(props[0]);
        assertTrue(props[1]);
        assertFalse(props[2]);
    }

    /*
     * - no variables
     * - constants
     */
    @Test
    public void testCollectProperties03() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Create View test03 As " +
                "Construct { " +
                    "?empl a ex:Employee " +
                "} " +
                "With " +
                    "?empl = uri('http://ex.org/empl23') " +
                "From " +
                    "empl");

        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertFalse(props[0]);
        assertFalse(props[1]);
        assertTrue(props[2]);
    }

    /*
     * - single variable
     * - constants
     */
    @Test
    public void testCollectProperties04() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Create View test05 As " +
                "Construct { " +
                    "?empl a ex:Employee " +
                "} " +
                "With " +
                    "?empl = uri('http://ex.org/empl', ?id) " +
                "From " +
                    "empl");

        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertTrue(props[0]);
        assertFalse(props[1]);
        assertTrue(props[2]);
    }

    /*
     * - multiples variables
     * - constants
     */
    @Test
    public void testCollectProperties05() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Create View test05 As " +
                "Construct { " +
                    "?empl a ex:Employee " +
                "} " +
                "With " +
                    "?empl = uri('http://ex.org/empl/',?name, '/', ?id) " +
                "From " +
                    "empl");

        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertTrue(props[0]);
        assertTrue(props[1]);
        assertTrue(props[2]);
    }

    /*
     * explicit function call
     */
    @Test
    public void testCollectProperties06() {
        Boolean[] props = createNewPropArray();
        ViewDefinition viewDef = vdf.create(
                "Create View test06 As " +
                "Construct { " +
                    "?empl a ex:Employee " +
                "} " +
                "With " +
                    "?empl = uri(concat('http://ex.org/empl', ?id)) " +
                "From " +
                    "empl");

        TermConstructorConverter tcc = createTCConverter(viewDef, "empl");
        tcc.collectProperties(tcc.exprs, props);

        assertTrue(props[0]);
        assertFalse(props[1]);
        assertTrue(props[2]);
    }
}
