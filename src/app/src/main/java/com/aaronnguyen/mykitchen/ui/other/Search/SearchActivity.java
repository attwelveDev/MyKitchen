package com.aaronnguyen.mykitchen.ui.other.Search;

import static com.aaronnguyen.mykitchen.FileWriter.FileUtils.updateCsvFile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Search.Product;
import com.aaronnguyen.mykitchen.model.Search.ProductTree;
import com.aaronnguyen.mykitchen.model.Search.ProductUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * Activity class for searching and displaying products.
 * This class handles user interactions, including searching, filtering, and adding products.
 * @author u7515796 ChengboYan
 */
public class SearchActivity extends AppCompatActivity {
    public static final String DAIRY = "DAIRY";
    public static final String FRUIT = "FRUIT";
    public static final String GRAIN = "GRAIN";
    public static final String PROTEIN = "PROTEIN";
    public static final String VEGETABLE = "VEGETABLE";
    public static final String OTHER = "OTHER";

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ProductAdapter adapter;
    private ProductTree products;
    private Button foodGroupBtn;
    private Button storageLocationBtn;
    private List<Product> allProductsList; // The complete product list

    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        products = ProductUtils.getInstance(this, "item_list.csv");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        allProductsList = products.getProductList(); // Save the complete product list
        Log.i("show trees number", "" + allProductsList.size());


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this, allProductsList);
        recyclerView.setAdapter(adapter);

        searchEditText = findViewById(R.id.searchEditText);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<Product> searchResults = products.search(s.toString().replaceAll("\\s+","")
                        .toLowerCase(), currentFilter);
                if(searchResults.isEmpty()){
                    updateRecyclerView(allProductsList);
                }
                else updateRecyclerView(searchResults);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextInputLayout searchInputLayout = findViewById(R.id.search_suggestions_edit_text_container);
        searchInputLayout.setEndIconOnClickListener(v -> showFilterAlertDialog());
        FloatingActionButton addButton = findViewById(R.id.search_btn);
        addButton.setOnClickListener(v -> showAddItemAlertDialog());
    }

    private void updateRecyclerView(List<Product> productList) {
        adapter.updateProductList(productList);
    }

    /**
     * Show the filter food type alert dialog. Users can filter the food type for search.
     *
     * @author u733216 Aaron Nguyen (UI; filtering logic by Humza, Chengbo)
     */
    private void showFilterAlertDialog() {
        View filterView = getLayoutInflater().inflate(R.layout.filter_alert_dialog, null);
        Button clearBtn = filterView.findViewById(R.id.clear_filter_btn);
        RadioGroup foodTypesRadioGroup = filterView.findViewById(R.id.food_type_radio_gp);

        MaterialAlertDialogBuilder filterDialogBuilder = new MaterialAlertDialogBuilder(SearchActivity.this);
        filterDialogBuilder.setView(filterView).setPositiveButton("Filter", (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        androidx.appcompat.app.AlertDialog filterDialog = filterDialogBuilder.create();
        filterDialog.setOnShowListener(dialog -> {
            Button positiveButton = filterDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {  // To prevent unintended dismiss (i.e. when invalid input)
                List<Product> searchResults = products.search(searchEditText.getText().toString()
                        .replaceAll("\\s+","").toLowerCase(), currentFilter);
                Log.i("show trees", "" + searchResults.size());
                updateRecyclerView(searchResults);

                filterDialog.dismiss();
            });

            switch (currentFilter) {
                case DAIRY -> foodTypesRadioGroup.check(R.id.dairy_filter_btn);
                case FRUIT -> foodTypesRadioGroup.check(R.id.fruit_filter_btn);
                case GRAIN -> foodTypesRadioGroup.check(R.id.grain_filter_btn);
                case PROTEIN -> foodTypesRadioGroup.check(R.id.protein_filter_btn);
                case VEGETABLE -> foodTypesRadioGroup.check(R.id.vegetable_filter_btn);
                case OTHER -> foodTypesRadioGroup.check(R.id.other_filter_btn);
                default -> {
                    clearBtn.setVisibility(View.GONE);
                    positiveButton.setEnabled(false);
                }
            }

            clearBtn.setOnClickListener(v -> {
                foodTypesRadioGroup.clearCheck();
                currentFilter = "";
                clearBtn.setVisibility(View.GONE);
                positiveButton.setEnabled(false);
            });

            foodTypesRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.dairy_filter_btn) {
                    currentFilter = DAIRY;
                } else if (checkedId == R.id.fruit_filter_btn) {
                    currentFilter = FRUIT;
                } else if (checkedId == R.id.grain_filter_btn) {
                    currentFilter = GRAIN;
                } else if (checkedId == R.id.protein_filter_btn) {
                    currentFilter = PROTEIN;
                } else if (checkedId == R.id.vegetable_filter_btn) {
                    currentFilter = VEGETABLE;
                } else if (checkedId == R.id.other_filter_btn) {
                    currentFilter = OTHER;
                } else {
                    return;
                }

                clearBtn.setVisibility(View.VISIBLE);
                positiveButton.setEnabled(true);
            });
        });

        filterDialog.show();
    }


    /**
     * Show the dialog for adding a new item to the product list.
     * Users can input the item's name, expiry days, food group, and storage location.
     * Upon adding the item, it is saved to the product list and the adapter is updated.
     * @author u7515796 ChenboYan
     *
     */
    private void showAddItemAlertDialog() {
        View addItemView = getLayoutInflater().inflate(R.layout.add_item_search_dialog, null);

        EditText foodNameEditText = addItemView.findViewById(R.id.food_name_edit_text);
        EditText expiryDaysEditText = addItemView.findViewById(R.id.expiry_days_edit_text);
        foodGroupBtn = addItemView.findViewById(R.id.food_gp_btn);
        storageLocationBtn = addItemView.findViewById(R.id.storage_loc_btn);

        MaterialAlertDialogBuilder addItemDialogBuilder = new MaterialAlertDialogBuilder(this);
        addItemDialogBuilder.setView(addItemView)
                .setPositiveButton(R.string.add_btn, null)  // Null here to override the default close behavior
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        androidx.appcompat.app.AlertDialog addItemDialog = addItemDialogBuilder.create();
        addItemDialog.setOnShowListener(dialog -> {
            Button positiveButton = addItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String foodNameText = foodNameEditText.getText().toString();
                String expiryDaysText = expiryDaysEditText.getText().toString();
                String foodGroupText = foodGroupBtn.getText().toString();
                String storageLocText = storageLocationBtn.getText().toString();

                if (foodNameText.isEmpty() || expiryDaysText.isEmpty() || foodGroupText.isEmpty() || storageLocText.isEmpty()) {
                    Toast.makeText(this, R.string.fill_in_all_fields_toast, Toast.LENGTH_LONG).show();
                } else {
                    Product product = new Product(
                            foodNameText, foodGroupText,
                            Integer.parseInt(expiryDaysText),
                            Integer.parseInt(expiryDaysText),
                            Integer.parseInt(expiryDaysText),
                            Integer.parseInt(expiryDaysText));
                    Log.i("newProduct",product.getName() + product.getType()
                            + product.getDefaultExpiryDays());

                    products.add(product);
                    updateCsvFile(this, products.getProductList(), "item_list.csv");
                    reloadProductsAndUpdateAdapter();  // Reload and update adapter

                    addItemDialog.dismiss();
                }
            });
        });

        foodGroupBtn.setOnClickListener(v -> showCategoryPopupMenu(foodGroupBtn));
        storageLocationBtn.setOnClickListener(v -> showCategoryPopupMenu(storageLocationBtn));
        addItemDialog.show();
    }


    /**
     * Reload the product list from the CSV file and update the RecyclerView adapter.
     * This method ensures that any changes made to the product list are reflected in the UI.
     * @author u7515796 ChenboYan
     *
     */
    private void reloadProductsAndUpdateAdapter() {
        products = ProductUtils.getInstance(this, "item_list.csv");
        allProductsList = products.getProductList(); // Update the complete product list
        adapter.updateAllProductsList(allProductsList); // Update the adapter with the complete list
        adapter.updateProductList(allProductsList); // Update the adapter with the complete list
        adapter.notifyDataSetChanged();
    }

    /**
     * Show a popup menu next to the specified button, allowing users to select a category.
     * Depending on the button clicked (food group or storage location), the menu is populated with relevant options.
     * @param button The button associated with the popup menu.
     * @author u7515796 ChenboYan
     *
     */
    private void showCategoryPopupMenu(Button button) {
        String[] menuItems;

        if (button.equals(foodGroupBtn)) {
            menuItems = new String[]{"Dairy", "Fruit", "Grain", "Protein", "Vegetable", "Other"};
        } else if (button.equals(storageLocationBtn)) {
            menuItems = new String[]{"Freezer", "Fridge", "Pantry", "Other"};
        } else {
            return;
        }

        // Set up a popup menu that appears next to the clicked button
        PopupMenu categoryPopupMenu = new PopupMenu(this, button);

        for (int i = 0; i < menuItems.length; i++) {
            categoryPopupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuItems[i]);
        }

        categoryPopupMenu.setOnMenuItemClickListener(item -> {
            int itemIndex = item.getItemId();
            if (itemIndex >= 0 && itemIndex < menuItems.length) {
                button.setText(menuItems[itemIndex]);
                return true;
            } else {
                return false;
            }
        });

        categoryPopupMenu.show();
    }

}

