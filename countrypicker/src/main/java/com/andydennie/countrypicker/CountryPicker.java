package com.andydennie.countrypicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CountryPicker extends AppCompatDialogFragment {

    private EditText searchEditText;
    private ListView countryListView;
    private CountryListAdapter adapter;
    private CountryPickerListener listener;
    private Country preselectedCountry;
    private List<Country> allCountriesByName;
    private CountryNameComparator nameComparator = new CountryNameComparator();

    // countries that matched user query
    private List<Country> selectedCountriesList;

    /**
     * To support show as dialog
     *
     * @param dialogTitle dialog title
     * @return a CountryPicker instance
     */
    public static CountryPicker newInstance(String dialogTitle) {
        CountryPicker picker = new CountryPicker();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        return picker;
    }

    /**
     * Create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get countries from the json
        loadCountries();

        // Inflate view
        View view = inflater.inflate(R.layout.country_picker, null);

        EditText search = (EditText) (view.findViewById(R.id.countryPickerSearch));

        // tint the search icon if the theme specifies colorControlNormal
        final TypedValue value = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.colorControlNormal, value, true)) {
            Drawable searchIcon = search.getCompoundDrawables()[0];
            DrawableCompat.setTint(searchIcon, value.data);
        }

        // Set dialog title if show as dialog
        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString("dialogTitle");
            getDialog().setTitle(dialogTitle);
        }

        // Get view components
        searchEditText = (EditText) view.findViewById(R.id.countryPickerSearch);
        countryListView = (ListView) view.findViewById(R.id.countryPickerListview);

        // Set adapter
        adapter = new CountryListAdapter(getActivity(), selectedCountriesList);
        countryListView.setAdapter(adapter);

        // if a preselected country was specified, select it
        preselectCountry();

        // Inform listener
        countryListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Country country = selectedCountriesList.get(position);
                    listener.onCountrySelected(country);
                }
            }
        });

        // Search for which countries matched user query
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    public void setListener(CountryPickerListener listener) {
        this.listener = listener;
    }

    /**
     * Specify a country to be preselected in the list.  The argument must have either a
     * non-null code or name; either is sufficient.
     *
     * @param country to preselect
     */
    public void setPreselectedCountry(Country country) {
        preselectedCountry = country;
    }

    private void loadCountries() {
        try {
            allCountriesByName = new ArrayList<>();

            // Parse resource string containing country data in JSON format
            String allCountriesString = getCountriesString();
            JSONObject jsonObject = new JSONObject(allCountriesString);
            Iterator<?> keys = jsonObject.keys();

            // Add the data to all countries list
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Country country = new Country(key, jsonObject.getString(key));
                allCountriesByName.add(country);
            }

            // Sort the lists
            Collections.sort(allCountriesByName, nameComparator);

            // Initialize selected countries with all countries
            selectedCountriesList = new ArrayList<>();
            selectedCountriesList.addAll(allCountriesByName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getCountriesString()
            throws java.io.IOException {
        // R.string.countries is a json string which is Base64 encoded to avoid
        // special characters in XML. It's Base64 decoded here to get original json.
        String base64 = getActivity().getResources().getString(R.string.countries);
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }

    private void preselectCountry() {
        if (preselectedCountry != null) {
            if (preselectedCountry.getName() != null) {
                countryListView.setSelection(
                        Collections.binarySearch(allCountriesByName, preselectedCountry, nameComparator));
            } else if (preselectedCountry.getCode() != null) {
                // brute force, but still fast enough to not matter
                int pos = 0;
                for (Country country : allCountriesByName) {
                    if (country.getCode().equals(preselectedCountry.getCode())) {
                        countryListView.setSelection(pos);
                        break;
                    }
                    pos++;
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCountriesList.clear();

        for (Country country : allCountriesByName) {
            if (country.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private static class CountryNameComparator implements Comparator<Country> {

        @Override
        public int compare(Country lhs, Country rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
