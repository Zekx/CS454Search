package Controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import Homework.Ranking;
import Model.Document;

public class MongoSearch {
	ArrayList<Document> objects;
	
	public MongoSearch(){
		this.objects = new ArrayList<Document>();
	}
	
	public ArrayList<Document> searchField(String field){
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
			Document docu = new Document(found.get("name").toString(), found.get("hash").toString(), found.get("url").toString(), found.get("path").toString(), docRank, tfidf);
			objects.add(docu);
		}
		
		return objects;
	}
}
