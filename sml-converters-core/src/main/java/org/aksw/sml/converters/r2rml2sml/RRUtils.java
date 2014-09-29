package org.aksw.sml.converters.r2rml2sml;

import java.util.Set;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class RRUtils {
    public static <T> T getFirst(Iterable<T> set) {		
        T result = set.iterator().next();

        return result;
    }

    public static RDFNode getNodeFromSet(Set<RDFNode> set) {
        if(set.isEmpty() || set.size() > 1) {
            throw new RuntimeException("Need exactly one element");
        }

        return getFirst(set);
    }

    public static RDFNode getNodeFromSetIfExists(Set<RDFNode> set) {
        if(set.size() > 1) {
            throw new RuntimeException("Need at most one element");
        } else if (set.isEmpty()) {
            return null;
        }

        return getFirst(set);
    }


    public static Resource getResourceFromSet(Set<RDFNode> set) {
        RDFNode item = getNodeFromSet(set);

        return (Resource) item;
    }

    public static Statement getStatementFromSet(Set<Statement> set) {
        if(set.isEmpty() || set.size() > 1) {
            throw new RuntimeException("Need exactly one statement");
        }

        Statement item = getFirst(set);

        Statement result = (Statement) item;
        return result;
    }

    public static Statement getStatementFromSetIfExists(Set<Statement> set) {
        if(set.size() > 1) {
            throw new RuntimeException("Need at most one statement");
        } else if (set.isEmpty()) {
            return null;
        }
        
        Statement item = getFirst(set);

        Statement result = (Statement) item;
        return result;
    }

    public static E_StrConcatPermissive parseTemplate(String str) {
        ExprList args = new ExprList();

        while (!str.isEmpty()) {
            if (str.contains("{") && str.contains("}")) {
                int openIdx = str.indexOf('{');
                int closeIdx = str.indexOf('}');
                int strLen = str.length();

                if (openIdx > 0) {
                    args.add(NodeValue.makeString(str.substring(0, openIdx)));
                }

                String varName = str.substring(openIdx+1, closeIdx);
                args.add(new ExprVar(Var.alloc(varName)));

                str = str.substring(closeIdx+1, strLen);

            } else {
                args.add(NodeValue.makeString(str));
                str = "";
            }
        }

        return new E_StrConcatPermissive(args);
    }
}