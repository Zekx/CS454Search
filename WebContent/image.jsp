<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>   

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>SEARCH ENGINE</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
	</head>
	<body>
		<h1>SEARCH ENGINE <small> CS454</small></h1><hr/>
		
		
		<form action = "search.jsp"> <input type = "submit" value = "Text Search"> </form>
		
		<form action="ImageController" method="get">
		<div class = "row" align = "center">
			<p>This application allows you to find documents on this server.</p>
			<div class = "col-md-offset-3">
				<input type = "file" name = "Search" class = "col-md-8" size = "50" >
			</div>
			
		</div>
		<div align = "center"><input class = "btn btn-primary" type = "submit" value = "Search"></div>
		
		</form>
		
		<div class="row">
        <div class="col-xs-12">

          <div class="page-header">
            <h2>Search Results</h2>
          </div>
          <table class="table table-striped table-bordered">
            <tr>
            	<th> <img src = "${ image }" /></th>
            </tr>

            <c:forEach items="${ result }" var="result" varStatus = "status">
            
            	<tr>
            		<td>
            		<strong><a href="${result.url}">${result.url}</a></strong> : <p>Path: ${result.filePath}</p>
            		</td>
            	</tr>
            
            </c:forEach>
          </table>
        </div>
      </div>
</body>
</html>