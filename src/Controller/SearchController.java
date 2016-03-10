package Controller;

import java.io.IOException;
import java.util.ArrayList;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model.Document;

@WebServlet({ "/SearchController", "/Controller/SearchController" })
public class SearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("HELLO!");
		String query = request.getParameter("Search");
		//String[] split = query.split(" ");
		
		ArrayList<Document> objects = null;
		if(!query.isEmpty()){
			MongoSearch searcher = new MongoSearch();
			objects = searcher.searchField(query);
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
