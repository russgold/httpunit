package com.meterware.website;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.cyberneko.html.parsers.DOMParser;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import com.meterware.xml.DocumentSemantics;
import com.meterware.website.FragmentTemplate;


/**
 *
 * @author <a href="mailto:russgold@meterware.com">Russell Gold</a>
 **/
public class PageFragment {

    private String _source;
    private String _extract;

    private static File _root;


    static void setRoot( File root ) {
        _root = root;
    }


    public String asText() {
        try {
            File file = new File( _source );
            if (!file.exists()) file = new File( _root, _source );
            if (isXmlFile( file )) {
                return getXMLBasedFragment( file );
            } else if (isHtmlFile( file )) {
                return getHtmlBasedFragment( file );
            } else {
                return getTextFragment( file );
            }
        } catch (SAXException e) {
            throw new RuntimeException( "Error parsing " + _source + ": " + e );
        } catch (IOException e) {
            throw new RuntimeException( "Error reading " + _source + ": " + e );
        }
    }


    private boolean isXmlFile( File file ) {
        return file.getName().toLowerCase().endsWith( ".xml" );
    }


    private boolean isHtmlFile( File file ) {
        return file.getName().toLowerCase().indexOf( ".htm" ) > 0;
    }


    private String getTextFragment( File file ) {
        return "??? some text goes here ???";
    }


    private String getHtmlBasedFragment( File file ) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        parser.parse( new InputSource( new FileInputStream( file ) ) );
        Document document = parser.getDocument();
        NodeList nl = document.getElementsByTagName( "body" );
        if (nl.getLength() == 0) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            appendSubtree( "  ", sb, nl.item(0).getChildNodes() );
            return sb.toString();
        }
    }


    private void appendSubtree( String prefix, StringBuffer sb, NodeList childNodes ) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                sb.append( node.getNodeValue() );
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (needsNewLine( node.getNodeName() )) sb.append( FragmentTemplate.LINE_BREAK ).append( prefix );
                sb.append( '<' ).append( node.getNodeName() );
                NamedNodeMap attributes = node.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attribute = attributes.item(j);
                    sb.append( ' ' ).append( attribute.getNodeName() ).append( "=\"" ).append( attribute.getNodeValue() ).append( '"' );
                }
                sb.append( '>' );
                appendSubtree( prefix + "  ", sb, node.getChildNodes() );
                sb.append( "</" ).append( node.getNodeName() ).append( '>' );
                if (needsNewLine( node.getNodeName() )) sb.append( FragmentTemplate.LINE_BREAK ).append( prefix.substring(2) );
            }
        }
    }


    private boolean needsNewLine( String nodeName ) {
        nodeName = nodeName.trim();
        if (nodeName.startsWith( "/")) nodeName = nodeName.substring(1).trim();
        if (nodeName.endsWith( "/")) nodeName = nodeName.substring( 0, nodeName.length()-1 ).trim();
        return !nodeName.equalsIgnoreCase( "a" ) && !nodeName.equalsIgnoreCase( "b" );
    }


    private String getXMLBasedFragment( File file ) throws SAXException, IOException {
        Document document = DocumentSemantics.parseDocument( file );
        FragmentTemplate template = FragmentTemplate.getTemplateFor( DocumentSemantics.getRootNode( document ).getNodeName() );
        DocumentSemantics.build( document, template, file.getAbsolutePath() );
        return _extract == null ? template.asText() : DocumentSemantics.getStringProperty( _extract, template );
    }


    public void setSource( String source ) {
        _source = source;
    }


    public void setExtract( String extract ) {
        _extract = extract;
    }
}
