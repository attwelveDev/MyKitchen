package com.aaronnguyen.mykitchen.ui.main.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.databinding.ActivityChatBinding;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.ButtonRequiringEditText;
import com.aaronnguyen.mykitchen.ui.main.KitchenFragmentsViewModelFactory;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;

/**
 * This is the fragment for the chat pages.
 *
 * @author Isaac Leong
 */
public class ChatFragment extends Fragment {
    ActivityChatBinding binding;
    ChatViewModel pageViewModel;

    private ChatAdapter chatAdapter;

    private Kitchen kitchen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Kitchen kitchen = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.KITCHEN_INTENT_TAG, Kitchen.class);
        User user = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.USER_INTENT_TAG, User.class);

        if (kitchen == null) {
            return;
        }

        this.kitchen = kitchen;
        kitchen.setShouldFetchUserProfilePictures(true);

        pageViewModel = new ViewModelProvider(
                this,
                new KitchenFragmentsViewModelFactory(
                kitchen,
                user,
                KitchenFragmentsViewModelFactory.TargetViewModelCode.Chat
            )
        ).get(ChatViewModel.class);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStatus) {
        binding = ActivityChatBinding.inflate(inflater, container, false);

        chatAdapter = new ChatAdapter(
                pageViewModel.getState().getValue(),
                pageViewModel.getUserID(),
                pageViewModel.getKitchen()
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);

        pageViewModel.getState().observe(getViewLifecycleOwner(), chatMessages -> {
            chatAdapter.updateCacheData(chatMessages);
            binding.chatRecyclerView.smoothScrollToPosition(Integer.max(0, chatAdapter.getItemCount()));
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the send button
        binding.sendBtn.setOnClickListener(v -> {
            String inputText = binding.inputMessageBox.getText().toString();
            if(inputText.isBlank()) {
                Toast.makeText(getContext(), R.string.fill_in_one_field_toast, Toast.LENGTH_LONG).show();
                return;
            }
            pageViewModel.sendMessage(inputText);
            binding.inputMessageBox.setText(null);
        });

        ButtonRequiringEditText.attachEditTextsToButton(binding.sendBtn, new EditText[]{binding.inputMessageBox});

        // This will then create a new thread to fetch the database
        pageViewModel.startUpFetch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        kitchen.setShouldFetchUserProfilePictures(false);
    }

    public ChatViewModel getPageViewModel() {
        return pageViewModel;
    }
}