package gui;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.sun.org.apache.xpath.internal.operations.Bool;

import filelocker.FileLocker;

public class SocketC {
	static BufferedOutputStream out;
	static BufferedInputStream in;
	static Socket clientSocket;
	static FileLocker filelocker;
	static List<String> storedFiles;
	public SocketC() {
		// TODO Auto-generated constructor stub
	}

	
	private static void write(byte[] msg) throws IOException
	{
		out.write(msg);
		out.flush();
	}
	
	private static byte[] read() throws IOException
	{	
		byte[] msg=null;
		if(in.available()>0)
		{
			msg=new byte[in.available()];
			in.read(msg);			
		}
		return msg;		
	}
	
	static String status_msg()
	{
		int space=filelocker.getSpacePercentage();
		String used=filelocker.getUsedKB();
		storedFiles = filelocker.getStoredFiles();
		String s="";
		s=s+space+"\n"+used+"\n";
		try{
		for (String name : storedFiles)
		{
			if (name!=" "||name!=null)
				s=s+name+"\n";
		}
		}catch(Exception ex)
		{
			System.out.println("No files here");
		}
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		ServerSocket serverSocket=null;
		try {
			serverSocket = new ServerSocket(6000);
			serverSocket.setReceiveBufferSize(1048576);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		filelocker =new FileLocker();
		System.out.println(filelocker.getStoredFiles());
		
		
		String s=status_msg();
		System.out.println(s);
		
		boolean msg=false;
		boolean newcon=true;
		String s1=null;
		while(true)
		{
			if(newcon==true)
			{
				try {
					clientSocket = serverSocket.accept();
				
					out= new BufferedOutputStream(clientSocket.getOutputStream());
					in=new BufferedInputStream(clientSocket.getInputStream());
					newcon=false;
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				while(in.available()>0)
				{
					s1=s1+new String(read(),"UTF-8");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					msg=true;
				}
				
				if(msg==true)
				{
					System.out.println(s1.length()+" FIle"+s1.charAt(0)+s1.charAt(1)+s1.charAt(2)+s1.charAt(3)+s1.charAt(4));
					if(s1.contains("STATUS\n"))
					{
						System.out.println("Status recieved");
						s=status_msg();
						write(s.getBytes("UTF-8"));
					}
					else if(s1.contains("ADD_FILE\n"))
					{
						System.out.println("Baam Baam file mil gaya lala");
						
						s1=s1.replaceFirst("ADD_FILE\n","");
					//	System.out.println(s1);
						
						//Here is the error yo..
						long loc=s1.substring(1).indexOf('\n');
						String filename=s1.substring(1,(int) loc+1);
						System.out.println("Filename is "+filename);
						s1=s1.replaceFirst(filename+"\n", "");
//						s1=s1.replaceFirst("\n\r\n","");
						s1=s1.replaceFirst("\n", "");

					//	System.out.println("Fresh file is"+s1);
						byte[] fil=s1.getBytes();
						FileOutputStream fstream=new FileOutputStream("C:\\Downloads\\"+filename);
						filename="C:\\Downloads\\"+filename;
						fstream.write(fil);
						JProgressBar progressBar = new JProgressBar();
						JProgressBar usageBar=new JProgressBar();
						DefaultListModel<String> listmodelLocal=new DefaultListModel<String>();
						JLabel lblUsedSpace = new JLabel();
						DefaultListModel<String> listmodelLocker = new DefaultListModel<String>();
						filelocker.setGUI(listmodelLocal, listmodelLocker, progressBar, usageBar, lblUsedSpace);
						filelocker.setFilename(filename);
						
						Thread filelockerThread = new Thread(filelocker);
						filelockerThread.start();
						System.out.println(listmodelLocker.elements());					
					}
					else if(s1.contains("DELETE_FILE\n")){
						s1=s1.replaceFirst("DELETE_FILE\n","");
						long loc=s1.substring(1).indexOf('\n');
						String filename=s1.substring(1,(int) loc+1);
						System.out.println("Filename is "+filename);
						if(filelocker.deleteFile(filename) >0)
							System.out.println("The file " + filename + " is stored to file locker successfully!");
					}
					
					else if(s1.contains("LOAD_FILE\n")){
						s1=s1.replaceFirst("LOAD_FILE\n","");
						long loc=s1.substring(1).indexOf('\n');
						String filename=s1.substring(1,(int) loc+1);
						System.out.println("Filename is "+filename);
						int result;
						if((result = filelocker.loadFile(filename)) > 0){
							FileInputStream file1=new FileInputStream(filename);
							ByteArrayOutputStream baos=new ByteArrayOutputStream();
							System.out.println("File size "+file1.available());
							//byte buffer[]= ByteStreams.to
							byte buff[] =new byte[2000];
							int count=0;
							while ((count = file1.read(buff)) > 0) {
								  baos.write(buff,0,count);
							}
							byte buffer[]=baos.toByteArray();
							baos.close();
							//String n=new String(buffer,"UTF-8");
							//System.out.println(n);
							write(buffer);
							 	
						}else if(result == FileLocker.ERR_LOCKER_FILENOTEXIST){
							System.out.println("No file in locker");	
						}
					}
					msg=false;
					newcon=true;
					clientSocket.close();in.close();out.close();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					s1="";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
