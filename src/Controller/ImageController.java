package Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Image.Image;
import Image.ImageAnalysis;
import org.bson.Document;

/**
 * Servlet implementation class ImageController
 */
@WebServlet({ "/ImageController", "/Controller/ImageController" })
public class ImageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String query = request.getParameter("Search");
		String filePath = query;
		Image searchTarg = new Image(new File (filePath));
		List<Image> imgList = new ArrayList<Image>();
		List<Document> docs = ImageAnalysis.searchDatabase(searchTarg);
		System.out.println(filePath);
		//System.out.println(docs.size());
		for (Document d : docs) {
			String imgName = "";
			int imgHeight = 0;
			int imgWidth = 0;
			String imgUrl = "";
			String imgPath = "";
			if( d.get("name").toString() != null) {
				imgName = d.get("name").toString();
			}
			if( d.get("height").toString() != null) {
				imgHeight = Integer.parseInt(d.get("height").toString());
			}
			if( d.get("width").toString() != null) {
				imgWidth = Integer.parseInt(d.get("width").toString());
			}
			if( d.get("path").toString() != null) {
				imgPath = d.get("path").toString();
			}
			if( d.get("url").toString() != null) {
				imgUrl = d.get("url").toString();
			}
			
			imgList.add(new Image(imgHeight, imgWidth, imgPath, imgName, imgUrl));
		}
		
		if(!query.equals(null)){
			request.setAttribute("result", imgList);
		}
		request.setAttribute("image", filePath);
//								
//		// Forward the request and response to the view
		RequestDispatcher dispatcher = request.getRequestDispatcher("image.jsp");
		dispatcher.forward(request, response);
		// response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}