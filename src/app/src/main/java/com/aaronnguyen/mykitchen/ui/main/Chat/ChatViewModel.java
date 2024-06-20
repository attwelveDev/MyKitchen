package com.aaronnguyen.mykitchen.ui.main.Chat;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.model.chat.ChatMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the chat view model that is responsible for temporarily storing the chat data
 *
 * @author Isaac Leong
 *
 */
public class ChatViewModel extends ViewModel {
    private MutableLiveData<List<ChatMessage>> chatMessagesData;
    private Kitchen kitchen;
    private User currentUser;

    public ChatViewModel(Kitchen kitchen, User currentUser) {
        this.kitchen = kitchen;
        this.currentUser = currentUser;
        chatMessagesData = new MutableLiveData<>(new ArrayList<>());
    }

    public void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(currentUser.getUid(), new Date(), message, currentUser.getUserName());
        kitchen.sendMessageOnline(chatMessage);
        kitchen.notifyChatToOthers(chatMessage, currentUser);
    }

    public void updateMessageList(List<ChatMessage> chatMessages) {
        Log.i("DEBUG", "updating the chat messages" + chatMessages);
        chatMessagesData.setValue(chatMessages);
    }

    public void startUpFetch() {
        this.chatMessagesData.setValue(kitchen.getChatRoom().getMessages());
    }

    public String getUserID() {
        return currentUser.getUid();
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    MutableLiveData<List<ChatMessage>> getState() {
        return chatMessagesData;
    }
}
