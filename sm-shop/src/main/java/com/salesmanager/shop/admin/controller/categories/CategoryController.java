package com.salesmanager.shop.admin.controller.categories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.utils.ajax.AjaxResponse;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.category.in.CategoryImportHeader;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.admin.in.ImportCsvHelper;
import com.salesmanager.shop.admin.in.ImportErrorProcessor;
import com.salesmanager.shop.admin.model.web.Menu;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.utils.CategoryUtils;
import com.salesmanager.shop.utils.LabelUtils;

@Controller
public class CategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

	@Inject
	LanguageService languageService;

	@Inject
	CategoryService categoryService;

	@Inject
	CountryService countryService;

	@Inject
	LabelUtils messages;

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/editCategory.html", method = RequestMethod.GET)
	public String displayCategoryEdit(@RequestParam("id") long categoryId, Model model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return displayCategory(categoryId, model, request);

	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/createCategory.html", method = RequestMethod.GET)
	public String displayCategoryCreate(Model model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return displayCategory(null, model, request);

	}

	private String displayCategory(Long categoryId, Model model, HttpServletRequest request) throws Exception {
		// display menu
		setMenu(model, request);
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language) request.getAttribute("LANGUAGE");
		// get parent categories
		List<Category> categories = categoryService.listByStore(store, language);
		List<Language> languages = store.getLanguages();
		Optional<Category> category;
		if (categoryId != null && categoryId != 0) {// edit mode
			category = Optional.ofNullable(categoryService.getById(categoryId, language.getId()));
			if (!category.isPresent()
					|| category.get().getMerchantStore().getId().intValue() != store.getId().intValue()) {
				return "catalogue-categories";
			}
		} else {
			category = Optional.of(new Category());
			category.get().setVisible(true);
		}
		com.salesmanager.shop.admin.model.catalog.Category adminCategory = new com.salesmanager.shop.admin.model.catalog.Category();
		List<CategoryDescription> descriptions = new LinkedList<>();
		List<com.salesmanager.shop.admin.model.catalog.Category> readableCategories = CategoryUtils
				.readableCategoryListConverter(categories, language);

		for (Language l : languages) {
			CategoryDescription description = null;
			for (CategoryDescription desc : category.get().getDescriptions()) {
				if (desc.getLanguage().getCode().equals(l.getCode())) {
					description = desc;
				}
			}
			if (description == null) {
				description = new CategoryDescription();
				description.setLanguage(l);
			}
			descriptions.add(description);
		}

		adminCategory.setDescriptions(descriptions);
		adminCategory.setCategory(category.get());

		model.addAttribute("category", adminCategory);
		model.addAttribute("categories", readableCategories);

		return "catalogue-categories-category";
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/save.html", method = RequestMethod.POST)
	public String saveCategory(
			@Valid @ModelAttribute("category") com.salesmanager.shop.admin.model.catalog.Category category,
			BindingResult result, Model model, HttpServletRequest request) throws Exception {

		model.addAttribute("category", category);
		Language language = (Language) request.getAttribute("LANGUAGE");
		// display menu
		setMenu(model, request);
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		if (category.getCategory().getId() != null && category.getCategory().getId() > 0) { // edit entry
			// get from DB
			Optional<Category> currentCategory = Optional
					.ofNullable(categoryService.getById(category.getCategory().getId(), store.getId()));

			if (!currentCategory.isPresent()
					|| currentCategory.get().getMerchantStore().getId().intValue() != store.getId().intValue()) {
				return "catalogue-categories";
			}
		}
		Map<String, Language> langs = languageService.getLanguagesMap();
		List<CategoryDescription> descriptions = category.getDescriptions();
		if (descriptions != null) {
			Set<CategoryDescription> categoryDescriptions = new HashSet<>();
			for (CategoryDescription description : descriptions) {

				String code = description.getLanguage().getCode();
				Language l = langs.get(code);
				description.setLanguage(l);
				description.setCategory(category.getCategory());
				categoryDescriptions.add(description);

			}
			category.getCategory().setDescriptions(categoryDescriptions);
		}
		// save to DB
		category.getCategory().setMerchantStore(store);
		// }
		if (result.hasErrors()) {
			return "catalogue-categories-category";
		}
		// check parent
		if (category.getCategory().getParent() != null) {
			if (category.getCategory().getParent().getId() == -1) {// this is a root category
				category.getCategory().setParent(null);
				category.getCategory().setLineage("/" + category.getCategory().getId() + "/");
				category.getCategory().setDepth(0);
			}
		}
		category.getCategory().getAuditSection().setModifiedBy(request.getRemoteUser());
		categoryService.saveOrUpdate(category.getCategory());
		// ajust lineage and depth
		if (category.getCategory().getParent() != null && category.getCategory().getParent().getId() != -1) {
			Category parent = new Category();
			parent.setId(category.getCategory().getParent().getId());
			parent.setMerchantStore(store);
			categoryService.addChild(parent, category.getCategory(), language);
		}
		// get parent categories
		List<Category> categories = categoryService.listByStore(store, language);
		List<com.salesmanager.shop.admin.model.catalog.Category> readableCategories = CategoryUtils
				.readableCategoryListConverter(categories, language);

		model.addAttribute("categories", readableCategories);
		model.addAttribute("success", "success");
		return "catalogue-categories-category";
	}

	// category list
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/categories.html", method = RequestMethod.GET)
	public String displayCategories(Model model, HttpServletRequest request) throws Exception {
		setMenu(model, request);
		// does nothing, ajax subsequent request
		return "catalogue-categories";
	}

	@SuppressWarnings({ "unchecked" })
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/paging.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> pageCategories(HttpServletRequest request) {
		String categoryName = request.getParameter("name");
		String categoryCode = request.getParameter("code");
		AjaxResponse resp = new AjaxResponse();
		try {
			Language language = (Language) request.getAttribute("LANGUAGE");
			MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
			List<Category> categories = null;
			if (!StringUtils.isBlank(categoryName)) {
				categories = categoryService.getByName(store, categoryName, language);
			} else if (!StringUtils.isBlank(categoryCode)) {
				categoryService.listByCodes(store, new ArrayList<>(Collections.singletonList(categoryCode)), language);
			} else {
				categories = categoryService.listByStore(store, language);
			}
			for (Category category : categories) {
				@SuppressWarnings("rawtypes")
				Map entry = new HashMap();
				entry.put("categoryId", category.getId());
				CategoryDescription description = category.getDescriptions().iterator().next();

				entry.put("name", description.getName());
				entry.put("code", category.getCode());
				entry.put("visible", category.isVisible());
				resp.addDataEntry(entry);
			}
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_SUCCESS);
		} catch (Exception e) {
			LOGGER.error("Error while paging categories", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
		}
		String returnString = resp.toJSONString();
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(returnString, httpHeaders, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/hierarchy.html", method = RequestMethod.GET)
	public String displayCategoryHierarchy(Model model, HttpServletRequest request) throws Exception {
		setMenu(model, request);
		// get the list of categories
		Language language = (Language) request.getAttribute("LANGUAGE");
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);

		List<Category> categories = categoryService.listByStore(store, language);
		List<com.salesmanager.shop.admin.model.catalog.Category> readableCategories = CategoryUtils
				.readableCategoryListConverter(categories, language);

		model.addAttribute("categories", readableCategories);

		return "catalogue-categories-hierarchy";
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/remove.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> deleteCategory(HttpServletRequest request, Locale locale) {
		String sid = request.getParameter("categoryId");
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		AjaxResponse resp = new AjaxResponse();
		try {
			Long id = Long.parseLong(sid);
			Optional<Category> category = Optional.ofNullable(categoryService.getById(id, store.getId()));
			if (category.isPresent()
					|| category.get().getMerchantStore().getId().intValue() != store.getId().intValue()) {
				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			} else {
				categoryService.delete(category.get());
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
			}
		} catch (Exception e) {
			LOGGER.error("Error while deleting category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		String returnString = resp.toJSONString();
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(returnString, httpHeaders, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/moveCategory.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> moveCategory(HttpServletRequest request, Locale locale) {
		String parentid = request.getParameter("parentId");
		String childid = request.getParameter("childId");
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language) request.getAttribute("LANGUAGE");
		AjaxResponse resp = new AjaxResponse();
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		try {
			Long parentId = Long.parseLong(parentid);
			Long childId = Long.parseLong(childid);
			Optional<Category> child = Optional.ofNullable(categoryService.getById(childId, store.getId()));
			Optional<Category> parent = Optional.ofNullable(categoryService.getById(parentId, store.getId()));
			if (child.isPresent() && child.get().getParent().getId().equals(parentId)) {
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
				String returnString = resp.toJSONString();
			}
			if (parentId != 1) {
				if (isValid(store, child, parent)) {
					return getResponseAjax(locale, resp, httpHeaders);
				}
			}
			parent.get().getAuditSection().setModifiedBy(request.getRemoteUser());
			categoryService.addChild(parent.get(), child.get(),language);
			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		} catch (Exception e) {
			LOGGER.error("Error while moving category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}

		String returnString = resp.toJSONString();
		return new ResponseEntity<>(returnString, httpHeaders, HttpStatus.OK);
	}

	private boolean isValid(MerchantStore store, Optional<Category> child, Optional<Category> parent) {
		return !child.isPresent() || !parent.isPresent()
				|| !child.get().getMerchantStore().getId().equals(store.getId())
				|| !parent.get().getMerchantStore().getId().equals(store.getId());
	}

	private ResponseEntity<String> getResponseAjax(Locale locale, AjaxResponse resp, HttpHeaders httpHeaders) {
		resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
		return getResponseAjax(resp, httpHeaders, AjaxResponse.RESPONSE_STATUS_FAIURE);
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/checkCategoryCode.html", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> checkCategoryCode(HttpServletRequest request) {
		String code = request.getParameter("code");
		String id = request.getParameter("id");
		MerchantStore store = (MerchantStore) request.getAttribute(Constants.ADMIN_STORE);
		AjaxResponse resp = new AjaxResponse();
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.isBlank(code)) {
			return getResponseAjax(resp, httpHeaders, AjaxResponse.CODE_ALREADY_EXIST);
		}
		try {
			Optional<Category> category = Optional.ofNullable(categoryService.getByCode(store, code));
			if (category.isPresent() && StringUtils.isBlank(id)) {
				return getResponseAjax(resp, httpHeaders, AjaxResponse.CODE_ALREADY_EXIST);
			}
			if (category.isPresent() && !StringUtils.isBlank(id)) {
				try {
					long lid = Long.parseLong(id);
					if (category.get().getCode().equals(code) && category.get().getId() == lid) {
						return getResponseAjax(resp, httpHeaders, AjaxResponse.CODE_ALREADY_EXIST);
					}
				} catch (Exception e) {
					return getResponseAjax(resp, httpHeaders, AjaxResponse.CODE_ALREADY_EXIST);
				}
			}
			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
		} catch (Exception e) {
			LOGGER.error("Error while getting category", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		String returnString = resp.toJSONString();
		return new ResponseEntity<>(returnString, httpHeaders, HttpStatus.OK);
	}

	private ResponseEntity<String> getResponseAjax(AjaxResponse resp, HttpHeaders httpHeaders, int codeAlreadyExist) {
		resp.setStatus(codeAlreadyExist);
		String returnString = resp.toJSONString();
		return new ResponseEntity<>(returnString, httpHeaders, HttpStatus.OK);
	}

	private void setMenu(Model model, HttpServletRequest request) {
		// display menu
		Map<String, String> activeMenus = new HashMap<>();
		activeMenus.put("catalogue", "catalogue");
		activeMenus.put("catalogue-categories", "catalogue-categories");
		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>) request.getAttribute("MENUMAP");
		Menu currentMenu = menus.get("catalogue");
		model.addAttribute("currentMenu", currentMenu);
		model.addAttribute("activeMenus", activeMenus);
		//
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value = "/admin/categories/importCategoryCsv.html", method = RequestMethod.GET)
	public String importCategoryCsv(Model model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// display menu
		setMenu(model, request);
		model.addAttribute("category", new com.salesmanager.shop.admin.model.catalog.ImportCsvFile());
		return "catalogue-categories-import-csv";
	}

	@PreAuthorize("hasRole('PRODUCTS')")
	@PostMapping("/admin/categories/saveCsv.html")
	public String saveCsvCategory(
			@Valid @ModelAttribute("category") com.salesmanager.shop.admin.model.catalog.ImportCsvFile categoryFile,
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
			     .withObjectName("category.file")
			     .build();


		// validate csv
		if (categoryFile.getFile() == null || categoryFile.getFile().isEmpty()) {
			errorProcessor.addNewError("NotEmpty");
		} else if (!ImportCsvHelper.hasCSVFormat(categoryFile.getFile())) {
			errorProcessor.addNewError("message.csv.invalid.extension");
		}

		if (errorProcessor.hasErrors()) {
			return "catalogue-categories-import-csv";
		}

		return processRecords(model, store, categoryFile.getFile().getInputStream(), language, errorProcessor);
	}

	public String processRecords(Model model, MerchantStore store, InputStream is,
			Language language, ImportErrorProcessor errorProcessor) throws Exception {
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withDelimiter(';')
						.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			int rowNumber = 0;
			for (CSVRecord csvRecord : csvRecords) {
				rowNumber++;
				validateRequiredFields(csvRecord, errorProcessor);
				if (errorProcessor.hasErrors()) {
					addRowNumberAndInfoToErrors(rowNumber, errorProcessor);
					return "catalogue-categories-import-csv";
				}
				String name = csvRecord.get(CategoryImportHeader.NAME.name());
				com.salesmanager.shop.admin.model.catalog.Category category = getCategoryToProcessByNameAndLanguage(store, name, errorProcessor);
				buildCategoryDataFromCsvRecord(store, category, csvRecord);
				buildCategoryDescription(language, category, csvRecord);
				buildCategoryParent(category, csvRecord, store, errorProcessor);
				if (errorProcessor.hasErrors()) {
					addRowNumberAndInfoToErrors(rowNumber, errorProcessor);
					return "catalogue-categories-import-csv";
				}
				categoryService.create(category.getCategory());
				
				// check parent
				if (category.getCategory().getParent() != null && category.getCategory().getParent().getId() == -1) {// this is a root category
					category.getCategory().setParent(null);
					category.getCategory().setLineage("/" + category.getCategory().getId() + "/");
					category.getCategory().setDepth(0);
				}
				
				// ajust lineage and depth
				if (category.getCategory().getParent() != null && category.getCategory().getParent().getId() != -1) {
					Category parent = new Category();
					parent.setId(category.getCategory().getParent().getId());
					parent.setMerchantStore(store);
					categoryService.addChild(parent, category.getCategory(), language);
				}
			}

			model.addAttribute("success", "success");

			return "catalogue-categories-import-csv";

		} catch (IOException e) {
			errorProcessor.addNewError("message.csv.error.read.file");
			return "catalogue-categories-import-csv";
		}
	}
	
	private void addRowNumberAndInfoToErrors(int rowNumber, ImportErrorProcessor errorProcessor) {
		errorProcessor.addNewError("message.csv.error.row.number", rowNumber);
		errorProcessor.addNewError("message.csv.error.info");
	}

	public void validateRequiredFields(CSVRecord csvRecord, ImportErrorProcessor errorProcessor) {
		for (CategoryImportHeader header : CategoryImportHeader.values()) {
			try {
				String record = csvRecord.get(header.name());
				if (header.isRequired() && (record == null || record.isEmpty())) {
					errorProcessor.addNewError("message.csv.filed.required", header.name());
				}
			}catch(IllegalArgumentException e) {
				if (header.isRequired()) {
					errorProcessor.addNewError("message.csv.filed.required", header.name());
				}
			}	
		}
	}

	private com.salesmanager.shop.admin.model.catalog.Category getCategoryToProcessByNameAndLanguage(
			MerchantStore store, String name, ImportErrorProcessor errorProcessor) {
		com.salesmanager.shop.admin.model.catalog.Category categoryToProcess = new com.salesmanager.shop.admin.model.catalog.Category();
		try {
			Category existingCategory = categoryService.getByCode(store, name);
			if (existingCategory == null) {
				existingCategory = new Category();
				existingCategory.setCode(name.toLowerCase());
			}
			categoryToProcess.setCategory(existingCategory);
			return categoryToProcess;
		} catch (com.salesmanager.core.business.exception.ServiceException e) {
			errorProcessor.addNewError("message.csv.error.", name);
			return categoryToProcess;
		}
	}

	private void buildCategoryDataFromCsvRecord(MerchantStore store, com.salesmanager.shop.admin.model.catalog.Category category, CSVRecord csvRecord) {
		String name = csvRecord.get(CategoryImportHeader.NAME.name());
		Boolean visible = ImportCsvHelper.getBooleanOrDefaultFromRecordWithHeaderName(csvRecord, CategoryImportHeader.VISIBLE, Boolean.FALSE);
		com.salesmanager.core.model.catalog.category.Category innerCategoryData = category.getCategory();
		innerCategoryData.setCode(name.toLowerCase());
		innerCategoryData.setMerchantStore(store);
		innerCategoryData.setVisible(visible);
		category.setCategory(innerCategoryData);
	}

//	public Boolean getBooleanOrDefaultFromRecordWithHeaderName(CSVRecord csvRecord, CategoryImportHeader csvHeader, Boolean defaultValue) {
//		//TODO para todos los casos, manejar IllegalArgumentExceotion  
//		String record = csvRecord.get(csvHeader.name());
//		  return record == null || "".equals(record.trim())? defaultValue : Boolean.parseBoolean(record);
//	  }
	
	private void buildCategoryDescription(Language language, com.salesmanager.shop.admin.model.catalog.Category category, CSVRecord csvRecord) {
		String description = csvRecord.get(CategoryImportHeader.DESCRIPTION);
		Set<CategoryDescription> categoryDescriptions = category.getCategory().getDescriptions();
		CategoryDescription categoryDescription = new CategoryDescription();

		if (categoryDescriptions != null && !categoryDescriptions.isEmpty()) {
			for (CategoryDescription cd : categoryDescriptions) {
				if (cd.getLanguage().equals(language)) {
					categoryDescriptions.remove(cd);
					categoryDescription = cd;
				}
			}
		} else {
			categoryDescriptions = new HashSet<>();
		}

		String name = csvRecord.get(CategoryImportHeader.NAME.name());
		categoryDescription.setLanguage(language);
		categoryDescription.setName(name);
		categoryDescription.setDescription(description);
		categoryDescription.setCategoryHighlight(name.toLowerCase());
		categoryDescription.setMetatagDescription(name.toLowerCase());
		categoryDescription.setMetatagKeywords(name.toLowerCase());
		categoryDescription.setMetatagTitle(name.toLowerCase());
		categoryDescription.setSeUrl(name.toLowerCase().replace(" ", "-"));
		categoryDescription.setCategory(category.getCategory());
		categoryDescriptions.add(categoryDescription);
		category.getCategory().setDescriptions(categoryDescriptions);

	}
	
	private void buildCategoryParent(com.salesmanager.shop.admin.model.catalog.Category category, CSVRecord csvRecord, MerchantStore store, ImportErrorProcessor errorProcessor) {
		String parentName=null;
		try {
			 parentName = csvRecord.get(CategoryImportHeader.PARENT);
			if(parentName==null || "".equals(parentName.trim())) return;
			Category parent = categoryService.getByCode(store, parentName);
			com.salesmanager.core.model.catalog.category.Category innerCategoryData = category.getCategory();
			if(parent==null) {
				errorProcessor.addNewError("category.parent.unknown", parentName);
				return;
			}
			innerCategoryData.setParent(parent);
			category.setCategory(innerCategoryData);
		} catch (com.salesmanager.core.business.exception.ServiceException e) {
			LOGGER.error(e.getMessage(), e);
			errorProcessor.addNewError("category.parent.unknown", parentName);
		} catch(IllegalArgumentException e) {
			LOGGER.warn("PARENT header not found in the file");
	}
		

	}
}
