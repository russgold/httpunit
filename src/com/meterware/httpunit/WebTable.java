package com.meterware.httpunit;
/*******************************************************************************************
 * $Id$
 ******************************************************************************************/

import java.net.URL;

import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.*;

/**
 * This class represents a table in an HTML page.
 **/
public class WebTable {


    /**
     * Returns the number of rows in the table.
     **/
    public int getRowCount() {
        if (_cells == null) readTable();
        return _cells.length;
    }


    /**
     * Returns the number of columns in the table.
     **/
    public int getColumnCount() {
        if (_cells == null) readTable();
        return _cells[0].length;
    }


    /**
     * Returns the contents of the specified table cell as text. 
     * The row and column numbers are zero-based.
     * @throws IndexOutOfBoundsException if the specified cell numbers are not valid
     **/
    public String getCell( int row, int column ) {
        if (_cells == null) readTable();
        return getCellContentsAsText( _cells[ row ][ column ] );
    }


    private String getCellContentsAsText( Node node ) {
        if (node == null) {
            return null;
        } else if (!node.hasChildNodes()) {
            return null;
        } else {
            return asText( node.getFirstChild() );
        }
    }



    private String asText( Node rootNode ) {
        StringBuffer sb = new StringBuffer();
        Stack pendingNodes = new Stack();
        pendingNodes.push( rootNode );

        while (!pendingNodes.empty()) {
            Object pending = pendingNodes.pop();
            if (pending instanceof String) {
                sb.append( pending );
            } else {
                Node node = (Node) pending;

                if (node.getNodeType() == Node.TEXT_NODE) {
                    sb.append( node.getNodeValue() );
                } else if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                } else if (node.getNodeName().equalsIgnoreCase( "p" )) {
                    sb.append( "\n" );
                } else if (node.getNodeName().equalsIgnoreCase( "tr")) {
                    sb.append( "\n" );
                    pendingNodes.push( " |" );
                } else if (node.getNodeName().equalsIgnoreCase( "td")) {
                    sb.append( " | " );
                } else if (node.getNodeName().equalsIgnoreCase( "th")) {
                    sb.append( " | " );
                }

                NodeList nl = node.getChildNodes();
                if (nl != null) {
                    for (int i = nl.getLength()-1; i >= 0; i--) {
                        pendingNodes.push( nl.item(i) );
                    }
                }
            }
        }
        return sb.toString();
    }


    public String toString() {
        String eol = System.getProperty( "line.separator" );
        if (_cells == null) readTable();
        StringBuffer sb = new StringBuffer( "WebTable:" ).append( eol );
        for (int i = 0; i < _cells.length; i++) {
            sb.append( "[" ).append( i ).append( "]: " );
            for (int j = 0; j < _cells[i].length; j++) {
                sb.append( "  [" ).append( j ).append( "]=" );
                if (_cells[i][j] == null) {
                    sb.append( "null" );
                } else {
                    sb.append( _cells[i][j].getFirstChild().getNodeValue() );
                }
            }
            sb.append( eol );
        }
        return sb.toString();
    }


//----------------------------------- package members -----------------------------------


    /**
     * Returns the top-level tables found in the specified DOM.
     **/
    static WebTable[] getTables( Document document ) {
        NodeList nl = document.getElementsByTagName( "table" );
        Vector topLevelTables = new Vector();

        for (int i = 0; i < nl.getLength(); i++) {
            if (isTopLevelTable( nl.item(i), document )) {
                topLevelTables.addElement( new WebTable( nl.item(i) ) );
            }
        }

        WebTable[] result = new WebTable[ topLevelTables.size() ];
        topLevelTables.copyInto( result );
        return result;
    }



