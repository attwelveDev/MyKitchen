package com.aaronnguyen.mykitchen.ui.other.login;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.DAO.UserDao;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.DAO.WriteListener;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The view model for the LoginActivity.
 * It holds the UI state, which depends on if the user is signed in, and also user interactions.
 *
 * @author u7333216 Aaron Nguyen
 */
public class LoginViewModel extends AndroidViewModel {
    private final MutableLiveData<LoginUiState> uiState
            = new MutableLiveData<>(new LoginUiState(true, false));

    private FirebaseAuth mAuth;
    private boolean didInit = false;

    public LoginViewModel(@NonNull Application application) {
        super(application);  // Required constructor
    }

    /**
     * Check if the user is already signed in and set the UI state if so.
     */
    protected void init() {
        if (didInit) {
            return;
        }
        didInit = true;

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            setUserFirebaseSignedIn();
        }
    }

    /**
     * Switch between signing in and creating an account and reflect in the UI state.
     */
    protected void switchSignInCreate() {
        LoginUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        uiState.setValue(new LoginUiState(
                !currentState.isSignInState,
                currentState.isFirebaseSignedIn
        ));
    }

    /**
     * Creates a user in Firebase Authentication if the given email and password are both valid.
     * This also logs the user in with their new email and password.
     *
     * @param email an email: must follow standard email convention.
     * @param password a password: must be at least 6 characters long.
     */
    protected void createUser(String email, String username, String password) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            displayMessage(R.string.fill_in_all_fields_toast);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (mAuth.getCurrentUser() == null) {
                    displayMessage(R.string.generic_error);
                    return;
                }

                User newUser = new User(
                        mAuth.getCurrentUser().getUid(),
                        username,
                        email,
                        new ArrayList<>()
                );

                UserDao userDao = UserDaoFirebase.getInstance();
                userDao.createUser(newUser, new WriteListener() {
                    @Override
                    public <T> void onWriteSuccess(T data) {
                        displayMessage(getApplication().getString(R.string.create_acc_success, username));
                        setUserFirebaseSignedIn();
                    }

                    @Override
                    public void onWriteFailure(Exception exception) {
                        displayMessage(R.string.generic_error);
                    }
                });
            } else {
                if (task.getException() == null) {
                    displayMessage(R.string.generic_error);
                    return;
                }

                displayMessage(task.getException().getLocalizedMessage());
            }
        });
    }

    /**
     * Signs in a user if they have provided an email and password registered with Firebase Authentication.
     *
     * @param email an email.
     * @param password a password.
     */
    protected void signIn(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            displayMessage(R.string.fill_in_all_fields_toast);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                UserDao userDao = UserDaoFirebase.getInstance();
                userDao.fetchUser(uid, new FetchListener() {
                    @Override
                    public <T> void onFetchSuccess(T data) {
                        if (data instanceof User) {
                            User user = (User) data;
                            updateFCMToken(user);
                        } else {
                            Log.e("LoginViewModel", "Fetched data is not an instance of User");
                            displayMessage(R.string.generic_error);
                        }
                    }
                    @Override
                    public void onFetchFailure(Exception e) {
                        displayMessage(R.string.generic_error);
                    }
                });

            } else {
                if (task.getException() == null) {
                    displayMessage(R.string.generic_error);
                    return;
                }

                displayMessage(task.getException().getLocalizedMessage());
            }
        });
    }

    private void updateFCMToken(User user) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put(UserDaoFirebase.FCM_TOKEN_FIELD_NAME, token);

            db.collection(UserDaoFirebase.USER_COLLECTION_NAME)
                    .document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        setUserFirebaseSignedIn();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginViewModel", "Failed to update FCM token", e);
                        displayMessage(R.string.generic_error);
                    });
        }).addOnFailureListener(e -> {
            Log.e("LoginViewModel", "Failed to get FCM token", e);
            displayMessage(R.string.generic_error);
        });
    }

    /**
     * Set the UI state such that the user is signed in.
     */
    private void setUserFirebaseSignedIn() {
        LoginUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        uiState.setValue(currentState.setUserFirebaseSignedIn());
    }

    /**
     * Display a Toast with a given message.
     *
     * @param message the message for the Toast.
     */
    private void displayMessage(@StringRes int message) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
    }
    private void displayMessage(String message) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Stores fields used for the LoginActivity.
     * Different states dictate how the UI appears and behaves.
     */
    protected static class LoginUiState {
        // true if user is signing in; false if creating an account
        private final boolean isSignInState;

        // true if user is signed in with Firebase Auth; false otherwise
        private final boolean isFirebaseSignedIn;

        protected LoginUiState(boolean isSignInState, boolean isFirebaseSignedIn) {
            this.isSignInState = isSignInState;
            this.isFirebaseSignedIn = isFirebaseSignedIn;
        }

        /**
         * Return a new UI state corresponding to the user being signed in with Firebase.
         * @return a UI state with the user signed in.
         */
        private LoginUiState setUserFirebaseSignedIn() {
            return new LoginUiState(isSignInState, true);
        }

        public boolean isSignInState() {
            return isSignInState;
        }

        public boolean isFirebaseSignedIn() {
            return isFirebaseSignedIn;
        }
    }

    protected MutableLiveData<LoginUiState> getUiState() {
        return uiState;
    }
}
