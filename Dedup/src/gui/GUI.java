package gui;


import java.awt.Dimension;


import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;


import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;


import filelocker.FileLocker;


public class GUI {


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
		frame.setBounds(100, 100, 452, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);


		JLabel lblTitle = new JLabel("DeDuplicator");
		lblTitle.setFont(new Font("STZhongsong", Font.BOLD, 16));
		lblTitle.setBounds(162, 11, 179, 29);
		frame.getContentPane().add(lblTitle);




		final DefaultListModel listmodel = new DefaultListModel(); 
//		listmodel.addElement("default");
		final JList filelist = new JList(listmodel);
		//filelist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//filelist.setSelectedIndex(0);
		//filelist.addListSelectionListener((ListSelectionListener) this);
//		JScrollPane listScrollPane = new JScrollPane(filelist);
		filelist.setModel(new AbstractListModel() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		filelist.setBounds(34, 52, 370, 129);
		frame.getContentPane().add(filelist);






		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(10, 226, 414, 14);
		frame.getContentPane().add(progressBar);


		//JFileChooser filechooser= new JFileChooser();


		JButton btnUploadfile = new JButton("add file...");
		btnUploadfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {




				File file=new File("Desktop");
				JFileChooser filechooser= new JFileChooser(file);
				filechooser.setDialogTitle("choose your file");
				filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				//Below codes for select file
				//FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif");// filter
				//filechooser.setFileFilter(filter);
				filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
				filechooser.showDialog(null, "submit");
				String filename=filechooser.getSelectedFile().getName();
				//String filetype=filechooser.getSelectedFile().
				listmodel.addElement(filename);
			}
		}); 


		btnUploadfile.setBounds(10, 192, 89, 23);
		frame.getContentPane().add(btnUploadfile);


		JButton btnDedup = new JButton("deduplicate");
		btnDedup.setBounds(120, 192, 89, 23);
		frame.getContentPane().add(btnDedup);


		final JButton btnDownload = new JButton("download");
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//code below is to download and create a text file 
				JFileChooser filedl = new JFileChooser();
				filedl.setFileSelectionMode(JFileChooser.SAVE_DIALOG |JFileChooser.DIRECTORIES_ONLY);
				filedl.showDialog(null, "save");
				File file=filedl.getSelectedFile();
				String s=file.getAbsolutePath()+"\\test.txt";
				System.out.println("save:"+s);
				try
				{
					FileWriter out=new FileWriter(s);
					out.write("you got it!");
					out.close();
				}
				catch(Exception m){}


			}
		});
		btnDownload.setBounds(231, 192, 89, 23);
		frame.getContentPane().add(btnDownload);


		final JButton btnDelete = new JButton("delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index= filelist.getSelectedIndex();
				listmodel.remove(index);


				Dimension size =filelist.getSize();
				if(size.height==0){
					btnDelete.setEnabled(false);
					btnDownload.setEnabled(false);
				}else{
					if(index==listmodel.getSize())
						index--;
					filelist.setSelectedIndex(index);
					filelist.ensureIndexIsVisible(index);
				}
			}
		});
		btnDelete.setBounds(335, 192, 89, 23);
		frame.getContentPane().add(btnDelete);


		JLabel lblStatus = new JLabel("New label");
		lblStatus.setBounds(10, 264, 46, 14);
		frame.getContentPane().add(lblStatus);
	}


}
