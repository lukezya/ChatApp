import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginScreen extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8109251577982871405L;
	
	//server name to connect to - 127.0.0.1
	public static final String SERVERNAME="localhost";	
	
	//GUI
	private JPanel contentPane;
	private JButton btnChat;
	private JTextField txtUsername;
	private JPasswordField passwordField;
	private Socket clientSocket = null;
	private BufferedReader inStream = null;
    private PrintStream outStream = null;
   
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginScreen frame = new LoginScreen();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginScreen() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 468, 391);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblChatApp = new JLabel("Chat App");
		lblChatApp.setFont(new Font("SansSerif", Font.BOLD, 42));
		lblChatApp.setBounds(131, 21, 225, 81);
		contentPane.add(lblChatApp);
		
		btnChat = new JButton("Let's Chat");
		btnChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//get username and password
				String sUsername = txtUsername.getText();
				String sPassword = String.valueOf(passwordField.getPassword());
				
				//connects to the server
				try {
					if (clientSocket==null) {
						clientSocket = new Socket(SERVERNAME, tcpServer.PORTNUMBER);
						inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						outStream = new PrintStream(clientSocket.getOutputStream());
					}
				} catch (ConnectException e) {
					System.out.println("Connection refused. You need to initiate a server first.");
				} catch(UnknownHostException unknownHost){
					System.out.println("You are trying to connect to an unknown host!");
				} catch(IOException ioException){
					ioException.printStackTrace();
				}
				//check sUsername and Password by sending a message to the servver
		       if (Authenticate(sUsername, sPassword)) {
		    	   //open chat client
		    	   tcpClientGUI chatClient = new tcpClientGUI(clientSocket, inStream, outStream, sUsername);
		    	   chatClient.setLocation(getX(), getY());
		    	   chatClient.setVisible(true);
		    	   //chatClient.run();
		    	   dispose();
		       } else {
		    	   //close connection to server and give reponse to client
		    	   try {
						if (inStream != null)
							inStream.close();
						if (outStream!=null)
							outStream.close();
						if (clientSocket!=null) {
							clientSocket.close();
							clientSocket=null;
						}
					} catch (IOException e) {
						System.out.println("Error closing sign up socket: "+e.getMessage());
					}
		    	   JOptionPane.showMessageDialog(contentPane, "Invalid Username or Password.");
		    	   
		       };
			}
		});
		btnChat.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnChat.setBounds(140, 253, 176, 33);
		contentPane.add(btnChat);
		
		txtUsername = new JTextField();
		txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtUsername.setBounds(218, 131, 176, 28);
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblUsername.setBounds(73, 131, 105, 28);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblPassword.setBounds(73, 185, 112, 26);
		contentPane.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//enter key does the same thing as button Let's Chat
				btnChat.doClick();
			}
		});
		passwordField.setFont(new Font("Tahoma", Font.PLAIN, 20));
		passwordField.setBounds(218, 183, 176, 31);
		contentPane.add(passwordField);
		JLabel lblImNew = new JLabel("<html><u>I'm New</u></html>");
		lblImNew.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//bring up sign up GUI
				NewUser newUserFrame = new NewUser();
				newUserFrame.setLocation(getX(), getY());
				newUserFrame.setVisible(true);
				
				dispose();
			}
		});
		lblImNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblImNew.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblImNew.setForeground(Color.BLUE);
		lblImNew.setBounds(336, 213, 60, 28);
		contentPane.add(lblImNew);
	}
	
	private boolean Authenticate(String username, String password) {
		//authenticate message
		String header = "MESSAGE TYPE: Command|"+
						"ACTION: Authentication|"+
						"TO: Server|"+
						"FROM: Client|";
		String body = "USERNAME: " + username + "|PASSWORD: " + password+"|";
		String sendMsg = header + body;
		//send message to server
		outStream.println(sendMsg);
		try {
			//find body response ok or xd from server
			String rcvMsg = inStream.readLine();
			int iPos = rcvMsg.indexOf("RESPONSE:");
			int iEnd = rcvMsg.lastIndexOf("|");
			String response = rcvMsg.substring(iPos+10,iEnd);
			if (response.equals("OK")) 
				//let client through to chat client
				return true;
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
}
