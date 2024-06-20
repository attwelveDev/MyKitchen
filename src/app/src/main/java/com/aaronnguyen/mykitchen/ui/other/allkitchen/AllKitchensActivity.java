package com.aaronnguyen.mykitchen.ui.other.allkitchen;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.DAO.KitchenData;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Search.ProductUtils;
import com.aaronnguyen.mykitchen.ui.ButtonRequiringEditText;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;
import com.aaronnguyen.mykitchen.ui.other.managekitchen.ManageKitchenActivity;
import com.aaronnguyen.mykitchen.ui.other.setting.SettingsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import java.util.List;

/**
 * The activity displaying all of the kitchens for the user.
 * The user can also create or join kitchens.
 *
 * @author u7333216 Aaron Nguyen
 */
public class AllKitchensActivity extends AppCompatActivity {
    // Declare UI elements
    private Button profileBtn;
    private FloatingActionButton joinCreateBtn;
    private GridView kitchenGridView;
    private ProgressBar progressBar;
    private TextView noKitchensLbl;

    private AllKitchensViewModel allKitchensViewModel;

    private final int MANAGE_KITCHEN_CONTEXT_MENU_ITEM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Necessary set up for transition between grid view item and kitchen home

        //initialise product list read on startup
        ProductUtils.getInstance(this, "item_list.csv");

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_kitchens);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise UI elements
        profileBtn = findViewById(R.id.profile_btn);
        joinCreateBtn = findViewById(R.id.join_create_btn);
        kitchenGridView = findViewById(R.id.kitchen_grid_view);
        progressBar = findViewById(R.id.all_kitchens_progress_bar);
        noKitchensLbl = findViewById(R.id.no_kitchens_lbl);

        // Set and initialise the view model
        allKitchensViewModel = new ViewModelProvider(this).get(AllKitchensViewModel.class);
        allKitchensViewModel.getUiState().observe(this, uiStateObserver());
        allKitchensViewModel.init();

        // Data from database needs to be loaded on start up
        progressBar.setVisibility(View.VISIBLE);

        profileBtn.setOnClickListener(v -> goToSettingsActivity());
        joinCreateBtn.setOnClickListener(v -> showJoinCreateAlertDialog());

        kitchenGridView.setOnItemClickListener((parent, view, position, id) -> {
            AllKitchensViewModel.AllKitchensUiState uiState = allKitchensViewModel.getUiState().getValue();
            if (uiState == null) {
                return;
            }

            List<KitchenData> kitchens = allKitchensViewModel.getUiState().getValue().getKitchens();

            Intent goToKitchenIntent = new Intent(getApplicationContext(), KitchenHomeActivity.class);
            goToKitchenIntent.putExtra("kitchen_id", kitchens.get(position).getKitchenID());
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this, view, "transition_to_kitchen_home");
            startActivity(goToKitchenIntent, activityOptions.toBundle());
        });

        // Enable long press on list view to open context menu for managing a kitchen
        registerForContextMenu(kitchenGridView);
    }

    /**
     * Show the add kitchen alert dialog that appears when the user goes to add a new kitchen.
     * Users can enter a name for the kitchen. Users can choose between creating a new kitchen or join an existing one.
     */
    private void showJoinCreateAlertDialog() {
        View addKitchenView = getLayoutInflater().inflate(R.layout.add_kitchen_alert_dialog, null);

        TabLayout addTabs = addKitchenView.findViewById(R.id.create_join_kitchen_tabs);
        final int CREATE_KITCHEN_TAB_POSITION = 0;

        EditText kitchenNameEditText = addKitchenView.findViewById(R.id.kitchen_name_edit_text);

        MaterialAlertDialogBuilder joinCreateDialogBuilder = new MaterialAlertDialogBuilder(AllKitchensActivity.this);
        joinCreateDialogBuilder.setView(addKitchenView).setPositiveButton(R.string.add_btn, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        AlertDialog joinCreateDialog = joinCreateDialogBuilder.create();
        joinCreateDialog.setOnShowListener(dialog -> {
            Button positiveButton = joinCreateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {  // To prevent unintended dismiss (i.e. when invalid input)
                if (kitchenNameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.fill_in_one_field_toast, Toast.LENGTH_LONG).show();
                } else if (addTabs.getSelectedTabPosition() == CREATE_KITCHEN_TAB_POSITION) {  // Create kitchen
                    allKitchensViewModel.createKitchen(kitchenNameEditText.getText().toString());
                    dialog.dismiss();
                } else {  // Join kitchen
                    allKitchensViewModel.joinKitchen(kitchenNameEditText.getText().toString());
                    dialog.dismiss();
                }
            });

            addTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == CREATE_KITCHEN_TAB_POSITION) {
                        kitchenNameEditText.setHint(R.string.kitchen_name_edit_text);
                    } else {
                        kitchenNameEditText.setHint(R.string.kitchen_code);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) { }

                @Override
                public void onTabReselected(TabLayout.Tab tab) { }
            });

            if (kitchenNameEditText.getText().toString().isEmpty()) {
                positiveButton.setEnabled(false);
            }

            ButtonRequiringEditText.attachEditTextsToButton(positiveButton, new EditText[]{kitchenNameEditText});
        });
        joinCreateDialog.show();
    }

    private void goToSettingsActivity() {
        Intent signOutIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(signOutIntent);
    }

    /**
     * Set the observer for when the UI state changes. Primarily loads kitchens into the grid view.
     *
     * @return the observer for UI state changes.
     */
    private Observer<AllKitchensViewModel.AllKitchensUiState> uiStateObserver() {
        return allKitchensUiState -> {
            List<KitchenData> kitchens = allKitchensUiState.getKitchens();

            if (allKitchensUiState.getKitchens() == null) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            KitchenGridViewAdapter kitchenGridViewAdapter = new KitchenGridViewAdapter(this, kitchens);
            kitchenGridView.setAdapter(kitchenGridViewAdapter);

            if (kitchens.isEmpty()) {
                noKitchensLbl.setText(R.string.no_kitchens_lbl);
                noKitchensLbl.setVisibility(View.VISIBLE);
            } else {
                noKitchensLbl.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Add 'manage kitchen' item to context menu upon holding on a list view item
        menu.add(ContextMenu.NONE, MANAGE_KITCHEN_CONTEXT_MENU_ITEM, ContextMenu.NONE, R.string.manage_kitchen_menu_item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null || item.getItemId() != MANAGE_KITCHEN_CONTEXT_MENU_ITEM) {
            return super.onContextItemSelected(item);
        }

        AllKitchensViewModel.AllKitchensUiState uiState = allKitchensViewModel.getUiState().getValue();
        if (uiState == null) {
            return false;
        }

        List<KitchenData> kitchens = allKitchensViewModel.getUiState().getValue().getKitchens();

        // Start the manage kitchen activity with the kitchen item that is held on in the list view
        Intent manageKitchenIntent = new Intent(getApplicationContext(), ManageKitchenActivity.class);
        manageKitchenIntent.putExtra("kitchen_id", kitchens.get(info.position).getKitchenID());
        startActivity(manageKitchenIntent);

        return true;
    }
}