-------------------------------------------------------------------------------
Running Chat Server and Client
-------------------------------------------------------------------------------
Navigate to Folder ChatApp\bin directory.

Open up 4 terminals/cmds at this directory path.

In the 1st window, run the following command:
  java tcpServer

In the rest of the 3 windows, run the following command:
  java LoginScreen

Layout the 3 GUIS nicely on screen.

To login, client can use any of the username and passwords found at path
ChatApp\Users.txt.

For the 3 logins, you can use:

username: gxxhen001
password: 123

username: flnmar011
password: 619

username: chnanr001
password: 789

The applications on the server and clients are now running, and clients are
able to send text messages and files to anyone online(signed in on server).

-------------------------------------------------------------------------------
How to Send a Message
-------------------------------------------------------------------------------
From gxxhen001's client, type in the username of a client online e.g. flnmar011
Type your message in the Text input field. e.g. "sup bro"
Press Enter or the Send Text button to send message to other client.

-------------------------------------------------------------------------------
How to Send a File
-------------------------------------------------------------------------------
Click the Send File button to open up SendFile GUI.
Type in the username of a client online if To field is empty e.g. flnmar011
Click the ... button to choose a file from your computer. Try the textfile
stored at ChatApp\suh dude.text
Click Select button in the file chooser to select file.
Click Send button to send file to other client/clients.
On the other client, click Yes button on the dialog to give permission.

-------------------------------------------------------------------------------
Tips
-------------------------------------------------------------------------------
Hovering mouse over the "To" input fields will give a helpful tooltip on
multicast.
