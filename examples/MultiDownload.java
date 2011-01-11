import com.meterware.httpunit.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.xml.sax.SAXException;

import sun.tools.tree.ThisExpression;

/**
 * an example that shows how to automate downloads
 * 
 * @author wf
 * 
 */
public class MultiDownload extends JPanel implements ActionListener {

	// where all visited videos should go to
	protected File targetFolder;

	// UI elements for targetFolder selection
	protected JLabel targetFolderLabel;

	protected JTextField targetFolderField;

	protected JButton FolderSelectButton;


	// UI elements for selection of videos to visit
	protected JLabel urlLabel;

	protected JTextArea urlArea;

	protected JButton downloadButton;

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
	
	final static String helpMessage = "Select Target Folder and cut&paste File URLs then click 'download'";

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
		// default files to download
		String[] urls = { 
				"http://downloads.sourceforge.net/project/httpunit/httpunit/1.7/httpunit-1.7.zip", // httpunit,
				"http://downloads.sourceforge.net/project/junit/junit/4.8.1/junit-4.8.1.jar"  // JUnit
		 };
		String urlString = "";
		for (int i = 0; i < urls.length; i++) {
			urlString += urls[i] + "\n";
		}
		this.urlArea.setText(urlString);
		// default for display in Browser is false
		this.setWithDisplay(false);
	}
	
  /**
   * When the worker needs to update the GUI we do so by queuing
   * a Runnable for the event dispatching thread with 
   * SwingUtilities.invokeLater().  In this case we're just
   * changing the progress bars value.
   */
  void updateStatus(final int progress, final String msg) {
      Runnable doSetProgressBarValue = new Runnable() {
          public void run() {
          	  setStatus(msg);
              progressBar.setValue(progress);
          }
      };
      SwingUtilities.invokeLater(doSetProgressBarValue);
  }


	/**
	 * constructor
	 */
	public MultiDownload() {
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
		
		// File URL Selection
		urlLabel = new JLabel("File-URLs:");

		urlArea = new JTextArea(10, 40);
		urlArea.setEditable(true);
		JScrollPane urlScrollPane = new JScrollPane(urlArea);

		downloadButton = new JButton("download");
		downloadButton.addActionListener(this);

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

		
		place(urlLabel            , 0, 2, 0.2, 0.60);
		place(urlScrollPane       , 1, 2, 0.6, 0.60);
		place(downloadButton         , 2, 2, 0.2, 0.60);

		gbc.gridwidth = 3;
		place(progressBar         , 0, 3, 1.0, 0.10);

		gbc.gridwidth=2;
		place(statusMessage       , 0, 4, 0.8, 0.10);
		gbc.gridwidth=1;
		place(displayCheckbox     , 2, 4, 0.2, 0.10);
		initFields();
	}

	/**
	 * get the target Folder
	 * @return
	 */
	public File getTargetFolder(){
		return new File(targetFolderField.getText());
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
	 * start downloading the Files asked for
	 */
	public void downloadAll() {
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
						urls[count++] = line.replace("\n","");
			}
		} catch (BadLocationException e) {
			setStatus(e.getMessage());
		}
		String msg="downloading "+count+" Files";
		this.progressBar.setMaximum(count);
		this.updateStatus(0, msg);
		for (int i=0;i<count;i++){
			download(i+1,urls[i]);
		}
	}
	private WebConversation conversation = new WebConversation();
	private WebResponse response;
	
	/**
	 * download a file from the given web link
	 * @param progress 
	 * @param url - the link to get the file from
	 */
	private void download(int progress, String url) {
		try {
			String msg="downloading "+url;
			this.updateStatus(progress, msg);
			response=conversation.getResponse(url);
			byte[] download = response.getBytes();
			int lastSlash=url.lastIndexOf("/");
			String filePart=url.substring(lastSlash);
			String targetFileName=this.getTargetFolder().getCanonicalPath()+"/"+filePart;
			File targetFile=new File(targetFileName);
			if (targetFile.exists())
				setStatus(targetFileName+" exists");
			else {
				setStatus("downloading "+url+" ("+download.length/1024+" KByte)");
				FileOutputStream fos=new FileOutputStream(targetFile);
				fos.write(download);
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
			int returnVal = fileChooser.showOpenDialog(MultiDownload.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				setTargetFolder(fileChooser.getSelectedFile(), true);
			}
		} // download action
		else if (evt.getSource().equals(this.downloadButton)) {
			SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		     downloadAll();
		    }
		  });

		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Multi-Download");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add contents to the window.
		frame.add(new MultiDownload());

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
