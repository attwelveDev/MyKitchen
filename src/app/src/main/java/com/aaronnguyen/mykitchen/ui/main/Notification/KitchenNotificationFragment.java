package com.aaronnguyen.mykitchen.ui.main.Notification;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.aaronnguyen.mykitchen.databinding.ActivityKitchenNotificationsBinding;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.KitchenFragmentsViewModelFactory;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;


/**
 * Fragment for displaying the notifications in a kitchen.
 * @author u7515796 ChengboYan
 */
public class KitchenNotificationFragment extends Fragment {
    NotificationViewModel notificationViewModel;
    NotificationAdapter notificationAdapter;
    ActivityKitchenNotificationsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Kitchen kitchen = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.KITCHEN_INTENT_TAG, Kitchen.class);
        User user = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.USER_INTENT_TAG, User.class);

        notificationViewModel = new ViewModelProvider(
                this,
                new KitchenFragmentsViewModelFactory(
                kitchen,
                user,
                KitchenFragmentsViewModelFactory.TargetViewModelCode.Notification
            )
        ).get(NotificationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityKitchenNotificationsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationViewModel.startUpFetch();

        notificationViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if(notifications.isEmpty()) {
                binding.noNotifsLbl.setVisibility(View.VISIBLE);
            } else {
                binding.noNotifsLbl.setVisibility(View.GONE);
                notificationAdapter = new NotificationAdapter(
                        getContext(),
                        notifications,
                        this
                );
                binding.notificationList.setAdapter(notificationAdapter);
                notificationAdapter.notifyDataSetChanged();
            }
        });
    }
    public NotificationViewModel getNotificationViewModel() {
        return notificationViewModel;
    }

    public  void deleteNotification(Notification notification){
        Log.i("NotificationViewModel","get" + notificationViewModel.toString());
        notificationViewModel.deleteNotification(notification);
    }
}