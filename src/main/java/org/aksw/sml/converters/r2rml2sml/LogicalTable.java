package org.aksw.sml.converters.r2rml2sml;

import java.util.Set;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


public class LogicalTable {
    private Model model;
    private Resource subject;

    /** @author sherif */
    public LogicalTable(Model model, Resource subject) {
        super();
        this.model = model;
        this.subject = subject;
    }

    public boolean isTableName() {
        return getTableName() != null;
    }

    public String getTableName() {
        Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.tableName).toSet();

        if(objects.isEmpty()) {
            return null;
        }

        RDFNode node = RRUtils.getFirst(objects);
        String result = "" + node.asNode().getLiteralValue();

        return result;
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
    public Resource getSubject() {
        return subject;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Implement properly - take the case of SQL query into account
        return getTableName();
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

        LogicalTable other = (LogicalTable) obj;
        if (model == null) {
            if (other.model != null) return false;

        } else if (!model.equals(other.model))
            return false;

        if (subject == null) {
            if (other.subject != null) return false;

        } else if (!subject.equals(other.subject)) return false;

        return true;
    }
}
