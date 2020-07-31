package com.salesmanager.core.model.catalog.product.in;

public enum ProductImportHeader {
		AVAILABLE(true, "available"),
		SKU(true, "sku"),
		NAME(true, "name"),
		PRODUCT_PRICE_AMOUNT(true, "price"),
		CATEGORY(true,"category"),
		PREORDER(false, "preorder"),
		PRODUCT_FREE(false, "free"),
		PRODUCT_SHIP(false, "ship"),
		PRODUCT_VIRTUAL(false, "virtual"),
		REF_SKU(false, "ref_sku"),
		MANUFACTURER(false, "manufacturer"),
		PRODUCT_ITEM_WEIGHT(false, "weight"),
		PRODUCT_HEIGHT(false,"height"),
		PRODUCT_WEIGHT(false,"weight"),
		PRODUCT_LENGTH(false,"length"),
		PRODUCT_WIDTH(false,"width"),
		FREE_SHIPPING(false,"free.sheeping"),
		QUANTITY(false,"quantity"),
		QUANTITY_ORD_MIN(false,"quantity.min"),
		QUANTITY_ORD_MAX(false,"quantity.max"),
		DESCRIPTION(false,"description"),
		META_TAGS(false,"meta.tags"),
		PRODUCT_PRICE_SPECIAL_AMOUNT(false,"special.amount"),
		PRODUCT_PRICE_SPECIAL_ST_DATE(false,"special.amount.from"),
		PRODUCT_PRICE_SPECIAL_END_DATE(false,"special.amount.to");

	  private boolean required;
	  private String description;
		  
	  ProductImportHeader(boolean required, String description){
		  this.required = required;
		  this.description = description;
	  }
		  
	  public boolean isRequired() {
		  return required;
	  }

	public String getDescription() {
		return description;
	}
	  
	  
	
}