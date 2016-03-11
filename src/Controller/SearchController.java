package Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

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
		
		ArrayList<Entry> objects = null;
		MongoSearch searcher = new MongoSearch();
		if(!query.isEmpty()){
			if(split.length == 1){
				objects = searcher.searchField(query);
				Collections.sort(objects, new Comparator<Entry>() {
					@Override
					public int compare(Entry one, Entry two){
						return Double.compare(one.gettfidf(), two.gettfidf());
					}
				});
				Collections.reverse(objects);
			}
			else{
				
				Boolean performAND = false, performOR = false;
				ArrayList<String> field1 = new ArrayList<String>();
				ArrayList<String> field2 = new ArrayList<String>();
				
				for(String text: split){
					if(text.toLowerCase().equals("and")&& !performOR){
						performAND = true;
					}
					else{
						if(text.toLowerCase().equals("or") && !performAND){
							performOR = true;
						}
					}
					
					if(!performAND && !performOR){
						field1.add(text);
					}
					else{
						if(!text.toLowerCase().equals("and") && !text.toLowerCase().equals("or")){
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
						objects = searcher.searchField(field1, true);
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
