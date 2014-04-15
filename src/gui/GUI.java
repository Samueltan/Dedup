package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import filelocker.FileLocker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * File locker GUI
 * @author Shuran Huang
 *
 */
public class GUI {

	private FileLocker filelocker;
	private JFrame frame;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				filelocker.closeDB();
			}
		});
		frame.setBounds(100, 100, 640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);


		JLabel lblTitle = new JLabel("DeDuplicator");
		lblTitle.setFont(new Font("STZhongsong", Font.BOLD, 16));
		lblTitle.setBounds(247, 10, 179, 29);
		frame.getContentPane().add(lblTitle);

		JLabel lblStatus = new JLabel("Progress:");
		lblStatus.setBounds(35, 349, 64, 14);
		frame.getContentPane().add(lblStatus);
				
		final JProgressBar usageBar = new JProgressBar();
		usageBar.setBounds(121, 379, 461, 14);
		frame.getContentPane().add(usageBar);
		
		JLabel lblSpaceUsage = new JLabel("Space Usage:");
		lblSpaceUsage.setBounds(35, 379, 72, 14);
		frame.getContentPane().add(lblSpaceUsage);
		
		final JLabel lblUsedSpace = new JLabel("Used space:");
		lblUsedSpace.setBounds(121, 403, 72, 14);
		frame.getContentPane().add(lblUsedSpace);

		final DefaultListModel<String> listmodelLocal = new DefaultListModel<String>(); 
		final DefaultListModel<String> listmodelLocker = new DefaultListModel<String>();
		// Initialize the file list of the file locker
		filelocker = new FileLocker();
		usageBar.setValue(filelocker.getSpacePercentage());
		lblUsedSpace.setText(filelocker.getUsedKB());
		
		List<String> storedFiles = filelocker.getStoredFiles();
		if(storedFiles != null){
			for(String filename: storedFiles)
				listmodelLocker.addElement(filename);
		}
		
		// Local file list box
		final JList<String> filelistLocal = new JList<String>(listmodelLocal);
		filelistLocal.setBounds(32, 75, 253, 211);
		frame.getContentPane().add(filelistLocal);

		// Remote filelocker  list box
		final JList<String> filelistLocker = new JList<String>(listmodelLocker);
		filelistLocker.setBounds(329, 75, 253, 211);
		frame.getContentPane().add(filelistLocker);

		JButton btnAddFile = new JButton("Add Files");
		btnAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file=new File("C:\\");
				File[] selectedFiles;
				JFileChooser filechooser= new JFileChooser(file);
				filechooser.setMultiSelectionEnabled(true);
				filechooser.setDialogTitle("choose your file");
				filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
				filechooser.showDialog(null, "Add");
				selectedFiles = filechooser.getSelectedFiles();
				for(int i=0;i<selectedFiles.length;++i){
					String filename=selectedFiles[i].getAbsolutePath();
					listmodelLocal.addElement(filename);
				}				
			}
		}); 

		btnAddFile.setBounds(43, 305, 99, 23);
		frame.getContentPane().add(btnAddFile);
		
		final JProgressBar progressBar = new JProgressBar(0,100);
		progressBar.setBounds(121, 349, 461, 14);
		progressBar.setStringPainted(true);     
		progressBar.setForeground(Color.blue);   
		frame.getContentPane().add(progressBar);
		
		// Store the file from local into file locker
		JButton btnStore = new JButton("Store");
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressBar.setValue(0);
				List<String> selectFiles = filelistLocal.getSelectedValuesList();
				if(selectFiles.size() == 0){
					JOptionPane.showMessageDialog(null, 
							"Please select a file to proceed!", 
							"Information", 
							JOptionPane.INFORMATION_MESSAGE);					
				}else{
					int result;
					for(String filename: selectFiles){
						filelocker.setGUI(listmodelLocal, listmodelLocker, progressBar, usageBar, lblUsedSpace);
						filelocker.setFilename(filename);
						
						Thread filelockerThread = new Thread(filelocker);
						filelockerThread.start();
					}
				}
			}
		});
		btnStore.setBounds(330, 305, 72, 23);
		frame.getContentPane().add(btnStore);

		// Load file from file server to local
		final JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> selectFiles = filelistLocker.getSelectedValuesList();
				if(selectFiles.size() == 0){
					JOptionPane.showMessageDialog(null, 
							"Please select a file to proceed!", 
							"Information", 
							JOptionPane.INFORMATION_MESSAGE);	
					
					return;
				}
				
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG |JFileChooser.DIRECTORIES_ONLY);
				filechooser.showDialog(null, "Load");
				File file = filechooser.getSelectedFile();
				String filePath;
				if(file != null){
					filePath=file.getAbsolutePath();
				}else{
					return;
				}
				
				try
				{
					int result;
					String filename = filePath + "\\" + filelistLocker.getSelectedValue();
					int confirm = JOptionPane.showConfirmDialog(null, 
							"The file will be loaded as " + filename + ", continue?", 
							"Confirmation", 
							JOptionPane.OK_CANCEL_OPTION);
					if(confirm == JOptionPane.CANCEL_OPTION){
						return;
					}
					
					if((result = filelocker.loadFile(filename)) > 0){
						JOptionPane.showMessageDialog(null, 
								"The file " + filename + " is loaded from file locker successfully!", 
								"Information", 
								JOptionPane.INFORMATION_MESSAGE);	
					}else if(result == FileLocker.ERR_LOCKER_FILENOTEXIST){
						JOptionPane.showMessageDialog(null, 
								"The file " + filename + FileLocker.MSG_LOCKER_FILENOTEXIST, 
								"Information", 
								JOptionPane.INFORMATION_MESSAGE);	
					}
				}
				catch(Exception m){}
			}
		});
		btnLoad.setBounds(418, 305, 72, 23);
		frame.getContentPane().add(btnLoad);

		// Delete the file from file locker
		final JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> selectFiles = filelistLocker.getSelectedValuesList();
				if(selectFiles.size() == 0){
					JOptionPane.showMessageDialog(null, 
							"Please select a file to proceed!", 
							"Information", 
							JOptionPane.INFORMATION_MESSAGE);	
					
					return;
				}
				
				String filename = filelistLocker.getSelectedValue();
				if(filelocker.deleteFile(filename) >0)
					System.out.println("The file " + filename + " is stored to file locker successfully!");
				
				listmodelLocker.removeElement(filename);
				lblUsedSpace.setText(filelocker.getUsedKB());
			}
		});
		btnDelete.setBounds(510, 305, 72, 23);
		frame.getContentPane().add(btnDelete);
		
		JLabel lblLocalFiles = new JLabel("Local files");
		lblLocalFiles.setBounds(112, 51, 81, 14);
		frame.getContentPane().add(lblLocalFiles);
		
		JLabel lblFilesInFile = new JLabel("Files in file locker");
		lblFilesInFile.setBounds(395, 51, 120, 14);
		frame.getContentPane().add(lblFilesInFile);
		
		JButton btnRemoveFile = new JButton("Remove");
		btnRemoveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(filelistLocal.getSelectedValue() == null){
					JOptionPane.showMessageDialog(null, 
							"Please select a file to proceed!", 
							"Information", 
							JOptionPane.INFORMATION_MESSAGE);	
				}else{
					String filename = filelistLocal.getSelectedValue();
					listmodelLocal.removeElement(filename);
				}
			}
		});
		btnRemoveFile.setBounds(160, 305, 99, 23);
		frame.getContentPane().add(btnRemoveFile);
	}
}
