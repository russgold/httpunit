import com.meterware.httpunit.*;

import java.io.*;
import java.util.ArrayList;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * This class demonstrates using httpunit to use the functionality of a web set
 * from a command line. To use it, specify a single word to be searched. The
 * program will use the Wiktionary web site to find all links which match the
 * pattern.
 * 
 * Note: this program is not robust, but should work if used properly.
 **/
public class NearWords {

	/***
	 * start a search for the given word
	 * 
	 * @param params
	 */
	public static void main(String[] params) {
		try {
			if (params.length < 1) {
				System.out.println("Usage: java NearWords [pattern]");
				System.out
						.println("will demonstrate usage with the pattern 'test' now ...");
				String[] defaultParams = { "test" };
				params = defaultParams;
			}
			WordSeeker seeker = new WordSeeker();

			String[] words = seeker.getWordsMatching(params[0]);
			for (int i = 0; i < words.length; i++) {
				System.out.println((i + 1) + ". " + words[i]);
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}

}

/**
 * subclass to seek words from the Wiktionary as of 2010-01-10
 * 
 */
class WordSeeker {
	String url = "http://simple.wiktionary.org/";
	
	/**
	 * create a word seeker that visits wiktionary.org
	 */
	public WordSeeker() {
		try {
			HttpUnitOptions.setScriptingEnabled(false);
			System.out.println("visiting " + url);
			WebRequest request = new GetMethodWebRequest(url);
			response = conversation.getResponse(request);
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving form: " + e);
		}
	}

	/**
	 * get word matching the given pattern
	 * 
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public String[] getWordsMatching(String pattern) throws Exception {
		System.out.println("getting Words matching '" + pattern + "'");
		WebForm lookupForm = response.getFormWithID("searchform");
		SubmitButton fulltextButton = lookupForm.getSubmitButton("button");
		if (fulltextButton==null){
			System.err.println("Could not find the fulltext submit button on webpage "+url);
			System.err.println("You might want to fix the source code and submit a patch to the httpunit Developer team");
			String [] result={"No result"};
			return result;
		}
		System.out.println("Clicking " + fulltextButton.getName());
		WebRequest request = lookupForm.getRequest(fulltextButton);
		request.setParameter("search", pattern);
		response = lookupForm.submit(fulltextButton);
		// alternative: goButton.click();
		return getOptionsFromResponse(pattern);
	}

	private WebConversation conversation = new WebConversation();

	private WebResponse response;

	/**
	 * 
	 * @param pattern 
	 * @return
	 * @throws Exception
	 */
	private String[] getOptionsFromResponse(String pattern) throws Exception {
		String[] words;
		BrowserDisplayer.showResponseInBrowser(response);
		WebLink[] links = response.getLinks();
		ArrayList result = new ArrayList();
		// start from the "Advanced" link section
		boolean start = false;
		for (int i = 0; i < links.length; i++) {
			WebLink link = links[i];
			String linkText=link.getText();
			//if (linkText.startsWith("Advanced"))
			//	start = true;
			//else if (link.getText().startsWith("next 20"))
			//	break;
			//else if (start && !link.getText().startsWith("(more"))
			if (linkText.indexOf(pattern)>=0)
				result.add(link);
		}
		words = new String[result.size()];
		for (int i = 0; i < result.size(); i++) {
			WebLink link = (WebLink) result.get(i);
			words[i] = link.getText() + " => " + link.getURLString();
		}
		return words;
	}
}
