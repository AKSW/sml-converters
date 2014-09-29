package org.aksw.sml.converters.r2rml2sml;

import org.aksw.sml.converters.vocabs.RR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


public class LogicalTable {
    public static final String tblType = "table";
    public static final String queryType = "query";
    public static enum LogTableType { TABLE, QUERY };

    private final Model model;
    private final Resource lTblResource;
    private LogTableType type;
    private final String lTblExpression;

    /** @author sherif */
    public LogicalTable(Model model, Resource logicalTableResource) {
        super();
        this.model = model;
        this.lTblResource = logicalTableResource;

        StmtIterator props = model.listStatements(lTblResource, null, (RDFNode) null);
        Statement sttmnt = RRUtils.getStatementFromSet(props.toSet());
        Property logTblProp = sttmnt.getPredicate();

        // get table type
        if (logTblProp.equals(RR.tableName)) {
            type = LogTableType.TABLE;

        } else if (logTblProp.equals(RR.sqlQuery)) {
            type = LogTableType.QUERY;

        } else {
            throw new RuntimeException("The logical table should have " +
                    "either an rr:tableName or rr:sqlQuery property");
        }

        // get table expression
        RDFNode logTblObject = sttmnt.getObject();
        lTblExpression = logTblObject.asLiteral().getString();
    }

    public boolean isTable() {
        return type.equals(LogTableType.TABLE);
    }

    public boolean isQuery() {
        return type.equals(LogTableType.QUERY);
    }

    public String getTableExpression() {
        return lTblExpression;
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
        return lTblResource;
    }

    /**
     * Returns the actual logical table resource (should be a blank node)
     * @return the actual logical table resource (should be a blank node)
     */
    public Resource getResource() {
        return lTblResource;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Implement properly - take the case of SQL query into account
        return getTableExpression();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lTblExpression == null) ? 0 : lTblExpression.hashCode());

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

        if (this.lTblExpression.equals(other.getTableExpression())) {
            return true;
        } else {
            return false;
        }
    }
}
