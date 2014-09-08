package org.aksw.sml.converters.sml2r2rml;

import java.util.List;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;

enum TermConstructorType { bNode, uri, plainLiteral, typedLiteral };

public class TermConstructorConverter {
    private final TermConstructorType type;
    protected Expr expr;
    private Property termMapPredicate = null;
    private final String langStr;
    private final Resource dtype;

    public TermConstructorConverter(TermConstructorType type, List<Expr> exprs) {
        this.type = type;

        if (type.equals(TermConstructorType.typedLiteral)) {
            Node dtype = ((NodeValueNode) exprs.get(2)).getNode();
            this.dtype = ResourceFactory.createResource(((Node_URI) dtype).getURI());
        } else {
            this.dtype = null;
        }

        String langStr = ((NodeValueString) exprs.get(1)).asString();

        if (type.equals(TermConstructorType.plainLiteral) && !langStr.isEmpty()) {
            this.langStr = langStr;
        } else {
            this.langStr = null;
        }
        this.expr = exprs.get(0);
    }

    public Resource getTermType() {
        if (type.equals(TermConstructorType.bNode)) {
            return RR.BlankNode;
        } else if (type.equals(TermConstructorType.uri)) {
            return RR.IRI;
        } else {
            return RR.Literal;
        }
    }

    /**
     * Returns rr:constant, rr:column or rr:template, depending on the
     * structure of the term constructor expressions:
     * - rr:constant: if the expressions only contain constants
     * - rr:template: if the expressions contain constants and variables or
     *   multiple variables without constants
     * - rr:column: if the expressions contain only one variable
     * @return
     */
    public Property getMapPredicate() {
        if (termMapPredicate != null) {
            return termMapPredicate;

        } else {
            boolean hasVar = false;
            boolean hasMultipleVars = false;
            boolean hasConstants = false;
            Boolean[] exprsProps = new Boolean[3];
            exprsProps[0] = hasVar;
            exprsProps[1] = hasMultipleVars;
            exprsProps[2] = hasConstants;

            collectProperties(expr, exprsProps);

            // rr:column
            if (exprsProps[0] && !exprsProps[1] && !exprsProps[2]) {  // only single var
                termMapPredicate = RR.column;

            // rr:constant
            } else if (exprsProps[2] && !exprsProps[0]) {  // only constant(s)
                termMapPredicate =  RR.constant;

            // rr:template
            } else {
                termMapPredicate = RR.template;
            }

            return termMapPredicate;
        }
    }

    protected void collectProperties(Expr expr, Boolean[] props) {
        if (expr.isConstant() && (!expr.getConstant().isString() ||
                !expr.getConstant().getString().isEmpty())) {
            props[2] = true;  // set the constants flag

        } else if (expr.isFunction()) {
            List<Expr> funcExprs = expr.getFunction().getArgs();
            for (Expr arg : funcExprs) {
                collectProperties(arg, props);
            }

        } else if (expr.isVariable()) {
            if (props[0]) {  // if there were already variables fond so far...
                props[1] = true;  // ...set the multiple variables flag
            } else {
                props[0] = true;  // ...else just set the variable flag
            }
        }
    }

    public Literal getMapObject() {
        StringBuilder exprStrBuilder = new StringBuilder();

        if (getMapPredicate().equals(RR.column)) {
            // there is only one variable in this.exprs
            Var colVar = expr.asVar();
            exprStrBuilder.append(colVar.getName());
        } else {
            for (Expr arg : expr.getFunction().getArgs()) {
                buildMapObjStr(exprStrBuilder, arg);
            }
        }

        return ResourceFactory.createPlainLiteral(exprStrBuilder.toString());
    }

    public Resource getDatatype() {
        return dtype;
    }

    public Literal getLang() {
        if (langStr != null) {
            return ResourceFactory.createPlainLiteral(langStr);
        } else {
            return null;
        }
    }

    private void buildMapObjStr(StringBuilder exprStr, Expr expr) {
        if (expr.isConstant() && (!expr.getConstant().isString() ||
                !expr.getConstant().getString().isEmpty())) {
            NodeValue constant = expr.getConstant();
            if (constant.isIRI() || constant.isLiteral()) {
                // strip off leading and trailing angle brackets or quotes
                String nodeStr = constant.toString();
                int strLen = nodeStr.length();
                exprStr.append(nodeStr.substring(1, strLen-1));
            } else {
                exprStr.append(expr.getConstant().toString());
            }

        } else if (expr.isFunction()) {
            ExprFunction func = expr.getFunction();
            List<Expr> funcArgs = func.getArgs();
            for (Expr arg : funcArgs) {
                buildMapObjStr(exprStr, arg);
            }

        } else if (expr.isVariable()) {
            String varName = expr.asVar().getName();
            exprStr.append("{");
            exprStr.append(varName);
            exprStr.append("}");
        }
    }
}
