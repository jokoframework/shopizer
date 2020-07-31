package com.salesmanager.shop.admin.controller.products;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.image.ProductImageService;
import com.salesmanager.core.business.services.catalog.product.manufacturer.ManufacturerService;
import com.salesmanager.core.business.services.catalog.product.type.ProductTypeService;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.business.utils.CoreConfiguration;
import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.business.utils.ajax.AjaxPageableResponse;
import com.salesmanager.core.business.utils.ajax.AjaxResponse;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.catalog.product.image.ProductImageDescription;
import com.salesmanager.core.model.catalog.product.in.ProductImportHeader;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.core.model.catalog.product.price.ProductPriceDescription;
import com.salesmanager.core.model.catalog.product.relationship.ProductRelationship;
import com.salesmanager.core.model.catalog.product.type.ProductType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.tax.taxclass.TaxClass;
import com.salesmanager.shop.admin.in.ImportCsvHelper;
import com.salesmanager.shop.admin.in.ImportErrorProcessor;
import com.salesmanager.shop.admin.model.web.Menu;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.utils.CategoryUtils;
import com.salesmanager.shop.utils.DateUtil;
import com.salesmanager.shop.utils.LabelUtils;

@Controller
public class ProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	@Inject
	private ProductService productService;

	@Inject
	private ManufacturerService manufacturerService;

	@Inject
	private ProductTypeService productTypeService;

	@Inject
	private ProductImageService productImageService;

	@Inject
	private TaxClassService taxClassService;

	@Inject
	private ProductPriceUtils priceUtil;

	@Inject
	LabelUtils messages;

	@Inject
	private CoreConfiguration configuration;

	@Inject
	private CategoryService categoryService;

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/editProduct.html", method = RequestMethod.GET)
	public String displayProductEdit(@RequestParam("id") long productId, Model model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return displayProduct(productId, model, request, response);

	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/viewEditProduct.html", method = RequestMethod.GET)
	public String displayProductEdit(@RequestParam("sku") String sku, Model model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Language language = (Language) request.getAttribute("LANGUAGE");
		Product dbProduct = productService.getByCode(sku, language);

		long productId = -1;// non existent
		if (dbProduct != null) {
			productId = dbProduct.getId();
		}

		return displayProduct(productId, model, request, response);
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/createProduct.html", method = RequestMethod.GET)
	public String displayProductCreate(Model model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return displayProduct(null, model, request, response);

	}

	private String displayProduct(Long productId, Model model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// display menu
		setMenu(model, request);

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language) request.getAttribute("LANGUAGE");

		List<Manufacturer> manufacturers = manufacturerService.listByStore(store, language);

		List<ProductType> productTypes = productTypeService.list();

		List<TaxClass> taxClasses = taxClassService.listByStore(store);

		List<Language> languages = store.getLanguages();

		com.salesmanager.shop.admin.model.catalog.Product product = new com.salesmanager.shop.admin.model.catalog.Product();
		List<ProductDescription> descriptions = new ArrayList<ProductDescription>();

		if (productId != null && productId != 0) {// edit mode

			Product dbProduct = productService.getById(productId);

			if (dbProduct == null || dbProduct.getMerchantStore().getId().intValue() != store.getId().intValue()) {
				return "redirect:/admin/products/products.html";
			}

			product.setProduct(dbProduct);
			Set<ProductDescription> productDescriptions = dbProduct.getDescriptions();

			for (Language l : languages) {

				ProductDescription productDesc = null;
				for (ProductDescription desc : productDescriptions) {

					Language lang = desc.getLanguage();
					if (lang.getCode().equals(l.getCode())) {
						productDesc = desc;
					}

				}

				if (productDesc == null) {
					productDesc = new ProductDescription();
					productDesc.setLanguage(l);
				}

				descriptions.add(productDesc);

			}

			for (ProductImage image : dbProduct.getImages()) {
				if (image.isDefaultImage()) {
					product.setProductImage(image);
					break;
				}

			}

			ProductAvailability productAvailability = null;
			ProductPrice productPrice = null;

			Set<ProductAvailability> availabilities = dbProduct.getAvailabilities();
			if (availabilities != null && availabilities.size() > 0) {

				for (ProductAvailability availability : availabilities) {
					if (availability.getRegion()
							.equals(com.salesmanager.core.business.constants.Constants.ALL_REGIONS)) {
						productAvailability = availability;
						Set<ProductPrice> prices = availability.getPrices();
						for (ProductPrice price : prices) {
							if (price.isDefaultPrice()) {
								productPrice = price;
								product.setProductPrice(
										priceUtil.getAdminFormatedAmount(store, productPrice.getProductPriceAmount()));
							}
						}
					}
				}
			}

			if (productAvailability == null) {
				productAvailability = new ProductAvailability();
			}

			if (productPrice == null) {
				productPrice = new ProductPrice();
			}

			product.setAvailability(productAvailability);
			product.setPrice(productPrice);
			product.setDescriptions(descriptions);

			product.setDateAvailable(DateUtil.formatDate(dbProduct.getDateAvailable()));

		} else {

			for (Language l : languages) {

				ProductDescription desc = new ProductDescription();
				desc.setLanguage(l);
				descriptions.add(desc);

			}

			Product prod = new Product();

			prod.setAvailable(true);

			ProductAvailability productAvailability = new ProductAvailability();
			ProductPrice price = new ProductPrice();
			product.setPrice(price);
			product.setAvailability(productAvailability);
			product.setProduct(prod);
			product.setDescriptions(descriptions);
			product.setDateAvailable(DateUtil.formatDate(new Date()));

		}

		model.addAttribute("product", product);
		model.addAttribute("manufacturers", manufacturers);
		model.addAttribute("productTypes", productTypes);
		model.addAttribute("taxClasses", taxClasses);
		return "admin-products-edit";
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/save.html", method = RequestMethod.POST)
	public String saveProduct(
			@Valid @ModelAttribute("product") com.salesmanager.shop.admin.model.catalog.Product product,
			BindingResult result, Model model, HttpServletRequest request, Locale locale) throws Exception {

		Language language = (Language) request.getAttribute("LANGUAGE");

		// display menu
		setMenu(model, request);

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		List<Manufacturer> manufacturers = manufacturerService.listByStore(store, language);

		List<ProductType> productTypes = productTypeService.list();

		List<TaxClass> taxClasses = taxClassService.listByStore(store);

		List<Language> languages = store.getLanguages();

		model.addAttribute("manufacturers", manufacturers);
		model.addAttribute("productTypes", productTypes);
		model.addAttribute("taxClasses", taxClasses);

		boolean productAlreadyExists = false;
		if (!StringUtils.isBlank(product.getProduct().getSku())
				&& (product.getProduct().getId() == null || product.getProduct().getId().longValue() == 0)) {
			try {
				Product productByCode = productService.getByCode(product.getProduct().getSku(), language);
				productAlreadyExists = productByCode != null;

				if (productAlreadyExists)
					throw new Exception();
			} catch (Exception e) {
				ObjectError error = new ObjectError("product.sku", messages.getMessage("message.sku.exists", locale));
				result.addError(error);
			}
		}

		// validate price
		BigDecimal submitedPrice = null;
		try {
			submitedPrice = priceUtil.getAmount(product.getProductPrice());
		} catch (Exception e) {
			ObjectError error = new ObjectError("productPrice",
					messages.getMessage("NotEmpty.product.productPrice", locale));
			result.addError(error);
		}
		Date date = new Date();
		if (!StringUtils.isBlank(product.getDateAvailable())) {
			try {
				date = DateUtil.getDate(product.getDateAvailable());
				product.getAvailability().setProductDateAvailable(date);
				product.setDateAvailable(DateUtil.formatDate(date));
			} catch (Exception e) {
				ObjectError error = new ObjectError("dateAvailable",
						messages.getMessage("message.invalid.date", locale));
				result.addError(error);
			}
		}

		// validate image
		if (product.getImage() != null && !product.getImage().isEmpty()) {

			try {

				String maxHeight = configuration.getProperty("PRODUCT_IMAGE_MAX_HEIGHT_SIZE");
				String maxWidth = configuration.getProperty("PRODUCT_IMAGE_MAX_WIDTH_SIZE");
				String maxSize = configuration.getProperty("PRODUCT_IMAGE_MAX_SIZE");

				BufferedImage image = ImageIO.read(product.getImage().getInputStream());

				if (!StringUtils.isBlank(maxHeight)) {

					int maxImageHeight = Integer.parseInt(maxHeight);
					if (image.getHeight() > maxImageHeight) {
						ObjectError error = new ObjectError("image",
								messages.getMessage("message.image.height", locale) + " {" + maxHeight + "}");
						result.addError(error);
					}

				}

				if (!StringUtils.isBlank(maxWidth)) {

					int maxImageWidth = Integer.parseInt(maxWidth);
					if (image.getWidth() > maxImageWidth) {
						ObjectError error = new ObjectError("image",
								messages.getMessage("message.image.width", locale) + " {" + maxWidth + "}");
						result.addError(error);
					}

				}

				if (!StringUtils.isBlank(maxSize)) {

					int maxImageSize = Integer.parseInt(maxSize);
					if (product.getImage().getSize() > maxImageSize) {
						ObjectError error = new ObjectError("image",
								messages.getMessage("message.image.size", locale) + " {" + maxSize + "}");
						result.addError(error);
					}

				}

			} catch (Exception e) {
				LOGGER.error("Cannot validate product image", e);
			}

		}

		if (result.hasErrors()) {
			return "admin-products-edit";
		}

		Product newProduct = product.getProduct();
		ProductAvailability newProductAvailability = null;
		ProductPrice newProductPrice = null;

		Set<ProductPriceDescription> productPriceDescriptions = null;

		// get tax class
		// TaxClass taxClass = newProduct.getTaxClass();
		// TaxClass dbTaxClass = taxClassService.getById(taxClass.getId());
		Set<ProductPrice> prices = new HashSet<ProductPrice>();
		Set<ProductAvailability> availabilities = new HashSet<ProductAvailability>();

		if (product.getProduct().getId() != null && product.getProduct().getId().longValue() > 0) {

			// get actual product
			newProduct = productService.getById(product.getProduct().getId());
			if (newProduct != null && newProduct.getMerchantStore().getId().intValue() != store.getId().intValue()) {
				return "redirect:/admin/products/products.html";
			}

			// copy properties
			newProduct.setSku(product.getProduct().getSku());
			newProduct.setRefSku(product.getProduct().getRefSku());
			newProduct.setAvailable(product.getProduct().isAvailable());
			newProduct.setDateAvailable(date);
			newProduct.setManufacturer(product.getProduct().getManufacturer());
			newProduct.setType(product.getProduct().getType());
			newProduct.setProductHeight(product.getProduct().getProductHeight());
			newProduct.setProductLength(product.getProduct().getProductLength());
			newProduct.setProductWeight(product.getProduct().getProductWeight());
			newProduct.setProductWidth(product.getProduct().getProductWidth());
			newProduct.setProductVirtual(product.getProduct().isProductVirtual());
			newProduct.setProductShipeable(product.getProduct().isProductShipeable());
			newProduct.setTaxClass(product.getProduct().getTaxClass());
			newProduct.setSortOrder(product.getProduct().getSortOrder());
			newProduct.setProductAlwaysInStock(product.getProduct().isProductAlwaysInStock());

			if(product.getProduct().getProductItemWeight() != null) {
				newProduct.setProductItemWeight(product.getProduct().getProductItemWeight());
			}

			Set<ProductAvailability> avails = newProduct.getAvailabilities();
			if (avails != null && avails.size() > 0) {

				for (ProductAvailability availability : avails) {
					if (availability.getRegion()
							.equals(com.salesmanager.core.business.constants.Constants.ALL_REGIONS)) {

						newProductAvailability = availability;
						Set<ProductPrice> productPrices = availability.getPrices();

						for (ProductPrice price : productPrices) {
							if (price.isDefaultPrice()) {
								newProductPrice = price;
								newProductPrice.setProductPriceAmount(submitedPrice);
								productPriceDescriptions = price.getDescriptions();
							} else {
								prices.add(price);
							}
						}
					} else {
						availabilities.add(availability);
					}
				}
			}

			for (ProductImage image : newProduct.getImages()) {
				if (image.isDefaultImage()) {
					product.setProductImage(image);
				}
			}
		}

		if (newProductPrice == null) {
			newProductPrice = new ProductPrice();
			newProductPrice.setDefaultPrice(true);
			newProductPrice.setProductPriceAmount(submitedPrice);
		}

		if (product.getProductImage() != null && product.getProductImage().getId() == null) {
			product.setProductImage(null);
		}

		if (productPriceDescriptions == null) {
			productPriceDescriptions = new HashSet<ProductPriceDescription>();
			for (ProductDescription description : product.getDescriptions()) {
				ProductPriceDescription ppd = new ProductPriceDescription();
				ppd.setProductPrice(newProductPrice);
				ppd.setLanguage(description.getLanguage());
				ppd.setName(ProductPriceDescription.DEFAULT_PRICE_DESCRIPTION);
				productPriceDescriptions.add(ppd);
			}
			newProductPrice.setDescriptions(productPriceDescriptions);
		}

		newProduct.setMerchantStore(store);

		if (newProductAvailability == null) {
			newProductAvailability = new ProductAvailability();
		}

		newProductAvailability.setProductQuantity(product.getAvailability().getProductQuantity());
		newProductAvailability.setProductQuantityOrderMin(product.getAvailability().getProductQuantityOrderMin());
		newProductAvailability.setProductQuantityOrderMax(product.getAvailability().getProductQuantityOrderMax());
		newProductAvailability.setProduct(newProduct);
		newProductAvailability.setPrices(prices);
		availabilities.add(newProductAvailability);

		newProductPrice.setProductAvailability(newProductAvailability);
		prices.add(newProductPrice);

		newProduct.setAvailabilities(availabilities);

		Set<ProductDescription> descriptions = new HashSet<ProductDescription>();
		if (product.getDescriptions() != null && product.getDescriptions().size() > 0) {

			for (ProductDescription description : product.getDescriptions()) {
				description.setProduct(newProduct);
				descriptions.add(description);

			}
		}

		newProduct.setDescriptions(descriptions);
		product.setDateAvailable(DateUtil.formatDate(date));

		if (product.getImage() != null && !product.getImage().isEmpty()) {

			String imageName = product.getImage().getOriginalFilename();

			ProductImage productImage = new ProductImage();
			productImage.setDefaultImage(true);
			productImage.setImage(product.getImage().getInputStream());
			productImage.setProductImage(imageName);

			List<ProductImageDescription> imagesDescriptions = new ArrayList<ProductImageDescription>();

			for (Language l : languages) {

				ProductImageDescription imageDescription = new ProductImageDescription();
				imageDescription.setName(imageName);
				imageDescription.setLanguage(l);
				imageDescription.setProductImage(productImage);
				imagesDescriptions.add(imageDescription);

			}

			productImage.setDescriptions(imagesDescriptions);
			productImage.setProduct(newProduct);

			newProduct.getImages().add(productImage);

			// productService.saveOrUpdate(newProduct);

			// product displayed
			product.setProductImage(productImage);

		} // else {

		// productService.saveOrUpdate(newProduct);

		// }

		productService.create(newProduct);
		model.addAttribute("success", "success");

		return "admin-products-edit";
	}

	/**
	 * Creates a duplicate product with the same inner object graph Will ignore SKU,
	 * reviews and images
	 * 
	 * @param id
	 * @param result
	 * @param model
	 * @param request
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/product/duplicate.html", method = RequestMethod.POST)
	public String duplicateProduct(@ModelAttribute("productId") Long id, BindingResult result, Model model,
			HttpServletRequest request, Locale locale) throws Exception {

		Language language = (Language) request.getAttribute("LANGUAGE");

		// display menu
		setMenu(model, request);

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		List<Manufacturer> manufacturers = manufacturerService.listByStore(store, language);
		List<ProductType> productTypes = productTypeService.list();
		List<TaxClass> taxClasses = taxClassService.listByStore(store);

		model.addAttribute("manufacturers", manufacturers);
		model.addAttribute("productTypes", productTypes);
		model.addAttribute("taxClasses", taxClasses);

		Product dbProduct = productService.getById(id);
		Product newProduct = new Product();

		if (dbProduct == null || dbProduct.getMerchantStore().getId().intValue() != store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}

		// Make a copy of the product
		com.salesmanager.shop.admin.model.catalog.Product product = new com.salesmanager.shop.admin.model.catalog.Product();

		Set<ProductAvailability> availabilities = new HashSet<ProductAvailability>();
		// availability - price
		for (ProductAvailability pAvailability : dbProduct.getAvailabilities()) {

			ProductAvailability availability = new ProductAvailability();
			availability.setProductDateAvailable(pAvailability.getProductDateAvailable());
			availability.setProductIsAlwaysFreeShipping(pAvailability.getProductIsAlwaysFreeShipping());
			availability.setProductQuantity(pAvailability.getProductQuantity());
			availability.setProductQuantityOrderMax(pAvailability.getProductQuantityOrderMax());
			availability.setProductQuantityOrderMin(pAvailability.getProductQuantityOrderMin());
			availability.setProductStatus(pAvailability.getProductStatus());
			availability.setRegion(pAvailability.getRegion());
			availability.setRegionVariant(pAvailability.getRegionVariant());
			availability.setProduct(newProduct);

			
			Set<ProductPrice> prices = pAvailability.getPrices();
			for (ProductPrice pPrice : prices) {

				ProductPrice price = new ProductPrice();
				price.setDefaultPrice(pPrice.isDefaultPrice());
				price.setProductPriceAmount(pPrice.getProductPriceAmount());
				price.setProductAvailability(availability);
				price.setProductPriceSpecialAmount(pPrice.getProductPriceSpecialAmount());
				price.setProductPriceSpecialEndDate(pPrice.getProductPriceSpecialEndDate());
				price.setProductPriceSpecialStartDate(pPrice.getProductPriceSpecialStartDate());
				price.setProductPriceType(pPrice.getProductPriceType());

				Set<ProductPriceDescription> priceDescriptions = new HashSet<ProductPriceDescription>();
				// price descriptions
				for (ProductPriceDescription pPriceDescription : pPrice.getDescriptions()) {

					ProductPriceDescription productPriceDescription = new ProductPriceDescription();
					productPriceDescription.setAuditSection(pPriceDescription.getAuditSection());
					productPriceDescription.setDescription(pPriceDescription.getDescription());
					productPriceDescription.setName(pPriceDescription.getName());
					productPriceDescription.setLanguage(pPriceDescription.getLanguage());
					productPriceDescription.setProductPrice(price);
					priceDescriptions.add(productPriceDescription);

				}
				price.setDescriptions(priceDescriptions);
				if (price.isDefaultPrice()) {
					product.setPrice(price);
					product.setProductPrice(priceUtil.getAdminFormatedAmount(store, price.getProductPriceAmount()));
				}

				availability.getPrices().add(price);
			}

			if (availability.getRegion().equals(com.salesmanager.core.business.constants.Constants.ALL_REGIONS)) {
				product.setAvailability(availability);
			}

			availabilities.add(availability);
		}

		newProduct.setAvailabilities(availabilities);

		// attributes
		Set<ProductAttribute> attributes = new HashSet<ProductAttribute>();
		for (ProductAttribute pAttribute : dbProduct.getAttributes()) {

			ProductAttribute attribute = new ProductAttribute();
			attribute.setAttributeDefault(pAttribute.getAttributeDefault());
			attribute.setAttributeDiscounted(pAttribute.getAttributeDiscounted());
			attribute.setAttributeDisplayOnly(pAttribute.getAttributeDisplayOnly());
			attribute.setAttributeRequired(pAttribute.getAttributeRequired());
			attribute.setProductAttributePrice(pAttribute.getProductAttributePrice());
			attribute.setProductAttributeIsFree(pAttribute.getProductAttributeIsFree());
			attribute.setProductAttributeWeight(pAttribute.getProductAttributeWeight());
			attribute.setProductOption(pAttribute.getProductOption());
			attribute.setProductOptionSortOrder(pAttribute.getProductOptionSortOrder());
			attribute.setProductOptionValue(pAttribute.getProductOptionValue());
			attribute.setProduct(newProduct);
			attributes.add(attribute);

		}
		newProduct.setAttributes(attributes);

		// relationships
		Set<ProductRelationship> relationships = new HashSet<ProductRelationship>();
		for (ProductRelationship pRelationship : dbProduct.getRelationships()) {

			ProductRelationship relationship = new ProductRelationship();
			relationship.setActive(pRelationship.isActive());
			relationship.setCode(pRelationship.getCode());
			relationship.setRelatedProduct(pRelationship.getRelatedProduct());
			relationship.setStore(store);
			relationship.setProduct(newProduct);
			relationships.add(relationship);

		}

		newProduct.setRelationships(relationships);

		// product description
		Set<ProductDescription> descsset = new HashSet<ProductDescription>();
		List<ProductDescription> desclist = new ArrayList<ProductDescription>();
		Set<ProductDescription> descriptions = dbProduct.getDescriptions();
		for (ProductDescription pDescription : descriptions) {

			ProductDescription description = new ProductDescription();
			description.setAuditSection(pDescription.getAuditSection());
			description.setName(pDescription.getName());
			description.setDescription(pDescription.getDescription());
			description.setLanguage(pDescription.getLanguage());
			description.setMetatagDescription(pDescription.getMetatagDescription());
			description.setMetatagKeywords(pDescription.getMetatagKeywords());
			description.setMetatagTitle(pDescription.getMetatagTitle());
			description.setProduct(newProduct);
			descsset.add(description);
			desclist.add(description);
		}
		newProduct.setDescriptions(descsset);
		product.setDescriptions(desclist);

		// product
		newProduct.setAuditSection(dbProduct.getAuditSection());
		newProduct.setAvailable(dbProduct.isAvailable());

		// copy
		// newProduct.setCategories(dbProduct.getCategories());
		newProduct.setDateAvailable(dbProduct.getDateAvailable());
		newProduct.setManufacturer(dbProduct.getManufacturer());
		newProduct.setMerchantStore(store);
		newProduct.setProductHeight(dbProduct.getProductHeight());
		newProduct.setProductIsFree(dbProduct.getProductIsFree());
		newProduct.setProductLength(dbProduct.getProductLength());
		newProduct.setProductOrdered(dbProduct.getProductOrdered());
		newProduct.setProductWeight(dbProduct.getProductWeight());
		newProduct.setProductWidth(dbProduct.getProductWidth());
		newProduct.setSortOrder(dbProduct.getSortOrder());
		newProduct.setTaxClass(dbProduct.getTaxClass());
		newProduct.setType(dbProduct.getType());
		newProduct.setSku(UUID.randomUUID().toString().replace("-", ""));
		newProduct.setProductVirtual(dbProduct.isProductVirtual());
		newProduct.setProductShipeable(dbProduct.isProductShipeable());
		newProduct.setProductItemWeight(dbProduct.getProductItemWeight());

		productService.update(newProduct);

		Set<Category> categories = dbProduct.getCategories();
		for (Category category : categories) {
			Category categoryCopy = categoryService.getById(category.getId(), store.getId());
			newProduct.getCategories().add(categoryCopy);
			productService.update(newProduct);
		}

		product.setProduct(newProduct);
		model.addAttribute("product", product);
		model.addAttribute("success", "success");

		return "redirect:/admin/products/editProduct.html?id=" + newProduct.getId();
	}

	/**
	 * Removes a product image based on the productimage id
	 * 
	 * @param request
	 * @param response
	 * @param locale
	 * @return
	 */
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/product/removeImage.html")
	public @ResponseBody ResponseEntity<String> removeImage(HttpServletRequest request, HttpServletResponse response,
			Locale locale) {
		String iid = request.getParameter("imageId");

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		AjaxResponse resp = new AjaxResponse();

		try {

			Long id = Long.parseLong(iid);
			ProductImage productImage = productImageService.getById(id);
			if (productImage == null
					|| productImage.getProduct().getMerchantStore().getId().intValue() != store.getId().intValue()) {

				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);

			} else {

				productImageService.removeProductImage(productImage);
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

			}

		} catch (Exception e) {
			LOGGER.error("Error while deleting product", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}

		String returnString = resp.toJSONString();
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
	}

	/**
	 * List all categories and let the merchant associate the product to a category
	 * 
	 * @param productId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/displayProductToCategories.html", method = RequestMethod.GET)
	public String displayAddProductToCategories(@RequestParam("id") long productId, Model model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		setMenu(model, request);
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language) request.getAttribute("LANGUAGE");

		// get the product and validate it belongs to the current merchant
		Product product = productService.getById(productId);

		if (product == null) {
			return "redirect:/admin/products/products.html";
		}

		if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}

		// get parent categories
		List<Category> categories = categoryService.listByStore(store, language);
		List<com.salesmanager.shop.admin.model.catalog.Category> readableCategories = CategoryUtils
				.readableCategoryListConverter(categories, language);

		model.addAttribute("product", product);
		model.addAttribute("categories", readableCategories);
		return "catalogue-product-categories";

	}

	/**
	 * List all categories associated to a Product
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/product-categories/paging.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> pageProductCategories(HttpServletRequest request,
			HttpServletResponse response) {

		String sProductId = request.getParameter("productId");
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		AjaxResponse resp = new AjaxResponse();

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

		Long productId;
		Product product = null;

		try {
			productId = Long.parseLong(sProductId);
		} catch (Exception e) {
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorString("Product id is not valid");
			String returnString = resp.toJSONString();
			return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
		}

		try {

			product = productService.getById(productId);

			if (product == null) {
				resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
				resp.setErrorString("Product id is not valid");
				String returnString = resp.toJSONString();
				return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
			}

			if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
				resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
				resp.setErrorString("Product id is not valid");
				String returnString = resp.toJSONString();
				return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
			}

			Language language = (Language) request.getAttribute("LANGUAGE");

			Set<Category> categories = product.getCategories();

			for (Category category : categories) {
				Map entry = new HashMap();
				entry.put("categoryId", category.getId());

				Set<CategoryDescription> descriptions = category.getDescriptions();
				String categoryName = category.getDescriptions().iterator().next().getName();
				for (CategoryDescription description : descriptions) {
					if (description.getLanguage().getCode().equals(language.getCode())) {
						categoryName = description.getName();
					}
				}
				entry.put("name", categoryName);
				resp.addDataEntry(entry);
			}

			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_SUCCESS);

		} catch (Exception e) {
			LOGGER.error("Error while paging products", e);
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}

		String returnString = resp.toJSONString();
		return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);

	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/product-categories/remove.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> deleteProductFromCategory(HttpServletRequest request,
			HttpServletResponse response, Locale locale) {
		String sCategoryid = request.getParameter("categoryId");
		String sProductId = request.getParameter("productId");

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		AjaxResponse resp = new AjaxResponse();

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

		try {

			Long categoryId = Long.parseLong(sCategoryid);
			Long productId = Long.parseLong(sProductId);

			Category category = categoryService.getById(categoryId, store.getId());
			Product product = productService.getById(productId);

			if (category == null || category.getMerchantStore().getId() != store.getId()) {

				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
				String returnString = resp.toJSONString();
				return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
			}

			if (product == null || product.getMerchantStore().getId() != store.getId()) {

				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
				String returnString = resp.toJSONString();
				return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
			}

			product.getCategories().remove(category);
			productService.update(product);

			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		} catch (Exception e) {
			LOGGER.error("Error while deleting category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}

		String returnString = resp.toJSONString();

		return new ResponseEntity<String>(returnString, httpHeaders, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/products/addProductToCategories.html", method = RequestMethod.POST)
	public String addProductToCategory(@RequestParam("productId") long productId, @RequestParam("id") long categoryId,
			Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		setMenu(model, request);
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language) request.getAttribute("LANGUAGE");

		// get the product and validate it belongs to the current merchant
		Product product = productService.getById(productId);

		if (product == null) {
			return "redirect:/admin/products/products.html";
		}

		if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}

		//get parent categories
		List<Category> categories = categoryService.listByStore(store,language);
		
		Category category = categoryService.getById(categoryId, language.getId());
		
		if(category==null) {
			return "redirect:/admin/products/products.html";
		}

		if (category.getMerchantStore().getId().intValue() != store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}

		product.getCategories().add(category);

		productService.update(product);

		List<com.salesmanager.shop.admin.model.catalog.Category> readableCategories = CategoryUtils
				.readableCategoryListConverter(categories, language);

		model.addAttribute("product", product);
		model.addAttribute("categories", readableCategories);

		return "catalogue-product-categories";

	}

	private void setMenu(Model model, HttpServletRequest request) throws Exception {

		// display menu
		Map<String, String> activeMenus = new HashMap<String, String>();
		activeMenus.put("catalogue", "catalogue");
		activeMenus.put("catalogue-products", "catalogue-products");

		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>) request.getAttribute("MENUMAP");

		Menu currentMenu = (Menu) menus.get("catalogue");
		model.addAttribute("currentMenu", currentMenu);
		model.addAttribute("activeMenus", activeMenus);
		//
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@GetMapping("/admin/products/importProductCsv.html")
	public String importProductCsv(Model model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// display menu
		setMenu(model, request);
		model.addAttribute("product", new com.salesmanager.shop.admin.model.catalog.ImportCsvFile());
		return "admin-products-import-csv";

	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@PostMapping("/admin/products/saveCsv.html")
	public String saveCsvProduct(
			@Valid @ModelAttribute("product") com.salesmanager.shop.admin.model.catalog.ImportCsvFile productFile,
			BindingResult result, Model model, HttpServletRequest request, Locale locale) throws Exception {
		Language language = (Language) request.getAttribute("LANGUAGE");
		// display menu
		setMenu(model, request);

		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		
		ImportErrorProcessor errorProcessor = ImportErrorProcessor
			     .builder()
			     .withBindingResult(result)
			     .withLocale(locale)
			     .withMessages(messages)
			     .withObjectName("product.file")
			     .build();

		// validate csv
		if (productFile.getFile() == null || productFile.getFile().isEmpty()) {
			errorProcessor.addNewError("NotEmpty");
		}else if (!ImportCsvHelper.hasCSVFormat(productFile.getFile())) {
			errorProcessor.addNewError("message.csv.invalid.extension");
		}

		if (result.hasErrors()) {
			return "admin-products-import-csv";
		}

		return processRecords(model, store, productFile.getFile().getInputStream(), language, errorProcessor);
	}
	
	public String processRecords(Model model, MerchantStore store, InputStream is, Language language, ImportErrorProcessor errorProcessor) throws Exception{ 
		
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	        CSVParser csvParser = new CSVParser(fileReader,
	            CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())){
	
	     Iterable<CSVRecord> csvRecords = csvParser.getRecords();
	     
	     int rowNumber = 0;
	      for (CSVRecord csvRecord : csvRecords) {
	    	  rowNumber++;
		    validateRequiredFields(csvRecord, errorProcessor);
		    if (errorProcessor.hasErrors()) {
				return "admin-products-import-csv";
			}	
		    String sku = csvRecord.get(ProductImportHeader.SKU.name());
		    com.salesmanager.shop.admin.model.catalog.Product product = getProductToProcessBySkuAndLanguage(sku, language );
		    buildProductDataFromCsvRecord(store, product, csvRecord, errorProcessor);
		    buildProductCategory(store, product, csvRecord, errorProcessor);
		    buildProductAvailabilityFromCsvRecord(store, product, csvRecord, errorProcessor);
		    buildProductDescriptionFromCsvRecord(product, language, csvRecord);
		    buildProductPriceFromCsvRecord(product, language, csvRecord, errorProcessor);
		    buildProductImages(product, language, csvRecord, errorProcessor);
		    if (errorProcessor.hasErrors()) {
		    	addRowNumberAndInfoToErrors(rowNumber, errorProcessor);
				return "admin-products-import-csv";
			}
			productService.create(product.getProduct());
	      }
	      
	      model.addAttribute("success", "success");

	      return "admin-products-import-csv";
	
	    } catch (IOException e) {
	    	errorProcessor.addNewError("message.csv.error.read.file");
			return "admin-products-import-csv";
	    }
	  }
	
	private void addRowNumberAndInfoToErrors(int rowNumber, ImportErrorProcessor errorProcessor) {
		errorProcessor.addNewError("message.csv.error.row.number", rowNumber);
		errorProcessor.addNewError("message.csv.error.info");
	}
	
	 public void validateRequiredFields(CSVRecord csvRecord, ImportErrorProcessor errorProcessor) {
		 for(ProductImportHeader header : ProductImportHeader.values()) {
			 try{
				 String record = csvRecord.get(header.name());
				 if(header.isRequired() && (record == null || record.isEmpty())) {
					 errorProcessor.addNewError("message.csv.filed.required", header.name());
				 }
			 }catch(IllegalArgumentException e) {
				if (header.isRequired()) {
					errorProcessor.addNewError("message.csv.filed.required", header.name());
				}
			}	
		 }
	  }
	
	private com.salesmanager.shop.admin.model.catalog.Product getProductToProcessBySkuAndLanguage(String sku, Language language) {
		com.salesmanager.shop.admin.model.catalog.Product  productToProcess = new com.salesmanager.shop.admin.model.catalog.Product();
		  Product existingProduct = productService.getByCode(sku,language);
		  if(existingProduct==null) {
			  existingProduct = new Product();
			  existingProduct.setSku(sku);
		  }
		  productToProcess.setProduct(existingProduct);
		  return productToProcess;
	  }	
	  
	
	 private void buildProductDataFromCsvRecord(MerchantStore store, com.salesmanager.shop.admin.model.catalog.Product product, CSVRecord csvRecord, ImportErrorProcessor errorProcessor){
		  Boolean available = Boolean.parseBoolean(csvRecord.get(ProductImportHeader.AVAILABLE.name()));
		  Boolean preOrder = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord,ProductImportHeader.PREORDER, Boolean.FALSE);
		  Boolean productIsFree = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord,ProductImportHeader.PRODUCT_FREE, Boolean.FALSE);		  
		  Boolean shipeable = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord,ProductImportHeader.PRODUCT_SHIP, Boolean.FALSE);
		  Boolean virtual = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord,ProductImportHeader.PRODUCT_VIRTUAL, Boolean.FALSE);
		  String manufacturerCode = csvRecord.get(ProductImportHeader.MANUFACTURER);
		  String refSku = csvRecord.get(ProductImportHeader.REF_SKU);
		  Double productItemWeight = ImportCsvHelper.getDoubleOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.PRODUCT_ITEM_WEIGHT, 0.0, errorProcessor);
		  BigDecimal productLength = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.PRODUCT_LENGTH, null, errorProcessor);
		  BigDecimal productWidth = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.PRODUCT_WIDTH, null, errorProcessor);
		  BigDecimal productHeight = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.PRODUCT_HEIGHT, null, errorProcessor);
		  BigDecimal productWeight = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.PRODUCT_WEIGHT, null, errorProcessor);
		  
		  com.salesmanager.core.model.catalog.product.Product innerProductData = product.getProduct();
		  innerProductData.setAvailable(available);
		  innerProductData.setPreOrder(preOrder);
		  innerProductData.setProductIsFree(productIsFree);
		  innerProductData.setProductShipeable(shipeable);
		  innerProductData.setProductVirtual(virtual);
		  Manufacturer manufacturer = getManufacturerIfExists(store, manufacturerCode, errorProcessor);
		  innerProductData.setManufacturer(manufacturer);
		  innerProductData.setRefSku(refSku);
		  innerProductData.setMerchantStore(store);
		  innerProductData.setProductItemWeight(productItemWeight);
		  innerProductData.setProductLength(productLength);
		  innerProductData.setProductWidth(productWidth);
		  innerProductData.setProductHeight(productHeight);
		  innerProductData.setProductWeight(productWeight);

		  //default values, shipeable tax and type
		  innerProductData.setProductShipeable(true);
		  innerProductData.setProductAlwaysInStock(true);

		  try {
			  TaxClass defaultTaxClass = taxClassService.getByCode(TaxClass.DEFAULT_TAX_CLASS);
			  ProductType generalType = productTypeService.getProductType(ProductType.GENERAL_TYPE);  
			  innerProductData.setTaxClass(defaultTaxClass);
			  innerProductData.setType(generalType);
		  }catch(ServiceException e) {
			  LOGGER.warn("Error setting defalt value for procuct tax and/or type", e);
		  }
		  
		  product.setProduct(innerProductData);
		  
	  }
	 
	  public com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer getManufacturerIfExists(MerchantStore store, String manufacturerCode, ImportErrorProcessor errorProcessor) {
		  if(manufacturerCode== null ) {
			  return null;
		  }
		  com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer manufacturer = manufacturerService.getByCode(store, manufacturerCode);
		  if(manufacturer == null) {
			  errorProcessor.addNewError("message.csv.unknown.manufacturer", manufacturerCode);
		  }
		  return manufacturer;
	  }
	  
	  private void buildProductCategory(MerchantStore store, com.salesmanager.shop.admin.model.catalog.Product product, CSVRecord csvRecord, ImportErrorProcessor errorProcessor) {
		  try {
			  String categoryCode = csvRecord.get(ProductImportHeader.CATEGORY);
			  if(categoryCode==null || "".equals(categoryCode)) return;
			  Category categoryReceived = categoryService.getByCode(store, categoryCode);
			  if(categoryReceived == null) {
				 errorProcessor.addNewError("category.unknown", categoryCode);
				 return;
			  }
			  Set<Category> categories = product.getProduct().getCategories();
			  for(Category c : categories) {
				  if(c.equals(categoryReceived)) {
					  return;
				  }
			  }
			  categories.add(categoryReceived);
			  product.getProduct().setCategories(categories);
		  }catch(com.salesmanager.core.business.exception.ServiceException e) {
			  errorProcessor.addNewError("message.csv.category.process.error");
		  }
		  
	  }
	  
	  private ProductAvailability buildProductAvailabilityFromCsvRecord(MerchantStore store, com.salesmanager.shop.admin.model.catalog.Product product, CSVRecord csvRecord, ImportErrorProcessor errorProcessor){		  
		   Set<ProductAvailability> availabilities = new HashSet<>();
		  	ProductAvailability productAvailability = product.getAvailability();
			if(productAvailability==null) {
				productAvailability = new ProductAvailability();
			}
			productAvailability.setMerchantStore(store);
			Boolean productIsAlwaysFreeShipping = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord, ProductImportHeader.FREE_SHIPPING, Boolean.FALSE);
			Double quantity = ImportCsvHelper.getDoubleOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord,ProductImportHeader.QUANTITY, 0.0, errorProcessor);
			Integer productQuantityOrderMin = ImportCsvHelper.getIntegerOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.QUANTITY_ORD_MIN, 1, errorProcessor);
			Integer productQuantityOrderMax = ImportCsvHelper.getIntegerOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.QUANTITY_ORD_MAX, 0, errorProcessor);
			
			productAvailability.setProductIsAlwaysFreeShipping(productIsAlwaysFreeShipping);
			productAvailability.setProductQuantity(quantity);
			productAvailability.setProductQuantityOrderMin(productQuantityOrderMin);
			productAvailability.setProductQuantityOrderMax(productQuantityOrderMax);
			productAvailability.setProduct(product.getProduct());
			product.setAvailability(productAvailability);
			availabilities.add(productAvailability);
			product.getProduct().setAvailabilities(availabilities);
			
			return productAvailability;
			
	  }
	  
	  private com.salesmanager.shop.admin.model.catalog.Product buildProductDescriptionFromCsvRecord(com.salesmanager.shop.admin.model.catalog.Product product, Language language, CSVRecord csvRecord){
			Set<ProductDescription> productDescriptions = product.getProduct().getDescriptions();
			ProductDescription productDescription = new ProductDescription();
			if(productDescriptions!=null && !productDescriptions.isEmpty()) {
				for(ProductDescription description: productDescriptions) {
					if(description.getLanguage().equals(language)) {
						productDescriptions.remove(description);
						productDescription = description;
					}
				}
			}else {
				productDescriptions = new HashSet<>();
			}
			
			String name = csvRecord.get(ProductImportHeader.NAME.name());
			productDescription.setLanguage(language);
			productDescription.setName(name);
			productDescription.setDescription("<p>"+csvRecord.get(ProductImportHeader.DESCRIPTION.name())+"</p>");
			productDescription.setProduct(product.getProduct());
			productDescription.setSeUrl(name.toLowerCase().replaceAll("[^a-z_0-9\\s]", "").replaceAll("\\s", "-"));
			productDescription.setMetatagDescription(name.toLowerCase());
			productDescription.setMetatagTitle(name.toLowerCase());
			String metatagsField  = csvRecord.get(ProductImportHeader.META_TAGS.name());
			productDescription.setMetatagKeywords(metatagsField);
			productDescription.setProductHighlight(name);
			productDescriptions.add(productDescription);
			product.getProduct().setDescriptions(productDescriptions);
			return product;
	  }
	  
	  private void buildProductPriceFromCsvRecord(com.salesmanager.shop.admin.model.catalog.Product product, Language language, CSVRecord csvRecord, ImportErrorProcessor errorProcessor) {
		Set<ProductPrice>prices=product.getAvailability().getPrices()== null ? new HashSet<>() : product.getAvailability().getPrices();
		Set<ProductPrice>newPrices = new HashSet<>();
		Set<ProductPriceDescription>productPriceDescriptions=new HashSet<>();
		ProductPriceDescription priceDescription = new ProductPriceDescription();
		ProductPrice newProductPrice = new ProductPrice();
		newProductPrice.setDefaultPrice(true);
		newProductPrice.setProductAvailability(product.getAvailability());
		//validate price
		BigDecimal submitedPrice = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.PRODUCT_PRICE_AMOUNT, BigDecimal.ZERO, errorProcessor);
		BigDecimal specialAmount = ImportCsvHelper.getBigDecimalOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.PRODUCT_PRICE_SPECIAL_AMOUNT, null, errorProcessor);
		Date specialAmountStartAt =  ImportCsvHelper.getDateOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.PRODUCT_PRICE_SPECIAL_ST_DATE, null, errorProcessor);
		Date specialAmountEndsAt =  ImportCsvHelper.getDateOrDefaultFromRecordWithHeaderNameAndProcessErrors(csvRecord, ProductImportHeader.PRODUCT_PRICE_SPECIAL_END_DATE, null, errorProcessor);
		
		newProductPrice.setProductPriceAmount(submitedPrice);
		newProductPrice.setProductPriceSpecialAmount(specialAmount);
		newProductPrice.setProductPriceSpecialStartDate(specialAmountStartAt );
		newProductPrice.setProductPriceSpecialStartDate(specialAmountEndsAt);
		
		for(ProductPrice price : prices) {
			if(price.isDefaultPrice()) {
				newProductPrice.setId(price.getId());
				productPriceDescriptions = price.getDescriptions();
			}else {
				newPrices.add(price);
			}
		}
		
		newPrices.add(newProductPrice);
		
		product.getAvailability().setPrices(newPrices);
		
		for(ProductPriceDescription ppd : productPriceDescriptions) {
			if(ppd.getLanguage().equals(language)) {
				priceDescription = ppd; 
				productPriceDescriptions.remove(ppd);
			}
		}
		priceDescription.setLanguage(language);
		priceDescription.setName(ProductPriceDescription.DEFAULT_PRICE_DESCRIPTION);
		priceDescription.setProductPrice(newProductPrice);
		productPriceDescriptions.add(priceDescription);
		newProductPrice.setDescriptions(productPriceDescriptions);
	  }
	  
	  private void buildProductImages(com.salesmanager.shop.admin.model.catalog.Product product, Language language, CSVRecord csvRecord, ImportErrorProcessor errorProcessor) {
		  if(product.getProduct().getImages()!=null && !product.getProduct().getImages().isEmpty()) {
			  return;
		  }
		  String sku = csvRecord.get(ProductImportHeader.SKU);
		  String imagesFolder = configuration.getProperty("PRODUCT_IMAGES_FOLDER");
		  //process only the first image
		  try (Stream<Path> paths = Files.walk(Paths.get(imagesFolder))) {
 			    		
			    List<Path> pathFiles = paths.filter(file -> file.toFile().isFile() && 
			    		file.getFileName().toString().split(" ")[0].equals(sku))
			    		.collect(Collectors.toList());
			    		

		    	for(Path pathFile : pathFiles) {
		    		MultipartFile multipartFile = pathToMultipartFile(pathFile);
		    		
					    	
			    	product.setImage(multipartFile);
			    	
			    	// validate image
					validateImageProperties(multipartFile, errorProcessor);
						
					String imageName = product.getImage().getOriginalFilename();

					ProductImage productImage = new ProductImage();
					productImage.setDefaultImage(isDefault(pathFiles, pathFile));
					productImage.setImage(product.getImage().getInputStream());
					productImage.setProductImage(imageName);

					List<ProductImageDescription> imagesDescriptions = new ArrayList<>();

					ProductImageDescription imageDescription = new ProductImageDescription();
					imageDescription.setName(imageName);
					imageDescription.setLanguage(language);
					imageDescription.setProductImage(productImage);
					imagesDescriptions.add(imageDescription);
					
					Product innerProduct = product.getProduct();

					productImage.setDescriptions(imagesDescriptions);
					productImage.setProduct(innerProduct);

					innerProduct.getImages().add(productImage);

					// product displayed
					product.setProductImage(productImage);	
			    
		    	}
		  }catch(NoSuchFileException e) {
			  LOGGER.warn("Image folder "+imagesFolder+" does not exists", e);
			  errorProcessor.addNewError("message.csv.product.images.folder.not.exists", imagesFolder);
		  }catch(Exception e) {
			  LOGGER.warn("Error trying to upload images", e);
			  errorProcessor.addNewError("message.csv.product.images.error");
		  } 
	  }
	  
	  private boolean isDefault(List<Path> pathFiles , Path current) {
		  if(pathFiles.size()!=1) {
  			String[] fileNameParts = current.getFileName().toString().split("_");
  			if(fileNameParts.length == 1 || fileNameParts[1].equals("1")) {
  				return true;
  			}
		  }
		  return false;
	  }
	  
	  private MultipartFile pathToMultipartFile(Path pathFile) throws IOException {
		  File file = pathFile.toFile();
	    	try(FileInputStream input = new FileInputStream(file)){
		    	String mimeType = Files.probeContentType(pathFile);
	    		 return new MockMultipartFile("file", file.getName(), mimeType, IOUtils.toByteArray(input));
	    	}
	  }  
	  
	  private void validateImageProperties(MultipartFile multipartFile, ImportErrorProcessor errorProcessor) {
		  try {

				String maxHeight = configuration.getProperty("PRODUCT_IMAGE_MAX_HEIGHT_SIZE");
				String maxWidth = configuration.getProperty("PRODUCT_IMAGE_MAX_WIDTH_SIZE");
				String maxSize = configuration.getProperty("PRODUCT_IMAGE_MAX_SIZE");

				BufferedImage image = ImageIO.read(multipartFile.getInputStream());

				if (!StringUtils.isBlank(maxHeight)) {
					int maxImageHeight = Integer.parseInt(maxHeight);
					if (image.getHeight() > maxImageHeight) {
						errorProcessor.addNewErrorWithExtra("message.image.height", " {" + maxHeight + "}");
					}
				}

				if (!StringUtils.isBlank(maxWidth)) {
					int maxImageWidth = Integer.parseInt(maxWidth);
					if (image.getWidth() > maxImageWidth) {
						errorProcessor.addNewErrorWithExtra("message.image.width", " {" + maxWidth + "}");
					}
				}

				if (!StringUtils.isBlank(maxSize)) {
					int maxImageSize = Integer.parseInt(maxSize);
					if (multipartFile.getSize() > maxImageSize) {
						errorProcessor.addNewErrorWithExtra("message.image.size", " {" + maxSize + "}");
					}
				}

			} catch (Exception e) {
				LOGGER.error("Cannot validate product image", e);
			}
	  }
}