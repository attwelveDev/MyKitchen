package com.aaronnguyen.mykitchen.ui.other.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.ui.ButtonRequiringEditText;
import com.aaronnguyen.mykitchen.ui.other.allkitchen.AllKitchensActivity;

/**
 * The activity allowing the user to sign in or create a new account.
 * If the user is already signed in, go to AllKitchensActivity.
 *
 * @author u7333216 Aaron Nguyen
 */
public class LoginActivity extends AppCompatActivity {
    // Declare UI elements
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText pwdEditText;
    private Button primaryBtn;
    private Button switchBtn;

    private EditText[] allEditTexts;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialise UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        usernameEditText = findViewById(R.id.login_username_edit_text);
        pwdEditText = findViewById(R.id.pwd_edit_text);
        primaryBtn = findViewById(R.id.primary_btn);
        switchBtn = findViewById(R.id.switch_btn);

        allEditTexts = new EditText[]{emailEditText, usernameEditText, pwdEditText};

        // Require all fields to be non-empty for primaryBtn to be enabled
        ButtonRequiringEditText.attachEditTextsToButton(primaryBtn, allEditTexts);

        // Set and initialise the view model
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getUiState().observe(this, uiStateObserver());
        loginViewModel.init();

        primaryBtn.setOnClickListener(v -> signInOrCreateAccount());
        switchBtn.setOnClickListener(v -> loginViewModel.switchSignInCreate());
    }

    private void goToSignedInActivity() {
        Intent signInIntent = new Intent(getApplicationContext(), AllKitchensActivity.class);
        startActivity(signInIntent);
    }

    /**
     * Set the observer for when the UI state changes.
     * Go to AllKitchensActivity if the user is signed in;
     * display log in or create account options depending on what the user has clicked.
     *
     * @return the observer for UI state changes.
     */
    private Observer<LoginViewModel.LoginUiState> uiStateObserver() {
        return loginUiState -> {
            boolean isSigningIn = loginUiState.isSignInState();
            boolean isFirebaseSignedIn = loginUiState.isFirebaseSignedIn();

            if (isFirebaseSignedIn) {
                goToSignedInActivity();
                return;
            }

            if (isSigningIn) {
                primaryBtn.setText(R.string.sign_in_btn);
                switchBtn.setText(R.string.switch_to_create_acc_btn);

                usernameEditText.setVisibility(View.GONE);
            } else {
                primaryBtn.setText(R.string.create_acc_btn);
                switchBtn.setText(R.string.switch_to_sign_in_btn);

                usernameEditText.setVisibility(View.VISIBLE);
            }

            ButtonRequiringEditText.buttonCheck(primaryBtn, allEditTexts);
        };
    }

    /**
     * Depending on if the user is to signing in or creating a new account, perform that action.
     */
    private void signInOrCreateAccount() {
        String email = emailEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String pwd = pwdEditText.getText().toString();

        LoginViewModel.LoginUiState currentState = loginViewModel.getUiState().getValue();
        if (currentState == null) {
            return;
        }

        if (currentState.isSignInState()) {
            loginViewModel.signIn(email, pwd);
        } else {
            loginViewModel.createUser(email, username, pwd);
        }
    }
}