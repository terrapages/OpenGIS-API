package com.terrapages.opengis.api.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * The contents of this file are subject to the terms of the Common Development and Distribution License
 * (the "License").  You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at https://jwsdp.dev.java.net/CDDLv1.0.html. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable, add the following below this CDDL HEADER, with
 * the fields enclosed by brackets "[]" replaced with your own identifying information:
 *
 * Portions Copyright [2006] [LISAsoft]
 * @author mkopka - Marty Kopka - 20060921 - 12:12pm (Original)
 * @author chartman - Chris Hartman - 30/03/2007 2:58:00 PM
 * @version $Id: NamespacePrefixMapperUtil.java,v 1.2 2007-04-01 23:12:31 chartman Exp $
 */
public class NamespacePrefixMapperUtil extends NamespacePrefixMapper {

    /**
     * Returns a preferred prefix for the given namespace URI.
     *
     * This method is intended to be overrided by a derived class.
     *
     * @param namespaceUri
     *      The namespace URI for which the prefix needs to be found. Never be null. "" is used to denote the default namespace.
     * @param suggestion
     *      When the content tree has a suggestion for the prefix to the given namespaceUri, that suggestion is passed as a
     *      parameter. Typicall this value comes from the QName.getPrefix to show the preference of the content tree. This
     *      parameter may be null, and this parameter may represent an already occupied prefix.
     * @param requirePrefix
     *      If this method is expected to return non-empty prefix. When this flag is true, it means that the given namespace
     *      URI cannot be set as the default namespace.
     *
     * @return
     *      null if there's no prefered prefix for the namespace URI. In this case, the system will generate a prefix for you.
     *
     *      Otherwise the system will try to use the returned prefix, but generally there's no guarantee if the prefix will be
     *      actually used or not.
     *
     *      return "" to map this namespace URI to the default namespace. Again, there's no guarantee that this preference will
     *      be honored.
     *
     *      If this method returns "" when requirePrefix=true, the return value will be ignored and the system will generate one.
     */
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        /**
         * Note: this whole class had to be added in due to the fact that the dumb asses at budget (well at Massive, the website
         * company that built the Budget website, dont know what an XML document is.  Rather than looking at the definition of
         * the XML, in particular what the namespaces have been defined as in the <XLS> tag, they just compare Hardcoded strings.
         * Dumbasses!!! Now, this has the affect that when we change the namespace (ie: when we implement JAXB say) then the String
         * comparison Massive is doing no longer works...ill say it again...dumbasses!!! So to maintain the same namespace definition
         * Im using this class, which I hope is part of the SPEC rather than just the RI which allows you to define the name of
         * the namespace that you want to use.                     mkopka - Marty - 20060921 - 12:06pm
         */

    	// I want this namespace to be mapped to "xls"; (NOT ANYMORE -> I want the namespace xls to be the default namespace).
        // There is no default namespace anymore because of the global attribute 'lang' in the RouteInstructionsList element of a DetermineRouteResponse
    	// If a global attribute is define the prefix is removed from the attribute and it can't be resolved correctly.
    	if( "http://www.opengis.net/xls".equals(namespaceUri))
            return "xls";
        
        // I want this namespace to be mapped to "gml"
        if( "http://www.opengis.net/gml".equals(namespaceUri) )
            return "gml";

        // I want this namespace to be mapped to "xsi"
        if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) )
            return "xsi";


        // otherwise I don't care. Just use the default suggestion, whatever it may be.
        return suggestion;
    }


    /**
     * Returns a list of namespace URIs that should be declared at the root element.
     * <p>
     * By default, the JAXB RI produces namespace declarations only when they are necessary, only at where they are used. Because
     * of this lack of look-ahead, sometimes the marshaller produces a lot of namespace declarations that look redundant to human
     * eyes. For example,
     * <pre>&lt;xmp&gt;
     * &lt;?xml version="1.0"?&gt;
     * &lt;root&gt;
     *   &lt;ns1:child xmlns:ns1="urn:foo"&gt; ... &lt;/ns1:child&gt;
     *   &lt;ns2:child xmlns:ns2="urn:foo"&gt; ... &lt;/ns2:child&gt;
     *   &lt;ns3:child xmlns:ns3="urn:foo"&gt; ... &lt;/ns3:child&gt;
     *   ...
     * &lt;/root&gt;
     * &lt;xmp&gt;</pre>
     * <p>
     * If you know in advance that you are going to use a certain set of namespace URIs, you can override this method and have the
     * marshaller declare those namespace URIs at the root element.
     * <p>
     * For example, by returning <code>new String[]{"urn:foo"}</code>, the marshaller will produce:
     * <pre>&lt;xmp&gt;
     * &lt;xml version="1.0"?&gt;
     * &lt;root xmlns:ns1="urn:foo"&gt;
     *   &lt;ns1:child&gt; ... &lt;/ns1:child&gt;
     *   &lt;ns1:child&gt; ... &lt;/ns1:child&gt;
     *   &lt;ns1:child&gt; ... &lt;/ns1:child&gt;
     *   ...
     * &lt;/root&gt;
     * &lt;xmp&gt; </pre>
     * <p>
     * To control prefixes assigned to those namespace URIs, use the {@link #getPreferredPrefix} method.
     *
     * @return
     *      A list of namespace URIs as an array of {@link String}s. This method can return a length-zero array but not null.
     *      None of the array component can be null. To represent the empty namespace, use the empty string <code>""</code>.
     *
     * @since
     *      JAXB RI 1.0.2
     */ 
    public String[] getPreDeclaredNamespaceUris() {
        //These are the two namespaces that are used by the Batch Geocoder.
        return new String[] { "http://www.opengis.net/xls", "http://www.opengis.net/gml", "http://www.w3.org/2001/XMLSchema-instance"};
    }                        
}
