package dao.vo;

import java.util.Date;

public class HashRow {
	private int id;  
	private byte[] hashbytes;  
	private String hash;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte[] getHashBytes() {
		return hashbytes;
	}
	public void setHashBytes(byte[] hashbytes) {
		this.hashbytes = hashbytes;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}  
	
}
