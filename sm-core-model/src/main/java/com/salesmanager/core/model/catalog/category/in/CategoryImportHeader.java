package com.salesmanager.core.model.catalog.category.in;

public enum CategoryImportHeader {
		NAME(true, "name"),
		PARENT(false, "parent"), 
		DESCRIPTION(false, "description"), 
		VISIBLE(false, "visible");
	
	  private boolean required;
	  private String description;
		  
	  CategoryImportHeader(boolean required, String description){
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