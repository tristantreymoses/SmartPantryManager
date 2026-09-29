package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddItemActivity extends AppCompatActivity {

    private EditText etItemName;
    private EditText etQuantity;
    private EditText etExpiryDate;
    private Button btnSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_item);

        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        btnSaveItem = findViewById(R.id.btnSaveItem);

        etExpiryDate.setOnClickListener(v -> showDatePicker());

        btnSaveItem.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String date = selectedDay + "/"
                                    + (selectedMonth + 1) + "/"
                                    + selectedYear;

                            etExpiryDate.setText(date);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    private void saveItem() {

        String itemName = etItemName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        if (itemName.isEmpty()) {
            etItemName.setError("Enter an item name");
            etItemName.requestFocus();
            return;
        }

        if (quantity.isEmpty()) {
            etQuantity.setError("Enter quantity");
            etQuantity.requestFocus();
            return;
        }

        if (expiryDate.isEmpty()) {
            etExpiryDate.setError("Select expiry date");
            etExpiryDate.requestFocus();
            return;
        }

        // Open pantry storage
        SharedPreferences preferences =
                getSharedPreferences("SmartPantry", MODE_PRIVATE);

        // Get current number of saved items
        int itemCount =
                preferences.getInt("itemCount", 0);

        // Save this item
        preferences.edit()
                .putString("itemName_" + itemCount, itemName)
                .putString("quantity_" + itemCount, quantity)
                .putString("expiryDate_" + itemCount, expiryDate)
                .putInt("itemCount", itemCount + 1)
                .apply();

        // Also send the item back to MainActivity
        Intent resultIntent = new Intent();

        resultIntent.putExtra("itemName", itemName);
        resultIntent.putExtra("quantity", quantity);
        resultIntent.putExtra("expiryDate", expiryDate);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(
                this,
                "Item saved successfully!",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}