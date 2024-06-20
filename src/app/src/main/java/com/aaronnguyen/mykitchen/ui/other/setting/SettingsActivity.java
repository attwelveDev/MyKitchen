package com.aaronnguyen.mykitchen.ui.other.setting;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.ui.ButtonRequiringEditText;
import com.aaronnguyen.mykitchen.ui.other.login.LoginActivity;
import com.aaronnguyen.mykitchen.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

/**
 * The activity containing user-centric settings for the application.
 * The user can change username and profile picture, sign out, or toggle night mode.
 *
 * @author u7333216 Aaron Nguyen (unless otherwise indicated)
 */
public class SettingsActivity extends AppCompatActivity {
    // Declare UI elements
    private TextView usernameTitleTextView;
    private ImageView pfpImageView;
    private EditText usernameEditText;
    private ProgressBar progressBar;
    private Button changePfpBtn;
    private Button discardBtn;
    private Button saveBtn;
    private Switch switchMode;
    private Button signOutBtn;

    // Night mode fields
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Other fields
    private FirebaseAuth mAuth;
    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise UI elements
        pfpImageView = findViewById(R.id.settings_pfp_image_view);
        usernameTitleTextView = findViewById(R.id.username_title_lbl);
        progressBar = findViewById(R.id.settings_progress_bar);
        usernameEditText = findViewById(R.id.username_edit_text);
        changePfpBtn = findViewById(R.id.change_pfp_btn);
        discardBtn = findViewById(R.id.settings_discard_btn);
        saveBtn = findViewById(R.id.settings_save_btn);
        switchMode = findViewById(R.id.night_mode_switch);
        signOutBtn = findViewById(R.id.sign_out_btn);

        // Set and initialise the view model
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.getUiState().observe(this, uiStateObserver());
        settingsViewModel.init();

        ActivityResultLauncher<PickVisualMediaRequest> imagePicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> {
            if (o != null) {
                settingsViewModel.newProfilePictureSelected(o);
            }
        });

        setStartUI();

        changePfpBtn.setOnClickListener(v -> imagePicker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        saveBtn.setOnClickListener(v -> settingsViewModel.saveAllChanges(usernameEditText.getText().toString()));
        discardBtn.setOnClickListener(v -> settingsViewModel.discardChanges());

        ButtonRequiringEditText.attachEditTextsToButton(saveBtn, new EditText[]{usernameEditText});

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (settingsViewModel.hasUnsavedChanges(s.toString())) {
                    setUnsavedChangesUI();
                } else {
                    setNoChangesUI();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        setupNightMode();

        mAuth = FirebaseAuth.getInstance();
        signOutBtn.setOnClickListener(v -> showSignOutDialog());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            goToSignedOutActivity();
        }
    }

    private void goToSignedOutActivity() {
        Intent signOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(signOutIntent);
    }

    /**
     * Show the sign out confirmation dialog and sign out the user if they confirm.
     */
    private void showSignOutDialog() {
        MaterialAlertDialogBuilder signOutDialogBuilder = new MaterialAlertDialogBuilder(SettingsActivity.this);
        signOutDialogBuilder.setTitle(R.string.sign_out_dialog_title).setMessage(R.string.sign_out_confirm_prompt).setPositiveButton(R.string.sign_out, (dialog, which) -> {
            mAuth.signOut();

            goToSignedOutActivity();
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        AlertDialog signOutDialog = signOutDialogBuilder.create();
        signOutDialog.show();
    }

    /**
     * Set the observer for when the UI state changes.
     * Change various parts of the activity accordingly, particularly the "profile section".
     *
     * @return the observer for UI state changes.
     */
    private Observer<SettingsViewModel.SettingsUiState> uiStateObserver() {
        return settingsUiState -> {
            // Get fields from the UI state
            Uri existingPfpUri = settingsUiState.getExistingPfpUri();
            Uri newPfpUri = settingsUiState.getNewPfpUri();
            String existingUsername = settingsUiState.getExistingUsername();
            boolean didJustFetchUsername = settingsUiState.getDidJustFetchUsername();
            boolean didDiscardChanges = settingsUiState.getDidDiscardChanges();

            if (existingUsername != null) {
                usernameTitleTextView.setText(existingUsername);
            }

            if ((didJustFetchUsername || didDiscardChanges) && existingUsername != null) {
                settingsUiState.setDidJustFetchUsername(false);
                settingsUiState.setDidDiscardChanges(false);

                usernameEditText.setText(existingUsername);
                usernameEditText.setSelection(existingUsername.length());
            }

            String currentUsernameText = usernameEditText.getText().toString();

            if (settingsViewModel.hasUnsavedChanges(currentUsernameText)) {
                setUnsavedChangesUI();
            } else {
                setNoChangesUI();
            }

            if (settingsViewModel.isDataLoading()) {
                progressBar.setVisibility(View.VISIBLE);

                // Don't allow further edits until new change/s is/are saved
                changePfpBtn.setEnabled(false);
                usernameEditText.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);

                usernameEditText.setEnabled(true);
                changePfpBtn.setEnabled(true);
            }

            if (newPfpUri != null) {
                loadUriIntoPfpImageView(newPfpUri);
            } else if (existingPfpUri != null) {
                loadUriIntoPfpImageView(existingPfpUri);
            }
        };
    }

    /**
     * Setup the night mode button and load its previously saved state.
     * Change the appearance of the app according to the night mode button.
     *
     * @author u7643339 Isaac Leong
     */
    private void setupNightMode() {
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);

        if(nightMode) {
            switchMode.setChecked(true);
            uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
        }
        switchMode.setOnClickListener(view -> {
            if(nightMode) {
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", false);
            } else {
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", true);
            }
            editor.apply();
        });
    }

    /**
     * Show starting UI.
     * User cannot change username or profile picture until existing ones are loaded.
     */
    private void setStartUI() {
        usernameEditText.setEnabled(false);
        changePfpBtn.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        setNoChangesUI();
    }

    /**
     * Show the discard and save buttons for the username and profile picture.
     */
    private void setNoChangesUI() {
        discardBtn.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);
    }

    /**
     * Hide the discard and save buttons for the profile picture and username.
     */
    private void setUnsavedChangesUI() {
        discardBtn.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Given a URI, load its corresponding image into the profile picture image view.
     * Library reference: <a href="ref">https://github.com/square/picasso</a>.
     *
     * @param uri the URI to be loaded into the profile picture image view.
     */
    private void loadUriIntoPfpImageView(Uri uri) {
        Picasso.get().load(uri.toString()).into(pfpImageView);
    }
}