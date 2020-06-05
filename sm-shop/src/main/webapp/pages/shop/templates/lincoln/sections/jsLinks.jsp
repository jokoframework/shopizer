<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<%@ page session="false" %>


    <script src="<c:url value="/resources/templates/sodep/js/bootstrap.min.js" />"></script>

        <script type="text/javascript">

            $('#product-tab a:first').tab('show');
            $('#product-tab a').click(function (e) {
            		e.preventDefault();
            		$(this).tab('show');
            }) 
            
            
        </script>
     

    