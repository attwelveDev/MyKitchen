package com.aaronnguyen.mykitchen.DAO;

import com.aaronnguyen.mykitchen.model.chat.ChatMessage;

/**
 * This is the data access object interface for chat room to access chat room documents
 */
public interface ChatRoomDAO {
    void sendMessage(String chatRoomId, ChatMessage chatMessage);

    void syncChatRoom(String chatRoomId, FetchListener fetchListener);

    void createChatRoom(WriteListener writeListener);

    void deleteChatRoom(String chatRoomId);
}
