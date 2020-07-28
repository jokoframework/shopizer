package com.salesmanager.shop.admin.model.catalog;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

/**
 * Wrapper to ease admin jstl
 * @author mechague
 *
 */
public class ImportCsvFile implements Serializable {

  /**
   * 
   */
	private static final long serialVersionUID = 1L;
  
	@NotNull
	private MultipartFile file;

	public MultipartFile getFile() {
		return file;
	}
	
	public void setFile(MultipartFile file) {
		this.file = file;
	}
}
