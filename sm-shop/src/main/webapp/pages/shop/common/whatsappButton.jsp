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
<%@ taglib uri="/WEB-INF/shopizer-tags.tld" prefix="sm" %> 
 
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<c:if test="${requestScope.CONFIGS['whatsapp'] != null}">
	<a href="https://api.whatsapp.com/send?phone=<c:out value="${requestScope.CONFIGS['whatsapp']}"/>&text=<c:out value="${requestScope.CONFIGS['whatsapp_default_text']}"/>" class="whatsapp-button" target="_blank">
		<i class="fab fa-whatsapp fa-4x"></i>
	</a>
</c:if>


					



		
