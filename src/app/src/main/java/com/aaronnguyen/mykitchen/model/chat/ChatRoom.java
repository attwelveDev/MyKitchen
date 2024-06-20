package com.aaronnguyen.mykitchen.model.chat;

import android.util.Log;

import com.aaronnguyen.mykitchen.DAO.ChatRoomFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.FetchListener;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is a representation of the chat room.
 * The aim of implementing this class is to normalise the database so that the kitchen document is not enormous
 * @author u7643339 Isaac Leong
 */
public class ChatRoom {
    public static final String CHAT_ROOM_COLLECTION_NAME = "chat rooms";
    public static final String CHAT_FIELD_NAME = "chats";
    private String chatID;
    private ArrayList<ChatMessage> messageList;
    private ChatRoomListener chatRoomListener;

    /**
     * This is a constructor for a chat Room. If the update on chat room is not required / not used,
     * then {@code chatRoomListener} can be {@code null}.
     * @param chatID The chat room id
     * @param chatRoomListener The chat room listener that will listen to updates of messages of the chatroom
     */
    public ChatRoom(String chatID, ChatRoomListener chatRoomListener) {
        this.chatID = chatID;
        this.chatRoomListener = chatRoomListener;
        syncChatRoomRealTime();
    }

    /**
     *  This function wil synchronise {@code this} to the data on Firebase
     */
    private void syncChatRoomRealTime() {
        if(this.chatID == null){
            Log.e("DEBUG", "Phantom chat room.");
            return;
        }

        ChatRoomFirebaseDAO.getInstance().syncChatRoom(this.chatID, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                if(!(data instanceof ArrayList<?>))
                    return;

                ArrayList<ChatMessage> chatMessages = (ArrayList<ChatMessage>) data;
                if(!Objects.equals(messageList, chatMessages)) {
                    messageList = chatMessages;
                    checkUpdateViewModel();
                }
            }

            @Override
            public void onFetchFailure(Exception exception) {
                Log.e("DEBUG", "Unable to fetch chat list" + exception.getMessage());
            }
        });
    }

    /**
     * This function will check if there is a chatRoomListener. If so, then it calls the listener to update.
     */
    private void checkUpdateViewModel() {
        if(chatRoomListener != null) {
            Log.i("DEBUG", "Updating message list - using chatroom listener");
            chatRoomListener.onChatListUpdateListener(messageList);
        }
    }

    public ArrayList<ChatMessage> getMessages() {
        return messageList;
    }

    /**
     *  This function will send a message through {@code ChatRoomFirebaseDAO} object.
     * @param message The message ot be sent. This should not be null otherwise the message will not be sent.
     */
    public void sendMessage(ChatMessage message) {
        if(message == null)
            return;
        ChatRoomFirebaseDAO.getInstance().sendMessage(this.chatID, message);
    }
}
