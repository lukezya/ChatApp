import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class clientHandler extends Thread {
	private tcpServer chatServer = null;
	private Socket uniqSocket = null;
	//username and id to distinguish between different client handlers
	private int ID = -1;
	private String username = "";
	//boolean for stopping thread - while loop terminator
	private volatile boolean bExit = false;
	private DataInputStream inStream = null;
	private PrintStream outStream = null;
	//reading file in from client
	private byte[] fileBuffer = null;
	
	public clientHandler(tcpServer chatServer, Socket uniqSocket) {
		//initialize fields
		super();
		this.chatServer = chatServer;
		this.uniqSocket = uniqSocket;
		ID = uniqSocket.getPort();
	}
	
	public void send(String msg) {
		//sends message to client
		outStream.println(msg);
		outStream.flush();
	}
	
	public void sendInt(int i) {
		//sends int to client
		outStream.print(i);
		//outStream.write(i);
		outStream.flush();
	}
	//getters
	public int getID() {
		return ID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public byte[] getFile() {
		return fileBuffer;
	}
	//sends file to client
	public void sendFile(byte[] file) {
		outStream.write(file, 0, file.length);
		outStream.flush();
	}
	//sets name for client handler
	public void setUsername(String username) {
		this.username=username;
	}
	
	public void open() throws IOException {
		//open input and output streams to get input from client and writing to client
		//inStream = new BufferedReader(new InputStreamReader(uniqSocket.getInputStream()));
		inStream = new DataInputStream(uniqSocket.getInputStream());
		outStream = new PrintStream(uniqSocket.getOutputStream());
		//outStream.println(uniqSocket.getPort() + " port was assigned to this client.");
	}
	
	public void close() throws IOException {
		//destructor of thread
		bStop();
		if (inStream != null)  
			inStream.close();
		if (outStream != null) 
			outStream.close();
		if (uniqSocket != null)    
			uniqSocket.close();
	}
	
	public void receiveFile(int size, String filename, int count) {
		//receives file and then adds to the array of files on the server
		fileBuffer = new byte[size];
		try {
			//System.out.println(is.read());
			int bytesRead = inStream.read(fileBuffer, 0, fileBuffer.length);
			int current = bytesRead;
			
			do {
				bytesRead = inStream.read(fileBuffer, current, (fileBuffer.length-current));
				if(bytesRead >= 0) current += bytesRead;
				log("Progress: "+current+"/"+fileBuffer.length);
				if (current == fileBuffer.length)
					break;
			} while((bytesRead > -1));
			Files f = new Files(filename, Integer.toString(size), fileBuffer, count);
			chatServer.addFile(f);
			log("File with "+current+" bytes received and stored on server.");
			
			//got bytes array fileBuffer
		} catch (IOException e) {
			log("Error receiving file: "+e.getMessage());
		}		
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		//keeps on listening for user input
		log("Server Thread with Socket " + ID + " running.");
		while (!bExit) {
			try {
				chatServer.handleClientInput(ID, inStream.readLine()); //method used as specified in notes of assignment 2
			} catch(IOException e) {
				log(ID+" Error reading from client: "+e.getMessage());
				chatServer.removeID(ID);
			} catch (NullPointerException e) {
				log(ID+" Error reading from client: "+e.getMessage());
				chatServer.removeID(ID);
			}
		}
	}
	
	public void bStop() {
		//stopping while loop of thread
		bExit=true;
	}
	
	public void log(String msg) {
		//prints message out to console
		System.out.println(msg);
	}
}
