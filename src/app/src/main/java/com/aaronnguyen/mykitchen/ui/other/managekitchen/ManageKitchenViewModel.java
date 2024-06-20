package com.aaronnguyen.mykitchen.ui.other.managekitchen;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.model.user.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ManageKitchenViewModel extends ViewModel {

    private ManageKitchenActivity homeActivity;

    private List<String> residentsID;
    private List<String> residents;
    private List<String> pendingResidentsID;
    private List<String> pendingResidents;
    private List<String> bannedResidentsID;
    private List<String> bannedResidents;

    public ManageKitchenViewModel() {
        residents = new ArrayList<>();
        pendingResidents = new ArrayList<>();
        bannedResidents = new ArrayList<>();

        residentsID = new ArrayList<>();
        pendingResidentsID = new ArrayList<>();
        bannedResidentsID = new ArrayList<>();
    }

    public void setActivity(ManageKitchenActivity activity) {
        this.homeActivity = activity;
    }

    public List<String> getResidents() {
        return residents;
    }

    public List<String> getResidentsID() {
        return residentsID;
    }

    public List<String> getPendingResidents() {
        return pendingResidents;
    }

    public List<String> getBannedResidents() {
        return bannedResidents;
    }

    public boolean removeActiveResidents(int index) {
        return homeActivity.getKitchen().removeActiveResidents(residentsID.get(index));
    }

    public boolean banActiveResidents(int index) {
        return homeActivity.getKitchen().banActiveResidents(residentsID.get(index));
    }

    public void removeBannedResidents(int index) {
        homeActivity.getKitchen().removeBannedResidents(bannedResidentsID.get(index));
    }

    public boolean approveResidentsRequest(int index) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        if (currentUserID != null) {
            return homeActivity.getKitchen().approveResidentsRequest(currentUserID, pendingResidentsID.get(index));
        }
        return false;
    }

    public boolean rejectResidentsRequest(int index) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        if (currentUserID != null) {
            return homeActivity.getKitchen().rejectResidentRequest(currentUserID, pendingResidentsID.get(index));
        }
        return false;
    }


    /**
     * This function refresh the list residents and their ids.
     * <p>
     *     Note that residentsID cannot be directly assigned to userIDs as this might have different order
     * </p>
     * @param userIDs
     */
    public void refreshResidents(final List<String> userIDs) {
        Log.i("DEBUG", "New list of residents are: " + userIDs.toString());

        if(userIDs.isEmpty()) {
            residents.clear();
            residentsID.clear();
            homeActivity.onResidentsListViewChanges();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(UserDaoFirebase.USER_COLLECTION_NAME).whereIn(FieldPath.documentId(), userIDs).get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        residents.clear();
                        residentsID.clear();

                        queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                            if (documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME) != null) {
                                residents.add(documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME).toString());
                            } else if (documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME) != null) {
                                residents.add(documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME).toString());
                            } else {
                                residents.add(documentSnapshot.getId());
                            }

                            residentsID.add(documentSnapshot.getId());

                            Log.i("DEBUG", "(Active) Successfully processed " + documentSnapshot.getId());
                            Log.i("DEBUG", "The list of residents are: " + residents.toString());
                            homeActivity.onResidentsListViewChanges();
                        });
                    }
                }
        );
    }

    public void refreshPendingResidents(final List<String> userIDs) {
        if(userIDs.isEmpty()) {
            pendingResidents.clear();
            pendingResidentsID.clear();
            homeActivity.onPendingResidentsListViewChanges();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(UserDaoFirebase.USER_COLLECTION_NAME).whereIn(FieldPath.documentId(), userIDs).get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        pendingResidents.clear();
                        pendingResidentsID.clear();

                        queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                            if (documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME) != null) {
                                pendingResidents.add(documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME).toString());
                            } else if (documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME) != null) {
                                pendingResidents.add(documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME).toString());
                            } else {
                                pendingResidents.add(documentSnapshot.getId());
                            }

                            pendingResidentsID.add(documentSnapshot.getId());

                            Log.i("DEBUG", "(Active) Successfully processed " + documentSnapshot.getId());
                            Log.i("DEBUG", "The list of residents are: " + pendingResidents.toString());
                            homeActivity.onPendingResidentsListViewChanges();
                        });
                    }
                }
        );
    }

    public void refreshBannedResidents(final List<String> userIDs) {
        if(userIDs.isEmpty()) {
            bannedResidents.clear();
            bannedResidentsID.clear();
            homeActivity.onBannedResidentsListViewChanges();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(UserDaoFirebase.USER_COLLECTION_NAME).whereIn(FieldPath.documentId(), userIDs).get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        bannedResidents.clear();
                        bannedResidentsID.clear();

                        queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                            if (documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME) != null) {
                                bannedResidents.add(documentSnapshot.get(UserDaoFirebase.USER_NAME_FIELD_NAME).toString());
                            } else if (documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME) != null) {
                                bannedResidents.add(documentSnapshot.get(UserDaoFirebase.EMAIL_FIELD_NAME).toString());
                            } else {
                                bannedResidents.add(documentSnapshot.getId());
                            }

                            bannedResidentsID.add(documentSnapshot.getId());


                            homeActivity.onBannedResidentsListViewChanges();
                        });
                    }
                }
        );
    }
}