//----------------------------------- private members -----------------------------------

    private Element _dom;


    private int[] _columnsRequired;
    private Node[][] _cells;


    private WebTable( Node domTreeRoot ) {
        _dom = (Element) domTreeRoot;
    }



    private void readTable() {
        TableRow[] rows = getRows();
        _columnsRequired = new int[ rows.length ];

        for (int i = 0; i < rows.length; i++) {
            TableCell[] cells = rows[i].getCells();
            for (int j = 0; j < cells.length; j++) {
                int spannedRows = Math.min( _columnsRequired.length-i, cells[j].getRowSpan() );
                for (int k = 0; k < spannedRows; k++) {
                    _columnsRequired[ i+k ]+= cells[j].getColSpan();
                }
            }
        }
        int numColumns = 0;
        for (int i = 0; i < _columnsRequired.length; i++) {
            numColumns = Math.max( numColumns, _columnsRequired[i] );
        }

        _cells = new Node[ _columnsRequired.length ][ numColumns ];

        for (int i = 0; i < rows.length; i++) {
            TableCell[] cells = rows[i].getCells();
            for (int j = 0; j < cells.length; j++) {
                int spannedRows = Math.min( _columnsRequired.length-i, cells[j].getRowSpan() );
                for (int k = 0; k < spannedRows; k++) {
                    for (int l = 0; l < cells[j].getColSpan(); l++) {
                       placeCell( i+k, j+l, cells[j].getElement() );
                    }     
                }
             }
         }
    }



    private void placeCell( int row, int column, Node cell ) {
        while (_cells[ row ][ column ] != null) column++;
        _cells[ row ][ column ] = cell;
    }


    static private int getAttributeValue( Node node, String attributeName, int defaultValue ) {
        NamedNodeMap nnm = node.getAttributes();
        Node span = nnm.getNamedItem( attributeName );
        if (span == null) {
            return defaultValue;
        } else try {
            return Integer.parseInt( span.getNodeValue() );
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * Returns true if the specified table node is not nested within another one.
     **/
    private static boolean isTopLevelTable( Node tableNode, Node root ) {
        return isMoreCloselyNested( tableNode, root, "table" );
    }


    /**
     * Returns true if the desiredParentTag is found in the tree above this node more closely nested than
     * the undesiredParentTag, or if the top of the tree is found before the undesired tag.
     **/ 
    private static boolean isMoreCloselyNested( Node node, Node desiredParentNode, String undesiredParentTag ) {
        node = node.getParentNode();
        while (true) {
           if (node == desiredParentNode) {
               return true;
           } else if (node.getNodeName().equalsIgnoreCase( undesiredParentTag )) {
               return false;
           } else if (node.getParentNode() == null) {
               return true;
           }
           node = node.getParentNode();
        }
    }



    private TableRow[] getRows() {
        final Vector rows = new Vector();

        processChildren( _dom, "tr", "table", new ElementHandler() {
            public void handleElement( Element element ) {
                rows.addElement( new TableRow( element ) );
            }
        } );

        TableRow[] result = new TableRow[ rows.size() ];
        rows.copyInto( result );
        return result;
    }


    class TableRow {
        Element getElement() {
            return _element;
        }

        TableCell[] getCells() {
            Vector cells = new Vector();
            collectChildren( "td", cells );
            collectChildren( "th", cells );

            TableCell[] result = new TableCell[ cells.size() ];
            cells.copyInto( result );
            return result;
        }


        private void collectChildren( String childTag, final Vector children ) {
            processChildren( _element, childTag, "table", new ElementHandler() {
                public void handleElement( Element element ) {
                    children.addElement( new TableCell( element ) );
                }
            } );
        }


        private Element _element;
        TableRow( Element rowNode ) {
            _element = rowNode;
        }
    }


    class TableCell {
        Element getElement() {
            return _element;
        }

        int getColSpan() {
            return _colSpan;
        }

        int getRowSpan() {
            return _rowSpan;
        }

        private Element _element;
        private int     _colSpan;
        private int     _rowSpan;

        TableCell( Element cellNode ) {
            _element = cellNode;
            _colSpan = getAttributeValue( cellNode, "colspan", 1 );
            _rowSpan = getAttributeValue( cellNode, "rowspan", 1 );
        }
    }


    interface ElementHandler {
        public void handleElement( Element element );
    }


    static void processChildren( Element root, String childTag, String avoidingParentTag, ElementHandler handler ) {
        NodeList nl = root.getElementsByTagName( childTag );
        for (int i = 0; i < nl.getLength(); i++) {
            if (isMoreCloselyNested( nl.item(i), root, avoidingParentTag )) {
                handler.handleElement( (Element) nl.item(i) );
            }
        }
    }


}




