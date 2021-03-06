/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.algo.netio;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.algo.ProgrammingError;

import java.util.TreeMap;

/**
 * ResourceLocator
 *
 * @author apete
 */
public final class ResourceLocator {

    private String myHost = null;
    private String myPath = null;
    private int myPort = -1; // -1 ==> undefined
    private Map<String, String> myQueryParameters = new TreeMap<>();
    private String myScheme = "https";

    public ResourceLocator() {
        super();
    }

    public String addQueryParameter(final String aKey, final String aValue) {
        return myQueryParameters.put(aKey, aValue);
    }

    /**
     * Open connection and return a buffered input stream reader.
     */
    public Reader getStreamReader() {
        try {
            return new InputStreamReader(this.toURL().openStream());
        } catch (final IOException anException) {
            return null;
        }
    }

    public String removeQueryParameter(final String aKey) {
        return myQueryParameters.remove(aKey);
    }

    public void setHost(final String someHost) {
        myHost = someHost;
    }

    public void setPath(final String somePath) {
        myPath = somePath;
    }

    /**
     * The default (null) value is -1.
     */
    public void setPort(final int somePort) {
        myPort = somePort;
    }

    public void setQueryParameters(final Map<String, String> someQueryParameters) {
        myQueryParameters = someQueryParameters;
    }

    /**
     * Protocol The default value is "https"
     */
    public void setScheme(final String someScheme) {
        myScheme = someScheme;
    }

    private URI makeURI() {
        try {
            return new URI(myScheme, null, myHost, myPort, myPath, this.query(), null);
        } catch (final URISyntaxException anException) {
            throw new ProgrammingError(anException);
        }
    }

    private String query() {

        if (myQueryParameters.size() >= 1) {

            final StringBuilder retVal = new StringBuilder();

            Entry<String, String> tmpEntry;
            for (final Iterator<Entry<String, String>> tmpIter = myQueryParameters.entrySet().iterator(); tmpIter.hasNext();) {
                tmpEntry = tmpIter.next();
                retVal.append(tmpEntry.getKey());
                retVal.append('=');
                retVal.append(tmpEntry.getValue());
                retVal.append('&');
            }

            // Remove that last '&'
            retVal.setLength(retVal.length() - 1);

            return retVal.toString();

        } else {

            return null;
        }
    }

    private URL toURL() {
        try {
            return this.makeURI().toURL();
        } catch (final MalformedURLException anException) {
            throw new ProgrammingError(anException);
        }
    }

}
