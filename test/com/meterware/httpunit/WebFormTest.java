package com.meterware.httpunit;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A test of the web form functionality.
 **/
public class WebFormTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( WebFormTest.class );
    }


    public WebFormTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "OneForm", "<h2>Login required</h2>" +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<input type=\"checkbox\" name=second checked>Enabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
    }
	
	
    public void testFindNoForm() throws Exception {
        defineWebPage( "NoForms", "This has no forms but it does" +
                                  "have <a href=\"/other.html\">an active link</A>" +
                                  " and <a name=here>an anchor</a>" );

        WebForm[] forms = _wc.getResponse( getHostPath() + "/NoForms.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 0, forms.length );
    }


    public void testFindOneForm() throws Exception {
        WebForm[] forms = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 1, forms.length );
    }


    public void testFindFormByName() throws Exception {
        defineWebPage( "Default", "<form name=oneForm method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
                                     
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNull( "Found nonexistent form", page.getFormWithName( "nobody" ) );
        assertNotNull( "Did not find named form", page.getFormWithName( "oneform" ) );
    }


    public void testFormParameters() throws Exception {
        WebForm form = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms()[0];
        String[] parameters = form.getParameterNames();
        assertNotNull( parameters );
        assertEquals( 3, parameters.length );
        assertEquals( "name", parameters[0] );

        assertEquals( "First checkbox",  "",   form.getParameterValue( "first" ) );
        assertEquals( "Second checkbox", "on", form.getParameterValue( "second" ) );
    }


    public void testFormRequest() throws Exception {
        WebForm form = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "master" );
        assert( "Should be a post request", !(request instanceof GetMethodWebRequest) );
        assertEquals( getHostPath() + "/servlet/Login", request.getURL().toExternalForm() );
    }


    public void testHiddenParameters() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
                                     
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "surprise", request.getParameter( "secret" ) );
        assertEquals( "nothing", request.getParameter( "typeless" ) );
    }


    public void testNullTextValues() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=>" +
                                  "<br><Input name=typeless value=>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
                                     
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "", request.getParameter( "secret" ) );
        assertEquals( "", request.getParameter( "typeless" ) );
    }


    public void testTableForm() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<table summary=\"\"><tr><td>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "</td><td><Input type=Radio name=sex value=male>Masculine" +
                                  "</td><td><Input type=Radio name=sex value=female checked>Feminine" +
                                  "</td><td><Input type=Radio name=sex value=neuter>Neither" +
                                  "<Input type=submit value = \"Log in\"></tr></table>" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "First parameter name", "name", parameterNames[0] );
        assertEquals( "Default name", "", form.getParameterValue( "name" ) );
        assertEquals( "Default sex", "female", form.getParameterValue( "sex" ) );
        WebRequest request = form.getRequest();
    }


    public void testSelect() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Select name=color><Option>blue<Option selected>red \n" +
                                  "<Option>green</select>" +
                                  "<TextArea name=\"text\">Sample text</TextArea>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "Default color", "red", form.getParameterValue( "color" ) );
        assertEquals( "Default text",  "Sample text", form.getParameterValue( "text" ) );
        WebRequest request = form.getRequest();
        assertEquals( "Submitted color", "red", request.getParameter( "color" ) );
        assertEquals( "Submitted text",  "Sample text", request.getParameter( "text" ) );
    }


    public void testMultiSelect() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select multiple size=4 name=colors>" +
                                  "<Option>blue<Option selected>red \n" +
                                  "<Option>green<Option value=\"pink\" selected>salmon</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "num parameters", 1, parameterNames.length );
        assertEquals( "parameter name", "colors", parameterNames[0] );
        assertMatchingSet( "Select defaults", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
        assertMatchingSet( "Select options", new String[] { "blue", "red", "green", "salmon" }, form.getOptions( "colors" ) );
        assertMatchingSet( "Select values", new String[] { "blue", "red", "green", "pink" }, form.getOptionValues( "colors" ) );
        WebRequest request = form.getRequest();
        assertMatchingSet( "Request defaults", new String[] { "red", "pink" }, request.getParameterValues( "colors" ) );
        assertEquals( "URL", getHostPath() + "/ask?colors=red&colors=pink", request.getURL().toExternalForm() );
    }

                              
    public void testUnspecifiedDefaults() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select name=colors><Option>blue<Option>red</Select>" +
                                  "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                  "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "inferred color default", "blue", form.getParameterValue( "colors" ) );
        assertEquals( "inferred fish default", "red", form.getParameterValue( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], form.getParameterValues( "media" ) );

        WebRequest request = form.getRequest();
        assertEquals( "inferred color request", "blue", request.getParameter( "colors" ) );
        assertEquals( "inferred fish request",  "red", request.getParameter( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], request.getParameterValues( "media" ) );
    }


    public void testCheckboxDefaults() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=checkbox name=ready value=yes checked>" +
                                  "<Input type=checkbox name=color value=red checked>" +
                                  "<Input type=checkbox name=color value=blue checked>" +
                                  "<Input type=checkbox name=gender value=male checked>" +
                                  "<Input type=checkbox name=gender value=female>" +
                                  "<Input type=submit></form>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNotNull( response.getForms() );
        assertEquals( "Num forms in page", 1, response.getForms().length );
        WebForm form = response.getForms()[0];
        assertEquals( "ready state", "yes", form.getParameterValue( "ready" ) );
        assertMatchingSet( "default genders allowed", new String[] { "male" }, form.getParameterValues( "gender" ) );
        assertMatchingSet( "default colors", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );
    }


    private WebConversation _wc;
}
