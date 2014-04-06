package dao.vo;

public class MappingRow {
	private String filename;  
	private int seqid;  
	private int hashid;  
	private String folder;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getSeqid() {
		return seqid;
	}
	public void setSeqid(int seqid) {
		this.seqid = seqid;
	}
	public int getHashid() {
		return hashid;
	}
	public void setHashid(int hashid) {
		this.hashid = hashid;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
}
