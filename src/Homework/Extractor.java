package Homework;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mchange.v1.util.ArrayUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.TeeContentHandler;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.JSONArray;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by NIck on 2/6/2016.
 */
public class Extractor {
    private ArrayList<String> stopWords;
    private CopyOnWriteArrayList<String> documentWords;
    private JSONObject meta;
    
    public Extractor(){
    	stopWords = new ArrayList<String>();
    	
    	File file = new File("C:/Users/Brandon/workspace/CS454Search/resources/stopwords.txt");
    	try{
    		BufferedReader stream = new BufferedReader(new FileReader(file));
    		
    		String line = stream.readLine();

    	    while (line != null) {
    	        stopWords.add(line);
    	        line = stream.readLine();
    	    }
    		
    	    stream.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public String SHA256Converter(String str){
		MessageDigest md;
		StringBuffer SHA256String = null;
		
		try {
			
			md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes());
			
			byte byteData[] = md.digest();
			
	        StringBuffer buffer = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	         buffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	 
	        SHA256String = new StringBuffer();
	    	for (int i=0;i<byteData.length;i++) {
	    		String hex=Integer.toHexString(0xff & byteData[i]);
	   	     	if(hex.length()==1) SHA256String.append('0');
	   	     	SHA256String.append(hex);
	    	}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return SHA256String.toString();
	}
    
    public String extractString(File file)throws IOException, SAXException, TikaException{
    	String body = "";
    	try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());
          
            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            body = bodyHandler.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    	return body;
    }

    //This method extracts the specific document and outputs a set of string
    //Not sure if we should keep track of the count per word
    public JSONArray extract(File file)  throws IOException, SAXException, TikaException
    {
    	JSONArray data = new JSONArray();
        //I heard wrapping the FileInputStream in BufferedInputStream is faster, idk if it actually is
        try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());
          

            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            String[] split = bodyHandler.toString().replaceAll("\\s+"," ").split(" ");
            for(String s: split){
            	//System.out.print(s + " ");
            	
                //Tokenizing the strings
                if(!s.equals("") && !this.stopWords.contains(s)){
                	if(s.contains("-") || s.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")){
                    	String[] split2 = s.replaceAll("-", " ").split(" ");
                    	data.add(s.replaceAll("[+.^:,]","").toLowerCase());
                    	
                    	for(String t: split2){
                    		if(!t.replaceAll("[^a-zA-Z]+","").toLowerCase().equals(""))data.add(t.replaceAll("[^a-zA-Z]+","").toLowerCase());
                    	}
                    }
                    else{
                    	if(!s.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("")) data.add(s.replaceAll("[^a-zA-Z]+","").toLowerCase());
                    }
                }
            }

//            set.add(handler.toString().replaceAll("\\s+"," ")); //This should remove most of the white spaces
//            data.add(handler.toString().replaceAll("\\s+"," "));
//            System.out.println(handler.toString().replaceAll("\\s+"," "));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public JSONObject extractMeta(File file) throws IOException, SAXException, TikaException{
        try {

            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());

            meta = new JSONObject();
            //For extracting metadata information
            for(String s : metadata.names()){
               // System.out.println(s + " : " + metadata.get(s));
                meta.put(s, metadata.get(s));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return meta;
    }
    
    public JSONArray getLinks(String url, String uri, File file) throws IOException
    {
    	JSONArray arr = new JSONArray();
    	Document doc = Jsoup.parse(file, uri, url);
    	
    	Elements allLinks = doc.select("a[href]");
        for (Element link : allLinks) {
        	arr.add(link.absUrl("href"));
        }
    	return arr;
    }
    //This extracts the information to a JSON file
    public void exportJson(File file, String name, String url, JSONArray dataSet, JSONObject metadata, DBCollection table, JSONArray arr)
    {
        //
        BasicDBObject doc = new BasicDBObject()
                .append("name", name)
                .append("url", url)
                .append("hash", this.SHA256Converter(url))
                .append("DocumentLength", dataSet.size())
                .append("metadata", metadata)
                .append("path", file.getAbsolutePath())
                .append("links", arr)
                .append("# of links", arr.size());

        table.insert(doc);
    }
    
    public void indexTerms(DB db, String urlHash, File file) throws InterruptedException{
    	DBCollection table = db.getCollection("urlpages");
    	DBCollection index = db.getCollection("index");
    	documentWords = new CopyOnWriteArrayList<String>();
    	
    	//I heard wrapping the FileInputStream in BufferedInputStream is faster, idk if it actually is
        try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());

            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            String[] split = bodyHandler.toString().replaceAll("\\s+"," ").split(" ");
            for(String s : split){
            	if(s.contains("-")){
                	String[] split2 = s.replaceAll("-", " ").split(" ");
                	documentWords.add(s.replaceAll("[^a-zA-Z-]+","").toLowerCase());
                	
                	for(String t: split2){
                		if(!t.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("") || !t.equals("-")) documentWords.add(t.replaceAll("[^a-zA-Z]+","").toLowerCase());
                	}
                }
                else{
                	if(s.contains(".")){
                		if(!s.endsWith(".")){
                			documentWords.add(s.replaceAll("[^a-zA-Z.//:]+","").toLowerCase());
                		}
                		else{
                			documentWords.add(s.replaceAll("[^a-zA-Z]+","").toLowerCase());
                		}
                	}
                	else{
                		if(!s.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("")) documentWords.add(s.replaceAll("[^a-zA-Z]+","").toLowerCase());
                	}
                }

            }
            Iterator<String> iter = documentWords.iterator();
            
            while(iter.hasNext()){  
            	String s = iter.next();
        		if(!this.stopWords.contains(s)){
        			if(index.findOne(new BasicDBObject("word",s.toString())) == null){
        				JSONArray doc = new JSONArray();
        				JSONObject innerDoc = new JSONObject();
        				
        				innerDoc.put("Frequency", 1);
        				innerDoc.put("docHash", urlHash);
        				doc.add(innerDoc);
        				
        				BasicDBObject entry = new BasicDBObject()
        					.append("word", s.toString())
        					.append("document", doc);
        				
        				index.insert(entry);
        			}
        			else{
        				DBObject entry = index.findOne(new BasicDBObject("word",s.toString()));
        				
        				BasicDBList doc = (BasicDBList) entry.get("document");
        				JSONObject innerDoc = new JSONObject();
        				Boolean docUpdate = false;
        				
        				CopyOnWriteArrayList<Object> arr = new CopyOnWriteArrayList<Object>();
        				for(int i = 0; i < doc.size(); i++){
        					arr.add(doc.get(i));
        				}

        				for(Object docu: arr){
        					BasicDBObject item = (BasicDBObject) docu;
        					
        					if( item.get("docHash").equals(urlHash)){     
        						int freq = Integer.parseInt(item.get("Frequency").toString()) + 1;
        						
            					innerDoc.put("Frequency", Integer.toString(freq));
                				innerDoc.put("docHash", urlHash);
            					
            					doc.remove(item);
                				doc.add(innerDoc);
                				
                				BasicDBObject update = new BasicDBObject();
            					update.put("$set", new BasicDBObject("word", s.toString()));
            					update.put("$set", new BasicDBObject("document", doc));
                				index.update(new BasicDBObject("word", s.toString()), update);
                				docUpdate = true;
            				}	       					
        				}
        				if(!docUpdate){
        					innerDoc.put("Frequency", 1);
            				innerDoc.put("docHash", urlHash);
        					
            				doc.add(innerDoc);
            				
            				BasicDBObject update = new BasicDBObject();
        					update.put("$set", new BasicDBObject("word", s.toString()));
        					update.put("$set", new BasicDBObject("document", doc));
            				
            				index.update(new BasicDBObject("word", s.toString()), update);
        				}
        			}
        		}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, TikaException
    {
//        //Connects to the Mongo Database.
//        MongoClient mongoClient = new MongoClient("localhost", 27017);
//        DB db = null;
//        DBCollection table = null;
//
//        System.out.println("Establishing connection...");
//
//        //Get the connection.
//        db = mongoClient.getDB("crawler");
//        table = db.getCollection("urlpages");
//
//        File file = new File ("C:\\data\\htmls\\99 Homes - Movies & TV on Google Play.html");
//        Extractor ext = new Extractor();


    }

}
