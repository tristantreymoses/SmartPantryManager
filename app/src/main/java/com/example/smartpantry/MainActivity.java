package com.example.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvItems;
    private TextView tvExpiring;
    private TextView tvEmptyPantry;
    private LinearLayout pantryList;
    private Button btnAddItem;
    private EditText searchBar;

    private int pantryCount = 0;
    private int expiringCount = 0;

    private static final int ADD_ITEM_REQUEST = 100;
    private static final int EDIT_ITEM_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {

            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        tvItems = findViewById(R.id.tvItems);
        tvExpiring = findViewById(R.id.tvExpiring);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        pantryList = findViewById(R.id.pantryList);
        btnAddItem = findViewById(R.id.btnAddItem);
        searchBar = findViewById(R.id.searchBar);

        // Add Item button
        btnAddItem.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddItemActivity.class
                    );

            startActivityForResult(
                    intent,
                    ADD_ITEM_REQUEST
            );
        });

        // Search
        searchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                filterPantryItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadSavedItems();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pantryList != null) {
            loadSavedItems();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if ((requestCode == ADD_ITEM_REQUEST
                || requestCode == EDIT_ITEM_REQUEST)
                && resultCode == RESULT_OK) {

            loadSavedItems();
        }
    }

    // ============================================================
    // LOAD SAVED ITEMS
    // ============================================================

    private void loadSavedItems() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartPantry",
                        MODE_PRIVATE
                );

        int savedItemCount =
                preferences.getInt(
                        "itemCount",
                        0
                );

        pantryCount = 0;
        expiringCount = 0;

        pantryList.removeAllViews();

        for (int i = 0; i < savedItemCount; i++) {

            String itemName =
                    preferences.getString(
                            "itemName_" + i,
                            ""
                    );

            String quantity =
                    preferences.getString(
                            "quantity_" + i,
                            ""
                    );

            String expiryDate =
                    preferences.getString(
                            "expiryDate_" + i,
                            ""
                    );

            if (!itemName.isEmpty()) {

                addItemView(
                        itemName,
                        quantity,
                        expiryDate,
                        i
                );

                pantryCount++;
            }
        }

        updatePantryDisplay();
    }

    // ============================================================
    // DISPLAY PANTRY ITEM
    // ============================================================

    private void addItemView(
            String itemName,
            String quantity,
            String expiryDate,
            int itemIndex) {

        TextView itemView =
                new TextView(this);

        itemView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(58)
                )
        );

        itemView.setGravity(
                Gravity.CENTER_VERTICAL
        );

        itemView.setPadding(
                dpToPx(18),
                0,
                dpToPx(18),
                0
        );

        itemView.setText(
                "🥚   "
                        + itemName
                        + "     "
                        + quantity
                        + "     "
                        + expiryDate
        );

        itemView.setTextColor(
                Color.parseColor("#333333")
        );

        itemView.setTextSize(16);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.WHITE
        );

        background.setCornerRadius(
                dpToPx(18)
        );

        background.setStroke(
                dpToPx(1),
                Color.parseColor("#E2E7E2")
        );

        itemView.setBackground(background);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        itemView.getLayoutParams();

        params.setMargins(
                0,
                dpToPx(5),
                0,
                dpToPx(5)
        );

        itemView.setLayoutParams(params);

        // Tap item
        itemView.setOnClickListener(v -> {

            showItemOptions(
                    itemName,
                    itemIndex
            );
        });

        pantryList.addView(itemView);
    }

    // ============================================================
    // EDIT / DELETE MENU
    // ============================================================

    private void showItemOptions(
            String itemName,
            int itemIndex) {

        String[] options = {
                "Edit Item",
                "Delete Item",
                "Cancel"
        };

        new AlertDialog.Builder(this)
                .setTitle(itemName)
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                editItem(itemIndex);

                            } else if (which == 1) {

                                confirmDelete(
                                        itemName,
                                        itemIndex
                                );
                            }
                        }
                )
                .show();
    }

    // ============================================================
    // OPEN EDIT SCREEN
    // ============================================================

    private void editItem(int itemIndex) {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        AddItemActivity.class
                );

        intent.putExtra(
                "editItemIndex",
                itemIndex
        );

        startActivityForResult(
                intent,
                EDIT_ITEM_REQUEST
        );
    }

    // ============================================================
    // CONFIRM DELETE
    // ============================================================

    private void confirmDelete(
            String itemName,
            int itemIndex) {

        new AlertDialog.Builder(this)

                .setTitle("Delete Item")

                .setMessage(
                        "Do you want to delete "
                                + itemName
                                + "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteItem(itemIndex);
                        }
                )

                .show();
    }

    // ============================================================
    // SEARCH
    // ============================================================

    private void filterPantryItems(
            String searchText) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartPantry",
                        MODE_PRIVATE
                );

        int savedItemCount =
                preferences.getInt(
                        "itemCount",
                        0
                );

        pantryList.removeAllViews();

        int matchingItems = 0;

        searchText =
                searchText
                        .toLowerCase()
                        .trim();

        for (int i = 0; i < savedItemCount; i++) {

            String itemName =
                    preferences.getString(
                            "itemName_" + i,
                            ""
                    );

            String quantity =
                    preferences.getString(
                            "quantity_" + i,
                            ""
                    );

            String expiryDate =
                    preferences.getString(
                            "expiryDate_" + i,
                            ""
                    );

            if (!itemName.isEmpty()
                    && itemName
                    .toLowerCase()
                    .contains(searchText)) {

                addItemView(
                        itemName,
                        quantity,
                        expiryDate,
                        i
                );

                matchingItems++;
            }
        }

        if (savedItemCount == 0) {

            tvEmptyPantry.setText(
                    "Your pantry is empty.\n" +
                            "Tap '+ Add Item' to get started."
            );

            tvEmptyPantry.setVisibility(
                    View.VISIBLE
            );

        } else if (matchingItems == 0) {

            tvEmptyPantry.setText(
                    "No pantry items found."
            );

            tvEmptyPantry.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvEmptyPantry.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // DELETE ITEM
    // ============================================================

    private void deleteItem(int itemIndex) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartPantry",
                        MODE_PRIVATE
                );

        int itemCount =
                preferences.getInt(
                        "itemCount",
                        0
                );

        if (itemCount <= 0) {
            return;
        }

        SharedPreferences.Editor editor =
                preferences.edit();

        // Move items after deleted item up
        for (
                int i = itemIndex;
                i < itemCount - 1;
                i++
        ) {

            String nextName =
                    preferences.getString(
                            "itemName_" + (i + 1),
                            ""
                    );

            String nextQuantity =
                    preferences.getString(
                            "quantity_" + (i + 1),
                            ""
                    );

            String nextExpiry =
                    preferences.getString(
                            "expiryDate_" + (i + 1),
                            ""
                    );

            editor.putString(
                    "itemName_" + i,
                    nextName
            );

            editor.putString(
                    "quantity_" + i,
                    nextQuantity
            );

            editor.putString(
                    "expiryDate_" + i,
                    nextExpiry
            );
        }

        // Remove final duplicate
        editor.remove(
                "itemName_" + (itemCount - 1)
        );

        editor.remove(
                "quantity_" + (itemCount - 1)
        );

        editor.remove(
                "expiryDate_" + (itemCount - 1)
        );

        editor.putInt(
                "itemCount",
                itemCount - 1
        );

        editor.apply();

        Toast.makeText(
                this,
                "Item deleted",
                Toast.LENGTH_SHORT
        ).show();

        loadSavedItems();
    }

    // ============================================================
    // UPDATE PANTRY DISPLAY
    // ============================================================

    private void updatePantryDisplay() {

        tvItems.setText(
                "🥫\n\n"
                        + pantryCount
                        + "\nPantry Items"
        );

        tvExpiring.setText(
                "⚠️\n\n"
                        + expiringCount
                        + "\nExpiring Soon"
        );

        if (pantryCount == 0) {

            tvEmptyPantry.setText(
                    "Your pantry is empty.\n" +
                            "Tap '+ Add Item' to get started."
            );

            tvEmptyPantry.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvEmptyPantry.setVisibility(
                    View.GONE
            );
        }
    }

    // ============================================================
    // DP TO PX
    // ============================================================

    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }
}