
public class Files {
	//object for storing sent files on the server
	private String filename;
	private String filesize;
	private byte[] file;
	private int iCount;
	
	public Files(String filename, String filesize, byte[] file, int count) {
		this.filename = filename;
		this.filesize = filesize;
		this.file = file;
		iCount = count;
	}
	//getters
	public byte[] getFile() {
		return file;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getFilesize() {
		return filesize;
	}
	
	public int getCount() {
		return iCount;
	}
	//count for keeping track of number of clients to send file to
	public void incCount() {
		iCount++;
	}
	
	public void decCount() {
		iCount--;
	}
	
}
