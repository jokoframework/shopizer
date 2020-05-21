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
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %> 
 
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

	  <!-- footer -->
            <footer>
				<div class="container">
					<div id="footer" class="footer row-fluid flex">
						<div class="footer__logo logo flex">
							<c:if test="${not empty requestScope.MERCHANT_STORE.storeLogo}">
								<!--  use merchant store logo -->
								<a class="grey store-name" href="<c:url value="/shop/"/>">
									<img class="logoImage" src="<sm:storeLogo/>"/>
								</a>
							</c:if>
							<div class="footer__menu flex">
								<c:forEach items="${requestScope.CONTENT_PAGE}" var="content">
									<div class="footer__menu-item"><a href="<c:url value="/shop/pages/${content.seUrl}.html"/>" class="current" style="color: #fff;">${content.name}</a></div>
								</c:forEach>
								<c:if test="${requestScope.CONFIGS['displayContactUs']==true}">
									<div class="footer__menu-item"><a href="<c:url value="/shop/store/contactus.html"/>" style="color: #fff;"><s:message code="label.customer.contactus" text="Contact us"/></a></div>
								</c:if>
							</div>
						</div>
						<div class="footer__info flex">
							<div class="footer__social">
								<h3 class="lead"><s:message code="label.generic.office.hours" text="Office Hours" /></h3>
								<div>
									<span>Lunes a viernes 8 a 18 hs | </span>
									<span>Sábados de 8 a 13 hs. </span>
								</div>
								<!-- Social links -->
								<c:if test="${requestScope.CONFIGS['facebook_page_url'] != null}">
									<c:if test="${requestScope.CONFIGS['facebook_page_url'] != null}">
										<a href="<c:out value="${requestScope.CONFIGS['facebook_page_url']}"/>"><i class="fab fa-facebook fa-2x"></i></a>
									</c:if>
									<c:if test="${requestScope.CONFIGS['twitter_handle'] != null}">
										<a href="<c:out value="${requestScope.CONFIGS['twitter_handle']}"/>"><i class="fab fa-twitter fa-2x"></i></a>
									</c:if>
								</c:if>
							</div>
							<c:if test="${requestScope.CONFIGS['displayStoreAddress'] == true}">
								<jsp:include page="/pages/shop/common/preBuiltBlocks/storeAddress.jsp"/>
							</c:if>
							<div class="footer__customer">
								<c:if test="${requestScope.CONFIGS['displayCustomerSection'] == true}">
									<h3 class="lead"><s:message code="label.customer.myaccount" text="My Account" /></h3>
									<ul class="footerLiks">
										<sec:authorize access="hasRole('AUTH_CUSTOMER') and fullyAuthenticated">
											<li><a href="<c:url value="/shop/customer/account.html"/>" style="color: #fff;"><s:message code="menu.profile" text="Profile"/></a></li>
											<li><a href="<c:url value="/shop/customer/billing.html"/>" style="color: #fff;"><s:message code="label.customer.billingshipping" text="Billing & shipping information"/></a></li>
											<li><s:message code="label.order.recent" text="Recent orders"/></li>
										</sec:authorize>
										<sec:authorize access="!hasRole('AUTH_CUSTOMER') and fullyAuthenticated">
											<li>
												<s:message code="label.security.loggedinas" text="You are logged in as"/> [<sec:authentication property="principal.username"/>]. <s:message code="label.security.nologinacces.store" text="We can't display store logon box"/>
											</li>
										</sec:authorize>
										<sec:authorize access="!hasRole('AUTH_CUSTOMER') and !fullyAuthenticated">
											<li><a href="#" style="color: #fff;"><s:message code="button.label.login" text="Login" /></a></li>
										</sec:authorize>
									</ul>
								</c:if>
							</div>
						</div>
					</div>
					<div id="footer-bottom">
						<div class="container">
						   <div class="row-fluid">
							   <div class="span12 text"><i class="fas fa-copyright"></i><s:message code="label.generic.providedby" /> <a href="http://www.sodep.com.py" class="footer-href" target="_blank">Sodep</a></div>
						   </div>
						 </div>
					</div>
				</div>
			</footer>