package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
import java.net.URL;

import java.util.*;

import org.w3c.dom.*;

/**
 * This class represents a table in an HTML page.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
public class WebTable {


    /** Predicate to match the complete text of a table's first non-blank cell. **/
    public final static HTMLElementPredicate MATCH_FIRST_NONBLANK_CELL;

    /** Predicate to match a prefix of a table's first non-blank cell. **/
    public final static HTMLElementPredicate MATCH_FIRST_NONBLANK_CELL_PREFIX;

    /** Predicate to match a table's summary attribute. **/
    public final static HTMLElementPredicate MATCH_SUMMARY;

    /** Predicate to match a table's ID. **/
    public final static HTMLElementPredicate MATCH_ID;


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
        if (_cells.length == 0) return 0;
        return _cells[0].length;
    }


    /**
     * Returns the contents of the specified table cell as text.
     * The row and column numbers are zero-based.
     * @throws IndexOutOfBoundsException if the specified cell numbers are not valid
     * @deprecated use #getCellAsText
     **/
    public String getCell( int row, int column ) {
        return getCellAsText( row, column );
    }


    /**
     * Returns the contents of the specified table cell as text.
     * The row and column numbers are zero-based.
     * @throws IndexOutOfBoundsException if the specified cell numbers are not valid
     **/
    public String getCellAsText( int row, int column ) {
        TableCell cell = getTableCell( row, column );
        return (cell == null) ? "" : cell.asText();
    }


    /**
     * Returns the contents of the specified table cell as text.
     * The row and column numbers are zero-based.
     * @throws IndexOutOfBoundsException if the specified cell numbers are not valid
     **/
    public TableCell getTableCell( int row, int column ) {
        if (_cells == null) readTable();
        return _cells[ row ][ column ];
    }


    /**
     * Returns the contents of the specified table cell with a given ID
     * @return TableCell with given ID or null if ID is not found.
     **/
    public TableCell getTableCellWithID( String id ) {
        if (_cells == null) readTable();
        String idToCompare;
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                if (_cells[i][j]!=null) {
                    idToCompare = NodeUtils.getNodeAttribute( _cells[i][j].getOriginalDOM(), "id" );
                    if (HttpUnitUtils.matches( id, idToCompare )) return _cells[i][j];
                }
            }
        }
        return null;
    }


    /**
     * Removes all rows and all columns from this table which have no visible text in them.
     **/
    public void purgeEmptyCells() {
        int numRowsWithText = 0;
        int numColumnsWithText = 0;
        boolean rowHasText[] = new boolean[ getRowCount() ];
        boolean columnHasText[] = new boolean[ getColumnCount() ];
        Hashtable spanningCells = new Hashtable();


        // look for rows and columns with any text in a non-spanning cell
        for (int i = 0; i < rowHasText.length; i++) {
            for (int j = 0; j < columnHasText.length; j++) {
                if (getCellAsText(i,j).trim().length() == 0) continue;
                if (getTableCell(i,j).getColSpan() == 1 && getTableCell(i,j).getRowSpan() == 1) {
                    if (!rowHasText[i]) numRowsWithText++;
                    if (!columnHasText[j]) numColumnsWithText++;
                    rowHasText[i] = columnHasText[j] = true;
                } else if (!spanningCells.containsKey( getTableCell(i,j) )) {
                    spanningCells.put( getTableCell(i,j), new int[] { i, j } );
                }
            }
        }

        // look for requirements to keep spanning cells: special processing is needed if either:
        // none of its rows already have text, or none of its columns already have text.
        for (Enumeration e = spanningCells.keys(); e.hasMoreElements();) {
            TableCell cell = (TableCell) e.nextElement();
            int coords[]   = (int[]) spanningCells.get( cell );
            boolean neededInRow = true;
            boolean neededInCol = true;
            for (int i = coords[0]; neededInRow && (i < coords[0] + cell.getRowSpan()); i++) {
                neededInRow = !rowHasText[i];
            }
            for (int j = coords[1]; neededInCol && (j < coords[1] + cell.getColSpan()); j++) {
                neededInCol = !columnHasText[j];
            }
            if (neededInRow) {
                rowHasText[ coords[0] ] = true;
                numRowsWithText++;
            }
            if (neededInCol) {
                columnHasText[ coords[1] ] = true;
                numColumnsWithText++;
            }
        }

        TableCell[][] remainingCells = new TableCell[ numRowsWithText ][ numColumnsWithText ];

        int targetRow = 0;
        for (int i = 0; i < rowHasText.length; i++) {
            if (!rowHasText[i]) continue;
            int targetColumn = 0;
            for (int j = 0; j < columnHasText.length; j++) {
                if (!columnHasText[j]) continue;
                remainingCells[ targetRow ][ targetColumn++ ] = _cells[i][j];
            }
            targetRow++;
        }

        _cells = remainingCells;

    }


    /**
     * Returns a rendering of this table with all cells converted to text.
     **/
    public String[][] asText() {
        String[][] result = new String[ getRowCount() ][ getColumnCount() ];

        for (int i = 0; i < result.length; i++) {
            for (int j= 0; j < result[0].length; j++) {
                result[i][j] = getCellAsText( i, j );
            }
        }
        return result;
    }


    /**
     * Returns the summary attribute associated with this table.
     **/
    public String getSummary() {
        return NodeUtils.getNodeAttribute( _dom, "summary" );
    }


    /**
     * Returns the unique ID attribute associated with this table.
     **/
    public String getID() {
        return NodeUtils.getNodeAttribute( _dom, "id" );
    }


    public String toString() {
        String eol = System.getProperty( "line.separator" );
        if (_cells == null) readTable();
        StringBuffer sb = new StringBuffer( HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE).append("WebTable:" ).append( eol );
        for (int i = 0; i < _cells.length; i++) {
            sb.append( "[" ).append( i ).append( "]: " );
            for (int j = 0; j < _cells[i].length; j++) {
                sb.append( "  [" ).append( j ).append( "]=" );
                if (_cells[i][j] == null) {
                    sb.append( "null" );
                } else {
                    sb.append( _cells[i][j].asText() );
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
    static WebTable[] getTables( WebResponse response, Node domRoot, URL baseURL, String parentTarget, String characterSet ) {
        NodeList nl = NodeUtils.getElementsByTagName( domRoot, "table" );
        Vector topLevelTables = new Vector();

        for (int i = 0; i < nl.getLength(); i++) {
            if (isTopLevelTable( nl.item(i), domRoot )) {
                topLevelTables.addElement( new WebTable( response, nl.item(i), baseURL, parentTarget, characterSet ) );
            }
        }

        WebTable[] result = new WebTable[ topLevelTables.size() ];
        topLevelTables.copyInto( result );
        return result;
    }



//----------------------------------- private members -----------------------------------

    private Element     _dom;
    private URL         _url;
    private String      _parentTarget;
    private String      _characterSet;
    private WebResponse _response;


    private TableCell[][] _cells;


    private WebTable( WebResponse response, Node domTreeRoot, URL sourceURL, String parentTarget, String characterSet ) {
        _response     = response;
        _dom          = (Element) domTreeRoot;
        _url          = sourceURL;
        _parentTarget = parentTarget;
        _characterSet = characterSet;
    }



    private void readTable() {
        TableRow[] rows = getRows();
        int[] columnsRequired = new int[ rows.length ];

        for (int i = 0; i < rows.length; i++) {
            TableCell[] cells = rows[i].getCells();
            for (int j = 0; j < cells.length; j++) {
                int spannedRows = Math.min( columnsRequired.length-i, cells[j].getRowSpan() );
                for (int k = 0; k < spannedRows; k++) {
                    columnsRequired[ i+k ]+= cells[j].getColSpan();
                }
            }
        }
        int numColumns = 0;
        for (int i = 0; i < columnsRequired.length; i++) {
            numColumns = Math.max( numColumns, columnsRequired[i] );
        }

        _cells = new TableCell[ columnsRequired.length ][ numColumns ];

        for (int i = 0; i < rows.length; i++) {
            TableCell[] cells = rows[i].getCells();
            for (int j = 0; j < cells.length; j++) {
                int spannedRows = Math.min( columnsRequired.length-i, cells[j].getRowSpan() );
                for (int k = 0; k < spannedRows; k++) {
                    for (int l = 0; l < cells[j].getColSpan(); l++) {
                       placeCell( i+k, j+l, cells[j] );
                    }
                }
             }
         }
    }



    private void placeCell( int row, int column, TableCell cell ) {
        while (_cells[ row ][ column ] != null) column++;
        _cells[ row ][ column ] = cell;
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

        processChildren( _dom, "table", new ElementHandler() {
            public void handleElement( Element element ) {
                rows.addElement( new TableRow( element ) );
            }
            public boolean shouldHandleElement( String tagName ) {
                return tagName.equalsIgnoreCase( "tr" );
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
            final Vector cells = new Vector();

            processChildren( _element, "table", new ElementHandler() {
                public void handleElement( Element element ) {
                    cells.addElement( new TableCell( _response, element, _url, _parentTarget, _characterSet ) );
                }
                public boolean shouldHandleElement( String tagName ) {
                    return tagName.equalsIgnoreCase( "th" ) || tagName.equalsIgnoreCase( "td" );
                }
            } );

            TableCell[] result = new TableCell[ cells.size() ];
            cells.copyInto( result );
            return result;
        }


        private Element _element;
        TableRow( Element rowNode ) {
            _element = rowNode;
        }
    }


    interface ElementHandler {
        public void handleElement( Element element );
        public boolean shouldHandleElement( String tagName );
    }


    static void processChildren( Element root, String avoidingParentTag, ElementHandler handler ) {

        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element) {
                Element element = (Element) child;
                String name = child.getNodeName();
                if (handler.shouldHandleElement( name )) handler.handleElement( element );
                if (!name.equalsIgnoreCase( avoidingParentTag )) processChildren( element, avoidingParentTag, handler );
            }
        }
    }

    static {
        MATCH_FIRST_NONBLANK_CELL = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                WebTable table = ((WebTable) htmlElement);
                table.purgeEmptyCells();
                return table.getRowCount() > 0 &&
                       HttpUnitUtils.matches( table.getCellAsText(0,0), (String) criteria );
            };
        };


        MATCH_FIRST_NONBLANK_CELL_PREFIX = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                WebTable table = ((WebTable) htmlElement);
                table.purgeEmptyCells();
                return table.getRowCount() > 0 &&
                       HttpUnitUtils.hasPrefix( table.getCellAsText(0,0).toUpperCase(), (String) criteria );
            };
        };


        MATCH_ID = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.matches( ((WebTable) htmlElement).getID(), (String) criteria );
            };
        };


        MATCH_SUMMARY = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object htmlElement, Object criteria ) {
                return HttpUnitUtils.matches( ((WebTable) htmlElement).getSummary(), (String) criteria );
            };
        };

    }

}



