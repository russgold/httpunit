import com.meterware.httpunit.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.xml.sax.SAXException;



/**
 * an example that shows how to automate visits you tube
 * please see
 * http://www.youtube.com/t/terms
 * for the legal aspects of modifying this software to allow downloads
 * 
 * @author wf
 * 
 */
public class YouTubeVisitor extends JPanel implements ActionListener {

	// where all visited videos should go to
	protected File targetFolder;

	// UI elements for targetFolder selection
	protected JLabel targetFolderLabel;

	protected JTextField targetFolderField;

	protected JButton FolderSelectButton;
	
	// UI elements for format selection
	String[] formats = {"mp4","flv (Low Quality)","flv (High Quality (320p))","flv (High Quality (480p)",
			"3gp","mp3","mpeg","mov"};
	protected JLabel formatLabel;
	protected JComboBox formatComboBox = new JComboBox(formats);
	protected String videoFormat;

	/**
	 * @return the videoFormat
	 */
	public String getVideoFormat() {
		videoFormat =formatComboBox.getSelectedItem().toString();
		return videoFormat;
	}

	// UI elements for selection of videos to visit
	protected JLabel urlLabel;

	protected JTextArea urlArea;

	protected JButton visitButton;

	// progressbar for visits
	protected JProgressBar progressBar;

	// status message
	protected JLabel statusMessage;
	protected JCheckBox displayCheckbox;
	private boolean withDisplay;

	/**
	 * @return the withDisplay
	 */
	public boolean isWithDisplay() {
		return withDisplay;
	}

	/**
	 * @param withDisplay the withDisplay to set
	 */
	public void setWithDisplay(boolean withDisplay) {
		this.withDisplay = withDisplay;
		this.displayCheckbox.setSelected(withDisplay);
	}
	final static String helpMessage = "Select Target Folder and cut&paste Video URLs then click 'visit'";

	// Create a file chooser
	protected JFileChooser fileChooser;

	final static String chooserTitle = "Select Target Folder";

	// Layout constraints
	GridBagConstraints gbc = new GridBagConstraints();

