package com.aaronnguyen.mykitchen.model.chat;

import java.util.ArrayList;

/**
 * This is the listener for the chat room message update and when it is established
 * @author u7643339 Isaac Leong
 */
public interface ChatRoomListener {

    /**
     * Called when the chat messages are updated
     * @param chatMessages the whole chat history
     */
    void onChatListUpdateListener(ArrayList<ChatMessage> chatMessages);

    /**
     * This is called when the chat room has been successfully established
     */
    void onChatRoomEstablishedSuccess();
}
