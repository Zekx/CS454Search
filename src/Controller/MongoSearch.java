package Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
		  document.add(new StringField("tfidf", Double.toString(doc.gettfidf()), Field.Store.YES));
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
        
        Ranking rank = new Ranking(db);
        rank.TFIDF(field);
		
		DBObject word = index.findOne(new BasicDBObject("word", field));
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
			result.add(docu);
		}
		
		return result;
	}
	
	public ArrayList<Entry> searchField(ArrayList<String> field, Boolean sort){
		ArrayList<Entry> result = new ArrayList<Entry>();
		try{
			MongoClient mongoClient = new MongoClient("localhost", 27017);

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        DB db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
	        DBCollection index = db.getCollection("index");

	        System.out.println("Connected to MongoDB!");
	        
	        Extractor ext = new Extractor();
	        Ranking rank = new Ranking(db);
	        
//	    	Specify the analyzer for tokenizing text.
		    //	The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();
			
			//	Code to create the index
			Directory indexL = new RAMDirectory();		
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter w = new IndexWriter(indexL, config);
	        
	        for(String item: field){
	        	DBObject word = index.findOne(new BasicDBObject("word", item));
		    		if(word != null){
		    			rank.TFIDF(item);
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
		        			if(!onHold.contains(docu)){
		        				onHold.add(docu);
		        				addDoc(w, docu, ext.extractString(new File(found.get("path").toString())));
		        			}
		    		}
	    		}
	        }		
	        w.close();	        

	        if(sort){
	        	Collections.sort(onHold, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getPageRank(), two.getPageRank());
					}
				});
				Collections.reverse(onHold);
	        }
	        
	        String querystr = "";
			for(String que: field){
				querystr = querystr + " " + que;
			}
			querystr = "\"" + querystr + "\"" + "~300";
			
			//	The "title" arg specifies the default field to use when no field is explicitly specified in the query
			Query q = new QueryParser("description", analyzer).parse(querystr);
			
			// Searching code
			int hitsPerPage = 50;
		    IndexReader reader = DirectoryReader.open(indexL);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    //	Code to display the results of search
		    System.out.println("Found " + hits.length + " hits.");
		    for(int i=0;i<hits.length;++i) 
		    {
		      int docId = hits[i].doc;
		      Document d = searcher.doc(docId);
		      Entry ent = new Entry(d.get("name"), d.get("hash"), d.get("url"), d.get("path"), Double.parseDouble(d.get("pageRank")), Double.parseDouble(d.get("tfidf")));
		      result.add(ent);
		      System.out.println((i + 1) + ". " + d.get("name") + " " + d.get("url"));
		    }
		    
		    for(Entry addIn: onHold){
		    	if(!result.contains(addIn)){
		    		result.add(addIn);
		    	}
		    }
		    onHold.clear();
		    
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
        rank.link_analysis();
        
        System.out.println(field1.size() + " " + field2.size());
        try
		{
			ArrayList<Entry> field1Arr = this.searchField(field1, false);
			ArrayList<Entry> field2Arr = this.searchField(field2, false);
			
			 for(Entry one: field1Arr){
			    	for(Entry two: field2Arr){
			    		if(one.getHash().equals(two.getHash())){
			    			intersection.add(two);
			    			break;
			    		}
			    	}
			    }
			
			 Collections.sort(intersection, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getPageRank(), two.getPageRank());
					}
				});
			Collections.reverse(intersection);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
        mongoClient.close();
		return intersection;
	}
	
	public ArrayList<Entry> searchFieldOR(ArrayList<String> field1, ArrayList<String> field2){
		ArrayList<Entry> union = new ArrayList<Entry>();
		MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("crawler");

        Ranking rank = new Ranking(db);
        rank.link_analysis();
        
        System.out.println(field1.size() + " " + field2.size());
        try
		{
			ArrayList<Entry> field1Arr = this.searchField(field1, false);
			ArrayList<Entry> field2Arr = this.searchField(field2, false);
			
			for(Entry one: field1Arr){
				union.add(one);
		    	for(Entry two: field2Arr){
		    		if(!union.contains(two)){
		    			union.add(two);
		    		}
		    	}
		    }
			
			 Collections.sort(union, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.getPageRank(), two.getPageRank());
					}
				});
			Collections.reverse(union);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
        mongoClient.close();
		return union;
	}
}