	/**
	 * place the given component add the given x, y position with the given weight
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param wx
	 * @param wy
	 */
	public void place(Component c, int x, int y, double wx, double wy) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.weightx = wx;
		gbc.weighty = wy;
		add(c, gbc);
	}

	/**
	 * post a status message;
	 * 
	 * @param msg
	 */
	public void setStatus(String msg) {
		statusMessage.setText(msg);
	}

	/**
	 * set default values for fields
	 */
	public void initFields() {
		// use current directory as default target folder
		this.setTargetFolder(new File("."), true);
		// guitar rock 
		// seven Videos of Jean Michel Jarre's millenium concert in Egypt at the
		// pyramids
		String[] urls = { 
				"http://www.youtube.com/watch?v=QjA5faZF1A8", // guitar
				"http://www.youtube.com/watch?v=7jCotM1m8yE",
				"http://www.youtube.com/watch?v=1GWPOjtSFk0",
				"http://www.youtube.com/watch?v=GlUcctk4pQA",
				"http://www.youtube.com/watch?v=P7eFIzgjveU",
				"http://www.youtube.com/watch?v=CXIjLwhkG4g",
				"http://www.youtube.com/watch?v=ekCotwVbzcI",
				"http://www.youtube.com/watch?v=AQE7IKcQJTs" };
		String urlString = "";
		for (int i = 0; i < urls.length; i++) {
			urlString += urls[i] + "\n";
		}
		this.urlArea.setText(urlString);
		// default for display in Browser is true
		this.setWithDisplay(true);
	}

	/**
	 * constructor
	 */
	public YouTubeVisitor() {
		super(new GridBagLayout());

		// Target Folder Selection
		targetFolderLabel = new JLabel("Target Folder:");
		targetFolderField = new JTextField(20);
		targetFolderField.addActionListener(this);
		FolderSelectButton = new JButton("...");
		FolderSelectButton.addActionListener(this);
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(chooserTitle);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
		// disable the "All files" option.
		//
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		// formatSelection
		formatLabel=new JLabel("Format:");

		// Video URL Selection
		urlLabel = new JLabel("Videos-URLs:");

		urlArea = new JTextArea(10, 40);
		urlArea.setEditable(true);
		JScrollPane urlScrollPane = new JScrollPane(urlArea);

		visitButton = new JButton("visit");
		visitButton.addActionListener(this);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		statusMessage = new JLabel(helpMessage);
		displayCheckbox=new JCheckBox("Browser");

		// Add Components to this panel.
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.fill = GridBagConstraints.BOTH;
		place(targetFolderLabel , 0, 0, 0.2, 0.10);
		place(targetFolderField , 1, 0, 0.6, 0.10);
		place(FolderSelectButton, 2, 0, 0.2, 0.10);
		
		place(formatLabel         , 0, 1, 0.2, 0.10);
		place(formatComboBox      , 1, 1, 0.2, 0.10);
		
		place(urlLabel            , 0, 2, 0.2, 0.60);
		place(urlScrollPane       , 1, 2, 0.6, 0.60);
		place(visitButton      , 2, 2, 0.2, 0.60);

		gbc.gridwidth = 3;
		place(progressBar         , 0, 3, 1.0, 0.10);

		gbc.gridwidth=2;
		place(statusMessage       , 0, 4, 0.8, 0.10);
		gbc.gridwidth=1;
		place(displayCheckbox     , 2, 4, 0.2, 0.10);
		initFields();
	}

	/**
	 * set a new target Folder
	 * 
	 * @param targetFolder
	 *          the targetFolder to set
	 * @param withField
	 *          - shall the GUI display also be set?
	 */
	public void setTargetFolder(File newTargetFolder, boolean withField) {
		this.targetFolder = newTargetFolder;
		if (!targetFolder.exists())
			this.setStatus("target Folder " + newTargetFolder + " does not exist!");
		if (withField)
			try {
				targetFolderField.setText(targetFolder.getCanonicalPath());
			} catch (IOException e) {
				setStatus(e.getMessage());
			}
	}

	/**
	 * start visiting the videos asked for
	 */
	public void visitAll() {
		String text = urlArea.getText();
		int totalLines = urlArea.getLineCount();
		int count=0;
		String urls[] = new String[totalLines];
		try {
			for (int i = 0; i < totalLines; i++) {
				int start;
				start = urlArea.getLineStartOffset(i);
				int end = urlArea.getLineEndOffset(i);
				String line = text.substring(start, end);
				if (!line.trim().equals(""))
						urls[count++] = line;
			}
		} catch (BadLocationException e) {
			setStatus(e.getMessage());
		}
		String msg="visiting "+count+" Videos";
		this.progressBar.setMaximum(count);
		setStatus(msg);
		for (int i=0;i<count;i++){
			this.progressBar.setValue(i+1);
			visit(urls[i]);
		}
	}
	private WebConversation conversation = new WebConversation();
	private WebResponse response;
	
	/**
	 * visit the youtube Video from the given URL to the targetFolder
	 * @param videourl
	 */
	private void visit(String videourl) {
		// website service to use
		// String url="http://www.visitfreeyoutube.com/";
		// String url="http://www.videovisitx.com/";
		// String url="http://www.share-tube.eu/";
		// String url="http://www.videogetting.com/download-youtube.php";
		// disable javascript
		HttpUnitOptions.setScriptingEnabled(false);
		System.out.println("visiting " + videourl);
		WebRequest request = new GetMethodWebRequest(videourl);
		try {
			response = conversation.getResponse(request);
			//if (this.isWithDisplay())
			//	BrowserDisplayer.showResponseInBrowser(response);
			//WebForm lookupForm = response.getFormWithID("frmSearch");
			//WebForm lookupForm=response.getForms()[0];
			//lookupForm.setParameter("q",videourl);
			//response=lookupForm.submit();
			if (this.isWithDisplay())
				BrowserDisplayer.showResponseInBrowser(response);
			
			WebLink[] links = response.getLinks();
			ArrayList result = new ArrayList();
			for (int i = 0; i < links.length; i++) {
				WebLink link = links[i];
				String linkText=link.getText();
				if (linkText.indexOf("videoplayback")>0) {
					System.out.println(linkText+"=>"+link.getURLString());
					download(link);
				}
			}	
		} catch (Throwable th) {
			setStatus(th.getMessage());
		}
	}

	/**
	 * download a file from the given web link
	 * @param link - the link to get the file from
	 */
	private void download(WebLink link) {
		try {
			setStatus("visiting "+link.getText());
			response=link.click();
			byte[] visit = response.getBytes();
			String targetFileName=targetFolder.getCanonicalPath()+"/"+link.getText();
			File targetFile=new File(targetFileName);
			if (targetFile.exists())
				setStatus(targetFileName+" exists");
			else {
				setStatus("visiting "+link.getText()+" ("+visit.length/1024/1024+" MByte");
				FileOutputStream fos=new FileOutputStream(targetFile);
				fos.write(visit);
				fos.close();
			}	
		} catch (Throwable th) {
			setStatus(th.getMessage());
		}
	}

	/**
	 * react on clicks
	 */
	public void actionPerformed(ActionEvent evt) {
		// Folder Selection
		if (evt.getSource().equals(FolderSelectButton)) {
			int returnVal = fileChooser.showOpenDialog(YouTubeVisitor.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				setTargetFolder(fileChooser.getSelectedFile(), true);
			}
		} // visit action
		else if (evt.getSource().equals(this.visitButton)) {
			visitAll();
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("YouTubevisit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add contents to the window.
		frame.add(new YouTubeVisitor());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * main routine to startup the GUI
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
