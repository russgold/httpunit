package com.meterware.xml;
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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.text.ParseException;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class DocumentSemantics {

    private static final Class[]  NO_ARG_CLASSES = new Class[0];
    private static final Object[] NO_ARGS        = new Object[0];

    public static void build( Document document, Object documentRoot ) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ParseException {
        interpretNode( getRootNode( document ), documentRoot );
    }


    public static Node getRootNode( Document document ) {
        Node node = document.getFirstChild();
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) node = node.getNextSibling();
        if (node != null) return node;
        throw new RuntimeException( "Document has no root node" );
    }


    private static void interpretNode( final Node node, Object elementObject ) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ParseException {
        Class elementClass = elementObject.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo( elementClass, Object.class );
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++ ) {
                Node attribute = nnm.item(i);
                setProperty( elementObject, beanInfo, toPropertyName( attribute.getNodeName() ), attribute.getNodeValue() );
            }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {;
                interpretNestedElement( elementClass, elementObject, beanInfo, child );
            } else if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                setContentsAsProperty( elementObject, beanInfo, node, "text" );
            }
        }
    }


    private static String toPropertyName( String attributeName ) {
        if (attributeName.indexOf( '-' ) < 0) return attributeName;
        int index;
        while ((index=attributeName.indexOf( '-' ))>0) {
            attributeName = attributeName.substring( 0, index )
                          + Character.toUpperCase( attributeName.charAt( index+1 ) )
                          + attributeName.substring( index+2 );
        }
        return attributeName;
    }


    private static void interpretNestedElement( Class elementClass, Object elementObject, BeanInfo beanInfo, Node child ) throws IllegalAccessException, InvocationTargetException, IntrospectionException, ParseException {
        try {
            Method method = elementClass.getMethod( getCreateMethodName( child.getNodeName() ), NO_ARG_CLASSES );
            Object subElement = method.invoke( elementObject, NO_ARGS );
            interpretNode( child, subElement );
        } catch (NoSuchMethodException e) {
            setContentsAsProperty( elementObject, beanInfo, child, child.getNodeName() );
        } catch (SecurityException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }


    private static String getCreateMethodName( String nodeName ) {
        nodeName = toPropertyName( nodeName );
        StringBuffer sb = new StringBuffer( "create" );
        sb.append( Character.toUpperCase( nodeName.charAt( 0 ) ) );
        sb.append( nodeName.substring( 1 ) );
        return sb.toString();
    }


    private static void setContentsAsProperty( Object elementObject, BeanInfo beanInfo, Node propertyNode, String propertyName ) throws IllegalAccessException, InvocationTargetException {
        for (Node child = propertyNode.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != Node.TEXT_NODE && child.getNodeType() != Node.CDATA_SECTION_NODE) continue;
            setProperty( elementObject, beanInfo, propertyName, child.getNodeValue() );
            return;
        }
    }


    private static void setProperty( Object elementObject, BeanInfo beanInfo, final String propertyName, final String propertyValue ) throws IllegalAccessException, InvocationTargetException {
        PropertyDescriptor descriptor = getProperty( beanInfo, propertyName );
        if (descriptor == null || descriptor.getWriteMethod() == null) return;
        Class propertyType = descriptor.getPropertyType();
        Method writeMethod = descriptor.getWriteMethod();
        Object[] args = toPropertyArgumentArray( propertyType, propertyValue );
        writeMethod.invoke( elementObject, args );
    }


    private static Object[] toPropertyArgumentArray( Class propertyType, String nodeValue ) {
        if (propertyType.equals( String.class )) {
            return new Object[] { nodeValue };
        } else if (propertyType.equals( int.class )) {
            return new Object[] { new Integer( nodeValue ) };
        } else if (propertyType.equals( Date.class )) {
            return new Object[] { new Date( nodeValue ) };
        } else if (propertyType.equals( boolean.class )) {
            return new Object[] { Boolean.valueOf( nodeValue ) };
        } else {
            throw new RuntimeException( propertyType + " attributes not supported" );
        }
    }


    private static PropertyDescriptor getProperty( BeanInfo beanInfo, String propertyName ) {
        PropertyDescriptor properties[] = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor property = properties[i];
            if (property.getName().equals( propertyName )) return property;
        }
        return null;
    }

}
