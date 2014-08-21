package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;

public class RRUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /*
     * string contains no variable
     */
    @Test
    public void testParseTemplate01() {
        String templateStr = "only constant";
        E_StrConcatPermissive res = RRUtils.parseTemplate(templateStr);

        // should be (E_StrConcatPermissive) concat("only constant")
        int expctdNumArgs = 1;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdArg = (new StringBuilder()).append("\"")
                .append(templateStr).append("\"").toString();
        assertEquals(expctdArg, fnArgs.get(0).toString());
    }

    /*
     * string only consists of a variable
     */
    @Test
    public void testParseTemplate02() {
        String varName1 = "var1";
        String tmpltStr = (new StringBuilder()).append("{").append(varName1).append("}").toString();
        E_StrConcatPermissive res = RRUtils.parseTemplate(tmpltStr);

        // should be (E_StrConcatPermissive) concat(?ar1)
        int expctdNumArgs = 1;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdArg = (new StringBuilder()).append("?").append(varName1).toString();
        assertEquals(expctdArg, fnArgs.get(0).toString());
    }

    /*
     * string consists of a constant string followed by a variable
     */
    @Test
    public void testParseTemplate03() {
        String varStr1 = "var1";
        String constStr1 = "constant1";
        String tmpltStr = (new StringBuilder()).append(constStr1).append(" {")
                .append(varStr1).append("}").toString();
        E_StrConcatPermissive res = RRUtils.parseTemplate(tmpltStr);

        // should be (E_StrConcatPermissive) concat("constant1 ", ?var1)
        int expctdNumArgs = 2;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdFnArg1Str = (new StringBuilder()).append("\"")
                .append(constStr1).append(" \"").toString();
        assertEquals(expctdFnArg1Str, fnArgs.get(0).toString());

        String expctdFnArg2Str = (new StringBuilder()).append("?").append(varStr1).toString();
        assertEquals(expctdFnArg2Str, fnArgs.get(1).toString());
    }

    /*
     * string consists of a variable followed by a constant string
     */
    @Test
    public void testParseTemplate04() {
        String varStr1 = "var1";
        String constStr1 = "constant1";
        String tmpltStr = (new StringBuilder()).append("{").append(varStr1)
                .append("} ").append(constStr1).toString();
        E_StrConcatPermissive res = RRUtils.parseTemplate(tmpltStr);

        // should be (E_StrConcatPermissive) concat(?var1, " constant1")
        int expctdNumArgs = 2;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdFnArg1Str = (new StringBuilder()).append("?")
                .append(varStr1).toString();
        assertEquals(expctdFnArg1Str, fnArgs.get(0).toString());

        String expctdFnArg2Str = (new StringBuilder()).append("\" ")
                .append(constStr1).append("\"").toString();
        assertEquals(expctdFnArg2Str, fnArgs.get(1).toString());
    }

    /*
     * string consists of a constant string followed by a variable, followed
     * by a constant string
     */
    @Test
    public void testParseTemplate05() {
        String varStr1 = "var1";
        String constStr1 = "constant1";
        String constStr2 = "constant2";
        String tmpltStr = (new StringBuilder()).append(constStr1).append(" {")
                .append(varStr1).append("} ").append(constStr2).toString();
        E_StrConcatPermissive res = RRUtils.parseTemplate(tmpltStr);
        
        // should be (E_StrConcatPermissive) concat("constant1 ", ?var1, " constant2")
        int expctdNumArgs = 3;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdFnArg1Str = (new StringBuilder()).append("\"")
                .append(constStr1).append(" \"").toString();
        assertEquals(expctdFnArg1Str, fnArgs.get(0).toString());

        String expctdFnArg2Str = (new StringBuilder()).append("?")
                .append(varStr1).toString();
        assertEquals(expctdFnArg2Str, fnArgs.get(1).toString());

        String expctdFnArg3Str = (new StringBuilder()).append("\" ")
                .append(constStr2).append("\"").toString();
        assertEquals(expctdFnArg3Str, fnArgs.get(2).toString());
    }

    /*
     * string consists of a constant string followed by a variable, followed
     * by a constant string, followed by a variable, followed by a constant
     * string
     */
    @Test
    public void testParseTemplate06() {
        String varStr1 = "var1";
        String varStr2 = "var2";
        String constStr1 = "constant1";
        String constStr2 = "constant2";
        String constStr3 = "constant3";
        String tmpltStr = (new StringBuilder()).append(constStr1).append(" {")
                .append(varStr1).append("} ").append(constStr2).append(" {")
                .append(varStr2).append("} ").append(constStr3).toString();
        E_StrConcatPermissive res = RRUtils.parseTemplate(tmpltStr);

        // should be (E_StrConcatPermissive) concat("constant1 ", ?var1, " constant2 ", ?var2, " constant3")
        int expctdNumArgs = 5;
        assertEquals(expctdNumArgs, res.numArgs());

        ExprFunction func = res.getFunction();
        FunctionLabel expctdFuncLabel = new FunctionLabel("concat");
        assertEquals(expctdFuncLabel, func.getFunctionSymbol());

        List<Expr> fnArgs = func.getArgs();
        String expctdArg1Str = (new StringBuilder()).append("\"")
                .append(constStr1).append(" \"").toString();
        assertEquals(expctdArg1Str, fnArgs.get(0).toString());

        String expctdArg2Str = (new StringBuilder()).append("?")
                .append(varStr1).toString();
        assertEquals(expctdArg2Str, fnArgs.get(1).toString());

        String expctdArg3Str = (new StringBuilder()).append("\" ")
                .append(constStr2).append(" \"").toString();
        assertEquals(expctdArg3Str, fnArgs.get(2).toString());

        String expctdArg4Str = (new StringBuilder()).append("?")
                .append(varStr2).toString();
        assertEquals(expctdArg4Str, fnArgs.get(3).toString());

        String expctdArg5Str = (new StringBuilder()).append("\" ")
                .append(constStr3).append("\"").toString();
        assertEquals(expctdArg5Str, fnArgs.get(4).toString());
    }
}