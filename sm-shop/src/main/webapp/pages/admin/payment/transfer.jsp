<%--
  Created by IntelliJ IDEA.
  User: nvaldez
  Date: 5/25/20
  Time: 4:02 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page session="false" %>


<script src="<c:url value="/resources/js/ckeditor/ckeditor.js" />"></script>




<div class="control-group">
    <label class="required"><strong><s:message code="module.payment.transfer.details" text="Transfer details"/></strong></label>
    <div class="controls">
	                        <span class="help-inline">
	                        	<c:if test="${details!=null}">
                                    <span id="identifiererrors" class="error"><s:message code="module.payment.transfer.message.details" text="Field in error"/></span>
                                </c:if>
	                        </span>
        <br/>

        <textarea cols="30" id="integrationKeys['details']" name="integrationKeys['details']">
                        				<c:out value="${configuration.integrationKeys['details']}"/>
                        			 </textarea>

        <script type="text/javascript">
            //<![CDATA[

            CKEDITOR.replace("integrationKeys['details']",
                {
                    skin : 'office2003',
                    toolbar :
                        [
                            ['Source','-','Save','NewPage','Preview'],
                            ['Cut','Copy','Paste','PasteText','-','Print'],
                            ['Undo','Redo','-','Find','-','SelectAll','RemoveFormat'], '/',
                            ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
                            ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
                            ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
                            ['Link','Unlink','Anchor'],
                            ['Image','Flash','Table','HorizontalRule','SpecialChar','PageBreak'], '/',
                            ['Styles','Format','Font','FontSize'], ['TextColor','BGColor'],
                            ['Maximize', 'ShowBlocks']
                        ],

                    filebrowserWindowWidth : '720',
                    filebrowserWindowHeight : '740',
                    filebrowserImageBrowseUrl :    '<c:url value="/admin/content/fileBrowser.html"/>'


                });

            //]]>
        </script>


    </div>

</div>
