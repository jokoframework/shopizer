<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="/WEB-INF/shopizer-tags.tld" prefix="sm"%>


<%@ page session="false"%>

<link
	href="<c:url value="/resources/css/bootstrap/css/datepicker.css" />"
	rel="stylesheet"></link>
<script
	src="<c:url value="/resources/js/bootstrap/bootstrap-datepicker.js" />"></script>
<script src="<c:url value="/resources/js/ckeditor/ckeditor.js" />"></script>
<script
	src="<c:url value="/resources/js/jquery.formatCurrency-1.4.0.js" />"></script>
<script
	src="<c:url value="/resources/js/jquery.alphanumeric.pack.js" />"></script>
<script src="<c:url value="/resources/js/adminFunctions.js" />"></script>

<div class="tabbable">


	<jsp:include page="/common/adminTabs.jsp" />

	<div class="tab-content">

		<div class="tab-pane active" id="catalogue-section">

			<div class="sm-ui-component">
				<h3>
					<s:message code="label.category.upload.csv.file"
						text="Upload csv category file" />
				</h3>

				<br/> 
				<br/>

				<c:url var="categorySave" value="/admin/categories/saveCsv.html" />
				<form:form method="POST" enctype="multipart/form-data"
					modelAttribute="category" action="${categorySave}">

					<form:errors path="*" cssClass="alert alert-error" element="div" />
					<div id="store.success" class="alert alert-success"
						style="<c:choose><c:when test="${success!=null}">display:block;</c:when><c:otherwise>display:none;</c:otherwise></c:choose>">
						<s:message code="message.success" text="Request successfull" />
					</div>
					<div id="store.error" class="alert alert-error"
						style="display: none;">
						<s:message code="message.error" text="An error occured" />
					</div>

					<div class="control-group">
						<label> <s:message code="label.file.csv" text="CSV File" />&nbsp;
						</label>
						<div class="controls" id="imageControl">
							<input class="input-file" id="file" name="file" type="file">
						</div>
					</div>

					<div class="form-actions">
						<div class="pull-right">
							<button type="submit" class="btn btn-success">
								<s:message code="button.label.submit2" text="Submit" />
							</button>
						</div>
					</div>
				</form:form>
			</div>
		</div>
	</div>
</div>