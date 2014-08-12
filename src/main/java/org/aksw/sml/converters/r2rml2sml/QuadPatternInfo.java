package org.aksw.sml.converters.r2rml2sml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

public class QuadPatternInfo {
    public static final Node defaultGraph = Quad.defaultGraphNodeGenerated;

    private Map<Node, List<SubjectPredicatesObjects>> quads;
    
    QuadPatternInfo() {
        quads = new HashMap<Node, List<SubjectPredicatesObjects>>();
        this.addGraph(defaultGraph);
    }
    
    public void addGraph(Node graph) {
        quads.put(graph, new ArrayList<SubjectPredicatesObjects>()); 
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

    public void addSubjectToGraph(Node graph, Node subject) {
        SubjectPredicatesObjects newSubject = new SubjectPredicatesObjects(subject);
        quads.get(graph).add(newSubject);
    }

    public void addPredicateToGraphSubject(Node graph, Node subject, Node predicate) {
        for (SubjectPredicatesObjects spo : quads.get(graph)) {
            if (spo.getSubject().equals(subject)) {
                spo.addPredicate(predicate);
                break;
            }
        }
    }

    /**
     * Adds a new predicate to a subject of a quad using the default graph
     */
    public void addPredicateToSubject(Node subject, Node predicate) {
        addPredicateToGraphSubject(defaultGraph, subject, predicate);
    }

    public void addObjectToGraphSubjectPrediacte(Node graph, Node subject,
            Node predicate, Node object) {

        for (SubjectPredicatesObjects spo : quads.get(graph)) {
            if (spo.getSubject().equals(subject)) {
                PredicateObjects po = spo.getPredicateObjects(predicate);
                po.addObject(object);
            }
        }
    }

    public void addObjectToSubjectPredicate(Node subject, Node predicate, Node object) {
        addObjectToGraphSubjectPrediacte(defaultGraph, subject, predicate, object);
    }

    public Set<Node> getGraphs() {
        return quads.keySet();
    }

    public Set<Node> getSubjectsOfGraph(Node graph) {
        Set<Node> subjects = new HashSet<Node>();
        for (SubjectPredicatesObjects subjectPredicatesObjects : quads.get(graph)) {
            subjects.add(subjectPredicatesObjects.getSubject());
        }
        
        return subjects;
    }
    
    public Set<Node> getPredicateOfGraphSubject(Node graph, Node subject) {
        Set<Node> predicates = new HashSet<Node>();

        for (SubjectPredicatesObjects subjectPredicatesObjects : quads.get(graph)) {
            if (subjectPredicatesObjects.getSubject().equals(subject)) {
                for ( PredicateObjects po : subjectPredicatesObjects.getPredicateObjects()) {
                    predicates.add(po.getPredicate());
                }
            }
        }

        return predicates;
    }

    public Set<Node> getObjectOfGraphSubjectPredicate(Node graph, Node subject, Node predicate) {
        Set<Node> objects = new HashSet<Node>();

        for (SubjectPredicatesObjects spo : quads.get(graph)) {
            if (spo.getSubject().equals(subject)) {
                for ( PredicateObjects po : spo.getPredicateObjects()) {
                    if (po.getPredicate().equals(predicate)) {
                        for (Node object : po.getObjects()) {
                            objects.add(object);
                        }
                    }
                }
            }
        }

        return objects;
    }
}

class SubjectPredicatesObjects {
    private Node subject;
    private Set<PredicateObjects> predicateObjects;

    SubjectPredicatesObjects(Node subject) {
        this.subject = subject;
        predicateObjects = new HashSet<PredicateObjects>();
    }

    void addPredicate(Node predicate) {
        PredicateObjects pred = new PredicateObjects(predicate);
        predicateObjects.add(pred);
    }

    Node getSubject() {
        return subject;
    }

    PredicateObjects getPredicateObjects(Node predicate) {
        for (PredicateObjects po : predicateObjects) {
            if (po.getPredicate().equals(predicate)) {
                return po;
            }
        }
        return null;
    }

    Set<PredicateObjects> getPredicateObjects() {
        return predicateObjects;
    }
}

class PredicateObjects {
    private Node predicate;
    private Set<Node> objects;

    PredicateObjects(Node predicate) {
        this.predicate = predicate;
        objects = new HashSet<Node>();
    }

    void addObject(Node object) {
        objects.add(object);
    }

    Node getPredicate() {
        return predicate;
    }

    Set<Node> getObjects() {
        return objects;
    }
}