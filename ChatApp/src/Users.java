
public class Users {
	//object for keeping record of client username and password
	private String username;
	private String password;
	
	public Users(String username, String password) {
		this.username = username;
		this.password = password;
	}
	//setters
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}
	//validates if the given user credentials are the same as this user objects
	public boolean validate(Users u) {
		if ((username.equals(u.getUsername()))&&(password.equals(u.getPassword()))) 
			return true;
		else
			return false;
	}
}
