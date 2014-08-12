package org.aksw.sml.converters.r2rml2sml;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class XSDUtils {
    private static final Map<Resource, RDFDatatype> resource2XSDDatatype = new HashMap<Resource, RDFDatatype>()
        { private static final long serialVersionUID = -2186317581076058820L;
            {
                put(XSD.anyURI, XSDDatatype.XSDanyURI);
                put(XSD.base64Binary, XSDDatatype.XSDbase64Binary);
                put(XSD.xboolean, XSDDatatype.XSDboolean);
                put(XSD.xbyte, XSDDatatype.XSDbyte);
                put(XSD.date, XSDDatatype.XSDdate);
                put(XSD.dateTime, XSDDatatype.XSDdateTime);
                put(XSD.decimal, XSDDatatype.XSDdecimal);
                put(XSD.xdouble, XSDDatatype.XSDdouble);
                put(XSD.duration, XSDDatatype.XSDduration);
                put(XSD.ENTITY, XSDDatatype.XSDENTITY);
                put(XSD.xfloat, XSDDatatype.XSDfloat);
                put(XSD.gDay, XSDDatatype.XSDgDay);
                put(XSD.gMonth, XSDDatatype.XSDgMonth);
                put(XSD.gMonthDay, XSDDatatype.XSDgMonthDay);
                put(XSD.gYear, XSDDatatype.XSDgYear);
                put(XSD.gYearMonth, XSDDatatype.XSDgYearMonth);
                put(XSD.hexBinary, XSDDatatype.XSDhexBinary);
                put(XSD.ID, XSDDatatype.XSDID);
                put(XSD.IDREF, XSDDatatype.XSDIDREF);
                put(XSD.xint, XSDDatatype.XSDint);
                put(XSD.integer, XSDDatatype.XSDinteger);
                put(XSD.language, XSDDatatype.XSDlanguage);
                put(XSD.xlong, XSDDatatype.XSDlong);
                put(XSD.Name, XSDDatatype.XSDName);
                put(XSD.NCName, XSDDatatype.XSDNCName);
                put(XSD.negativeInteger, XSDDatatype.XSDnegativeInteger);
                put(XSD.NMTOKEN, XSDDatatype.XSDNMTOKEN);
                put(XSD.nonNegativeInteger, XSDDatatype.XSDnonNegativeInteger);
                put(XSD.nonPositiveInteger, XSDDatatype.XSDnonPositiveInteger);
                put(XSD.normalizedString, XSDDatatype.XSDnormalizedString);
                put(XSD.NOTATION, XSDDatatype.XSDNOTATION);
                put(XSD.positiveInteger, XSDDatatype.XSDpositiveInteger);
                put(XSD.QName, XSDDatatype.XSDQName);
                put(XSD.xshort, XSDDatatype.XSDshort);
                put(XSD.xstring, XSDDatatype.XSDstring);
                put(XSD.time, XSDDatatype.XSDtime);
                put(XSD.token, XSDDatatype.XSDtoken);
                put(XSD.unsignedByte, XSDDatatype.XSDunsignedByte);
                put(XSD.unsignedInt, XSDDatatype.XSDunsignedInt);
                put(XSD.unsignedLong, XSDDatatype.XSDunsignedLong);
                put(XSD.unsignedShort, XSDDatatype.XSDunsignedShort);
            }
        };

    public static RDFDatatype getDatatype(Resource dType) {
        return resource2XSDDatatype.get(dType);
    }
}
