import java.io.BufferedReader;
import java.io.IOException;

//thread keeps on waiting for input from server
public class tcpClientThread extends Thread {
	public BufferedReader inStream;
	private tcpClientGUI client;
	private volatile boolean bExit = false;
    
	
	public tcpClientThread(tcpClientGUI client, BufferedReader inStream) {
		//initialize clientGUI and input stream from server
		super();
		this.inStream = inStream;
		this.client = client;
	}
	
	public void run() {  
		while (!bExit) {
			try {
				//get input from server and let clientGUI to handle input
				String input = inStream.readLine();
				client.handleInput(input);
			} catch (IOException e) {
				System.out.println("Error reading input from server: "+e.getMessage());
				close();
			}
	   }
	}
	
	public void close() {
		//destructor of thread objects
		bStop();
		try {
			if (inStream!=null)
				inStream.close();
		} catch (IOException e) {
			System.out.println("Error closing input stream: " + e.getMessage());
		}
	}
	
	public void bStop() {
		//stopping running of thread
		bExit = true;
	}
}
