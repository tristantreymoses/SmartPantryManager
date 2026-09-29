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

    // Used to determine whether we are adding or editing
    private boolean isEditMode = false;
    private int editItemIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_item);

        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        btnSaveItem = findViewById(R.id.btnSaveItem);

        // Check if MainActivity opened this screen for editing
        Intent intent = getIntent();

        if (intent.hasExtra("editItemIndex")) {

            isEditMode = true;

            editItemIndex =
                    intent.getIntExtra(
                            "editItemIndex",
                            -1
                    );

            loadItemForEditing();
        }

        etExpiryDate.setOnClickListener(
                v -> showDatePicker()
        );

        btnSaveItem.setOnClickListener(
                v -> saveItem()
        );
    }

    // ============================================================
    // LOAD ITEM FOR EDITING
    // ============================================================

    private void loadItemForEditing() {

        if (editItemIndex < 0) {
            return;
        }

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartPantry",
                        MODE_PRIVATE
                );

        String itemName =
                preferences.getString(
                        "itemName_" + editItemIndex,
                        ""
                );

        String quantity =
                preferences.getString(
                        "quantity_" + editItemIndex,
                        ""
                );

        String expiryDate =
                preferences.getString(
                        "expiryDate_" + editItemIndex,
                        ""
                );

        etItemName.setText(itemName);
        etQuantity.setText(quantity);
        etExpiryDate.setText(expiryDate);

        // Change button text
        btnSaveItem.setText("Save Changes");
    }

    // ============================================================
    // DATE PICKER
    // ============================================================

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        int year =
                calendar.get(Calendar.YEAR);

        int month =
                calendar.get(Calendar.MONTH);

        int day =
                calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            String date =
                                    selectedDay
                                            + "/"
                                            + (selectedMonth + 1)
                                            + "/"
                                            + selectedYear;

                            etExpiryDate.setText(date);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    // ============================================================
    // SAVE OR UPDATE ITEM
    // ============================================================

    private void saveItem() {

        String itemName =
                etItemName
                        .getText()
                        .toString()
                        .trim();

        String quantity =
                etQuantity
                        .getText()
                        .toString()
                        .trim();

        String expiryDate =
                etExpiryDate
                        .getText()
                        .toString()
                        .trim();

        // Validate item name
        if (itemName.isEmpty()) {

            etItemName.setError(
                    "Enter an item name"
            );

            etItemName.requestFocus();

            return;
        }

        // Validate quantity
        if (quantity.isEmpty()) {

            etQuantity.setError(
                    "Enter quantity"
            );

            etQuantity.requestFocus();

            return;
        }

        // Validate expiry date
        if (expiryDate.isEmpty()) {

            etExpiryDate.setError(
                    "Select expiry date"
            );

            etExpiryDate.requestFocus();

            return;
        }

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartPantry",
                        MODE_PRIVATE
                );

        SharedPreferences.Editor editor =
                preferences.edit();

        // ========================================================
        // EDIT EXISTING ITEM
        // ========================================================

        if (isEditMode && editItemIndex >= 0) {

            editor.putString(
                    "itemName_" + editItemIndex,
                    itemName
            );

            editor.putString(
                    "quantity_" + editItemIndex,
                    quantity
            );

            editor.putString(
                    "expiryDate_" + editItemIndex,
                    expiryDate
            );

            editor.apply();

            Intent resultIntent =
                    new Intent();

            resultIntent.putExtra(
                    "itemName",
                    itemName
            );

            resultIntent.putExtra(
                    "quantity",
                    quantity
            );

            resultIntent.putExtra(
                    "expiryDate",
                    expiryDate
            );

            setResult(
                    RESULT_OK,
                    resultIntent
            );

            Toast.makeText(
                    this,
                    "Item updated successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        // ========================================================
        // ADD NEW ITEM
        // ========================================================

        int itemCount =
                preferences.getInt(
                        "itemCount",
                        0
                );

        editor.putString(
                "itemName_" + itemCount,
                itemName
        );

        editor.putString(
                "quantity_" + itemCount,
                quantity
        );

        editor.putString(
                "expiryDate_" + itemCount,
                expiryDate
        );

        editor.putInt(
                "itemCount",
                itemCount + 1
        );

        editor.apply();

        Intent resultIntent =
                new Intent();

        resultIntent.putExtra(
                "itemName",
                itemName
        );

        resultIntent.putExtra(
                "quantity",
                quantity
        );

        resultIntent.putExtra(
                "expiryDate",
                expiryDate
        );

        setResult(
                RESULT_OK,
                resultIntent
        );

        Toast.makeText(
                this,
                "Item saved successfully!",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}