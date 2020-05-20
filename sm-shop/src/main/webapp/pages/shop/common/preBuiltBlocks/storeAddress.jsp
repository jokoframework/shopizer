<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="/WEB-INF/shopizer-tags.tld" prefix="sm" %>

<%@ page session="false" %>



 <address class="footer__address">
 	<div itemscope itemtype="http://schema.org/Organization">
		<div itemprop="address" itemscope itemtype="http://schema.org/PostalAddress">
		<h3><s:message code="label.generic.address.findus" text="Find us" /></h3>
		<div itemprop="streetAddress" class="footer-address-item"><c:out value="${requestScope.MERCHANT_STORE.storeaddress}"/> <c:out value="${requestScope.MERCHANT_STORE.storecity}"/></div>
		<div>
			<span itemprop="addressLocality" class="footer-address-item"><c:choose><c:when test="${not empty requestScope.MERCHANT_STORE.storestateprovince}"><c:out value="${requestScope.MERCHANT_STORE.storestateprovince}"/></c:when><c:otherwise><script>$.ajax({url: "<c:url value="/shop/reference/zoneName"/>",type: "GET",data: "zoneCode=${requestScope.MERCHANT_STORE.zone.code}",success: function(data){$('#storeZoneName').html(data)}})</script><span id="storeZoneName"><c:out value="${requestScope.MERCHANT_STORE.zone.code}"/></span></c:otherwise></c:choose>,
			<span id="storeCountryName" class="footer-address-item"><script>$.ajax({url: "<c:url value="/shop/reference/countryName"/>",type: "GET",data: "countryCode=${requestScope.MERCHANT_STORE.country.isoCode}",success: function(data){$('#storeCountryName').html(data)}})</script></span></span>
		</div>
		<div itemprop="postalCode" class="footer-address-item"><c:out value="${requestScope.MERCHANT_STORE.storepostalcode}"/></div>
		<div>
			<span title="Phone" class="footer-address-item"><i class="fas fa-phone-alt"></i><s:message code="label.generic.phone" text="Phone" /></span>: <span itemprop="telephone"><c:out value="${requestScope.MERCHANT_STORE.storephone}"/></span>
		</div>
		</div>
 	</div>
 </address> 
