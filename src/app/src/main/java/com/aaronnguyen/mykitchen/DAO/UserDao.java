package com.aaronnguyen.mykitchen.DAO;

import android.net.Uri;

import com.aaronnguyen.mykitchen.model.user.User;

public interface UserDao {
    /**
     * This should create the user on the user collection
     * @param user The user to be created on the firebase firestore
     * @param writeListener The listener / callback for writing
     */
    void createUser(User user, WriteListener writeListener);

    /**
     * This fetches the user from the firebase firestore
     * <STRONG>This is only a one-time fetch.</STRONG>
     * <p>
     *     If you want real-time synchronisation with firestore, use {@code syncUser()}
     * </p>
     * @param uid The identifier of the user document that you wish to fetch
     * @param fetchListener The listener / callback when the fetch is finished
     */
    void fetchUser(String uid, FetchListener fetchListener);

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
    void syncUser(String uid, FetchListener fetchListener);

    /**
     * This function will fetch the user profile image given the user identifier
     * @param uid This is the user identifier
     * @param fetchListener This is the listener / callback when fetching is done
     */
    void fetchProfilePicture(String uid, FetchListener fetchListener);

    /**
     * This function will update the detail of the user
     * @param user This is the user object / detail container
     * @param writeListener This is listener / callback that is called when the write is completed
     */
    void updateUser(User user, WriteListener writeListener);

    /**
     * This function updates the user profile image
     * @param user The user object
     * @param uri The new profile picture given the uri
     * @param writeListener The write listener / callback when writing is done
     */
    void updateProfilePicture(User user, Uri uri, WriteListener writeListener);

    /**
     * This creates the kitchen and put the kitchen inside the user object
     * @param uid The user id
     * @param name The name of the user
     * @param writeListener The write listener / callback for the result when writing is completed
     */
    void createKitchen(String uid, String name, WriteListener writeListener);

    /**
     * This function will try to request to join the kitchen
     * @param userId The identifier of the user that is wanting to join the kitchen
     * @param kitchenId The identifier of the kitchen that the user want to join
     * @param writeListener The write listener / callback for the result when writing is completed
     */
    void joinKitchen(String userId, String kitchenId, WriteListener writeListener);

    /**
     * This function removes the kitchen from the user. (i.e. The user is kicked / banned from the kitchen)
     * @param userId The identifier of the user that is being removed
     * @param kitchenID The identifier of the kitchen that is getting removed from the user
     */
    void removeKitchen(String userId, String kitchenID);

    /**
     * This will add the kitchen to the user object. (i.e. This is typically use when the user
     * requested join a kitchen and get accepted)
     * @param userId This identifier of the user that the kitchen is added in
     * @param kitchenID This identifier of the kitchen that is added in the user
     * @param writeListener The write listener / callback when writing is completed
     */
    void addKitchen(String userId, String kitchenID, WriteListener writeListener);
}
