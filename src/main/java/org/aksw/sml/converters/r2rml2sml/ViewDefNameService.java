package org.aksw.sml.converters.r2rml2sml;

import java.util.HashSet;
import java.util.Set;

public class ViewDefNameService {
    Set<String> seenNames;

    public ViewDefNameService() {
        seenNames = new HashSet<String>();
    }
    
    /**
     * Uses the local part of a given URI as name. If this name was already
     * used it gets a number postfix
     * @param triplesMapUri an URI string
     * @return
     */
    public String getNameFromUri(String triplesMapUri) {
        String[] triplesMapUriParts = triplesMapUri.split("/");
        String localPart = triplesMapUriParts[triplesMapUriParts.length-1];
        String name = localPart;
        int counter = 2;

        while (seenNames.contains(name)) {
            name = localPart + counter;
            counter++;
        }
        seenNames.add(name);
        return name;
    }

}
