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

import javax.swing.AbstractButton;
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
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import filelocker.FileLocker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Files;

/**
 * File locker GUI
 * @author Shuran Huang
 *
 */
public class GUINet {

	private FileLocker filelocker;
	private JFrame frame;
	static Socket socket;
	static BufferedOutputStream out;
	static BufferedInputStream in;
	String close_CMD="CLOSE\n";
	String status_CMD="STATUS\n";
	String filelist_CMD="FILELIST\n";
	String addfile_CMD="ADD_FILE\n";
	String deletefile_CMD="DELETE_FILE\n";
	String fetchfile_CMD="LOAD_FILE\n";
	
	
	/**
	 * Launch the application.
	 */
	
	private static void create_connection() {
		// TODO Auto-generated method stub
		socket=new Socket();
		SocketAddress sockaddr=new InetSocketAddress("localhost",6000);
		try {
			socket.connect(sockaddr, 200);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out= new BufferedOutputStream(socket.getOutputStream());
			in=new BufferedInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//create_connection();
					GUINet window = new GUINet();
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
	public GUINet() {
		initialize();
	}
	
	private void write(byte[] msg) throws IOException
	{
		out.write(msg);
		out.flush();
	}
	
	private byte[] read() throws IOException
	{	
		byte[] msg=null;
		if(in.available()>0)
		{
			msg=new byte[in.available()];
			in.read(msg);			
		}
		return msg;		
	}
	
	private void read_status(JProgressBar usageBar, JLabel lblUsedSpace, DefaultListModel<String> listmodelLocker)
	{
		create_connection();
		try {
			write(status_CMD.getBytes("UTF-8")); 
			String ans=null;
			int t=0;
			Thread.sleep(400);
			do{
			try{ 
			ans=new String(read(),"UTF-8");
			 System.out.println("Status is"+ans);
			 Thread.sleep(400);
			}catch(Exception ex)
			{
				System.out.println("Chillnbro");
			}
			}while(ans==null&&t++!=10);
		
		String arr[]=ans.split("[\\r\\n]+");
		usageBar.setValue(Integer.parseInt(arr[0]));
		lblUsedSpace.setText(arr[1]);
		listmodelLocker.removeAllElements();
		if (arr.length>2)
		{
			for (int i=2;i<arr.length;i++)
				listmodelLocker.addElement(arr[i]);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeDB();
			}

			private void closeDB() {
				// TODO Auto-generated method stub
				
			}
		});
		frame.setBounds(100, 100, 640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);


		JLabel lblTitle = new JLabel("DeDuplicator (Network)");
		lblTitle.setFont(new Font("STZhongsong", Font.BOLD, 16));
		lblTitle.setBounds(247, 10, 179, 29);
		frame.getContentPane().add(lblTitle);

		JLabel lblStatus = new JLabel("Progress:");
		lblStatus.setBounds(35, 349, 64, 14);
		frame.getContentPane().add(lblStatus);
				
		final JProgressBar usageBar = new JProgressBar();
		usageBar.setBounds(133, 379, 449, 14);
		frame.getContentPane().add(usageBar);
		
		JLabel lblSpaceUsage = new JLabel("Space Usage:");
		lblSpaceUsage.setBounds(35, 379, 107, 14);
		frame.getContentPane().add(lblSpaceUsage);
		
		final JLabel lblUsedSpace = new JLabel("Used space:");
		lblUsedSpace.setBounds(133, 403, 449, 14);
		frame.getContentPane().add(lblUsedSpace);

		final DefaultListModel<String> listmodelLocal = new DefaultListModel<String>(); 
		final DefaultListModel<String> listmodelLocker = new DefaultListModel<String>();
		// Initialize the file list of the file locker
		read_status(usageBar,lblUsedSpace,listmodelLocker);
		// Local file list box
		final JList<String> filelistLocal = new JList<String>(listmodelLocal);
		filelistLocal.setBounds(32, 75, 253, 211);
//		JScrollPane scrollPaneLocal = new JScrollPane(filelistLocal);
//		scrollPaneLocal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		scrollPaneLocal.setPreferredSize(new Dimension(50,100));
		frame.getContentPane().add(filelistLocal);

		// Remote filelocker  list box
		final JList<String> filelistLocker = new JList<String>(listmodelLocker);
		filelistLocker.setBounds(329, 75, 253, 211);
//		JScrollPane scrollPaneLocker = new JScrollPane(filelistLocker);
//		scrollPaneLocker.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(filelistLocker);

		final DefaultListModel<String> lm = new DefaultListModel<String>(); 
		final JList<String> fl = new JList<String>(lm);
		JScrollPane sp = new JScrollPane(fl);
		fl.setBounds(32, 75, 253, 211);
//		scrollPaneLocal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(sp);
		
		ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent actionEvent) {
	           read_status(usageBar, lblUsedSpace, listmodelLocker);
	        }
	    };
	    final Timer timer = new Timer(4000, actionListener);
	    timer.start();
		
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
					file = new File (filename);
					
