package org.aksw.sml.converters.r2rml2sml;

import java.util.Arrays;
import java.util.List;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public abstract class TermMap {

    protected Model model;
    protected Resource resource;
    protected Property termMapProperty = null;

    private Resource datatype;
    private boolean datatypeChecked;
    private Literal language;
    private boolean languageChecked;
    
    private Resource termType = null;

    public TermMap(Model model, Resource termMapResource) {
        this.model = model;
        this.resource = termMapResource;
        setTermMapProperty();
        datatype = null;
        datatypeChecked = false;
        language = null;
        languageChecked = false;
    }

    private void setTermMapProperty() {
        List<Property> mapTypeStatements = Arrays.asList(RR.column,
                RR.template, RR.constant);

        StmtIterator sttmntIt = model.listStatements(resource, null, (RDFNode) null);

        while(sttmntIt.hasNext()) {
            Statement statement = sttmntIt.next();
            Property prop = statement.getPredicate();
            if (mapTypeStatements.contains(prop)) {
                termMapProperty = prop;
                break;
            }
        }
        if (termMapProperty == null) {
            throw new RuntimeException("There should be a term map property " +
                    "but none is contained in the current term map model: " +
                    model.toString());
        }
    }

    public boolean isConstantTermMap() {
        return termMapProperty.equals(RR.constant);
    }

    public boolean isTemplateTermMap() {
        return termMapProperty.equals(RR.template);
    }

    public boolean isColumnTermMap() {
        return termMapProperty.equals(RR.column);
    }

    public Resource getResource() {
        return resource;
    }

    public Model getModel() {
        return model;
    }

    public RDFNode getConstantTerm() {
        RDFNode term = RRUtils.getNodeFromSet(model.listObjectsOfProperty(
                resource, RR.constant).toSet());
        return term;
    }

    public Literal getColumnTerm() {
        RDFNode term = RRUtils.getNodeFromSet(model.listObjectsOfProperty(
                resource, RR.column).toSet());
        return term.asLiteral();
    }

    public Literal getTemplateTerm() {
        RDFNode term = RRUtils.getNodeFromSet(model.listObjectsOfProperty(
                resource, RR.template).toSet());
        return term.asLiteral();
    }

    public RDFNode getTerm() {
        if (isConstantTermMap()) {
            return getConstantTerm();

        } else if (isColumnTermMap()) {
            return getColumnTerm();

        } else if (isTemplateTermMap()) {
            return getTemplateTerm(); 

        } else {
            return null;
        }
    }

    public Literal getLanguage() {
        if (languageChecked) {
            return language;

        } else {
            Literal lang = (Literal) RRUtils.getNodeFromSetIfExists(model
                    .listObjectsOfProperty(resource, RR.language).toSet());
            language = lang;
            languageChecked = true;
            return lang;
        }
    }

    public boolean hasLanguage() {
        return getLanguage() != null;
    }

    public Resource getDatatype() {
        if (datatypeChecked) {
            return datatype;
        } else {
            Resource datatype = (Resource) RRUtils.getNodeFromSetIfExists(model
                    .listObjectsOfProperty(resource, RR.datatype).toSet());
            this.datatype = datatype;
            datatypeChecked = true;
            return datatype;
        }
    }

    public boolean hasDatatype() {
        return getDatatype() != null;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        TermMap other = (TermMap) obj;

        if (model == null) {
            if (other.model != null) return false;

        } else if (!model.equals(other.model)) return false;

        if (resource == null) {
            if (other.resource != null) return false;

        } else if (!resource.equals(other.resource)) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return resource.toString();
    }

    /**
     * FIXME: consider rr:join (if this can be considered as type)
     */
    public Resource getTermType() {
        if (termType == null) {

            /* first check if a term type is set explicitely */
            Statement termTypeStatement = RRUtils.getStatementFromSetIfExists(model
                    .listStatements(resource, RR.termType, (RDFNode) null).toSet());
    
            if (termTypeStatement != null) {
                termType = (Resource) termTypeStatement.getObject();
            } else {
    
                /*
                 * Quote from http://www.w3.org/TR/r2rml/#termtype:
                 * 
                 * If the term map does not have a rr:termType property, then
                 * its term type is:
                 * - rr:Literal, if it is an object map and at least one of the
                 *   following conditions is true:
                 *   - It is a column-based term map. --> a)
                 *   - It has a rr:language property (and thus a specified
                 *     language tag). --> b)
                 *   - It has a rr:datatype property (and thus a specified
                 *     datatype). --> c)
                 * - rr:IRI, otherwise. --> d)
                 */
                // a)
                if (isColumnTermMap()) {
                    termType = RR.Literal;
        
                // b)
                } else if (hasLanguage()) {
                    termType = RR.Literal;
        
                // c)
                } else if (hasDatatype()) {
                    termType = RR.Literal;
        
                } else {
                    termType = RR.IRI;
                }
            }
        }

        return termType;
    }
}
