package com.misys.ub.dc.common;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;

public class LocaleHelp {

	private static LocaleHelp localeHelp = null;
	private static final String EMPTY_STRING= "";
	private Map<String, String> iso3ToIso2Mapping = null;
	private Map<String, String> iso2ToIso3Mapping = null;
	private Map<String, String> iso3Countries = null;
	private Map<String, String> iso2Countries = null;
	
	public static LocaleHelp getInstance() {
		
		if(localeHelp == null) {
			localeHelp = new LocaleHelp();
		}
		return localeHelp;
	}
	
	/**
	 * This method will load all the locale information into the maps for use. This will get 
	 * initialized once for first time use.
	 */
	private LocaleHelp() {
		
		String[] countries = null;
		Locale locale = null;
		
		countries = Locale.getISOCountries();
		iso3ToIso2Mapping = new HashMap<String, String>();
		iso2ToIso3Mapping = new HashMap<String, String>();
		iso3Countries = new HashMap<String, String>();
		iso2Countries = new HashMap<String, String>();
		
		for(String country : countries) {
			
			locale = new Locale("", country);
			
			iso2ToIso3Mapping.put(country, locale.getISO3Country());
			iso3ToIso2Mapping.put(locale.getISO3Country(), country);
			
			iso3Countries.put(locale.getISO3Country(), locale.getDisplayCountry());
			iso2Countries.put(country, locale.getDisplayCountry());
		}		
	}
	
	/**
	 * Fetch the ISO2 Country Code when ISO3 Country Code is given.
	 * 
	 * @param iso3Country
	 * @return
	 */
	public static String getIso2CountryFromIso3Country(String iso3Country) {

		if(iso3Country == null || iso3Country.isEmpty()) {
			return null;
		}
		return localeHelp.iso3ToIso2Mapping.get(iso3Country);
	}
	
	/**
	 * Fetch the ISO3 Country Code when ISO2 Country Code is given.
	 * 
	 * @param iso2Country
	 * @return
	 */
	public static String getIso3CountryFromIso2Country(String iso2Country) {
		
		if(iso2Country == null || iso2Country.isEmpty()) {
			return null;
		}
		return localeHelp.iso2ToIso3Mapping.get(iso2Country);
	}
	
	/**
	 * Fetch the country name for the given country code.
	 * 
	 * @param iso3Country
	 * @return
	 */
	public static String getIso3CountryName(String iso3Country) {
		
		if(iso3Country == null || iso3Country.isEmpty()) {
			return null;
		}
		return localeHelp.iso3Countries.get(iso3Country);
	}
	
	/**
	 * Fetch the country name for the given country code.
	 * 
	 * @param iso2Country
	 * @return
	 */
	public static String getIso2CountryName(String iso2Country) {
		
		if(iso2Country == null || iso2Country.isEmpty()) {
			return null;
		}
		return localeHelp.iso2Countries.get(iso2Country);
	}
	
	public static String getDisplayLanguage(String languageCode) {
		return LocaleUtils.toLocale(languageCode).getDisplayLanguage();
	}
	
	public static String getLanguageFromCountry(String countryCode) {
		List<String> languages = LocaleUtils.languagesByCountry(countryCode);
		
		if(languages!= null && languages.size() > 0) {
			return languages.get(0);
		}
		
		return EMPTY_STRING;
	}

}
