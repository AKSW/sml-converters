package org.aksw.sml.converters.r2rml2sml;

import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SubjectMap {

    private Model model;
    private Resource subject;

    /** @author sherif */
    public SubjectMap(Model model, Resource subject) {
        super();
        this.model = model;
        this.subject = subject;
    }

    public RDFNode getRrClass() {
        Set<RDFNode> objects = model.listObjectsOfProperty(subject,
                ResourceFactory.createProperty(RR.class_)).toSet();

        if(objects.isEmpty()) {
            return null;
        }

        RDFNode node = RRUtils.getFirst(objects);

        return node;
    }

    public String getTemplate() {
        Set<RDFNode> objects = model.listObjectsOfProperty(subject,
                ResourceFactory.createProperty(RR.template)).toSet();

        if(objects.isEmpty()) {
            return null;
        }

        RDFNode node = RRUtils.getFirst(objects);
        String result = "" + node.asNode().getLiteralValue();

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ""+ subject;
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

        SubjectMap other = (SubjectMap) obj;

        if (model == null) {
            if (other.model != null) return false;

        } else if (!model.equals(other.model)) return false;

        if (subject == null) {
            if (other.subject != null) return false;

        } else if (!subject.equals(other.subject)) return false;

        return true;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * @return the subject
     */
    public Resource getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(Resource subject) {
        this.subject = subject;
    }
}
