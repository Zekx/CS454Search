package Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import Homework.Extractor;
import Homework.Ranking;
import Modeler.Entry;
/*
 * String name;
	String hash;
	String url;
	String path;
	double pageRank;
	double tfidf;
 */

public class MongoSearch {
	ArrayList<Entry> onHold;
	
	public MongoSearch(){
		this.onHold = new ArrayList<Entry>();
	}
	
	private static void addDoc(IndexWriter w, Entry doc, String description) throws IOException 
	{
		  Document document = new Document();
		 
		  document.add(new StringField("name", doc.getName(), Field.Store.YES));
		  document.add(new StringField("hash", doc.getHash(), Field.Store.YES));
		  document.add(new StringField("url", doc.getUrl(), Field.Store.YES));
		  document.add(new StringField("path", doc.getPath(), Field.Store.YES));
		  document.add(new StringField("pageRank", Double.toString(doc.getPageRank()), Field.Store.YES));
		  document.add(new TextField("description", description, Field.Store.YES));
		  
		  w.addDocument(document);
	}
	
	public ArrayList<Entry> searchField(String field){
		ArrayList<Entry> result = new ArrayList<Entry>();
		
		MongoClient mongoClient = new MongoClient("localhost", 27017);

        System.out.println("Establishing connection...");

        //Get the connection.
        DB db = mongoClient.getDB("crawler");
        DBCollection table = db.getCollection("urlpages");
        DBCollection index = db.getCollection("index");

        System.out.println("Connected to MongoDB!");
		
		DBObject word = index.findOne(new BasicDBObject("word", field));
		ArrayList<Double> tfidfs = new ArrayList<Double>();
		BasicDBList document = (BasicDBList) word.get("document");
		
		for(int i = 0; i < document.size(); i++){
			DBObject obj = (DBObject) document.get(i);
			String hash = obj.get("docHash").toString();
			Double tfidf = Double.parseDouble(obj.get("tfidf").toString());
			
			BasicDBObject pageRank = (BasicDBObject) db.getCollection("pagerank").findOne(new BasicDBObject("Hash", hash));
			Double docRank = (double) 0;
			if(pageRank != null){
				docRank = Double.parseDouble(pageRank.get("PageRank Value").toString());
				if(docRank == 1){
					docRank = (double) 0;
				}
			}
			
			DBObject found = table.findOne(new BasicDBObject("hash", hash));
			Entry docu = new Entry(found.get("name").toString(), found.get("hash").toString(), found.get("url").toString(), found.get("path").toString(), docRank, tfidf);
			Boolean existed = false;
			for(Entry ent: result){
				if(ent.getHash().equals(docu.getHash())){
					ent.insertTFIDF(docu.gettfidf());
					existed = true;
				}
			}
			if(!existed) result.add(docu);
		}
		Collections.sort(result, new Comparator<Entry>() {
			@Override
			public int compare(Entry one, Entry two){
				return Double.compare(one.gettfidf().get(0)*.70 + one.getPageRank()*.30, two.gettfidf().get(0)*.70 + two.getPageRank()*.30);
			}
		});
		Collections.reverse(result);
		mongoClient.close();
		
		return result;
	}
	
