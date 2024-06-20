package com.aaronnguyen.mykitchen.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.Simulator.ActionSimulator;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.databinding.ActivityKitchenHomeBinding;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Search.ProductUtils;
import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.aaronnguyen.mykitchen.model.chat.ChatRoomListener;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenItemListener;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenMemberListener;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenPropertyListener;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Chat.ChatFragment;
import com.aaronnguyen.mykitchen.ui.main.History.HistoryFragment;
import com.aaronnguyen.mykitchen.ui.main.Notification.KitchenNotificationFragment;
import com.aaronnguyen.mykitchen.ui.main.Overview.KitchenOverviewFragment;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.CartExpandableListAdapter;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.ShoppingCartFragment;
import com.aaronnguyen.mykitchen.ui.other.allkitchen.AllKitchensActivity;
import com.aaronnguyen.mykitchen.ui.other.managekitchen.ManageKitchenActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.transition.platform.MaterialArcMotion;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class KitchenHomeActivity extends AppCompatActivity implements KitchenPropertyListener, KitchenMemberListener, KitchenItemListener, ChatRoomListener {

    public static final String KITCHEN_INTENT_TAG = "Kitchen";
    public static final String USER_INTENT_TAG = "User";

    private static boolean useSimulation = false;

    private ActivityKitchenHomeBinding binding;
    private TextView kitchenNameTextView;
    private MaterialButton moreBtn;

    private PopupMenu kitchenNamePopupMenu;
    public CartExpandableListAdapter cartExpandableListAdapter;

    HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Necessary set up for transition between grid view item and kitchen home
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        findViewById(android.R.id.content).setTransitionName("transition_to_kitchen_home");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform startTransition = new MaterialContainerTransform();
        startTransition.addTarget(android.R.id.content)
                .setInterpolator(MotionUtils.resolveThemeInterpolator(
                        getApplicationContext(),
                        com.google.android.material.R.attr.motionEasingEmphasizedDecelerateInterpolator,
                        new FastOutSlowInInterpolator()))
                .setDuration(400)
                .setPathMotion(new MaterialArcMotion());

        MaterialContainerTransform returnTransition = new MaterialContainerTransform();
        returnTransition.addTarget(android.R.id.content)
                .setInterpolator(MotionUtils.resolveThemeInterpolator(
                        getApplicationContext(),
                        com.google.android.material.R.attr.motionEasingEmphasizedAccelerateInterpolator,
                        new FastOutSlowInInterpolator()))
                .setDuration(300)
                .setPathMotion(new MaterialArcMotion());

        getWindow().setSharedElementEnterTransition(startTransition);
        getWindow().setSharedElementReturnTransition(returnTransition);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        User currentUser = new User(mAuth.getCurrentUser().getUid());
        Intent kitchenIntent = getIntent();
        String kitchenId = kitchenIntent.getStringExtra("kitchen_id");
        Kitchen kitchen = new Kitchen(kitchenId, this, this, this, this);
        viewModel = new ViewModelProvider(
                this,
                new KitchenFragmentsViewModelFactory(kitchen, currentUser, KitchenFragmentsViewModelFactory.TargetViewModelCode.Home)
        ).get(HomeViewModel.class);

        binding = ActivityKitchenHomeBinding.inflate(getLayoutInflater());

        binding.bottomNavigationView.setSelectedItemId(viewModel.getSelected());

        setContentView(binding.getRoot());

        super.onCreate(savedInstanceState);

        replaceFragment(viewModel.fragment);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        EdgeToEdge.enable(this);

        // We need to initialise the selection if there is not any
        if(viewModel.fragment == null) {
            ((BottomNavigationView) findViewById(R.id.bottom_navigation_view)).setSelectedItemId(HomeViewModel.defaultSelected);
            replaceFragment(viewModel.fragment);
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();
             viewModel.pickFragment(itemID);
             replaceFragment(viewModel.fragment);
            return true;
        });

        kitchenNameTextView = findViewById(R.id.kitchen_title_lbl);
        moreBtn = findViewById(R.id.kitchen_home_more_btn);

        moreBtn.setEnabled(false);

        setUpPopupMenu();
        moreBtn.setOnClickListener(v -> kitchenNamePopupMenu.show());

    }

    /**
     * Set up the popup menu that appears when a user clicks on the three dots.
     * It has an option to manage the current kitchen.
     */
    private void setUpPopupMenu() {
        // Set up a popup menu that appears beneath the kitchen name 'button title'
        kitchenNamePopupMenu = new PopupMenu(this, moreBtn);

        // IDs used to identify which popup menu item is clicked
        final int MANAGE_KITCHEN = 0;
        kitchenNamePopupMenu.getMenu().add(Menu.NONE, MANAGE_KITCHEN, Menu.NONE, R.string.manage_kitchen_menu_item);

        kitchenNamePopupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case MANAGE_KITCHEN -> {
                    Intent manageKitchenIntent = new Intent(getApplicationContext(), ManageKitchenActivity.class);
                    manageKitchenIntent.putExtra("kitchen_id", getKitchen().getKitchenID());
                    startActivity(manageKitchenIntent);

                    return true;
                }
                default -> {
                    return false;
                }
            }
        });
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getIntent().putExtra(KITCHEN_INTENT_TAG, getKitchen());
        getIntent().putExtra(USER_INTENT_TAG, getCurrentUser());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onKitchenNameUpdateListener(String kitchenName) {
        kitchenNameTextView.setText(kitchenName);

        moreBtn.setEnabled(true);
    }

    @Override
    public void onKitchenDeleteListener(String kitchenID) {
        exitToAllKitchenPage();
    }

    @Override
    public void onKitchenDeleteFailureListener(Exception e) {
        if(e instanceof FirebaseFirestoreException) {
            Toast.makeText(getApplicationContext(), "Unsuccessful connection with the database.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Unsuccessful deletion.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOwnerIDUpdateListener(String ownerID) {
        // Don't need to handle
    }

    @Override
    public void onKitchenActiveMembersUpdateListener(List<String> userID) {
        if(!userID.contains(getCurrentUser().getUid())) {
            Toast.makeText(getApplicationContext(), R.string.on_removed_notification_message, Toast.LENGTH_LONG).show();
            exitToAllKitchenPage();
        }
    }

    @Override
    public void onKitchenPendingMembersUpdateListener(List<String> userID) {
        if(userID.contains(getCurrentUser().getUid())) {
            Toast.makeText(getApplicationContext(), R.string.on_pending_accepted_notification_message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onKitchenBannedMembersUpdateListener(List<String> userID) {
        if(userID.contains(getCurrentUser().getUid())) {
            Toast.makeText(getApplicationContext(), R.string.on_banned_notification_message, Toast.LENGTH_LONG).show();
            exitToAllKitchenPage();
        }
    }

    @Override
    public void onItemListUpdateListener(ArrayList<Item> items) {
        if(viewModel.fragment instanceof KitchenOverviewFragment overviewFragment
            && overviewFragment.getPageViewModel() != null) {
            overviewFragment.getPageViewModel().refreshList(items);
        }
    }

    @Override
    public void onCartItemListUpdateListener(ArrayList<CartItem> items) {
        if(viewModel.fragment instanceof ShoppingCartFragment cartFragment
            && cartFragment.getCartViewModel() != null) {
            cartFragment.getCartViewModel().refreshList(items);
        }
    }

    @Override
    public void onItemUsageListUpdateListener(ArrayList<ItemUsage> itemUsageList) {
        if(viewModel.fragment instanceof HistoryFragment historyFragment
            && historyFragment.getHistoryViewModel() != null) {
            historyFragment.getHistoryViewModel().refreshList(itemUsageList);
        }
    }

    @Override
    public void onNotificationListener(ArrayList<Notification> notifications) {

        ArrayList<Notification> thisUserNotifications = new ArrayList<>(notifications);

        ArrayList<Notification> removedNotifications = new ArrayList<>();

        // JSON read into 

        thisUserNotifications.removeAll(removedNotifications);

        setNotificationsBadge(thisUserNotifications.size());

        if(viewModel.fragment instanceof KitchenNotificationFragment kitchenNotificationFragment
            && kitchenNotificationFragment.getNotificationViewModel() != null) {
            kitchenNotificationFragment.getNotificationViewModel().refreshList(thisUserNotifications);
        }
        setNotificationsBadge(thisUserNotifications.size());

    }



    /**
     * Show badge attached to notifications navigation bar item if user has notifications.
     *
     * @param numberNotifications the number of notifications.
     * @author u7333216 Aaron Nguyen
     */
    private void setNotificationsBadge(int numberNotifications) {
        BadgeDrawable badgeDrawable = binding.bottomNavigationView.getOrCreateBadge(R.id.navigation_notifications);

        if (numberNotifications == 0) {
            badgeDrawable.setVisible(false);
            return;
        }

        badgeDrawable.setNumber(numberNotifications);
        badgeDrawable.setVisible(true);
    }

    @Override
    public void onChatListUpdateListener(ArrayList<ChatMessage> chatMessages) {
        if(viewModel.fragment instanceof ChatFragment chatFragment
            && chatFragment.getPageViewModel() != null) {
            chatFragment.getPageViewModel().updateMessageList(chatMessages);
        }
    }

    private void exitToAllKitchenPage() {
        Intent allKitchensIntent = new Intent(getApplicationContext(), AllKitchensActivity.class);
        startActivity(allKitchensIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActionSimulator.getInstance().stopSimulation();
    }

    @Override
    public void onChatRoomEstablishedSuccess() {
        if(useSimulation)
            ActionSimulator.getInstance().startSimulation(ActionSimulator.TESTCODE.MainStream, getKitchen(), this.getApplicationContext());
    }

    public Kitchen getKitchen() {
        return viewModel.getKitchen();
    }

    public User getCurrentUser() {
        return viewModel.getCurrentUser();
    }


}