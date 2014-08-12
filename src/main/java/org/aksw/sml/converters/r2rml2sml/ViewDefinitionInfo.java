package org.aksw.sml.converters.r2rml2sml;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.Expr;

public class ViewDefinitionInfo {
    public String name = null;
    public SqlOp from = null;
    public QuadPatternInfo quadPatternInfo;
    public Map<Node, Expr> termConstructors;

    ViewDefinitionInfo() {
        quadPatternInfo = new QuadPatternInfo();
        termConstructors = new HashMap<Node, Expr>();
    }
}
