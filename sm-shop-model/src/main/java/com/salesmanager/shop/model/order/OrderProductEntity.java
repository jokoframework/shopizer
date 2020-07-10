package com.salesmanager.shop.model.order;

import java.io.Serializable;

import com.salesmanager.shop.model.catalog.product.ReadableProduct;

public class OrderProductEntity extends OrderProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Double orderedQuantity;
	private ReadableProduct product;


	
	
	public void setOrderedQuantity(Double orderedQuantity) {
		this.orderedQuantity = orderedQuantity;
	}
	public Double getOrderedQuantity() {
		return orderedQuantity;
	}
	public ReadableProduct getProduct() {
		return product;
	}
	public void setProduct(ReadableProduct product) {
		this.product = product;
	}




}
