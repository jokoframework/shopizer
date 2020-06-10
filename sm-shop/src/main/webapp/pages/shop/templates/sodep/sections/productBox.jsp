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

<script>

	$(function(){

		initQtBindings();

	});

	function updateProductQty(stepBy, productId) {
		let qty = '#qty-productId-' + productId;

		var qtyValue = $(qty).val();

		if (!qtyValue || qtyValue == null) {
			qtyValue = "1";
		}

		if (parseFloat(qtyValue) <= 1 && parseFloat(stepBy) == -1) {
			qtyValue = "2";
		}

		$(qty).val(parseFloat(qtyValue) + parseFloat(stepBy));
	}


	function initQtBindings() {
		/** add to cart **/
		$(".updateQty").click(function(){
			updateProductQty($(this).attr("stepBy"), $(this).attr("productId"));
		});

	}

</script>
										<c:forEach items="${requestScope.ITEMS}" var="product">
											<li class="span3" data-id="${product.id}" item-price="${product.price}" item-name="${product.description.name}" item-order="${product.sortOrder}">
												<div class="product-box">
													<div class="product-box__body">
														<div class="product-box__img">
														<c:if test="${product.image!=null}"><a href="<c:url value="/shop/product/" /><c:out value="${product.description.friendlyUrl}"/>.html"><img src="<sm:shopProductImage imageName="${product.image.imageName}" sku="${product.sku}"/>"/></a></c:if>
														</div>
														<div class="product-box__info">
															<h3>
															<c:choose>
																<c:when test="${product.discounted}">
																	<del><c:out value="${product.originalPrice}" /></del>
																	<div class="specialPrice">
																		<c:out value="${product.finalPrice}" />
																		<i class="fas fa-tag"></i>
																	</div>
																</c:when>
																<c:otherwise>
																	<c:out value="${product.finalPrice}" />
																</c:otherwise>
															</c:choose>
														</h3>
															<a href="<c:url value="/shop/product/" /><c:out value="${product.description.friendlyUrl}"/>.html<sm:breadcrumbParam productId="${product.id}"/>"><h4><c:out value="${product.description.name}"/></h4></a>
															<div class="bottom">
																<c:if test="${product.quantity>0}">
																	<div class="store-btn form-inline">
																	<div class="form-group product-qty">
																		<a class="btn button btn-info updateQty" productid="<c:out value="${product.id}"/>" stepBy="<c:out value="-${product.productItemWeight}"/>"><i class="fas fa-minus"></i></a>
																		<input name="qty-product" id="qty-productId-<c:out value="${product.id}"/>" class="input-mini form-control form-control-sm" placeholder="1" type="text"/>
																		<a class="btn button btn-info updateQty" productid="<c:out value="${product.id}"/>" stepBy="<c:out value="${product.productItemWeight}"/>"><i class="fas fa-plus"></i></a>
																		</div>
																	</div>
																</c:if>
																<a class="productDetail" data-toggle="tooltip" title="<s:message code="label.product.details" text="Details"/>" href="<c:url value="/shop/product/" /><c:out value="${product.description.friendlyUrl}"/>.html<sm:breadcrumbParam productId="${product.id}"/>"><i class="far fa-eye"></i></a> <c:choose><c:when test="${requestScope.FEATURED==true}"><c:if test="${requestScope.CONFIGS['displayAddToCartOnFeaturedItems']==true}"><a class="addToCart" data-toggle="tooltip" title="<s:message code="label.cart.add" text="Add to cart"/>" href="#" productId="${product.id}"><i class="fas fa-cart-plus"></i></a></c:if></c:when><c:otherwise>/ <a class="addToCart" data-toggle="tooltip" title="<s:message code="label.cart.add" text="Add to cart"/>" href="#" productId="${product.id}"><i class="fas fa-cart-plus"></i></a></c:otherwise></c:choose>
															</div>
														</div>
													</div>
												</div>
										    </li>
										</c:forEach>   