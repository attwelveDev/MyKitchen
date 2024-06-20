package com.aaronnguyen.mykitchen.ui.other.allkitchen;

import static com.aaronnguyen.mykitchen.model.user.User.startExpiryCheckService;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.CustomExceptions.JoinKitchenException;
import com.aaronnguyen.mykitchen.DAO.KitchenDAO;
import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.KitchenData;
import com.aaronnguyen.mykitchen.DAO.UserDao;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.DAO.WriteListener;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The view model for the AllKitchensActivity.
 * It holds the UI state, which depends on a database with user and kitchen info.
 *
 * @author u7333216 Aaron Nguyen
 */
public class AllKitchensViewModel extends AndroidViewModel {
    private final MutableLiveData<AllKitchensUiState> uiState
            = new MutableLiveData<>(new AllKitchensUiState(null));

    private UserDao userDao;
    private User user;
    private List<String> kitchenIDs;
    private boolean didInit = false;

    public AllKitchensViewModel(@NonNull Application application) {
        super(application);  // Required constructor
    }

    /**
     * Fetch the info of the current user;
     * specifically, the list of kitchen IDs are used to display all the kitchens of the user.
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
    }

    /**
     * Extract the list of kitchen IDs of the fetched user.
     *
     * @param user the fetched user.
     */
    private void fetchedUser(User user) {
        AllKitchensUiState currentState = uiState.getValue();
        if (currentState == null) {
            return;
        }

        if (user.getKitchenIDs() == null) {
            return;
        }

        kitchenIDs = user.getKitchenIDs();

        fetchKitchenObjects(user.getKitchenIDs());
    }

    /**
     * Fetch the entire kitchens corresponding to the given kitchen IDs.
     *
     * @param kitchenIDs the list of kitchen IDs of the user.
     */
    private void fetchKitchenObjects(List<String> kitchenIDs) {
        AllKitchensUiState currentState = getUiState().getValue();
        if (currentState == null) {
            return;
        }

        // If list is empty, the kitchen map will not be initialised, so need to do it here
        if (kitchenIDs.isEmpty()) {
            uiState.setValue(new AllKitchensUiState(new HashMap<>()));
        }

        for (String kitchenID : kitchenIDs) {
            KitchenDAO kitchenDAO = KitchenFirebaseDAO.getInstance();
            kitchenDAO.syncKitchen(kitchenID, new FetchListener() {
                @Override
                public <T> void onFetchSuccess(T data) {
                    if(!(data instanceof KitchenData kitchenData))
                        return;

                    if (currentState.kitchens == null) {
                        currentState.kitchens = new HashMap<>();
                    }

                    currentState.kitchens.put(kitchenID, kitchenData);
                    uiState.setValue(new AllKitchensUiState(currentState.kitchens));
                }

                @Override
                public void onFetchFailure(Exception exception) {
                    // Invalid kitchen ID, e.g. kitchen deleted. Can ignore
                    if (exception instanceof IllegalArgumentException) {
                        return;
                    }

                    displayMessage(R.string.generic_error);
                }
            });
        }
    }

    /**
     * Create a kitchen with the given name for the current user.
     *
     * @param kitchenName the name of the kitchen to be created.
     */
    protected void createKitchen(String kitchenName) {
        userDao.createKitchen(user.getUid(), kitchenName, new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                startExpiryCheckService(getApplication(), (String) data);

                displayMessage(R.string.create_kitchen_success);

                /*
                State changes are handled automatically by fetch snapshot listener;
                assume any DAO implementation would have similar behaviour.
                */
            }

            @Override
            public void onWriteFailure(Exception exception) {
                displayMessage(R.string.generic_error);
            }
        });

    }

    /**
     * Send a join request for the kitchen with the given id.
     *
     * @param kitchenId the id of the kitchen to send a join request.
     */
    protected void joinKitchen(String kitchenId) {
        userDao.joinKitchen(user.getUid(), kitchenId, new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                displayMessage(R.string.request_join_kitchen_success);
            }

            @Override
            public void onWriteFailure(Exception exception) {
                if (exception instanceof JoinKitchenException joinKitchenException) {
                    // TODO: get switch to work; code goes into the correct branch but outputs the incorrect message
                    switch (joinKitchenException.getCode()) {
                        case BANNED            -> displayMessage(R.string.generic_error);
                        case ALREADY_REQUESTED -> displayMessage(R.string.already_requested_message);
                        case ALREADY_JOINED    -> displayMessage(R.string.already_joined_message);
                    }
                } else {
                    displayMessage(R.string.generic_error);
                }
            }
        });
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
     * Stores fields used for the AllKitchensActivity. The stored kitchens are displayed in the UI.
     */
    protected class AllKitchensUiState {
        private Map<String, KitchenData> kitchens;

        protected AllKitchensUiState(Map<String, KitchenData> kitchens) {
            this.kitchens = kitchens;
        }

        /**
         * Return a list of KitchenData objects whose ith object corresponds to the ith id in the list of kitchenIDs.
         *
         * @return the list of KitchenData objects accessible to the user.
         */
        protected List<KitchenData> getKitchens() {
            List<KitchenData> kitchens = new ArrayList<>();

            if (this.kitchens == null) {
                return null;
            }

            for (String kitchenID : kitchenIDs) {
                KitchenData kitchen = this.kitchens.get(kitchenID);
                if (kitchen != null) {
                    kitchens.add(kitchen);
                }
            }

            return kitchens;
        }
    }

    protected MutableLiveData<AllKitchensUiState> getUiState() {
        return uiState;
    }
}