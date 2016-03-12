package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Modeler.Entry;

public class Magnitude {
	
	public static Map<String, Double> Magnitude(ArrayList<String> field1, ArrayList<String> field2, Map<String, Entry> vector){
		ArrayList<String> combined = new ArrayList<String>();
		combined.addAll(field1);combined.addAll(field2);
		ArrayList<Double> queryVector = new ArrayList<Double>();
		
		double tf = ((double)1.0/((double)combined.size()));
		double idf = (double) Math.log10(((double)combined.size())/1.0);
		double tfidfQ = (double) tf * idf;
		System.out.println(tfidfQ);
		
		for(int i = combined.size(); i > 0; i--){
			queryVector.add(tfidfQ);
		}
		
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		Map<String, Double> magnitude = new HashMap<String, Double>();
		
		Iterator iter = vector.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Entry> item = (java.util.Map.Entry<String, Entry>) iter.next();
			String docHash = item.getKey();
			Entry entry = item.getValue();
			ArrayList<Double> tfidfList = entry.gettfidf();
			
			while(tfidfList.size() < combined.size()){
				tfidfList.add(0.0);
			}
			for(int j = 0; j < combined.size(); j++){
				dotProduct = dotProduct + (queryVector.get(j) * tfidfList.get(j));
				normA = normA + Math.pow(queryVector.get(j), 2);
				normB = normB + Math.pow(tfidfList.get(j), 2);
			}
			double magni = (double) dotProduct / (double)(Math.sqrt(normA)*Math.sqrt(normB));
			magnitude.put(docHash, magni);
		}
		
		return magnitude;
	}
	
	public static Map<String, Double> Magnitude(ArrayList<String> field1, Map<String, Entry> vector){
		ArrayList<String> combined = new ArrayList<String>();
		combined.addAll(field1);
		ArrayList<Double> queryVector = new ArrayList<Double>();
		
		double tf = ((double)1.0/((double)combined.size()));
		double idf = (double) 1.0 + Math.log(((double)combined.size())/1.0);
		double tfidfQ = (double) tf * idf;
		System.out.println(tfidfQ);
		
		for(int i = combined.size(); i > 0; i--){
			queryVector.add(tfidfQ);
		}
		
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		Map<String, Double> magnitude = new HashMap<String, Double>();
		
		Iterator iter = vector.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Entry> item = (java.util.Map.Entry<String, Entry>) iter.next();
			String docHash = item.getKey();
			Entry entry = item.getValue();
			ArrayList<Double> tfidfList = entry.gettfidf();
			
			while(tfidfList.size() < combined.size()){
				tfidfList.add(0.0);
			}
			for(int j = 0; j < combined.size(); j++){
				dotProduct = dotProduct + queryVector.get(j) * tfidfList.get(j);
				//System.out.println(queryVector.get(j) + " * " + tfidfList.get(j));
				normA = normA + Math.pow(queryVector.get(j), 2);
				normB = normB + Math.pow(tfidfList.get(j), 2);
			}
			double magni = (double) dotProduct / (Math.sqrt(normA)*Math.sqrt(normB));
			//System.out.println("Mag: " + magni);
			magnitude.put(docHash, magni);
		}
		
		return magnitude;
	}
	
}
