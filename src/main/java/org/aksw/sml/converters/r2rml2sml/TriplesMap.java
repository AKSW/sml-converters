package org.aksw.sml.converters.r2rml2sml;

import java.util.HashSet;
import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TriplesMap {
    private Model model;
    private Resource subject;

    /**
     * @author sherif
     */
    public TriplesMap(Model model, Resource subject) {
        super();
        this.model = model;
        this.subject = subject;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @return the subject
     */
    @Deprecated
    public Resource getSubject() {
        return subject;
    }

    /**
     * Returns the actual triples map resource
     * @return the actual triples map resource
     */
    public Resource getResource() {
        return subject;
    }

    public Set<PredicateObjectMap> getPredicateObjectMaps() {
        Set<PredicateObjectMap> result = new HashSet<PredicateObjectMap>();

        // list all predicate object maps
        Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.predicateObjectMap).toSet();

        for(RDFNode object : objects) {
            Resource r = (Resource)object;
            PredicateObjectMap item = new PredicateObjectMap(model, r); 

            result.add(item);
        }
        return result;
    }

    public LogicalTable getLogicalTable() {
        Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.logicalTable).toSet();

        Resource resource = RRUtils.getResourceFromSet(objects);
        LogicalTable result = new LogicalTable(model, resource);

        return result;
    }

    public SubjectMap getSubjectMap() {
        Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.subjectMap).toSet();

        Resource resource = RRUtils.getResourceFromSet(objects);
        SubjectMap result = new SubjectMap(model, resource);

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        TriplesMap other = (TriplesMap) obj;

        if (model == null) {
            if (other.model != null) return false;

        } else if (!model.equals(other.model)) return false;

        if (subject == null) {
            if (other.subject != null) return false;

        } else if (!subject.equals(other.subject)) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + subject;
    }
}
