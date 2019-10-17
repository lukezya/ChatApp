import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class tcpServer {
	//port number server is listening on
	public static final int PORTNUMBER=12061;
	//filename of textfile storing all user credentials
	public static final String FILENAME_USERS="../Users.txt";	
	//array of server side client sockets for each client
	private ArrayList<clientHandler> clients = new ArrayList<clientHandler>();
	//array of users for validating credentials
	private ArrayList<Users> users = new ArrayList<Users>();
	//array of files stored on the server
	private ArrayList<Files> files = new ArrayList<Files>();
	private ServerSocket listener = null;
	//boolean for while loop, to exit
	private volatile boolean bExit;
	//keeping track of number of sockets connected to the server
	private int clientCount = 0;
		
	public tcpServer(int portNo) {
		try {
			//create a server socket listening onto portNo
			log("Chat Server binding to port "+portNo+", please wait ...");
			listener = new ServerSocket(portNo);
			readUsers();
			bExit = false;
			log("Server started: "+listener);
		} catch (BindException e) {
			log("Port is in use: "+e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			log("Cannot bind to port "+portNo+": "+e.getMessage());
		}
	}
	
	public void run() {
		//wihle loop that keepings on listening for client connections
		while (!bExit) {
			try {
				log("Waiting for connection from a client ...");
				addClient(listener.accept());
			} catch(IOException e) {
				log("Server accept error: "+e.getMessage());
				stop();
			}
		}
	}
	
	private boolean isUnique(String n) {
		//checks if a username is unique for new users signing up
		for(int i=0;i<users.size();i++) {
			if (users.get(i).getUsername().equals(n)) {
				return false;
			}
		}
		return true;
	}
	
	private void readUsers() {
		//read users from textfile into array
		File userFile = new File(FILENAME_USERS);
		Scanner sc = null;
		try {
			sc = new Scanner(userFile);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.equals(""))
					continue;
				int iPos = line.indexOf("|");
				String name = line.substring(0,iPos);
				String pass = line.substring(iPos+1);
				//System.out.println(name);
				users.add(new Users(name, pass));
			}
		} catch (FileNotFoundException e) {
			log("Reader users data - file not found: "+e.getMessage());
		} finally {
			sc.close();
		}
		
	}
	
	private void writeUser(Users addUser) {
		//if a new user signs up, their name is added to the textfile
		File userFile = new File(FILENAME_USERS);
		try {
			FileWriter fw = new FileWriter(userFile, true);
			fw.append("\n"+addUser.getUsername()+"|"+addUser.getPassword());
			fw.close();
		} catch (IOException e) {
			log("Error writing to users file: "+e.getMessage());
		}
	}
	
	public void stop() {
		//stop while loop
		bExit=true;
	}
	
	private int getFileIndex(String f, String s) {
		//get specific file index in array
		for(int i=0;i<files.size();i++) {
			if (files.get(i).getFilename().equals(f)&&(files.get(i).getFilesize().equals(s))) {
				return i;
			}
		}
		return -1;
	}
	
	private byte[] findFile(String f, String s) {
		//get file from File object in array of files
		for(int i=0;i<files.size();i++) {
			if (files.get(i).getFilename().equals(f)&&(files.get(i).getFilesize().equals(s))) {
				return files.get(i).getFile();
			}
		}
		return null;
	}
	
	private int findClient(int ID) {
		//gets index for clientHandler in the clients array
		for(int i=0;i<clients.size();i++) {
			if (clients.get(i).getID()==ID) {
				return i;
			}
		}
		return -1;
	}
	
	public String findName(int ID) {
		//find the username of a clientHandler from their ID
		for(int i=0;i<clients.size();i++) {
			if (clients.get(i).getID()==ID) {
				return clients.get(i).getUsername();
			}
		}
		return "NF";
	}
	
	public int findClient(String name) {
		//finds their array index from their username
		for(int i=0;i<clients.size();i++) {
			if (clients.get(i).getUsername().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public int findID(String name) {
		//finds their ID from their username
		for(int i=0;i<clients.size();i++) {
			if (clients.get(i).getUsername().equals(name)) {
				return clients.get(i).getID();
			}
		}
		return -1;
	}
	
	public synchronized void handleClientInput(int ID, String input) {
		//break down header - message type, action, to, from
		int iPos = input.indexOf("MESSAGE TYPE:");
		int iEnd = input.indexOf("|");
		String MT = input.substring(iPos+14,iEnd);
		input = input.substring(iEnd+1);
		
		iPos = input.indexOf("ACTION:");
		iEnd = input.indexOf("|");
		String action = input.substring(iPos+8,iEnd);
		input = input.substring(iEnd+1);
		
		iPos = input.indexOf("TO:");
		iEnd = input.indexOf("|");
		String to = input.substring(iPos+4,iEnd);
		input = input.substring(iEnd+1);
		
		iPos = input.indexOf("FROM:");
		iEnd = input.indexOf("|");
		String from = input.substring(iPos+6,iEnd);
		input = input.substring(iEnd+1);
		
		switch (MT) {
			case "Command":
				switch (action) {
					//message for authenticating client credentials
					case "Authentication": {
						boolean bAuthenticate = false;
						//find body username and pass
						iPos = input.indexOf("USERNAME:");
						iEnd = input.indexOf("|");
						String username = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("PASSWORD:");
						iEnd = input.indexOf("|");
						String password = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						Users check = new Users(username, password);
						for (Users u:users) {
							//if a user is found with the same credentials in the array, their credentials is correct
							if(u.validate(check)) {
								bAuthenticate = true;
								break;
							}
						}
						if (bAuthenticate) {
							//send OK response message
							clients.get(findClient(ID)).setUsername(username);
							String header = "MESSAGE TYPE: Command|"+
									"ACTION: Authentication|"+
									"TO: "+findName(ID)+"|"+
									"FROM: Server"+"|";
									//"SEQUENCE:"
							String body = "RESPONSE: " + "OK"+"|";
							String sendMsg = header + body;
							clients.get(findClient(ID)).send(sendMsg);
						} else {
							//send XD response message
							String header = "MESSAGE TYPE: Command|"+
									"ACTION: Authentication|"+
									"TO: "+findName(ID)+"|"+ 
									"FROM: Server"+"|";
									//"SEQUENCE:"
							String body = "RESPONSE: " + "XD"+"|";
							String sendMsg = header + body;
							clients.get(findClient(ID)).send(sendMsg);
						}
						
						break;
					}
					//case for new user signing up
					case "New-Client": {
						//fix like client already took name, blank name etc
						iPos = input.indexOf("USERNAME:");
						iEnd = input.indexOf("|");
						String username = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						//check username for no duplicates
						if (isUnique(username)) {
							iPos = input.indexOf("PASSWORD:");
							iEnd = input.indexOf("|");
							String password = input.substring(iPos+10,iEnd);
							try {
								Users addUser = new Users(username, password);
								users.add(addUser);
								writeUser(addUser);
								//send OK response message
								String header = "MESSAGE TYPE: Command|"+
										"ACTION: New-Client|"+
										"TO: "+findName(ID)+"|"+
										"FROM: Server"+"|";
								String body = "RESPONSE: " + "OK"+"|";
								String sendMsg = header + body;
								clients.get(findClient(ID)).send(sendMsg);
							} catch (Exception e) {
								//send XD reponse message
								String header = "MESSAGE TYPE: Command|"+
										"ACTION: New-Client|"+
										"TO: "+findName(ID)+"|"+
										"FROM: Server"+"|";
								String body = "RESPONSE: " + "XD"+"|";
								String sendMsg = header + body;
								clients.get(findClient(ID)).send(sendMsg);
							}
						} else {
							String header = "MESSAGE TYPE: Command|"+
									"ACTION: New-Client|"+
									"TO: "+findName(ID)+"|"+
									"FROM: Server"+"|";
							String body = "RESPONSE: " + "Username already taken."+"|";
							String sendMsg = header + body;
							clients.get(findClient(ID)).send(sendMsg);
						}
						break;
					}
					default:
						break;
				}
				
				break;
			//case for sending a file or text
			case "Data":
				switch(action) {
					//case for sending text
					case "Send-Text": {
						//send a protocol message
						//get text
						iPos = input.indexOf("TEXT:");
						iEnd = input.indexOf("|");
						String msg = input.substring(iPos+6,iEnd);
						//process to
						//check whether broadcast
						if (to.equals("BROADCAST")) {
							for (clientHandler client: clients) {
								//send message to all clients but not to self
								if (client.getUsername().equals(findName(ID))) {
									continue;
								}
								client.send(getMessageForClient(from,"All",msg));
							}
						} else {
							//can be one name, multiple names
							String[] tos = to.replaceAll("\\s+", "").split(",");
							for (String recipient:tos) {
								int iClient = findClient(recipient);
								if (iClient<0)
									//if client is offline or does not exist, inform sender
									clients.get(findClient(ID)).send(getFailedToSend(from, recipient,"Could not send message to "+recipient+"."));
								else
									//forward message to correct client
									clients.get(iClient).send(getMessageForClient(from,recipient,msg));
							}
						}
						break;
					}
					//case for sending file to another client
					case "Send-File": {
						//get filename and size
						clients.get(findClient(ID)).setUsername(from);
						iPos = input.indexOf("FILENAME:");
						iEnd = input.indexOf("|");
						String filename = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("SIZE:");
						iEnd = input.indexOf("|");
						String size = input.substring(iPos+6,iEnd);
						
						int iFrom = from.indexOf("SEND");
						String realfrom = from.substring(0, iFrom);
						//read in byte array to server
						//check out tos
						String[] tos = to.replaceAll("\\s+", "").split(",");
						clients.get(findClient(ID)).receiveFile(Integer.parseInt(size), filename, tos.length);
						//find recipients to ask permission
						for (String recipient:tos) {
							int iClient = findClient(recipient);
							//if client does not exist, or is offline
							if (iClient<0) {
								clients.get(findClient(realfrom)).send(getFailedToSend(from, recipient,"Could not send file to "+recipient+"."));
								files.get(getFileIndex(filename, size)).decCount();
							}
							else
								//send messages to ask for permission
								clients.get(iClient).send(getPermissionFromClient(from,recipient,filename, size));
						}
						break;
					}
					//case for response of permissions
					case "Permission": {
						//get response
						iPos = input.indexOf("RESPONSE:");
						iEnd = input.indexOf("|");
						String response = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("FILENAME:");
						iEnd = input.indexOf("|");
						String filename = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("SIZE:");
						iEnd = input.indexOf("|");
						String size = input.substring(iPos+6,iEnd);
						//get byte array from getFrom
						
						
						int iSend = to.indexOf("SEND");
						String realto = to.substring(0, iSend);
						switch (response) {
							case "YES": {
								//inform sender of other client's response
								clients.get(findClient(realto)).send(getVerdictMsg(to, from, "YES"));
								break;
								
							}
							case "NO": {
								//send back to client message decline and remove socket
								//process to
								clients.get(findClient(realto)).send(getVerdictMsg(to, from, "NO"));
								files.get(getFileIndex(filename, size)).decCount();
								removeID(findID(to));
								break;
							}
						}
						break;
					}
					//case when the client's receive socket is ready to receive the file from the server
					case "Ready": {
						//get get
						iPos = input.indexOf("GET:");
						iEnd = input.indexOf("|");
						String getFrom = input.substring(iPos+5,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("FILENAME:");
						iEnd = input.indexOf("|");
						String filename = input.substring(iPos+10,iEnd);
						input = input.substring(iEnd+1);
						
						iPos = input.indexOf("SIZE:");
						iEnd = input.indexOf("|");
						String size = input.substring(iPos+6,iEnd);
						//find file in file array and send to ready client
						clients.get(findClient(ID)).sendFile(findFile(filename, size));
						int iFile = getFileIndex(filename,size);
						files.get(iFile).decCount();
						if (files.get(iFile).getCount()<=0) {
							//remove file if all clients are sent to
							files.remove(iFile);
							//System.out.println("file was removed");
						}
						//tell receiver its done
						int iRecipient = from.indexOf("RECEIVE");
						String recipient = from.substring(0,iRecipient);
						int iGet = getFrom.indexOf("SEND");
						String origin = getFrom.substring(0, iGet);
						clients.get(findClient(recipient)).send(getControlMsg(recipient, origin,"File transfer from "+origin+" complete."));
						//removeID(ID);
						//clients.get(findClient(ID)).sendInt(0);
						//removeID(findID(getFrom));
						//clients.get(findClient(getFrom)).sendInt(0);
						break;
					}
					
					default:
						break;
					
				}
				break;
			case "Control":
				break;
			default:
				break;
		}
		
		//termination
	}
	
	public void addFile(Files f) {
		//add file to file array
		files.add(f);
	}
	
	private String getMessageForClient(String from, String to, String msg) {
		//forward send-text message to client
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Send-Text|"+
						"TO: "+to+"|"+
						"FROM: "+from+"|";
		String body = "TEXT: " + msg +"|";
		return header + body;
	}
	
	private String getFailedToSend(String from, String to, String msg) {
		//file or text failed to send to client with reason to sender of file
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Send-Fail|"+
						"TO: "+to+"|"+
						"FROM: "+from+"|";
		String body = "TEXT: " + msg +"|";
		return header + body;
	}
	
	private String getControlMsg(String to, String from, String text) {
		//inform sender that sending the file to the client(s) is complete
		String header = "MESSAGE TYPE: Control|"+
						"ACTION: Transfer-Complete|"+
						"TO: "+to+"|"+
						"FROM: "+from+"|";
		String body = "TEXT: " + text +"|";
		return header + body;
	}
	
	private String getVerdictMsg(String to, String from, String reponse) {
		//tells the sender of the file whether the client accepted or declined
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Verdict|"+
						"TO: "+to+"|"+
						"FROM: "+from+"|";
						//"SEQUENCE:"
		String body = "RESPONSE: " + reponse +"|";
		return header + body;
	}
	
	private String getPermissionFromClient(String from, String to, String filename, String size) {
		//message that asks client for permission to send a file to them
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Permission|"+
						"TO: "+to+"|"+
						"FROM: "+from+"|";
						//"SEQUENCE:"
		String body = "FILENAME: " + filename +"|" + "SIZE: "+size+"|";
		return header + body;
	}
	
	public synchronized void removeID(int ID) {
		//removes clientHandler thread if client closes chat application
		int iPos = findClient(ID);
		if (iPos>=0) {
			clientHandler toTerminate = clients.get(iPos);
			log("Removing client at socket "+ID+" at position "+iPos);
			clients.remove(iPos);
			clientCount--;
			log("clientCount= "+clientCount);
			try {
				toTerminate.close();
			} catch (IOException e) {
				log("Error closing thread: "+e.getMessage());
				toTerminate.bStop();
			}
		}
	}
	
	private void addClient(Socket toClientSocket) {
		//when a client connects to the server, it is accepted, spawned on a new thread and added to the array of clients
		log("Client accepted to socket: "+toClientSocket);
		clients.add(new clientHandler(this, toClientSocket));
		try {
			clients.get(clientCount).open();
			clients.get(clientCount).start();
			clientCount++;
			log("clientCount= "+clientCount);
		} catch (IOException e) {
			log("Error opening thread: "+e.getMessage());
		}
	}
	
	public static void main (String[] args) {
		//create a server to listen on a specific port and starts listening for new connections
		tcpServer chatServer = new tcpServer(PORTNUMBER);
		chatServer.run();
	}
	
	public void log(String msg) {
		//prints out to the console
		System.out.println(msg);
	}
	
	
}
