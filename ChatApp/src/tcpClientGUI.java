import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class tcpClientGUI extends JFrame {

	private static final long serialVersionUID = -842143684412957326L;
	
	//GUI
	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JButton btnSendText;
	private Socket clientSocket;
    private PrintStream outStream;
    private JTextField txtInput;
    private JTextField txtTo;
    private tcpClientThread clientThread = null;
    private JTextArea messageArea;
    private String username;
    
	/**
	 * Create the frame.
	 */
    
	public tcpClientGUI(Socket socket, BufferedReader inStream, PrintStream outStream, String username) {
		setTitle("Welcome");
		this.username = username;
		clientThread = new tcpClientThread(this,inStream);
		clientThread.start();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 686, 656);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtInput = new JTextField();
		txtInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//do same as button Send Text
				btnSendText.doClick();
			}
		});
		txtInput.setFont(new Font("Tahoma", Font.PLAIN, 13));
		txtInput.setBounds(61, 159, 474, 36);
		contentPane.add(txtInput);
		txtInput.setColumns(10);
		
		btnSendText = new JButton("Send Text");
		btnSendText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//get to and text
				String toGuys;
				String msgToSend = txtInput.getText();
				//check if message is to be broadcasted
				if (txtTo.isEditable()) {
					toGuys = txtTo.getText();
					if (toGuys.equals("")) {
						JOptionPane.showMessageDialog(contentPane, "To field cannot be blank.");
						return;						
					}
					messageArea.append("You: "+msgToSend+"\n");
				} else {
					toGuys = "BROADCAST";
					messageArea.append("You broadcasted: "+msgToSend+"\n");
				}
				//construct a message and send message
				sendMessage(msgToSend, toGuys);
				//reset text input field for next message
				txtInput.setText("");
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			}
		});
		btnSendText.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnSendText.setBounds(545, 157, 119, 36);
		contentPane.add(btnSendText);
		
		txtTo = new JTextField();
		txtTo.setToolTipText("If more than 1 name, separate with \",\"");
		txtTo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		txtTo.setBounds(61, 113, 474, 36);
		contentPane.add(txtTo);
		txtTo.setColumns(10);
		
		JCheckBox chckbxBroadcast = new JCheckBox("Broadcast");
		chckbxBroadcast.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				//change to field if message is to be broadcasted
				if (txtTo.isEditable())
					txtTo.setEditable(false);
				else
					txtTo.setEditable(true);
			}
		});
		chckbxBroadcast.setFont(new Font("Tahoma", Font.BOLD, 15));
		chckbxBroadcast.setBounds(426, 74, 109, 33);
		contentPane.add(chckbxBroadcast);
		
		JLabel lblTo = new JLabel("To");
		lblTo.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTo.setBounds(14, 118, 27, 25);
		contentPane.add(lblTo);
		
		JLabel lblChatClient = new JLabel("Chat Client");
		lblChatClient.setFont(new Font("SansSerif", Font.BOLD, 42));
		lblChatClient.setBounds(14, 10, 236, 70);
		contentPane.add(lblChatClient);
		
		JLabel lblText = new JLabel("Text");
		lblText.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblText.setBounds(14, 164, 37, 25);
		contentPane.add(lblText);
		
		JLabel lblUserLoggedIn = new JLabel("User logged in:");
		lblUserLoggedIn.setFont(new Font("Rockwell", Font.BOLD, 15));
		lblUserLoggedIn.setBounds(14, 74, 123, 24);
		contentPane.add(lblUserLoggedIn);
		
		JLabel lblUser = new JLabel("");
		lblUser.setForeground(Color.BLUE);
		lblUser.setFont(new Font("Rockwell", Font.BOLD, 15));
		lblUser.setText(username);
		lblUser.setBounds(134, 76, 92, 21);
		contentPane.add(lblUser);
		
		JButton btnSendFile = new JButton("Send File");
		btnSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//open send file GUI
				String toSend = txtTo.getText();
				SendFileGUI sendGUI = new SendFileGUI(username, toSend);
				sendGUI.setLocation(getX(), getY());
				sendGUI.setVisible(true);
				
			}
		});
		btnSendFile.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnSendFile.setBounds(545, 113, 119, 36);
		contentPane.add(btnSendFile);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(14, 218, 650, 336);
		contentPane.add(scrollPane);
		
		messageArea = new JTextArea();
		scrollPane.setViewportView(messageArea);
		messageArea.setLineWrap(true);
		
		JButton btnSave = new JButton("Save Chat");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//save chat to textfile with the name e.g.<username>2018/03/23.txt
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				String filename = username + dateFormat.format(date)+".txt";
				//File file = new File(filename);
				FileWriter fw = null;
				BufferedWriter bw = null;
				try {
					fw = new FileWriter(filename);
					bw = new BufferedWriter(fw);
					bw.write(messageArea.getText());
					JOptionPane.showMessageDialog(contentPane, "Chat was saved.");
				} catch (IOException e) {
					System.out.println("Error writing to file: "+e.getMessage());
					JOptionPane.showMessageDialog(contentPane, "Error saving chat was saved.");
				} finally {
					try {
						if (bw != null)
							bw.close();
						if (fw != null)
							fw.close();
					} catch (IOException e) {
						System.out.println("Error closing file written to: "+e.getMessage());

					}
				}
				
				
			}
		});
		btnSave.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnSave.setBounds(545, 571, 119, 36);
		contentPane.add(btnSave);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				messageArea.setText("");
			}
		});
		btnClear.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnClear.setBounds(401, 571, 119, 36);
		contentPane.add(btnClear);
		
		clientSocket = socket;
		this.outStream = outStream;
		//run();
	}
	
	private void sendMessage(String msg, String to) {
		//construct message to send to server that will send to client
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Send-Text|"+
						"TO: "+to+"|"+
						"FROM: "+username+"|";
		String body = "TEXT: " + msg +"|";
		String sendMsg = header + body;
		outStream.println(sendMsg);
	}
	
	private void sendPermission(String response, String to, String filename, String size) {
		//construct message to reply to server of permission
		String header = "MESSAGE TYPE: Data|"+
						"ACTION: Permission|"+
						"TO: "+to+"|"+
						"FROM: "+username+"|";
		String body = "RESPONSE: " + response +"|" + 
					  "FILENAME: "+filename + "|"+
					  "SIZE: "+size+"|";
		String sendPermission = header + body;
		outStream.println(sendPermission);
	}
	
	public void handleInput(String inMsg) {		
        // handle input from server - same as protocol
		//break down inMsg - message type, action, to, from
		int iPos = inMsg.indexOf("MESSAGE TYPE:");
		int iEnd = inMsg.indexOf("|");
		String MT = inMsg.substring(iPos+14,iEnd);
		inMsg = inMsg.substring(iEnd+1);
		
		iPos = inMsg.indexOf("ACTION:");
		iEnd = inMsg.indexOf("|");
		String action = inMsg.substring(iPos+8,iEnd);
		inMsg = inMsg.substring(iEnd+1);
		
		iPos = inMsg.indexOf("TO:");
		iEnd = inMsg.indexOf("|");
		String to = inMsg.substring(iPos+4,iEnd);
		inMsg = inMsg.substring(iEnd+1);
		
		iPos = inMsg.indexOf("FROM:");
		iEnd = inMsg.indexOf("|");
		String from = inMsg.substring(iPos+6,iEnd);
		inMsg = inMsg.substring(iEnd+1);
		
		//switch cases for specific conditions
		switch(MT) {
			case "Command":{
				break;
			}
			case "Data":{
				switch (action) {
					case "Send-Text":{
						//receiving a message
						iPos = inMsg.indexOf("TEXT:");
						iEnd = inMsg.indexOf("|");
						String msgRec = inMsg.substring(iPos+6,iEnd);
						messageArea.append(from+": "+msgRec+"\n");
						JScrollBar vertical = scrollPane.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
						break;
					}
					
					case "Permission": {
						//get filename and size and ask for permission in form of dialog
						iPos = inMsg.indexOf("FILENAME:");
						iEnd = inMsg.indexOf("|");
						String filename = inMsg.substring(iPos+10,iEnd);
						inMsg = inMsg.substring(iEnd+1);
						
						iPos = inMsg.indexOf("SIZE:");
						iEnd = inMsg.indexOf("|");
						String size = inMsg.substring(iPos+6,iEnd);
						
						int iSend = from.indexOf("SEND");
						String realFrom = from.substring(0,iSend);
						
						int response = JOptionPane.showConfirmDialog(contentPane, "Do you want to accept "+filename+" from "+realFrom +"?", "Confirm",
						        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						
						if ((response == JOptionPane.NO_OPTION)||(response == JOptionPane.CLOSED_OPTION)) {
							//send response back to server
							sendPermission("NO", from, filename, size);
						} else if (response == JOptionPane.YES_OPTION) {
							//send accept and create new socket with server for transferring on a new thread
							sendPermission("YES", from, filename ,size);
							new Thread(new AcceptThread(filename, size, realFrom, to)).start();
						}
						break;
					}
					
					case "Verdict": {
						//get response of permission message back from client through server
						iPos = inMsg.indexOf("RESPONSE:");
						iEnd = inMsg.indexOf("|");
						String response = inMsg.substring(iPos+10,iEnd);
						switch(response) {
							case "YES": {
								messageArea.append(from+" accepted your sent-file.\n");
								break;
							}
							case "NO": {
								messageArea.append(from+" declined your sent-file.\n");
								break;
							}
						}
						JScrollBar vertical = scrollPane.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
						break;
					}
					
					case "Send-Fail":{
						//failed to send file to a client
						iPos = inMsg.indexOf("TEXT:");
						iEnd = inMsg.indexOf("|");
						String fail = inMsg.substring(iPos+6,iEnd);
						messageArea.append(fail+"\n");
						JScrollBar vertical = scrollPane.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
						break;
						
					}
					
					default:
						break;
				}
				break;
			}
			case "Control":{
				switch (action) {
					case "Transfer-Complete":{
						//file transfer was successful and completed
						iPos = inMsg.indexOf("TEXT:");
						iEnd = inMsg.indexOf("|");
						String controlMsg = inMsg.substring(iPos+7,iEnd);
						messageArea.append(controlMsg+"\n");
						JScrollBar vertical = scrollPane.getVerticalScrollBar();
						vertical.setValue(vertical.getMaximum());
						break;
					}
				}
				
		
				break;
			}		
			default:
				break;
		}
    }
	
	public void close() {
		//destructor of client
		try {
			if (outStream != null)  
				outStream.close();
			if (clientSocket != null)  
				clientSocket.close();
			System.exit(0);
		} catch(IOException e) {
			System.out.println("Error closing client: "+e.getMessage()); 
		}
	}
}
