import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class NewUser extends JFrame {

	private static final long serialVersionUID = -4352261043677843617L;
	
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JPasswordField txtConfirm;

	private Socket clientSocket = null;
	private BufferedReader inStream = null;
    private PrintStream outStream = null;
    
    JButton btnCancel;

	/**
	 * Create the frame.
	 */
	public NewUser() {
		setTitle("New User");
		//set closing the window to do the same thing as pressing button Cancel
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        btnCancel.doClick();
		    }
		});
		setBounds(100, 100, 474, 326);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnSignUp = new JButton("Sign Up");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				//get username and password and confirm
				String sUsername = txtUsername.getText();
				String sPassword = String.valueOf(txtPassword.getPassword());
				String sConfirm = String.valueOf(txtConfirm.getPassword());
				// validation checks
				if (sUsername.equals("")) {
					JOptionPane.showMessageDialog(contentPane, "Username cannot be blank.");
					return;
				}
				if (!sPassword.equals(sConfirm)) {
					JOptionPane.showMessageDialog(contentPane, "Passwords do not match.");
					return;
				}
				
				//open connection to server to sign up
				try {
					if (clientSocket==null) {
						clientSocket = new Socket(LoginScreen.SERVERNAME, tcpServer.PORTNUMBER);
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
				//sign user up
				signUp(sUsername, sConfirm);
				
			}
		});
		btnSignUp.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnSignUp.setBounds(262, 214, 140, 33);
		contentPane.add(btnSignUp);
		
		txtUsername = new JTextField();
		txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtUsername.setColumns(10);
		txtUsername.setBounds(247, 38, 176, 28);
		contentPane.add(txtUsername);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//return to login screen
				LoginScreen ls = new LoginScreen();
				ls.setLocation(getX(), getY());
				ls.setVisible(true);
				dispose();
			}
		});
		btnCancel.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnCancel.setBounds(68, 214, 140, 33);
		contentPane.add(btnCancel);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblUsername.setBounds(43, 38, 105, 28);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblPassword.setBounds(43, 92, 112, 26);
		contentPane.add(lblPassword);
		
		txtPassword = new JPasswordField();
		txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtPassword.setBounds(247, 90, 176, 31);
		contentPane.add(txtPassword);
		
		JLabel lblConfirm = new JLabel("Confirm Password");
		lblConfirm.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblConfirm.setBounds(43, 145, 171, 26);
		contentPane.add(lblConfirm);
		
		txtConfirm = new JPasswordField();
		txtConfirm.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtConfirm.setBounds(247, 143, 176, 31);
		contentPane.add(txtConfirm);
	}
	
	private void signUp(String username, String password) {
		boolean bMoveOn=false;
		//sign up message
		String header = "MESSAGE TYPE: Command|"+
						"ACTION: New-Client|"+
						"TO: Server|"+
						"FROM: Client|";
		String body = "USERNAME: " + username + "|PASSWORD: " + password+"|";
		String sendMsg = header + body;
		//send message
		outStream.println(sendMsg);
		try {
			//find body response ok or xd from server
			String rcvMsg = inStream.readLine();	
			int iPos = rcvMsg.indexOf("RESPONSE:");
			int iEnd = rcvMsg.lastIndexOf("|");
			String response = rcvMsg.substring(iPos+10,iEnd);
			if (response.equals("OK")) {
				//if client was added 
				JOptionPane.showMessageDialog(contentPane, "Client successfully added.");
				bMoveOn=true;
			}
			else
				//if signing up failed
				JOptionPane.showMessageDialog(contentPane, "Failed to add client: "+response);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close connection to server after contacting server
			try {
				if (inStream != null)
					inStream.close();
				if (outStream!=null)
					outStream.close();
				if (clientSocket!=null)
					clientSocket.close();
			} catch (IOException e) {
				System.out.println("Error closing sign up socket: "+e.getMessage());
			}
		}
		if (bMoveOn) {
			//go back to login screen if sign up was successful
			LoginScreen ls = new LoginScreen();
			ls.setLocation(getX(), getY());
			ls.setVisible(true);
			dispose();
		}
		
	}
}
