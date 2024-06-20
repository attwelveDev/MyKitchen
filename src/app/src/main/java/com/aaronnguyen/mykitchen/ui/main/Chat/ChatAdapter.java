package com.aaronnguyen.mykitchen.ui.main.Chat;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.databinding.ItemContainerReceiveMessageBinding;
import com.aaronnguyen.mykitchen.databinding.ItemContainerSentMessageBinding;
import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.squareup.picasso.Picasso;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenUserSubject;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenUsersObserver;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * This adapter handles everything for rendering the UI chat messages. i.e., this class takes care of
 * whether it is a received message or a sent message. Therefore, it is important that you set the correct
 * sender Id as the id of the user of the current session.
 * </p>
 *
 * <p>
 * <STRONG>
 *     Also, this adapter assumed that chat messages are never deleted to optimise performance of the recycler view.
 * </STRONG>
 * This is currently the case since we have not implemented deleting message and it is unlikely until this
 * project finished.
 * </p>
 *
 * @author Isaac Leong
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private final KitchenUserSubject kitchenUserSubject;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, KitchenUserSubject kitchenUserSubject) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.kitchenUserSubject = kitchenUserSubject;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceiveMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if(viewHolder instanceof ReceivedMessageViewHolder receivedMessageViewHolder) {
            kitchenUserSubject.detach(receivedMessageViewHolder);
            receivedMessageViewHolder.binding.imageProfile.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).getUserID().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getMessageTime());
        }
    }
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder implements KitchenUsersObserver {
        private final ItemContainerReceiveMessageBinding binding;
        private ChatMessage chatmessage;
        ReceivedMessageViewHolder(ItemContainerReceiveMessageBinding itemContainerReceiveBinding) {
            super(itemContainerReceiveBinding.getRoot());
            binding = itemContainerReceiveBinding;
            kitchenUserSubject.attach(this);
        }
        void setData(ChatMessage chatMessage) {
            this.chatmessage = chatMessage;
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getMessageTime());
            kitchenUserSubject.fetchUserInfo(userInfo -> {
                if(userInfo.get(chatMessage.getUserID()) != null) {
                    chatMessage.setUserName(userInfo.get(chatMessage.getUserID()).getUserName());
                    chatMessage.setProfilePicture(userInfo.get(chatMessage.getUserID()).getProfilePicture());
                };
                return null;
            });
            binding.userName.setText(chatMessage.getUserName());

            Uri uri = Uri.parse("android.resource://" + "com.aaronnguyen.mykitchen" + "/" + R.drawable.baseline_person_outline_24);
            binding.imageProfile.setImageURI(uri);

            if (chatMessage.getProfilePicture() != null) {
                Picasso.get().load(chatMessage.getProfilePicture().toString()).into(binding.imageProfile);
            }
        }

        @Override
        public void notify(HashMap<String, User> usersDictionary) {
            String uid = this.chatmessage.getUserID();

            System.out.println(usersDictionary);

            if(usersDictionary.get(uid) != null
                    && (!usersDictionary.get(uid).getUserName().equals(this.chatmessage.getUserName())
                    || usersDictionary.get(uid).getProfilePicture() != null)) {
                chatmessage.setUserName(Objects.requireNonNull(usersDictionary.get(uid)).getUserName());
                chatmessage.setProfilePicture(Objects.requireNonNull(usersDictionary.get(uid)).getProfilePicture());
                setData(chatmessage);
            }
        }
    }

    public void updateCacheData(List<ChatMessage> newData) {
        int differIndex = 0;

        for(differIndex = 0; differIndex < this.chatMessages.size(); differIndex++) {
            if(!newData.get(differIndex).equals(this.chatMessages.get(differIndex))) {
                break;
            }
        }

        this.chatMessages.clear();
        this.chatMessages.addAll(newData);
        this.notifyItemRangeInserted(differIndex, chatMessages.size() - differIndex);
    }
}