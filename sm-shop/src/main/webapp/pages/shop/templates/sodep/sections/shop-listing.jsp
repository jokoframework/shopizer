<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="/WEB-INF/shopizer-tags.tld" prefix="sm" %>

<%@ page session="false" %>

/**
* Builds the product container div from the product list
**/

function buildProductsList(productList, divProductsContainer) {

for (var i = 0; i < productList.products.length; i++) {
    var productHtml = '<li itemscope itemtype="http://schema.org/Enumeration" class="item listing-item" data-id="' + productList.products[i].id  + '" item-price="' +  productList.products[i].price +'" item-name="' +  productList.products[i].description.name +'" item-order="' +  productList.products[i].sortOrder +'">';
    productHtml = productHtml + '<div class="product-box">';
    productHtml = productHtml + '<div class="product-box__body">';
    productHtml = productHtml + '<div class="product-box__img">';
    var productUrl = '<c:url value="/shop/product/" />' + productList.products[i].description.friendlyUrl + '.html<sm:breadcrumbParam/>';
    if(productList.products[i].image!=null) {
        productHtml = productHtml + '<a href="' + productUrl + '"><img src="<c:url value=""/>' + productList.products[i].image.imageUrl +'" itemprop="image"></a>';
    }
    productHtml = productHtml + '</div>'
    productHtml = productHtml + '<div class="product-box__info">';
    if(productList.products[i].discounted) {
        productHtml = productHtml + '<h3><del>' + productList.products[i].originalPrice +'</del><div class="specialPrice">' + productList.products[i].finalPrice + '<i class="fas fa-tag"></i></div></h3>';
    } else {
        productHtml = productHtml + '<h3>' + productList.products[i].finalPrice +'</h3>';
    }
    productHtml = productHtml + '<a href="<c:url value="/shop/product/" />' + productList.products[i].description.friendlyUrl + '.html<sm:breadcrumbParam/>"><h4 class="name" itemprop="name">' + productList.products[i].description.name +'</h4></a>';
    productHtml = productHtml + '<div class="bottom">'
    if(productList.products[i].quantity>0) {
        productHtml = productHtml + '<div class="store-btn form-inline">'
        if(productList.products[i].quantityOrderMaximum>=-1 || productList.products[i].quantityOrderMaximum>1 && !productList.products[i].productVirtual) {
            productHtml = productHtml + '<div class="form-group product-qty">'
            productHtml = productHtml + '<a class="btn button btn-info updateQty" productid="' + productList.products[i].id + '" stepBy="' + -productList.products[i].productItemWeight + '"><i class="fas fa-minus"></i></a>'
            productHtml = productHtml + '<input name="qty-product" id="qty-productId-'+productList.products[i].id+'" class="input-mini form-control form-control-sm" placeholder="1" type="text"/>'
            productHtml = productHtml + '<a class="btn button btn-info updateQty" productid="' + productList.products[i].id + '" stepBy="' + productList.products[i].productItemWeight + '"><i class="fas fa-plus"></i></a>'
            productHtml = productHtml + '</div>'
        }
        productHtml = productHtml + '</div>'
    }
    productHtml = productHtml + '<a href="' + productUrl + '" class="view productDetail"  data-toggle="tooltip" title="<s:message code="label.product.details" text="Details"/>"><i class="far fa-eye"></i></a><a productid="' + productList.products[i].id + '" href="#" class="addToCart" data-toggle="tooltip" title="<s:message code="label.cart.add" text="Add to cart"/>"><i class="fas fa-cart-plus"></i></a>';
    productHtml = productHtml + '</div>'
    productHtml = productHtml + '</div>'
    productHtml = productHtml + '</div>'
    productHtml = productHtml + '</div>'
    productHtml = productHtml + '</li>'
    $(divProductsContainer).append(productHtml);

}

initBindings();
initQtBindings();

}