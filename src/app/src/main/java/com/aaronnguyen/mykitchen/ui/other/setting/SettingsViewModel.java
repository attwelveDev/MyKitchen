package com.aaronnguyen.mykitchen.ui.other.setting;

import android.app.Application;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseUser;

/**
 * The view model for the SettingsActivity.
 * It holds the UI state, which depends on a database with user info, and also user interactions.
 *
 * @author u7333216 Aaron Nguyen
 */
public class SettingsViewModel extends AndroidViewModel {
    private final MutableLiveData<SettingsUiState> uiState
            = new MutableLiveData<>(new SettingsUiState(
                    null,
                    null,
                    null
            ));

    private UserDao userDao;
    private User user;
    private boolean didInit = false;

    public SettingsViewModel(@NonNull Application application) {
        super(application);  // Required constructor
    }

    /**
     * Fetch the info of the current user;
     * specifically, the username and profile picture are to be displayed.
     */
    protected void init() {
        if (didInit) {
            return;
        }
        didInit = true;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userDao = UserDaoFirebase.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            displayMessage(R.string.generic_error);
            return;
        }

        String uid = currentUser.getUid();
        userDao.syncUser(uid, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                user = (User) data;
                fetchedUser(user);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                displayMessage(R.string.generic_error);
            }
        });

        userDao.fetchProfilePicture(uid, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                fetchedProfilePicture((Uri) data);
            }

            @Override
            public void onFetchFailure(Exception exception) {
                displayMessage(R.string.generic_error);
            }
        });
    }

    /**
     * Reflect the username from the fetched user in the UI state.
     *
     * @param user the fetched user.
     */
    private void fetchedUser(User user) {
        SettingsUiState currentState = uiState.getValue();
        if (currentState == null) {
            return;
        }

        if (user.getUserName() == null) {
            return;
        }

        SettingsUiState newState = new SettingsUiState(
                currentState.existingPfpUri,
                currentState.newPfpUri,
                user.getUserName()
        );
        newState.didJustFetchUsername = true;
        uiState.setValue(newState);
    }

    /**
     * Reflect the fetched profile picture in the UI state.
     *
     * @param uri the URI of the profile picture.
     */
    private void fetchedProfilePicture(Uri uri) {
        SettingsUiState currentState = uiState.getValue();
        if (currentState == null) {
            return;
        }

        if (uri == null) {
            return;
        }

        uiState.setValue(new SettingsUiState(
                uri,
                null,
                currentState.existingUsername
        ));
    }

    /**
     * Reflect the new profile picture the user has selected in the UI state.
     *
     * @param uri the URI of the new profile picture.
     */
    protected void newProfilePictureSelected(Uri uri) {
        SettingsUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        uiState.setValue(new SettingsUiState(
                currentState.existingPfpUri,
                uri,
                currentState.existingUsername
        ));
    }

    /**
     * Save the profile picture or username if either has been changed by the user.
     *
     * @param currentUsernameText the current text of the username edit text.
     */
    protected void saveAllChanges(String currentUsernameText) {
        SettingsUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        if (currentUsernameText.isEmpty()) {
            displayMessage(R.string.fill_in_one_field_toast);
            return;
        }

        boolean didPfpChange = currentState.newPfpUri != null;
        boolean didUsernameChange = !currentState.existingUsername.equals(currentUsernameText);

        /*
        If profile picture or username has been changed by user,
        set the existing field to null so the UI transitions to a "loading" state.
         */
        Uri newExistingPfpUri = didPfpChange ? null : currentState.existingPfpUri;
        String newExistingUsername = didUsernameChange ? null : currentState.existingUsername;
        uiState.setValue(new SettingsUiState(
                newExistingPfpUri,
                currentState.newPfpUri,
                newExistingUsername
        ));

        if (didPfpChange) {
            saveProfilePicture(currentState.newPfpUri);
        }

        if (didUsernameChange) {
            saveUsername(currentUsernameText);
        }
    }

    /**
     * Save the new profile picture of the user and if successful, reflect in the UI state.
     *
     * @param uri the URI of the profile picture.
     */
    private void saveProfilePicture(Uri uri) {
        userDao.updateProfilePicture(user, uri, new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                displayMessage(R.string.save_pfp_success);

                SettingsUiState currentState = getUiState().getValue();
                if (currentState == null) {
                    return;
                }

                uiState.setValue(new SettingsUiState(
                        (Uri) data,
                        null,
                        currentState.existingUsername
                ));
            }

            @Override
            public void onWriteFailure(Exception exception) {
                displayMessage(R.string.save_error);
            }
        });
    }

    /**
     * Save the new username of the user.
     *
     * @param currentUsernameText the current text of the username edit text.
     */
    private void saveUsername(String currentUsernameText) {
        user.setUserName(currentUsernameText);
        userDao.updateUser(user, new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                displayMessage(R.string.save_username_success);

                /*
                State changes are handled automatically by fetch snapshot listener;
                assume any DAO implementation would have similar behaviour.
                */
            }

            @Override
            public void onWriteFailure(Exception exception) {
                displayMessage(R.string.save_error);
            }
        });
    }

    /**
     * Discard all current changes to username or profile picture, and reflect in the UI state.
     */
    protected void discardChanges() {
        SettingsUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        SettingsUiState newState = new SettingsUiState(
                currentState.existingPfpUri,
                null,
                currentState.existingUsername
        );
        newState.setDidDiscardChanges(true);
        uiState.setValue(newState);
    }

    /**
     * Check if there are currently unsaved changes to the profile picture or the username.
     *
     * @param currentUsernameText the current text of the username edit text.
     * @return true if there are currently unsaved changes; false otherwise.
     */
    protected boolean hasUnsavedChanges(String currentUsernameText) {
        SettingsUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return false;
        }

        if (isDataLoading()) {
            return false;
        }

        return !currentState.existingUsername.equals(currentUsernameText) || currentState.newPfpUri != null;
    }

    /**
     * Check if either the profile picture or username is currently loading.
     *
     * @return true if either profile picture or username are loading; false otherwise.
     */
    protected boolean isDataLoading() {
        SettingsUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return false;
        }

        return currentState.existingPfpUri == null || currentState.existingUsername == null;
    }

    /**
     * Display a Toast with a given message.
     *
     * @param message the message for the Toast.
     */
    private void displayMessage(@StringRes int message) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Stores fields used for the SettingsActivity.
     * Different states dictate how the UI appears and behaves.
     */
    protected static class SettingsUiState {
        private final Uri existingPfpUri;
        private final Uri newPfpUri;

        private final String existingUsername;
        private boolean didJustFetchUsername = false;
        private boolean didDiscardChanges = false;

        protected SettingsUiState(
                Uri existingPfpUri,
                Uri newPfpUri,
                String existingUsername) {
            this.existingPfpUri = existingPfpUri;
            this.newPfpUri = newPfpUri;
            this.existingUsername = existingUsername;
        }

        public void setDidJustFetchUsername(boolean didJustFetchUsername) {
            this.didJustFetchUsername = didJustFetchUsername;
        }

        public void setDidDiscardChanges(boolean didDiscardChanges) {
            this.didDiscardChanges = didDiscardChanges;
        }

        public Uri getExistingPfpUri() {
            return existingPfpUri;
        }

        public Uri getNewPfpUri() {
            return newPfpUri;
        }

        public String getExistingUsername() {
            return existingUsername;
        }

        public boolean getDidJustFetchUsername() {
            return didJustFetchUsername;
        }

        public boolean getDidDiscardChanges() {
            return didDiscardChanges;
        }
    }

    protected MutableLiveData<SettingsUiState> getUiState() {
        return uiState;
    }
}