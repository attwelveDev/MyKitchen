package com.aaronnguyen.mykitchen.DAO;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aaronnguyen.mykitchen.CustomExceptions.JoinKitchenException;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.user.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserDaoFirebase implements UserDao {
    public static final String USER_COLLECTION_NAME = "users";
    public static final String KITCHENS_FIELD_NAME = "kitchens";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String USER_NAME_FIELD_NAME = "user name";
    public static final String FCM_TOKEN_FIELD_NAME = "fcmToken";

    public static final String PROFILE_PICTURE_PATH = "profile_pictures/";

    public static final String PACKAGE_NAME = "com.aaronnguyen.mykitchen";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static UserDaoFirebase instance;

    private UserDaoFirebase() { }

    public static UserDaoFirebase getInstance() {
        if (instance == null) {
            instance = new UserDaoFirebase();
        }

        return instance;
    }

    @Override
    public void createUser(User user, WriteListener writeListener) {
        db.collection(USER_COLLECTION_NAME)
                .document(user.getUid())
                .set(user.toJSONObject())
                .addOnSuccessListener(unused -> saveUserAndToken(user, writeListener))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * @author u7515796 Chengbo Yan
     */
    private void saveUserAndToken(User user, WriteListener writeListener) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put(FCM_TOKEN_FIELD_NAME, s);

            db.collection(USER_COLLECTION_NAME)
                    .document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(unused -> writeListener.onWriteSuccess(user))
                    .addOnFailureListener(writeListener::onWriteFailure);
        }).addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This fetches the user from the firebase firestore
     * <STRONG>This is only a one-time fetch.</STRONG>
     * <p>
     *     If you want real-time synchronisation with firestore, use {@code syncUser()}
     * </p>
     * @param uid The identifier of the user document that you wish to fetch
     * @param fetchListener The listener / callback when the fetch is finished
     */
    @Override
    public void fetchUser(String uid, FetchListener fetchListener) {
        db.collection(USER_COLLECTION_NAME).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.getString(USER_NAME_FIELD_NAME) != null
                        && documentSnapshot.getString(EMAIL_FIELD_NAME) != null
                        && documentSnapshot.getString(FCM_TOKEN_FIELD_NAME) != null
                        && documentSnapshot.get(KITCHENS_FIELD_NAME) != null) {

                        fetchListener.onFetchSuccess(
                            new User(
                                uid,
                                documentSnapshot.getString(USER_NAME_FIELD_NAME),
                                documentSnapshot.getString(EMAIL_FIELD_NAME),
                                (List<String>) documentSnapshot.get(KITCHENS_FIELD_NAME),
                                documentSnapshot.getString(FCM_TOKEN_FIELD_NAME)
                            )
                        );
                    }
                })
                .addOnFailureListener(fetchListener::onFetchFailure);
    }

    /**
     * This will create a real-time synchronisation channel with firestore. <STRONG>Note that this is costly
     * and expensive process that should not be overused</STRONG>>.
     *
     * <p>
     *     If you would only need a single fetch from the firebase, consider using {@code fetchUser()} instead.
     * </p>
     *
     * @param uid The user identifier that is aiming to synchronise with
     * @param fetchListener The listener / callback whenever there are changes on Firebase Firestore.
     */
    @Override
    public void syncUser(String uid, FetchListener fetchListener) {
        db.collection(USER_COLLECTION_NAME).document(uid).addSnapshotListener((value, error) -> {
            if (error != null) {
                fetchListener.onFetchFailure(error);
                return;
            }

            if (value == null || !value.exists()) {
                fetchListener.onFetchFailure(new IllegalArgumentException("Could not find document for user with UID " + uid));
                return;
            }

            String[] fields = new String[]{
                    EMAIL_FIELD_NAME,
                    KITCHENS_FIELD_NAME,
                    USER_NAME_FIELD_NAME,
                    FCM_TOKEN_FIELD_NAME
            };

            for (String field : fields) {
                if (value.get(field) == null) {
                    fetchListener.onFetchFailure(new IllegalArgumentException("Could not find all fields for user with UID " + uid));
                    return;
                }
            }

            @SuppressWarnings("unchecked")
            User user = new User(
                    uid,
                    String.valueOf(value.get(USER_NAME_FIELD_NAME)),
                    String.valueOf(value.get(EMAIL_FIELD_NAME)),
                    (List<String>) value.get(KITCHENS_FIELD_NAME),
                    value.getString(FCM_TOKEN_FIELD_NAME)
            );

            fetchListener.onFetchSuccess(user);
        });
    }

    /**
     * This function will fetch the user profile image given the user identifier
     * @param uid This is the user identifier
     * @param fetchListener This is the listener / callback when fetching is done
     */
    @Override
    public void fetchProfilePicture(String uid, FetchListener fetchListener) {
        storage.getReference()
                .child(PROFILE_PICTURE_PATH + uid)
                .getDownloadUrl()
                .addOnSuccessListener(fetchListener::onFetchSuccess)
                .addOnFailureListener(e -> {
                    // Error is not that the user does not have a profile picture
                    if (((StorageException) e).getErrorCode() != StorageException.ERROR_OBJECT_NOT_FOUND) {
                        fetchListener.onFetchFailure(e);
                        return;
                    }

                    // Otherwise, load in the default profile picture
                    Uri uri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.ic_default_profile_picture);
                    fetchListener.onFetchSuccess(uri);
                });
    }

    @Override
    public void updateUser(User user, WriteListener writeListener) {
        db.collection(USER_COLLECTION_NAME)
                .document(user.getUid())
                .set(user.toJSONObject(), SetOptions.merge())
                .addOnSuccessListener(unused -> writeListener.onWriteSuccess(user))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function updates the user profile image
     * @param user The user object
     * @param uri The new profile picture given the uri
     * @param writeListener The write listener / callback when writing is done
     */
    @Override
    public void updateProfilePicture(User user, Uri uri, WriteListener writeListener) {
        storage.getReference()
                .child(PROFILE_PICTURE_PATH + user.getUid())
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot -> writeListener.onWriteSuccess(uri))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This creates the kitchen and put the kitchen inside the user object
     * @param uid The user id
     * @param name The name of the user
     * @param writeListener The write listener / callback for the result when writing is completed
     */
    @Override
    public void createKitchen(String uid, String name, WriteListener writeListener) {
        ChatRoomFirebaseDAO.getInstance().createChatRoom(new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                if(!(data instanceof String chatID))
                    return;

                KitchenFirebaseDAO kitchenFirebaseDAO = KitchenFirebaseDAO.getInstance();
                kitchenFirebaseDAO.createKitchen(uid, name, chatID, new WriteListener() {
                    @Override
                    public <T> void onWriteSuccess(T data) {
                        putKitchenInUser(uid, (String) data, writeListener);
                    }

                    @Override
                    public void onWriteFailure(Exception exception) {
                        writeListener.onWriteFailure(exception);
                    }
                });
            }

            @Override
            public void onWriteFailure(Exception exception) {
                writeListener.onWriteFailure(exception);
            }
        });
    }

    /**
     * Put the given id of a kitchen in the "kitchens" field of the given user.
     *
     * @param userId the id of the user to put the kitchen in.
     * @param kitchenId the id of the kitchen that should be stored for the current user.
     * @param writeListener the listener called when the Firestore operation is complete.
     */
    private void putKitchenInUser(String userId, String kitchenId, WriteListener writeListener) {
        db.collection(UserDaoFirebase.USER_COLLECTION_NAME)
                .document(userId)
                .update(UserDaoFirebase.KITCHENS_FIELD_NAME, FieldValue.arrayUnion(kitchenId))
                .addOnSuccessListener(unused -> writeListener.onWriteSuccess(kitchenId))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function will try to request to join the kitchen
     * @param userId The identifier of the user that is wanting to join the kitchen
     * @param kitchenId The identifier of the kitchen that the user want to join
     * @param writeListener The write listener / callback for the result when writing is completed
     * @author u7643339 Isaac Leong
     * @author u7333216 Aaron Nguyen
     */
    @Override
    public void joinKitchen(String userId, String kitchenId, WriteListener writeListener) {
        if (userId == null) {
            writeListener.onWriteFailure(new IllegalArgumentException("User does not have an id"));
            return;
        }

        DocumentReference kitchenDoc = db.collection(KitchenFirebaseDAO.KITCHEN_COLLECTION_NAME).document(kitchenId);
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(kitchenDoc);

                Map<String, JoinKitchenException.Code> joinKitchenExceptions = new HashMap<>();
                joinKitchenExceptions.put(KitchenFirebaseDAO.BANNED_RESIDENTS_FIELD_NAME, JoinKitchenException.Code.BANNED);
                joinKitchenExceptions.put(KitchenFirebaseDAO.PENDING_RESIDENTS_FIELD_NAME, JoinKitchenException.Code.ALREADY_REQUESTED);
                joinKitchenExceptions.put(KitchenFirebaseDAO.RESIDENTS_FIELD_NAME, JoinKitchenException.Code.ALREADY_JOINED);

                for (String fieldName : joinKitchenExceptions.keySet()) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) snapshot.get(fieldName);

                    if (list == null) {
                        FirebaseFirestoreException exception = new FirebaseFirestoreException("Could not find " + fieldName + " field for kitchen with id " + kitchenId, FirebaseFirestoreException.Code.ABORTED);
                        writeListener.onWriteFailure(exception);
                        throw exception;
                    }

                    if (list.contains(userId)) {
                        JoinKitchenException.Code code = Objects.requireNonNull(joinKitchenExceptions.get(fieldName));
                        System.out.println(code);
                        writeListener.onWriteFailure(new JoinKitchenException(code));
                        throw new FirebaseFirestoreException("User with id " + userId + " cannot send request to join kitchen with id " + kitchenId + "; code:" + code.name(), FirebaseFirestoreException.Code.ABORTED);
                    }
                }

                transaction.update(db.collection(KitchenFirebaseDAO.KITCHEN_COLLECTION_NAME).document(kitchenId), KitchenFirebaseDAO.PENDING_RESIDENTS_FIELD_NAME, FieldValue.arrayUnion(userId));

                return null;
            }
        }).addOnSuccessListener(unused -> writeListener.onWriteSuccess(kitchenId)).addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function removes the kitchen from the user. (i.e. The user is kicked / banned from the kitchen)
     * @param userId The identifier of the user that is being removed
     * @param kitchenID The identifier of the kitchen that is getting removed from the user
     * @author u7643339 Isaac Leong
     */
    @Override
    public void removeKitchen(String userId, String kitchenID) {
        db.collection(USER_COLLECTION_NAME).document(userId).update(KITCHENS_FIELD_NAME, FieldValue.arrayRemove(kitchenID));
    }

    /**
     * This will add the kitchen to the user object. (i.e. This is typically use when the user
     * requested join a kitchen and get accepted)
     * @param userId This identifier of the user that the kitchen is added in
     * @param kitchenID This identifier of the kitchen that is added in the user
     * @param writeListener The write listener / callback when writing is completed
     * @author u7643339 Isaac Leong
     */
    @Override
    public void addKitchen(String userId, String kitchenID, WriteListener writeListener) {
        db.collection(USER_COLLECTION_NAME).document(userId).update(KITCHENS_FIELD_NAME, FieldValue.arrayUnion(kitchenID))
                .addOnSuccessListener(writeListener::onWriteSuccess)
                .addOnFailureListener(writeListener::onWriteFailure);
    }
}
