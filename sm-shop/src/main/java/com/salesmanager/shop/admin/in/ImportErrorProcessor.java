package com.salesmanager.shop.admin.in;

import java.util.Locale;

import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import com.salesmanager.shop.utils.LabelUtils;
import javax.annotation.Generated;

public class ImportErrorProcessor {

	private BindingResult bindingResult;
	private Locale locale;
	private LabelUtils messages;
	private String objectName;
	
	@Generated("SparkTools")
	private ImportErrorProcessor(Builder builder) { 
		this.bindingResult= builder.bindingResult;
		this.locale = builder.locale;
		this.messages = builder.messages;
		this.objectName = builder.objectName;
	}
	public BindingResult getBindingResult() {
		return bindingResult;
	}
	public void setResult(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
	}
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public LabelUtils getMessages() {
		return messages;
	}
	public void setMessages(LabelUtils messages) {
		this.messages = messages;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public void addNewError(String messageCode, @Nullable Object... arguments) {
		ObjectError error = new ObjectError(this.objectName, null, arguments, messages.getMessage(messageCode, this.locale));
	  	this.bindingResult.addError(error);
	}
	
	public void addNewErrorWithExtra(String messageCode, String extraInfo, @Nullable Object... arguments) {
		ObjectError error = new ObjectError(this.objectName, null, arguments, messages.getMessage(messageCode, this.locale) + extraInfo);
	  	this.bindingResult.addError(error);
	}
	
	public boolean hasErrors() {
	  	return this.bindingResult.hasErrors();
	}
	
	/**
	 * Creates builder to build {@link ImportErrorProcessor}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * Builder to build {@link ImportErrorProcessor}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private BindingResult bindingResult;
		private Locale locale;
		private LabelUtils messages;
		private String objectName;

		private Builder() {
		}

		public Builder withBindingResult(BindingResult bindingResult) {
			this.bindingResult = bindingResult;
			return this;
		}

		public Builder withLocale(Locale locale) {
			this.locale = locale;
			return this;
		}

		public Builder withMessages(LabelUtils messages) {
			this.messages = messages;
			return this;
		}

		public Builder withObjectName(String objectName) {
			this.objectName = objectName;
			return this;
		}

		public ImportErrorProcessor build() {
			if(this.bindingResult==null ||
				this.locale == null ||
				this.messages ==null ||
				this.objectName == null ) {					
				throw new IllegalArgumentException("All fields are required");
			}
			return new ImportErrorProcessor(this);
		}
	}
}