					// If filename is a file, add directly
					if(file.isFile()){
						listmodelLocal.addElement(filename);
					}else{
						// If filename is a directory, add all the non-directory files under the directory
						File[] filenames = file.listFiles();
						for (File f: filenames){
							if(f.isFile())
								listmodelLocal.addElement(f.getAbsolutePath());
						}
					}
					
				}				
			}
		}); 

		btnAddFile.setBounds(43, 305, 99, 23);
		frame.getContentPane().add(btnAddFile);
		
		final JProgressBar progressBar = new JProgressBar(0,100);
		progressBar.setBounds(133, 349, 449, 14);
		progressBar.setStringPainted(true);     
		progressBar.setForeground(Color.blue);   
		frame.getContentPane().add(progressBar);
		
		// Store the file from local into file locker
		JButton btnStore = new JButton("Store");
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
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
						try {
							create_connection();
							int len=0;
							System.out.println("I am here yo");
							
							String fname = new File(filename).getName();
							ByteArrayOutputStream baos=new ByteArrayOutputStream();
							String name =addfile_CMD+"\n"+fname+"\n";
							baos.write(name.getBytes(), 0,name.getBytes().length);
							//out.write(addfile_CMD.getBytes());
							len+=name.getBytes().length;
							System.out.println("length is "+len);
							FileInputStream file1=new FileInputStream(filename);
							
							System.out.println("File size "+file1.available());
							//byte buffer[]= ByteStreams.to
							byte buff[] =new byte[2000];
							int count=0;
							while ((count = file1.read(buff)) > 0) {
								  baos.write(buff,0,count);
								  len+=count;
							}
							byte buffer[]=baos.toByteArray();
							baos.close();
							//String n=new String(buffer,"UTF-8");
							//System.out.println(n);
							write(buffer);
							read_status(usageBar, lblUsedSpace, listmodelLocker);
					} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
					}
				}
				timer.start();
			}
		});
		btnStore.setBounds(330, 305, 72, 23);
		frame.getContentPane().add(btnStore);

		// Load file from file server to local
		final JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
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
					String send=fetchfile_CMD+"\n"+filename+"\n";
					create_connection();
					write(send.getBytes());
					Thread.sleep(100);
					
					//String s1="";
					
					ByteArrayOutputStream baos=new ByteArrayOutputStream();
					
					//byte buffer[]= ByteStreams.to
					byte buff[] =new byte[2000];
					int count=0;
					while ((count = in.read(buff)) > 0) {
						  baos.write(buff,0,count);
					}
					byte fil[]=baos.toByteArray();
					
					/*while(in.available()>0)
					{
						s1=s1+new String(read(),"UTF-8");
						try {
							Thread.sleep(10);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					byte fil[]=s1.getBytes();*/
					FileOutputStream fstream=new FileOutputStream(filename);
					System.out.println("Filename is "+filename);
					fstream.write(fil);
					fstream.close();
					timer.start();
					
					/*if((result = filelocker.loadFile(filename)) > 0){
						JOptionPane.showMessageDialog(null, 
								"The file " + filename + " is loaded from file locker successfully!", 
								"Information", 
								JOptionPane.INFORMATION_MESSAGE);	
					}else if(result == FileLocker.ERR_LOCKER_FILENOTEXIST){
						JOptionPane.showMessageDialog(null, 
								"The file " + filename + FileLocker.MSG_LOCKER_FILENOTEXIST, 
								"Information", 
								JOptionPane.INFORMATION_MESSAGE);	
					}*/
				}
				catch(Exception m){
					timer.start();
				}
			}
		});
		btnLoad.setBounds(421, 305, 72, 23);
		frame.getContentPane().add(btnLoad);

		// Delete the file from file locker
		final JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				List<String> selectFiles = filelistLocker.getSelectedValuesList();
				if(selectFiles.size() == 0){
					JOptionPane.showMessageDialog(null, 
							"Please select a file to proceed!", 
							"Information", 
							JOptionPane.INFORMATION_MESSAGE);	
					
					return;
				}
				
				String filename = filelistLocker.getSelectedValue();
				String send=deletefile_CMD+"\n"+filename+"\n";
				try {
					create_connection();
					write(send.getBytes());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				read_status(usageBar, lblUsedSpace, listmodelLocker);
			//	if(filelocker.deleteFile(filename) >0)
				//	System.out.println("The file " + filename + " is stored to file locker successfully!");
				
			//	listmodelLocker.removeElement(filename);
			//	lblUsedSpace.setText(filelocker.getUsedKB());
				timer.start();
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
