package Modeler;

import org.json.simple.JSONObject;

public class Entry{
	String name;
	String hash;
	String url;
	String path;
	double pageRank;
	double tfidf;
	
	public Entry(String name, String hash, String url, String path, double rank, double tfidf){
		this.name = name;
		this.hash = hash;
		this.url = url;
		this.path = path;
		this.pageRank = rank;
		this.tfidf = tfidf;
	}
	
	@Override
	public boolean equals(Object object){
		boolean isEqual = false;
		if(object != null && object instanceof Entry){
			isEqual = (this.getHash().equals(((Entry)object).getHash()));
		}
		
		return isEqual;
	}
	
	@Override
	public int hashCode(){
		return this.hashCode();
	}
	
	public String getName(){
		return name;
	}
	
	public String getHash(){
		return hash;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getPath(){
		return path;
	}
	
	public double getPageRank(){
		return pageRank;
	}
	
	public double gettfidf(){
		return tfidf;
	}
}
