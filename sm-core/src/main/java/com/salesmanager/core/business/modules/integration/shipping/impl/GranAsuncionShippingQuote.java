package com.salesmanager.core.business.modules.integration.shipping.impl;

import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.shipping.*;
import com.salesmanager.core.model.system.CustomIntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModule;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuotePrePostProcessModule;
import org.apache.commons.lang.Validate;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GranAsuncionShippingQuote implements ShippingQuoteModule {
	
	
	public final static String MODULE_CODE = "granAsuncion";

	@Inject
	private MerchantConfigurationService merchantConfigurationService;
	
	@Inject
	private ProductPriceUtils productPriceUtils;


	@Override
	public void validateModuleConfiguration(
			IntegrationConfiguration integrationConfiguration,
			MerchantStore store) throws IntegrationException {
		
		
		
		
		List<String> errorFields = null;
		
		//validate integrationKeys['account']
		Map<String,String> keys = integrationConfiguration.getIntegrationKeys();
		//if(keys==null || StringUtils.isBlank(keys.get("price"))) {
		if(keys==null) {
			errorFields = new ArrayList<String>();
			errorFields.add("price");
		} else {
			//validate it can be parsed to BigDecimal
			try {
				BigDecimal price = new BigDecimal(keys.get("price"));
			} catch(Exception e) {
				errorFields = new ArrayList<String>();
				errorFields.add("price");
			}
		}
		
		//if(keys==null || StringUtils.isBlank(keys.get("note"))) {
		if(keys==null) {
			errorFields = new ArrayList<String>();
			errorFields.add("note");
		}


		
		if(errorFields!=null) {
			IntegrationException ex = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
			ex.setErrorFields(errorFields);
			throw ex;
			
		}

	}

	@Override
	public List<ShippingOption> getShippingQuotes(
			ShippingQuote shippingQuote,
			List<PackageDetails> packages, BigDecimal orderTotal,
			Delivery delivery, ShippingOrigin origin, MerchantStore store,
			IntegrationConfiguration configuration, IntegrationModule module,
			ShippingConfiguration shippingConfiguration, Locale locale)
			throws IntegrationException {

		List<ShippingOption> options = new ArrayList<>();
		ShippingOption option = null;
		try {
			option = getShippingOption(shippingQuote, delivery, store, configuration);
			options.add(option);
		} catch (Exception e) {
			throw new IntegrationException(e);
		}
		return options;

	}

	@Override
	public CustomIntegrationConfiguration getCustomModuleConfiguration(
			MerchantStore store) throws IntegrationException {
		// TODO Auto-generated method stub
		return null;
	}


	private ShippingOption getShippingOption(ShippingQuote quote, Delivery delivery, MerchantStore store, IntegrationConfiguration globalShippingConfiguration) throws Exception {
		if(!globalShippingConfiguration.isActive())
			return null;

		String region = null;

		String price = globalShippingConfiguration.getIntegrationKeys().get("price");
		String note = globalShippingConfiguration.getIntegrationKeys().get("note");



		if(delivery.getZone()!=null) {
			region = delivery.getZone().getCode();
		} else {
			region = delivery.getState();
		}

		ShippingOption shippingOption = new ShippingOption();
		shippingOption.setShippingModuleCode(MODULE_CODE);
		shippingOption.setOptionCode(MODULE_CODE);
		shippingOption.setOptionId(new StringBuilder().append(MODULE_CODE).append("_").append(region).toString());

		shippingOption.setOptionPrice(productPriceUtils.getAmount(price));

		shippingOption.setOptionPriceText(productPriceUtils.getStoreFormatedAmountWithCurrency(store, productPriceUtils.getAmount(price)));
		shippingOption.setNote(note);

		List<ShippingOption> options = quote.getShippingOptions();

		if(options == null) {
			options = new ArrayList<ShippingOption>();
			quote.setShippingOptions(options);
		}

		options.add(shippingOption);
		return shippingOption;
	}



}
