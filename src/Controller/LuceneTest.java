package Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import Homework.Extractor;
import Homework.Ranking;
import Modeler.Entry;


public class LuceneTest 
{
	public static ArrayList<Entry> searchField(ArrayList<String> field){
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
	        	DBObject word = index.findOne(new BasicDBObject("word", field));
		    		if(word != null){
		    			rank.TFIDF(item);
		        		BasicDBList document = (BasicDBList) word.get("document");
		        		for(int i = 0; i < document.size(); i++){
		        			DBObject obj = (DBObject) document.get(i);
		        			String hash = obj.get("docHash").toString();
		        			int Frequency = Integer.parseInt(obj.get("Frequency").toString());
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
		        			addDoc(w, docu, ext.extractString(new File(found.get("path").toString())));
		    		}
	    		}
	        }		
	        w.close();
	        String querystr = "";
			for(String que: field){
				querystr = querystr + " " + que;
			}
			querystr = "\"" + querystr + "\"" + "~200";
			
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
		      System.out.println((i + 1) + ". " + d.get("isbn") + " t " + d.get("title"));
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args)
	{
		ArrayList<String> field1 = new ArrayList<String>();
    	field1.add("tennis");
    	ArrayList<String> field2 = new ArrayList<String>();
    	field2.add("player");
    	MongoClient mongoClient = new MongoClient("localhost", 27017);

        System.out.println("Establishing connection...");

        //Get the connection.
        DB db = mongoClient.getDB("crawler");
        DBCollection table = db.getCollection("urlpages");
        DBCollection index = db.getCollection("index");

        System.out.println("Connected to MongoDB!");
        
        Ranking rank = new Ranking(db);
        Extractor ext = new Extractor();
        
        try
		{
			//	Specify the analyzer for tokenizing text.
		    //	The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();
			
			//	Code to create the index
			Directory indexL = new RAMDirectory();
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			
			IndexWriter w = new IndexWriter(indexL, config);
			
			ArrayList<Entry> field1Arr = searchField(field1);
			ArrayList<Entry> field2Arr = searchField(field2);
			
			//	Text to search
			String querystr = "";
			for(String que: field1){
				querystr = querystr + " " + que;
			}
			querystr = "\"" + querystr + "\"" + "~100 ";
			
			String querystr2 = "";
			for(String que: field2){
				querystr2 = querystr2 + " " + que;
			}
			querystr2 = "\"" + querystr2 + "\"" + "~100";
			String finalQuery = querystr + querystr2;
			
			//	The "title" arg specifies the default field to use when no field is explicitly specified in the query
			Query q = new QueryParser("description", analyzer).parse(finalQuery);
			
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
		      System.out.println((i + 1) + ". " + d.get("isbn") + " t " + d.get("title"));
		    }
		    
		    // reader can only be closed when there is no need to access the documents any more
		    reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
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
	
	private static void addDoc(IndexWriter w, String title, String isbn) throws IOException 
	{
		  Document doc = new Document();
		  // A text field will be tokenized
		  doc.add(new TextField("title", title, Field.Store.YES));
		  // We use a string field for isbn because we don't want it tokenized
		  doc.add(new StringField("isbn", isbn, Field.Store.YES));
		  w.addDocument(doc);
	}
}
