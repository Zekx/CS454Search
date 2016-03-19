package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Modeler.Entry;

public class Magnitude {
	static ArrayList<String> combined;
	
	public static void normalize(ArrayList<Double> queryVector, Map<String, Entry> vector)
	{
		double max = 0;
		double min = 0;
		
		max = queryVector.get(0);
		min = queryVector.get(0);
		
		Iterator vectorIter1 = vector.entrySet().iterator();
		while(vectorIter1.hasNext())
		{
			Map.Entry<String, Entry> item = (java.util.Map.Entry<String, Entry>) vectorIter1.next();
			Entry current = item.getValue();
			ArrayList<Double> currentTFIDFList = current.gettfidf();
			for(Double num: currentTFIDFList){
				if(num > max){
					max = num;
				}
				if(num < min){
					min = num;
				}
			}
		}
		
		Iterator queryIter = queryVector.iterator();
		int counter = 0, originalSize = queryVector.size();
		while(queryIter.hasNext())
		{
			queryVector.add(Math.abs( (queryVector.get(counter) - min) / (max - min) ));
			queryVector.remove(counter);
			counter++;
			if(counter >= originalSize-1){
				break;
			}
		}
		
		Iterator vectorIter2 = vector.entrySet().iterator();
		while(vectorIter2.hasNext()){
			Map.Entry<String, Entry> item = (java.util.Map.Entry<String, Entry>) vectorIter2.next();
			Entry ent = item.getValue();
			
			Iterator tfidfValues = ent.gettfidf().iterator();
			int counter2 = 0, originalSizeVector = ent.gettfidf().size();
			while(tfidfValues.hasNext()){
				ent.gettfidf().add((ent.gettfidf().get(counter2) - min) / (max - min) );
				ent.gettfidf().remove(counter2);
				counter2++;
				if(counter2 >= originalSizeVector-1){
					break;
				}
			}
		}
	}
	
	public static Map<String, Double> Magnitude(ArrayList<String> field1, ArrayList<String> field2, Map<String, Entry> vector){
		combined = new ArrayList<String>();
		combined.addAll(field1);combined.addAll(field2);
		ArrayList<Double> queryVector = new ArrayList<Double>();
		
		double tf = ((double)1.0/((double)combined.size()));
		double idf = (double) Math.log(((double)combined.size())/1.0);
		double tfidfQ = (double) tf * idf;
		System.out.println(tfidfQ);
		
		for(int i = combined.size(); i > 0; i--){
		queryVector.add(tfidfQ);
		}
		
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		Map<String, Double> magnitude = new HashMap<String, Double>();
		normalize(queryVector, vector);
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
		combined = new ArrayList<String>();
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
		normalize(queryVector, vector);
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
