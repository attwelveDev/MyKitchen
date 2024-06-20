package com.aaronnguyen.mykitchen.DAO;

import android.util.Log;

import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.aaronnguyen.mykitchen.model.chat.ChatRoom;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is an implementation of the chatRoom data access object
 * @author u7643339 Isaac Leong
 */
public class ChatRoomFirebaseDAO implements ChatRoomDAO {
    private static ChatRoomFirebaseDAO instance;
    public static ChatRoomFirebaseDAO getInstance() {
        if(instance == null)
            instance = new ChatRoomFirebaseDAO();
        return instance;
    }
    public static final String CHAT_ROOM_COLLECTION_NAME = "chat rooms";
    public static final String CHAT_FIELD_NAME = "chats";

    private FirebaseFirestore db;

    public ChatRoomFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This will send the given chat message to the chat room
     * @param chatRoomID The chat room id for the chat room to have the message send to
     * @param chatMessage The chat message that is going to be sent out
     */
    @Override
    public void sendMessage(String chatRoomID, ChatMessage chatMessage) {
        Map<String, Object> messageJSON = chatMessage.toJSONObject();
        db.collection(CHAT_ROOM_COLLECTION_NAME).document(chatRoomID)
                .update(CHAT_FIELD_NAME, FieldValue.arrayUnion(messageJSON));
    }

    /**
     * This function will synchronise the chat room object with chat room document online
     * @param chatRoomId The chat room of the document
     * @param fetchListener This is the fetch listener
     */
    @Override
    public void syncChatRoom(String chatRoomId, FetchListener fetchListener) {
        if(chatRoomId == null) {
            fetchListener.onFetchFailure(new IllegalArgumentException());
            return;
        }

        db.collection(CHAT_ROOM_COLLECTION_NAME).document(chatRoomId).addSnapshotListener((value, error) -> {
            if (value.get(ChatRoom.CHAT_FIELD_NAME) != null) {
                Log.i("DEBUG", "Syncing messages");
                List<Map<String, Object>> chatJSON = (List<Map<String, Object>>) value.get(CHAT_FIELD_NAME);
                ArrayList<ChatMessage> chatMessages = new ArrayList<>();
                for (var jsonObject : chatJSON) {
                    if (jsonObject.get(ChatMessage.USER_ID_FIELD_NAME) != null
                            && jsonObject.get(ChatMessage.USER_NAME_FIELD_NAME) != null
                            && jsonObject.get(ChatMessage.MESSAGE_FIELD_NAME) != null
                            && jsonObject.get(ChatMessage.TIME_FIELD_NAME) != null) {

                        SimpleDateFormat df = new SimpleDateFormat(ChatMessage.MESSAGE_TIME_FORMAT, Locale.ENGLISH);
                        Date sentDate = null;
                        try {
                            sentDate = df.parse((String) jsonObject.get(ChatMessage.TIME_FIELD_NAME));
                        } catch (ParseException e) {
                            fetchListener.onFetchFailure(new RuntimeException("Unable to parse this date " + jsonObject.get(ChatMessage.TIME_FIELD_NAME)));
                        }
                        ChatMessage chatMessage = new ChatMessage(
                                (String) jsonObject.get(ChatMessage.USER_ID_FIELD_NAME),
                                sentDate,
                                (String) jsonObject.get(ChatMessage.MESSAGE_FIELD_NAME),
                                (String) jsonObject.get(ChatMessage.USER_NAME_FIELD_NAME)
                        );
                        chatMessages.add(chatMessage);
                    }
                }
                fetchListener.onFetchSuccess(chatMessages);
            }
        });
    }

    /**
     * This function will create the chat room
     * @param writeListener The write listener to listen for the result
     */
    @Override
    public void createChatRoom(WriteListener writeListener) {
        Map<String, Object> newChatRoom = new HashMap<>();
        newChatRoom.put(ChatRoom.CHAT_FIELD_NAME, Collections.EMPTY_LIST);
        db.collection(CHAT_ROOM_COLLECTION_NAME)
                .add(newChatRoom)
                .addOnSuccessListener(documentReference -> writeListener.onWriteSuccess(documentReference.getId()))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function will delete the chat room document on the firebase firestore
     * @param chatRoomID The chat room document identifier for the chat room that we are deleting
     */
    @Override
    public void deleteChatRoom(String chatRoomID) {
        db.collection(ChatRoom.CHAT_ROOM_COLLECTION_NAME).document(chatRoomID)
                .delete();
    }
}
