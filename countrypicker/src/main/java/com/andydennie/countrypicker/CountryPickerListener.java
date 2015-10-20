package com.andydennie.countrypicker;

/**
 * Inform the client which country has been selected
 *
 */
public interface CountryPickerListener {
	void onCountrySelected(Country country);
}
