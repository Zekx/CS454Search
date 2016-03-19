package Modeler;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class Entry{
	String name;
	String hash;
	String url;
	String path;
	double pageRank;
	ArrayList<Double> tfidf;
	double magnitude;
	private double score;
	
	public Entry(String name, String hash, String url, String path, double rank, double tfidf){
		this.name = name;
		this.hash = hash;
		this.url = url;
		this.path = path;
		this.pageRank = rank;
		this.tfidf = new ArrayList<Double>();
		this.tfidf.add(tfidf);
		this.magnitude = 0;
		this.score = 0;
	}
	
	public Entry(String name, String hash, String url, String path, double rank){
		this.name = name;
		this.hash = hash;
		this.url = url;
		this.path = path;
		this.pageRank = rank;
		this.tfidf = new ArrayList<Double>();
		this.magnitude = 0;
		this.score = 0;
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
	
	public ArrayList<Double> gettfidf(){
		return tfidf;
	}
	
	public void insertTFIDF(ArrayList<Double> value){
		this.tfidf.addAll(value);
	}
	
	public double getMagnitude(){
		return magnitude;
	}
	
	public void setMagnitude(double mag){
		this.magnitude = mag;
	}
	
	public double getScore(){
		return score;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
}
