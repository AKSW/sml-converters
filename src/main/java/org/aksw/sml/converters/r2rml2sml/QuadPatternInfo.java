package org.aksw.sml.converters.r2rml2sml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

public class QuadPatternInfo {
    public static final Node defaultGraph = Quad.defaultGraphNodeGenerated;

    private final Map<Node, Set<PredicatesObjects>> triples;
    private final Map<Node, Set<Node>> subjectGraphs;
    private final Map<PredicatesObjects, Set<Node>> predObjGraphs;

    QuadPatternInfo() {
        triples = new HashMap<Node, Set<PredicatesObjects>>();
        subjectGraphs = new HashMap<Node, Set<Node>>();
        predObjGraphs = new HashMap<PredicatesObjects, Set<Node>>();
    }

    /**
     * Adds a new subject to the quad pattern info structure using the default
     * graph
     *
     * @param subject the subject node
     */
    public void addSubject(Node subject) {
        addSubjectToGraph(defaultGraph, subject);
    }

    public void addSubjectToGraph(Node subject, Node graph) {
        if (!subjectGraphs.containsKey(subject)) {
            subjectGraphs.put(subject, new HashSet<Node>());
        }
        subjectGraphs.get(subject).add(graph);
    }

    public void addPredicatesObjectsToGraph(PredicatesObjects pos, Node graph) {
        if (!predObjGraphs.containsKey(pos)){
            predObjGraphs.put(pos, new HashSet<Node>());
        }
        predObjGraphs.get(pos).add(graph);
    }

    public void addPredicatesObjectsToSubject(PredicatesObjects pos, Node subject) {
        if (!triples.containsKey(subject)) {
            triples.put(subject, new HashSet<PredicatesObjects>());
        }
        triples.get(subject).add(pos);
    }

    public Set<Node> getSubjects() {
        return triples.keySet();
    }

    public Set<Node> getGraphsForSubject(Node subject) {
        Set<Node> subjGraphs = subjectGraphs.get(subject);

        if (subjGraphs == null) { subjGraphs = new HashSet<Node>(); }

        return subjGraphs;
    }

    public Set<PredicatesObjects> getPredicatesObjectsForSubject(Node subject) {
        return triples.get(subject);
    }

    public Set<Node> getGraphsForPredicatesObjects(PredicatesObjects pos) {
        Set<Node> poGraphs = predObjGraphs.get(pos);

        if (poGraphs == null) { poGraphs = new HashSet<Node>(); }

        return poGraphs;
    }
}

class PredicatesObjects {
    private final Set<Node> predicates;
    private final Set<Node> objects;

    PredicatesObjects() {
        predicates = new HashSet<Node>();
        objects = new HashSet<Node>();
    }

    void addPredicate(Node predicate) {
        predicates.add(predicate);
    }

    void addObject(Node object) {
        objects.add(object);
    }

    Set<Node> getPredicates() {
        return predicates;
    }

    Set<Node> getObjects() {
        return objects;
    }
}