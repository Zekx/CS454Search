package Model;

import org.json.simple.JSONObject;

public class Document {
	String name;
	String hash;
	String url;
	String path;
	double pageRank;
	double tfidf;
	
	public Document(String name, String hash, String url, String path, double rank, double tfidf){
		this.name = name;
		this.hash = hash;
		this.url = url;
		this.path = path;
		this.pageRank = rank;
		this.tfidf = tfidf;
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
	
	public double getRank(){
		return pageRank;
	}
	
	public double gettfidf(){
		return tfidf;
	}
}
