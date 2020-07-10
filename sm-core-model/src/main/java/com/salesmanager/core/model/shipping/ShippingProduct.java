package com.salesmanager.core.model.shipping;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;

public class ShippingProduct {
	
	public ShippingProduct(Product product) {
		this.product = product;

	}
	
	private Double quantity = 1d;
	private Product product;
	
	private FinalPrice finalPrice;
	
	
	
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public Double getQuantity() {
		return quantity;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Product getProduct() {
		return product;
	}
	public FinalPrice getFinalPrice() {
		return finalPrice;
	}
	public void setFinalPrice(FinalPrice finalPrice) {
		this.finalPrice = finalPrice;
	}

}
