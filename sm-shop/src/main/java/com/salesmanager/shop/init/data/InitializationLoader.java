package com.salesmanager.shop.init.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.constants.SystemConstants;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.init.InitializationDatabase;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.business.services.system.SystemConfigurationService;
import com.salesmanager.core.business.services.user.GroupService;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.business.utils.CoreConfiguration;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantConfig;
import com.salesmanager.core.model.system.SystemConfiguration;
import com.salesmanager.shop.admin.security.WebUserServices;
import com.salesmanager.shop.constants.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


@Component
public class InitializationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationLoader.class);
    @Inject
    protected PermissionService permissionService;
    @Inject
    protected GroupService groupService;
    @Inject
    protected MerchantStoreService merchantService;
    @Inject
    private MerchantConfigurationService merchantConfigurationService;
    @Inject
    private InitializationDatabase initializationDatabase;
    @Inject
    private InitData initData;
    @Inject
    private SystemConfigurationService systemConfigurationService;
    @Inject
    private WebUserServices userDetailsService;
    @Inject
    private CoreConfiguration configuration;
    @Inject
    private ObjectMapper jacksonObjectMapper;

    @Inject
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {

        try {

            if (initializationDatabase.isEmpty()) {

                //All default data to be created

                LOGGER.info(String.format("%s : Shopizer database is empty, populate it....", "sm-shop"));

                initializationDatabase.populate("sm-shop");

                MerchantStore store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);

                userDetailsService.createDefaultAdmin();
                MerchantConfig config = new MerchantConfig();
                config.setAllowPurchaseItems(true);
                config.setDisplayAddToCartOnFeaturedItems(true);
                config.setAllowRatingAndReviews(false);

                merchantConfigurationService.saveMerchantConfig(config, store);

                loadData();

            }

        } catch (Exception e) {
            LOGGER.error("Error in the init method", e);
        }


    }

    private void loadData() throws ServiceException {

        String loadTestData = configuration.getProperty(ApplicationConstants.POPULATE_TEST_DATA);
        boolean loadData = !StringUtils.isBlank(loadTestData) && loadTestData.equals(SystemConstants.CONFIG_VALUE_TRUE);

        //deprecated. data is now included in h2 default database file
        if (loadData) {

            SystemConfiguration configuration = systemConfigurationService.getByKey(ApplicationConstants.TEST_DATA_LOADED);

            if (configuration != null) {
                if (configuration.getKey().equals(ApplicationConstants.TEST_DATA_LOADED)) {
                    if (configuration.getValue().equals(SystemConstants.CONFIG_VALUE_TRUE)) {
                        return;
                    }
                }
            }

            initData.initInitialData();

            configuration = new SystemConfiguration();
            configuration.getAuditSection().setModifiedBy(SystemConstants.SYSTEM_USER);
            configuration.setKey(ApplicationConstants.TEST_DATA_LOADED);
            configuration.setValue(SystemConstants.CONFIG_VALUE_TRUE);
            systemConfigurationService.create(configuration);


        }
    }


}
