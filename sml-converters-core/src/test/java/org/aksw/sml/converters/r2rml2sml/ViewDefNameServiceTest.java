package org.aksw.sml.converters.r2rml2sml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ViewDefNameServiceTest {

    @Test
    public void test_getNameFromUri() {
        ViewDefNameService nameService = new ViewDefNameService();

        String uri1 = "http://ex.org/someLocalPart";
        String xpctdName1 = "someLocalPart";
        String name = nameService.getNameFromUri(uri1);
        assertEquals(xpctdName1, name);

        String uri2 = "http://ex.org/anotherLocalPart/";
        String xpctdName2 = "anotherLocalPart";
        name = nameService.getNameFromUri(uri2);
        assertEquals(xpctdName2, name);

        String xpctdName3 = "someLocalPart2";
        name = nameService.getNameFromUri(uri1);
        assertEquals(xpctdName3, name);
        
        String xpctdName4 = "someLocalPart3";
        name = nameService.getNameFromUri(uri1);
        assertEquals(xpctdName4, name);
    }
}
