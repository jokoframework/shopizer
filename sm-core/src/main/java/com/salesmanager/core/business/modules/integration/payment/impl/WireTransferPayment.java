package com.salesmanager.core.business.modules.integration.payment.impl;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WireTransferPayment extends MoneyOrderPayment {

    @Override
    public void validateModuleConfiguration(IntegrationConfiguration integrationConfiguration, MerchantStore store) throws IntegrationException {
        List<String> errorFields = null;
        Map<String, String> keys = integrationConfiguration.getIntegrationKeys();

        if(keys==null || StringUtils.isBlank(keys.get("details"))) {
            errorFields = new ArrayList<String>();
            errorFields.add("details");
        }

        if(errorFields != null) {
            IntegrationException ex = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
            ex.setErrorFields(errorFields);
            throw ex;
        }

        return;
    }

    @Override
    public Transaction authorizeAndCapture(MerchantStore store, Customer customer, List<ShoppingCartItem> items, BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module) throws IntegrationException {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionDate(new Date());
        transaction.setTransactionType(TransactionType.AUTHORIZECAPTURE);
        transaction.setPaymentType(PaymentType.TRANSFER);


        return transaction;
    }
}