	public ArrayList<Entry> searchFieldSingle(ArrayList<String> field, boolean OR){
		ArrayList<Entry> result = new ArrayList<Entry>();
		ArrayList<Entry> finalResult = new ArrayList<Entry>();
		MongoClient mongoClient = null;
		try{
			mongoClient = new MongoClient("localhost", 27017);

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        DB db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
	        DBCollection index = db.getCollection("index");

	        System.out.println("Connected to MongoDB!");
	        
	        for(String item: field){
	        	DBObject word = index.findOne(new BasicDBObject("word", item));
		    		if(word != null){
		    			System.out.println(item);
		        		BasicDBList document = (BasicDBList) word.get("document");
		        		for(int i = 0; i < document.size(); i++){
		        			DBObject obj = (DBObject) document.get(i);
		        			String hash = obj.get("docHash").toString();
		        			Double tfidf = Double.parseDouble(obj.get("tfidf").toString());
		        			
		        			BasicDBObject pageRank = (BasicDBObject) db.getCollection("pagerank").findOne(new BasicDBObject("Hash", hash));
		        			Double docRank = (double) 0;
		        			if(pageRank != null){
		        				docRank = Double.parseDouble(pageRank.get("PageRank Value").toString());
		        				if(docRank == 1){
		        					docRank = (double) 0;
		        				}
		        			}
		        			
		        			DBObject found = table.findOne(new BasicDBObject("hash", hash));
		        			Entry docu = new Entry(found.get("name").toString(), found.get("hash").toString(), found.get("url").toString(), found.get("path").toString(), docRank, tfidf);
		        			Boolean existed = false;
		        			for(int j = 0; j < result.size();j++){
		        				Entry ent = result.get(j);
		        				if(ent.getHash().equals(docu.getHash())){
		        					ent.insertTFIDF(docu.gettfidf());
		        					System.out.println("ENT: " + ent.getName() + " " + ent.getHash());
			        				existed = true;
			        			}
		        			}		
		        			if(!existed) result.add(docu);
		    		}
	    		}
	        }
	        
	        Map<String, Entry> vector = new HashMap<String, Entry>();
	        
			 for(Entry one: result){
	    		if(OR){
	    			vector.put(one.getHash(), one);
	    			finalResult.add(one);
	    		}
	    		else{
	    			if(one.gettfidf().size() == field.size()){
		    			vector.put(one.getHash(), one);
		    			finalResult.add(one);
		    		}
	    		}
			  }
			 
			 Map<String, Double> magnitude = Magnitude.Magnitude(field, vector);
			 
			 for(int i = 0; i < finalResult.size(); i++){
				 String hash = finalResult.get(i).getHash();
				 
				 finalResult.get(i).setMagnitude(magnitude.get(hash));
			 }
			 
			 Collections.sort(finalResult, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getMagnitude(), two.getMagnitude());
					}
				});
		}catch(Exception e){
			e.printStackTrace();
		}
		mongoClient.close();
		
		return finalResult;
	}
	
	public ArrayList<Entry> searchField(ArrayList<String> field){
		ArrayList<Entry> result = new ArrayList<Entry>();
		try{
			MongoClient mongoClient = new MongoClient("localhost", 27017);

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        DB db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
	        DBCollection index = db.getCollection("index");

	        System.out.println("Connected to MongoDB!");
	        
	        for(String item: field){
	        	DBObject word = index.findOne(new BasicDBObject("word", item));
		    		if(word != null){
		        		BasicDBList document = (BasicDBList) word.get("document");
		        		for(int i = 0; i < document.size(); i++){
		        			DBObject obj = (DBObject) document.get(i);
		        			String hash = obj.get("docHash").toString();
		        			Double tfidf = Double.parseDouble(obj.get("tfidf").toString());
		        			
		        			BasicDBObject pageRank = (BasicDBObject) db.getCollection("pagerank").findOne(new BasicDBObject("Hash", hash));
		        			Double docRank = (double) 0;
		        			if(pageRank != null){
		        				docRank = Double.parseDouble(pageRank.get("PageRank Value").toString());
		        				if(docRank == 1){
		        					docRank = (double) 0;
		        				}
		        			}
		        			
		        			DBObject found = table.findOne(new BasicDBObject("hash", hash));
		        			Entry docu = new Entry(found.get("name").toString(), found.get("hash").toString(), found.get("url").toString(), found.get("path").toString(), docRank, tfidf);
		        			Boolean existed = false;
		        			for(int j = 0; j<result.size();j++){
		        				Entry ent = result.get(j);
		        				if(ent.getHash().equals(docu.getHash())){
			        				ent.insertTFIDF(docu.gettfidf());
			        				existed = true;
			        			}
		        			}		
		        			if(!existed) result.add(docu);
		    		}
	    		}
	        }		
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return result;
	}
	
	public ArrayList<Entry> searchFieldAND(ArrayList<String> field1, ArrayList<String> field2){
		ArrayList<Entry> intersection = new ArrayList<Entry>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("crawler");

        Extractor ext = new Extractor();
        Ranking rank = new Ranking(db);
        
        try
		{
        	ArrayList<Entry> field1Arr, field2Arr;
			if(field1.size() > 1){
				field1Arr = this.searchFieldSingle(field1, false);
			}
			else{
				field1Arr = this.searchField(field1);
			}
			if(field2.size() > 1){
				field2Arr = this.searchFieldSingle(field2, false);
			}
			else{
				field2Arr = this.searchField(field2);
			}
			//System.out.println(field1Arr.size() + " " + field2Arr.size());
			
			Map<String, Entry> vector = new HashMap<String, Entry>();
			 for(Entry one: field1Arr){
			    	for(Entry two: field2Arr){
			    		if(one.getHash().equals(two.getHash())){
			    			one.insertTFIDF(two.gettfidf());
			    			intersection.add(one);
			    			vector.put(one.getHash(), one);
			    			break;
			    		}
			    	}
			    }
			 
			 Map<String, Double> magnitude = Magnitude.Magnitude(field1, field2, vector);
			 
			 for(int i = 0; i < intersection.size(); i++){
				 String hash = intersection.get(i).getHash();
				 
				 intersection.get(i).setMagnitude(magnitude.get(hash));
			 }
			
			 Collections.sort(intersection, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getMagnitude(), two.getMagnitude());
					}
				});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        mongoClient.close();
		return intersection;
	}
	
	public ArrayList<Entry> searchFieldOR(ArrayList<String> field1, ArrayList<String> field2){
		ArrayList<Entry> union = new ArrayList<Entry>();
		ArrayList<Entry> finalUnion = new ArrayList<Entry>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("crawler");

        Ranking rank = new Ranking(db);
        
        System.out.println(field1.size() + " " + field2.size());
        try
		{
        	ArrayList<Entry> field1Arr, field2Arr;
			if(field1.size() > 1){
				field1Arr = this.searchFieldSingle(field1, false);
			}
			else{
				field1Arr = this.searchField(field1);
			}
			if(field2.size() > 1){
				field2Arr = this.searchFieldSingle(field2, false);
			}
			else{
				field2Arr = this.searchField(field2);
			}

			union.addAll(field1Arr); union.addAll(field2Arr);
			System.out.println("Before "+union.size());
			Map<String, Entry> vector = new HashMap<String, Entry>();
			
			Iterator iter = union.iterator();
			while(iter.hasNext()){
				Entry ent = (Entry) iter.next();
				Boolean duplicate = false;
					
				for(int j = 0; j < finalUnion.size(); j++){
					if(finalUnion.get(j).getHash().equals(ent.getHash())){
						finalUnion.get(j).insertTFIDF(ent.gettfidf());
						duplicate = true;
						break;
					}
				}
				
				if(!duplicate) {
					finalUnion.add(ent);
				}
			}
			System.out.println("After "+finalUnion.size());
			
			for(Entry ent: finalUnion){
				//System.out.println(ent.gettfidf().toString());
				vector.put(ent.getHash(), ent);
			}
			
			Map<String, Double> magnitude = Magnitude.Magnitude(field1, field2, vector);
			 
			 for(int i = 0; i < finalUnion.size(); i++){
				 String hash = finalUnion.get(i).getHash();
				 
				 finalUnion.get(i).setMagnitude(magnitude.get(hash));
			 }

			 Collections.sort(finalUnion, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getMagnitude(), two.getMagnitude());
					}
				});
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
        mongoClient.close();
		return finalUnion;
	}
	
	//Searches proximity
	public ArrayList<Entry> searchFieldAROUND(ArrayList<String> field1, ArrayList<String> field2){
		ArrayList<Entry> proximity = new ArrayList<Entry>();
		ArrayList<Entry> filteredResults = new ArrayList<Entry>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("crawler");

        Extractor ext = new Extractor();  
        try
		{
        	ArrayList<Entry> field1Arr, field2Arr;
			if(field1.size() > 1){
				field1Arr = this.searchFieldSingle(field1, false);
			}
			else{
				field1Arr = this.searchField(field1);
			}
			if(field2.size() > 1){
				field2Arr = this.searchFieldSingle(field2, false);
			}
			else{
				field2Arr = this.searchField(field2);
			}

			StandardAnalyzer analyzer = new StandardAnalyzer();
			Directory indexL = new RAMDirectory();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter w = new IndexWriter(indexL, config);
			
			Map<String, Entry> vector = new HashMap<String, Entry>();
			 for(Entry one: field1Arr){
				 System.out.println("1");
			    	for(Entry two: field2Arr){
			    		System.out.println("2");
			    		if(one.getHash().equals(two.getHash())){
			    			System.out.println("3");
			    			one.insertTFIDF(two.gettfidf());
			    			proximity.add(one);
			    			break;
			    		}
			    	}
			    }
			 System.out.println("PROX SIZE: "+proximity.size());
			 
//			Code to create the index
			Map<String, ArrayList<Double>> tfidfMap = new HashMap<String, ArrayList<Double>>();			
			
			for(int i = 0; i < proximity.size(); i++){
				addDoc(w, proximity.get(i), ext.extractString(new File(proximity.get(i).getPath())));
				System.out.println(ext.extractString(new File(proximity.get(i).getPath())));
				tfidfMap.put(proximity.get(i).getHash(), proximity.get(i).gettfidf());
			}
			w.close();
			
			String querystr = "";
			for(String que: field1){
				querystr = querystr + " " + que;
			}
			for(String que: field2){
				querystr = querystr + " " + que;
			}
			querystr = "\"" + querystr + "\"" + "~10";
			
			//	The "title" arg specifies the default field to use when no field is explicitly specified in the query
			Query q = new QueryParser("description", analyzer).parse(querystr);
			
			// Searching code
			int hitsPerPage = 50;
		    IndexReader reader = DirectoryReader.open(indexL);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    System.out.println("Found " + hits.length + " hits.");
		    
		    for(int i=0;i<hits.length;++i) 
		    {
		      int docId = hits[i].doc;
		      Document d = searcher.doc(docId);
		      Entry ent = new Entry(d.get("name"), d.get("hash"), d.get("url"), d.get("path"), Double.parseDouble(d.get("pageRank")));
		      ent.insertTFIDF(tfidfMap.get(ent.getHash()));
		      filteredResults.add(ent);
		      vector.put(ent.getHash(), ent);
		    }
		    
		    Map<String, Double> magnitude = Magnitude.Magnitude(field1, field2, vector);
			 
			 for(int i = 0; i < filteredResults.size(); i++){
				 String hash = filteredResults.get(i).getHash();
				 
				 filteredResults.get(i).setMagnitude(magnitude.get(hash));
			 }

			 Collections.sort(filteredResults, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getMagnitude(), two.getMagnitude());
					}
				});
	}catch(Exception e){
		e.printStackTrace();
	}
        
    mongoClient.close();
    return filteredResults;
}}
