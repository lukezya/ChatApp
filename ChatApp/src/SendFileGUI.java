import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

public class SendFileGUI extends JFrame {

	private static final long serialVersionUID = -6780325306676265822L;
	
	//GUI to send file
	private JPanel contentPane;
	private JTextField txtTo;
	private JTextField txtPath;
	private String username;

	/**
	 * Create the frame.
	 */
	public SendFileGUI(String name, String toSend) {
		//socket name
		this.username = name + "SEND";
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 582, 342);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("Send File");
		lblTitle.setFont(new Font("SansSerif", Font.BOLD, 42));
		lblTitle.setBounds(181, 26, 195, 64);
		contentPane.add(lblTitle);
		
		txtTo = new JTextField();
		txtTo.setToolTipText("If more than 1 name, separate with \",\"");
		txtTo.setBounds(96, 115, 381, 35);
		contentPane.add(txtTo);
		txtTo.setColumns(10);
		txtTo.setText(toSend);
		
		JLabel lblTo = new JLabel("To");
		lblTo.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTo.setBounds(41, 113, 45, 35);
		contentPane.add(lblTo);
		
		JLabel lblFilename = new JLabel("Path");
		lblFilename.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblFilename.setBounds(41, 174, 53, 26);
		contentPane.add(lblFilename);
		
		txtPath = new JTextField();
		txtPath.setColumns(10);
		txtPath.setBounds(96, 172, 381, 35);
		contentPane.add(txtPath);
		
		JButton btnChoose = new JButton("...");
		btnChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//button opens a file chooser to choose a file
				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfc.setDialogTitle("Choose a file ...");
				
				
				int returnValue = jfc.showDialog(null, "SELECT");
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					txtPath.setText((jfc.getSelectedFile().getPath()));
				}
			}
		});
		btnChoose.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnChoose.setBounds(487, 172, 45, 35);
		contentPane.add(btnChoose);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//check for any errors				
				//get to
				String to = txtTo.getText();
				if (to.equals("")) {
					JOptionPane.showMessageDialog(contentPane, "To field cannot be empty.");
					return;
				}
				//get path
				String path = txtPath.getText();
				//check path exists
				File checkFile = new File(path);
				if (!checkFile.exists()) {
					JOptionPane.showMessageDialog(contentPane, "Need to enter a valid file path.");
					return;
				}
				//spawns a new thread to send file to server - in the background
				new Thread(new SendThread(to, path, username)).start();
				dispose();				
				
			}
		});
		btnSend.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnSend.setBounds(316, 235, 123, 35);
		contentPane.add(btnSend);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		btnCancel.setFont(new Font("Goudy Old Style", Font.PLAIN, 20));
		btnCancel.setBounds(141, 235, 114, 35);
		contentPane.add(btnCancel);
	}
}
