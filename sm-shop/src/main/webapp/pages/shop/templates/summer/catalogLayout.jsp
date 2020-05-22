<%
response.setCharacterEncoding("UTF-8");
response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", -1);
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
 
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
 

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  
 <%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  
 <c:set var="lang" scope="request" value="${requestScope.locale.language}"/> 
 
 
 <html xmlns="http://www.w3.org/1999/xhtml"> 
 
 
     <head>
        	 	<meta charset="utf-8">
    			<title><c:out value="${requestScope.PAGE_INFORMATION.pageTitle}" /></title>
    			<meta name="viewport" content="width=device-width, initial-scale=1.0">
    			<meta name="description" content="<c:out value="${requestScope.PAGE_INFORMATION.pageDescription}" />">
    			<meta name="author" content="<c:out value="${requestScope.MERCHANT_STORE.storename}"/>">

				 <!-- theme variables css file -->
				 <link href="<c:url value="/resources/templates/summer/css/vars.css" />" rel="stylesheet">

                <jsp:include page="/pages/shop/templates/sodep/sections/shopLinks.jsp" />
 	</head>
 
 	<body>
 	
	<div id="pageContainer" class="page">
		<tiles:insertAttribute name="header" ignore="true"/>

		<tiles:insertAttribute name="navbar" ignore="true"/>

		<div class="page__content container">
			<tiles:insertAttribute name="body" ignore="true"/>
		</div>

		<tiles:insertAttribute name="footer" ignore="true"/>

		<jsp:include page="/pages/shop/common/whatsappButton.jsp" />
	</div>
	<!-- end container -->
	   <jsp:include page="/pages/shop/templates/sodep/sections/jsLinks.jsp" />

 	</body>
 
 </html>
 
