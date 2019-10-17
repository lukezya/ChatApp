import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class AcceptThread implements Runnable{

	private Socket receiveSocket = null;
	private InputStream is = null;
	private FileOutputStream fos = null;
    private BufferedOutputStream bos = null;
    private PrintStream ps = null;
    
    private String filename;
    private String size;
    private String from;
    private String me;
	
	public AcceptThread(String filename, String size, String from, String me) {
		this.filename = filename;
		this.size = size;
		this.from = from;
		this.me = me;
	}
	
	@Override
	public void run() {
		try {
			receiveSocket = new Socket(LoginScreen.SERVERNAME, tcpServer.PORTNUMBER);
			is = receiveSocket.getInputStream();
			ps = new PrintStream(receiveSocket.getOutputStream());
			//signal server this socket
			ps.println(readyMsg());
			fos = new FileOutputStream(me+filename);
		    bos = new BufferedOutputStream(fos);
		    byte[] rcvBytes = new byte[Integer.parseInt(size)];
		    int current = 0;		    
		    int bytesRead = is.read(rcvBytes, 0 , rcvBytes.length);
		    current = bytesRead;
		    
		    do {
		    	bytesRead = is.read(rcvBytes, current, rcvBytes.length-current);
		    	if (bytesRead>=0)
		    		current += bytesRead;
		    	System.out.println(current + "/"+rcvBytes.length);
		    	if (current == rcvBytes.length) {
		    		break;
		    	}
		    } while (bytesRead > -1);
		    
		    //got byte array
		    bos.write(rcvBytes,0,current);
		    bos.flush();
		    System.out.println("File "+filename+" downloaded: "+current+"bytes read");
		} catch (IOException e) {
			System.out.println("Error opening a socket to receive file transfer: "+e.getMessage());
		} finally {
		
			try {
				//int iTerminate = is.read();
				//if (iTerminate==0) {
					if (is!=null)
						is.close();
					if (fos!=null)
						fos.close();
					if (bos!=null)
						bos.close();
					if (receiveSocket!=null)
						receiveSocket.close();
				//}
			} catch (IOException e) {
				System.out.println("Trouble closing sockets and streams of receiving socket: "+e.getMessage());
			}
		}
	}
	
	public String readyMsg() {
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Ready|"+
						"TO: Server|"+
						"FROM: "+me+"RECEIVE|";
						//"SEQUENCE:"
		String body = "GET: "+from+"SEND|"+
					  "FILENAME: "+filename+"|"+
					  "SIZE: "+size+"|";
		String sendReady = header + body;
		return sendReady;
	}

}
