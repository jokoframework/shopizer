package com.salesmanager.shop.admin.in;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.shop.utils.DateUtil;

public class ImportCsvHelper {
	
	private static final String TYPE = "text/csv";
	private static final String INVALID_DATA_FORMAT_MESSAGE_KEY = "message.csv.invalid.data.format";
	
	 public static boolean hasCSVFormat(MultipartFile file) {
	 	return TYPE.equals(file.getContentType());
	  }
		  
	  public static BigDecimal getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(CSVRecord csvRecord, Enum<?> csvHeader, BigDecimal defaultValue, ImportErrorProcessor errorProcessor) {
		  try {
			  String record = csvRecord.get(csvHeader);
			  return record == null || "".equals(record.trim())? defaultValue : new BigDecimal(record);
		  }catch(NumberFormatException e) {
			  errorProcessor.addNewError(INVALID_DATA_FORMAT_MESSAGE_KEY, csvHeader.name());
			  return BigDecimal.ZERO;
		  }
		  
	  }
	  
	  public static Double getDoubleOrDefaultFromRecordWithHeaderNameAndProcessErrors(CSVRecord csvRecord, Enum<?> csvHeader, Double defaultValue, ImportErrorProcessor errorProcessor) {
		try {
		  String record = csvRecord.get(csvHeader);
		  return record == null || "".equals(record.trim())? defaultValue : Double.parseDouble(record);
		}catch(NumberFormatException e) {
			errorProcessor.addNewError(INVALID_DATA_FORMAT_MESSAGE_KEY, csvHeader.name());
			return new Double("0");
		}
	  }
	  
	  public static Boolean getBooleanOrDefaultFromRecordWithHeaderName(CSVRecord csvRecord, Enum<?> csvHeader, Boolean defaultValue) {
		  String record = csvRecord.get(csvHeader);
		  return record == null || "".equals(record.trim())? defaultValue : Boolean.parseBoolean(record);
	  }
	  
	  public static Integer getIntegerOrDefaultFromRecordWithHeaderNameAndProcessErrors(CSVRecord csvRecord, Enum<?> csvHeader, Integer defaultValue, ImportErrorProcessor errorProcessor) {
		  try{
			  String record = csvRecord.get(csvHeader.name());
			  return record == null || "".equals(record.trim()) ? defaultValue : Integer.parseInt(record);
		  }catch(NumberFormatException e) {
			  errorProcessor.addNewError(INVALID_DATA_FORMAT_MESSAGE_KEY, csvHeader.name());
			  return new Integer("0");
			}
	  }
	  
	  public static Date getDateOrDefaultFromRecordWithHeaderNameAndProcessErrors(CSVRecord csvRecord, Enum<?> csvHeader, Date defaultValue, ImportErrorProcessor errorProcessor) {
		  try {
			  String record = csvRecord.get(csvHeader.name());
			  return record == null || "".equals(record.trim()) ? defaultValue : DateUtil.getDate(record.trim());
		  }catch (Exception e) {
			  errorProcessor.addNewError(INVALID_DATA_FORMAT_MESSAGE_KEY, csvHeader.name());
			  return defaultValue;
		} 
	  }
}



