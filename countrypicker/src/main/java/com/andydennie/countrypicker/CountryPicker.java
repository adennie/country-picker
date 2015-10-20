package com.andydennie.countrypicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
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

public class CountryPicker extends DialogFragment implements
        Comparator<Country> {

    private EditText searchEditText;
    private ListView countryListView;
    private CountryListAdapter adapter;
    private CountryPickerListener listener;

    // all countries, sorted by country name
    private List<Country> allCountriesList;

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
        // Inflate view
        View view = inflater.inflate(R.layout.country_picker, null);

        EditText search = (EditText) (view.findViewById(R.id.country_picker_search));

        // tint the search icon if the theme specifies an accent color
        final TypedValue value = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.colorAccent, value, true)) {
            Drawable searchIcon = search.getCompoundDrawables()[0];
            DrawableCompat.setTint(searchIcon, value.data);
        }

        // Get countries from the json
        getAllCountries();

        // Set dialog title if show as dialog
        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString("dialogTitle");
            getDialog().setTitle(dialogTitle);
        }

        // Get view components
        searchEditText = (EditText) view
                .findViewById(R.id.country_picker_search);
        countryListView = (ListView) view
                .findViewById(R.id.country_picker_listview);

        // Set adapter
        adapter = new CountryListAdapter(getActivity(), selectedCountriesList);
        countryListView.setAdapter(adapter);

        // Inform listener
        countryListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Country country = selectedCountriesList.get(position);
                    listener.onSelectCountry(country.getName(),
                            country.getCode());
                }
            }
        });

        // Search for which countries matched user query
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
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

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCountryListView() {
        return countryListView;
    }

    /**
     * Support sorting the countries list
     */
    @Override
    public int compare(Country lhs, Country rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }


    private List<Country> getAllCountries() {
        if (allCountriesList == null) {
            try {
                allCountriesList = new ArrayList<>();

                // Read from local file
                String allCountriesString = readFileAsString(getActivity());
                Log.d("countrypicker", "country: " + allCountriesString);
                JSONObject jsonObject = new JSONObject(allCountriesString);
                Iterator<?> keys = jsonObject.keys();

                // Add the data to all countries list
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Country country = new Country();
                    country.setCode(key);
                    country.setName(jsonObject.getString(key));
                    allCountriesList.add(country);
                }

                // Sort the all countries list based on country name
                Collections.sort(allCountriesList, this);

                // Initialize selected countries with all countries
                selectedCountriesList = new ArrayList<>();
                selectedCountriesList.addAll(allCountriesList);

                // Return
                return allCountriesList;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String readFileAsString(Context context)
            throws java.io.IOException {
        // R.string.countries is a json string which is Base64 encoded to avoid
        // special characters in XML. It's Base64 decoded here to get original json.
        String base64 = context.getResources().getString(R.string.countries);
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }


    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCountriesList.clear();

        for (Country country : allCountriesList) {
            if (country.getName().toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private int getThemeAccentColor() {
        final TypedValue value = new TypedValue();
        boolean found = getContext().getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return found ? value.data : -1;
    }
}
