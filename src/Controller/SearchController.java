package Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import Homework.Ranking;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Modeler.Entry;

@WebServlet({ "/SearchController", "/Controller/SearchController" })
public class SearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String query = request.getParameter("Search");
		String[] split = query.split(" ");
		
		MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("crawler");
        Ranking rank = new Ranking(db);
		
		ArrayList<Entry> objects = null;
		MongoSearch searcher = new MongoSearch();
		if(!query.isEmpty()){
			if(split.length == 1){
				for(String str: split){
		        	if(!str.toLowerCase().equals("and") && !str.toLowerCase().equals("or") && !str.equals("AROUND")){
		        		rank.TFIDF(str, true);
		        	}
		        }
				objects = searcher.searchField(query);
			}
			else{
				for(String str: split){
		        	if(!str.toLowerCase().equals("and") && !str.toLowerCase().equals("or") && !str.equals("AROUND")){
		        		rank.TFIDF(str, false);
		        	}
		        }
				
				Boolean performAND = false, performOR = false, performAROUND = false;
				ArrayList<String> field1 = new ArrayList<String>();
				ArrayList<String> field2 = new ArrayList<String>();
				
				for(String text: split){
					if(text.toLowerCase().equals("and")&& !performOR && !performAROUND){
						performAND = true;
					}
					else{
						if(text.toLowerCase().equals("or") && !performAND && !performAROUND){
							performOR = true;
						}
						else{
							if(text.equals("AROUND") && !performAND && !performOR){
								performAROUND = true;
							}
						}
					}
					
					if(!performAND && !performOR && !performAROUND){
						field1.add(text);
					}
					else{
						if(!text.toLowerCase().equals("and") && !text.toLowerCase().equals("or") && !text.equals("AROUND")){
							field2.add(text);
						}
					}
				}
				
				if(performAND){
					objects = searcher.searchFieldAND(field1, field2);
				}
				else{
					if(performOR){
						objects = searcher.searchFieldOR(field1, field2);
					}
					else{
						if(performAROUND){
							objects = searcher.searchFieldAROUND(field1, field2);
						}
						else{
							objects = searcher.searchFieldSingle(field1, false);
						}
					}
				}
			}
		}
		
//		// Attach the 'users' collection to the request object
		if(!query.equals(null)){
			request.setAttribute("result", objects);
		}
		request.setAttribute("query", query);
//								
//		// Forward the request and response to the view
		RequestDispatcher dispatcher = request.getRequestDispatcher("search.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
