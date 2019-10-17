import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

//handles transferring file to server
public class SendThread implements Runnable{
	
	private FileInputStream fis = null;
	private BufferedInputStream bis = null;
	private Socket clientSendSocket = null;
	private PrintStream ps = null;
	private DataInputStream dis = null;
	
	private String to;
	private String path;
	private String username;
	
	public SendThread(String to, String path, String username) {
		//keeping track of who the file is for
		this.to = to;
		this.path = path;
		this.username = username;
	}
	
	@Override
	public void run() {
		//make a byte array to send
		File selectedFile = new File(path);
		byte[] fileByteArray = new byte[(int)selectedFile.length()];
		//read file into byte array
		try {
			fis = new FileInputStream(selectedFile);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			dis.readFully(fileByteArray,0,fileByteArray.length);
		} catch (FileNotFoundException e) {
			System.out.println("Selected file error: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("Error reading file into byte array: "+e.getMessage());
		}
		
		try {
			//connect to server to send byte array
			clientSendSocket = new Socket(LoginScreen.SERVERNAME, tcpServer.PORTNUMBER);
			ps = new PrintStream(clientSendSocket.getOutputStream());
			System.out.println("Sending file to server ...");
			//first send message according to protocol
			String header = "MESSAGE TYPE: Data|"+
							"ACTION: Send-File|"+
							"TO: "+to+"|"+
							"FROM: "+username+"|";
			String body = "FILENAME: " + selectedFile.getName() + "|SIZE: " + selectedFile.length()+"|";
			String sendFileMsg = header + body;
			ps.println(sendFileMsg);
			//after telling server it is sending a file, sends the byte array
			ps.write(fileByteArray,0,fileByteArray.length);
	        ps.flush();
	        System.out.println("Done sending file to server.");
		} catch (ConnectException e) {
			System.out.println("Connection refused. You need to initiate a server first.");
		} catch (UnknownHostException unknownHost){
			System.out.println("You are trying to connect to an unknown host!");
		} catch (IOException e){
			System.out.println("Error connecting to the server: "+e.getMessage());
		} finally {
			//close connection after file is sent to the server
			try {
				if (fis!=null)
					fis.close();
				if (bis!=null)
					bis.close();
				if (dis!=null)
					dis.close();
				if (ps!=null)
					ps.close();
				if (clientSendSocket!=null)
					clientSendSocket.close();
			} catch (IOException e) {
				System.out.println("Error closing send thread streams of client: ");
				e.printStackTrace();
			}
		}
		
	}

}
