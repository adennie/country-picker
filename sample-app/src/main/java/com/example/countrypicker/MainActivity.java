package com.example.countrypicker;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

import com.andydennie.countrypicker.CountryPicker;
import com.andydennie.countrypicker.CountryPickerListener;

import java.util.Currency;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        final CountryPicker picker = new CountryPicker();
        picker.setListener(new CountryPickerListener() {

            @Override
            public void onSelectCountry(String name, String code) {
                Toast.makeText(
                        MainActivity.this,
                        "Country Name: " + name + " - Code: " + code
                                + " - Currency: "
                                + getCurrencyCode(code),
                        Toast.LENGTH_SHORT).show();
            }
        });

        transaction.replace(R.id.content_frame, picker);

        transaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.show_dialog);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CountryPicker picker = CountryPicker.newInstance("Select Country");
                picker.setListener(new CountryPickerListener() {

                    @Override
                    public void onSelectCountry(String name, String code) {
                        Toast.makeText(
                                MainActivity.this,
                                "Country Name: " + name + " - Code: " + code
                                        + " - Currency: "
                                        + getCurrencyCode(code),
                                Toast.LENGTH_SHORT).show();
                    }
                });

                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                return false;
            }
        });
        return true;
    }

    private  Currency getCurrencyCode(String countryCode) {
        try {
            return Currency.getInstance(new Locale("en", countryCode));
        } catch (Exception e) {

        }
        return null;
    }
}
