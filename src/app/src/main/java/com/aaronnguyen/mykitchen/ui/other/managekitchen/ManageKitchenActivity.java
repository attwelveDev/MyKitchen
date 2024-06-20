package com.aaronnguyen.mykitchen.ui.other.managekitchen;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenMemberListener;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenPropertyListener;
import com.aaronnguyen.mykitchen.ui.other.allkitchen.AllKitchensActivity;
import com.aaronnguyen.mykitchen.ui.other.login.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;
import java.util.Objects;

public class ManageKitchenActivity extends AppCompatActivity implements KitchenPropertyListener, KitchenMemberListener {
    // Declare UI elements
    private EditText kitchenNameEditText;
    private Button kitchenNameDiscardBtn;
    private Button kitchenNameSaveBtn;
    private ListView residentsListView;
    private ActiveResidentsListAdapter residentsListViewAdapter;
    private TextView pendingHeadingTextView;
    private ListView pendingListView;
    private PendingResidentsListAdapter pendingListViewAdapter;
    private TextView bannedHeadingTextView;
    private ListView bannedListView;
    private BannedResidentsListAdapter bannedListViewAdapter;
    private Button inviteResidentsBtn;
    private TextView noPendingRequestsTextView;
    private TextView noBannedResidentsTextView;
    private MaterialSwitch muteNotificationsSwitch;
    private Button leaveDeleteKitchenBtn;

    private enum ContextMenuItems {
        RESIDENTS_REMOVE_ITEM(0),
        RESIDENTS_BAN_ITEM(1),
        PENDING_REJECT_ITEM(2),
        PENDING_APPROVE_ITEM(3),
        BANNED_REMOVE_ITEM(4);

        final int id;

        ContextMenuItems(int id) {
            this.id = id;
        }


    }

    private Kitchen kitchen;
    private String kitchenId;
    private String kitchenName;

    private boolean activityJustCreated = true;

    private ManageKitchenViewModel pageViewModel;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageViewModel = new ViewModelProvider(this).get(ManageKitchenViewModel.class);
        pageViewModel.setActivity(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_kitchen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // Initialise UI elements
        kitchenNameEditText = findViewById(R.id.manage_kitchen_name_edit_text);
        kitchenNameDiscardBtn = findViewById(R.id.kitchen_name_discard_btn);
        kitchenNameSaveBtn = findViewById(R.id.save_kitchen_name_btn);
        inviteResidentsBtn = findViewById(R.id.invite_residents_btn);
        pendingHeadingTextView = findViewById(R.id.pending_lbl);
        noPendingRequestsTextView = findViewById(R.id.no_pending_reqs_lbl);
        bannedHeadingTextView = findViewById(R.id.banned_residents_lbl);
        noBannedResidentsTextView = findViewById(R.id.no_banned_residents_lbl);
        muteNotificationsSwitch = findViewById(R.id.mute_kitchen_notifications_switch);
        leaveDeleteKitchenBtn = findViewById(R.id.delete_kitchen_btn);

        // Fetch all the list view
        residentsListView = findViewById(R.id.active_kitchen_residents_list_view);
        pendingListView = findViewById(R.id.pending_kitchen_residents_list_view);
        bannedListView = findViewById(R.id.banned_kitchen_residents_list_view);

        // Set Adapters for the list view
        pendingListViewAdapter = new PendingResidentsListAdapter(
                getApplicationContext(),
                R.id.pending_kitchen_residents_list_view,
                pageViewModel.getPendingResidents()
        );
        pendingListView.setAdapter(pendingListViewAdapter);

        bannedListViewAdapter = new BannedResidentsListAdapter(
                getApplicationContext(),
                R.id.banned_kitchen_residents_list_view,
                pageViewModel.getBannedResidents()
        );
        bannedListView.setAdapter(bannedListViewAdapter);

        // TODO: add profile pictures to users

        // Set edit text to kitchen name
        Intent kitchenIntent = getIntent();
        kitchenId = kitchenIntent.getStringExtra("kitchen_id");
        kitchen = new Kitchen(kitchenId, (KitchenPropertyListener) this, (KitchenMemberListener) this);

        kitchenNameEditText.setEnabled(false);

        setNoChangesUI();

        kitchenNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (kitchenName != null) {
                    if (!kitchenName.equals(s.toString())) {
                        setUnsavedChangesUI();
                    } else {
                        setNoChangesUI();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        kitchenNameDiscardBtn.setOnClickListener(v -> {
            setNoChangesUI();

            kitchenNameEditText.setText(kitchenName);
            kitchenNameEditText.setSelection(kitchenName.length());
        });

        kitchenNameSaveBtn.setOnClickListener(v -> {
            String kitchenNameText = kitchenNameEditText.getText().toString();
            if (kitchenNameText.isEmpty()) {
                Toast.makeText(getApplicationContext(), R.string.fill_in_one_field_toast, Toast.LENGTH_LONG).show();
            } else {
                kitchen.changeKitchenName(kitchenNameText);
                setNoChangesUI();
            }
        });

        inviteResidentsBtn.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.kitchen_code), kitchenId));
        });

        // TODO: implement kitchen notification muting

        setDefaultUI();
    }

    private void setUpLeaveDeleteKitchenAlertDialog(boolean isOwner) {
        String title;
        String message;
        String positiveBtnText;
        if (isOwner) {
            title = getString(R.string.delete_kitchen_dialog_title);
            message = getString(R.string.delete_kitchen_confirm_prompt);
            positiveBtnText = getString(R.string.delete);
        } else {
            title = getString(R.string.leave_kitchen_dialog_title);
            message = getString(R.string.leave_kitchen_confirm_prompt);
            positiveBtnText = getString(R.string.leave_btn);
        }

        leaveDeleteKitchenBtn.setOnClickListener(v -> {
            MaterialAlertDialogBuilder leaveDeleteKitchenDialogBuilder = new MaterialAlertDialogBuilder(ManageKitchenActivity.this);
            leaveDeleteKitchenDialogBuilder.setTitle(title).setMessage(message).setPositiveButton(positiveBtnText, (dialog, which) -> {
                if (isOwner) {
                    kitchen.deleteKitchen();
                } else {
                    kitchen.removeActiveResidents(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                }
            }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            AlertDialog leaveDeleteKitchenDialog = leaveDeleteKitchenDialogBuilder.create();
            leaveDeleteKitchenDialog.setOnShowListener(dialog -> leaveDeleteKitchenDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED));
            leaveDeleteKitchenDialog.show();
        });
    }

    /**
     * Show the discard and save buttons for the kitchen name.
     */
    private void setUnsavedChangesUI() {
        kitchenNameDiscardBtn.setVisibility(View.VISIBLE);
        kitchenNameSaveBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the discard and save buttons for the kitchen name.
     */
    private void setNoChangesUI() {
        kitchenNameDiscardBtn.setVisibility(View.GONE);
        kitchenNameSaveBtn.setVisibility(View.GONE);
    }

    /**
     * Update the pending join requests UI section depending on whether the pending list is empty.
     * If it is empty, just display a text view stating there are no requests.
     *
     * @param isPendingListEmpty true if the pending list is empty; false otherwise.
     */
    private void updatePendingListUI(boolean isPendingListEmpty) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent signOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(signOutIntent);
            return;
        }

        String userID = currentUser.getUid();
        if (kitchen.getOwnerID() == null) {
            return;
        }

        if (!kitchen.getOwnerID().equals(userID)) {
            return;
        }

        if (isPendingListEmpty) {
            noPendingRequestsTextView.setVisibility(View.VISIBLE);
            pendingListView.setVisibility(View.GONE);
        } else {
            noPendingRequestsTextView.setVisibility(View.GONE);
            pendingListView.setVisibility(View.VISIBLE);
        }

        registerForContextMenu(pendingListView);
        pendingListView.setEnabled(true);
    }

    /**
     * Update the banned residents UI section depending on whether the banned list is empty.
     * If it is empty, just display a text view stating there are no banned residents.
     *
     * @param isBannedListEmpty true if the banned list is empty; false otherwise.
     */
    private void updateBannedListUI(boolean isBannedListEmpty) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent signOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(signOutIntent);
            return;
        }

        String userID = currentUser.getUid();
        if (kitchen.getOwnerID() == null) {
            return;
        }

        if (!kitchen.getOwnerID().equals(userID)) {
            return;
        }

        if (isBannedListEmpty) {
            noBannedResidentsTextView.setVisibility(View.VISIBLE);
            bannedListView.setVisibility(View.GONE);
        } else {
            noBannedResidentsTextView.setVisibility(View.GONE);
            bannedListView.setVisibility(View.VISIBLE);
        }

        registerForContextMenu(bannedListView);
        bannedListView.setEnabled(true);
    }

    /**
     * Display the UI depending on whether the user is the owner or not.
     * If they are not the owner, they cannot see pending join requests, banned residents,
     * and can only leave the kitchen instead of deleting it.
     */
    private void displayUIForUserLevel() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent signOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(signOutIntent);
            return;
        }

        String userID = currentUser.getUid();
        boolean isOwner = kitchen.getOwnerID().equals(userID);

        // Hide or show the relevant UI elements
        if (isOwner) {
            pendingHeadingTextView.setVisibility(View.VISIBLE);
            bannedHeadingTextView.setVisibility(View.VISIBLE);

            leaveDeleteKitchenBtn.setText(R.string.delete_kitchen_btn);

            registerForContextMenu(residentsListView);
            residentsListView.setEnabled(true);
        } else {
            setDefaultUI();
        }

        // Update the leave/delete kitchen button to display the correct alert dialog
        setUpLeaveDeleteKitchenAlertDialog(isOwner);

        // Set adapter for resident list view
        residentsListViewAdapter = new ActiveResidentsListAdapter(
                getApplicationContext(),
                R.id.active_kitchen_residents_list_view,
                pageViewModel.getResidents(),
                pageViewModel.getResidentsID(),
                userID
        );
        residentsListView.setAdapter(residentsListViewAdapter);

        // Update the pending and banned UI sections
        if (kitchen.getPendingResidents() != null) {
            updatePendingListUI(kitchen.getPendingResidents().isEmpty());
        }

        if (kitchen.getBannedResidents() != null) {
            updateBannedListUI(kitchen.getBannedResidents().isEmpty());
        }
    }

    /**
     * Show the default UI.
     * Pending join requests, banned residents are not shown; can only leave the kitchen.
     */
    private void setDefaultUI() {
        pendingHeadingTextView.setVisibility(View.GONE);
        noPendingRequestsTextView.setVisibility(View.GONE);
        pendingListView.setVisibility(View.GONE);
        bannedHeadingTextView.setVisibility(View.GONE);
        noBannedResidentsTextView.setVisibility(View.GONE);
        bannedListView.setVisibility(View.GONE);

        residentsListView.setEnabled(false);
        pendingListView.setEnabled(false);
        bannedListView.setEnabled(false);

        leaveDeleteKitchenBtn.setText(R.string.leave_kitchen_btn);
    }

    @Override
    public void onKitchenNameUpdateListener(String kitchenName) {
        this.kitchenName = kitchenName;

        if (activityJustCreated) {
            kitchenNameEditText.setText(kitchenName);
            activityJustCreated = false;

            kitchenNameEditText.setEnabled(true);
        }

        if (!kitchenName.equals(kitchenNameEditText.getText().toString())) {
            setUnsavedChangesUI();
        } else {
            setNoChangesUI();
        }
    }

    private void goToAllKitchensActivity() {
        Intent allKitchensIntent = new Intent(getApplicationContext(), AllKitchensActivity.class);
        startActivity(allKitchensIntent);
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.equals(residentsListView)) {
            menu.add(ContextMenu.NONE, ContextMenuItems.RESIDENTS_REMOVE_ITEM.id, ContextMenu.NONE, getString(R.string.remove_resident_menu_item));
            menu.add(ContextMenu.NONE, ContextMenuItems.RESIDENTS_BAN_ITEM.id, ContextMenu.NONE, getString(R.string.ban_resident_menu_item));
        } else if (v.equals(pendingListView)) {
            menu.add(ContextMenu.NONE, ContextMenuItems.PENDING_APPROVE_ITEM.id, ContextMenu.NONE, getString(R.string.accept_request_menu_item));
            menu.add(ContextMenu.NONE, ContextMenuItems.PENDING_REJECT_ITEM.id, ContextMenu.NONE, getString(R.string.ignore_request_menu_item));
        } else if (v.equals(bannedListView)) {
            menu.add(ContextMenu.NONE, ContextMenuItems.BANNED_REMOVE_ITEM.id, ContextMenu.NONE, getString(R.string.unban_resident_menu_item));
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }

        int id = item.getItemId();
        int position = info.position;
        if (id == ContextMenuItems.RESIDENTS_REMOVE_ITEM.id) {
            removeActiveResidents(position);
            return true;
        } else if (id == ContextMenuItems.RESIDENTS_BAN_ITEM.id) {
            banActiveResidents(position);
            return true;
        } else if (id == ContextMenuItems.PENDING_REJECT_ITEM.id) {
            rejectResidentsRequest(position);
            return true;
        } else if (id == ContextMenuItems.PENDING_APPROVE_ITEM.id) {
            approveResidentsRequest(position);
            return true;
        } else if (id == ContextMenuItems.BANNED_REMOVE_ITEM.id) {
            removeBannedResidents(position);
            return true;
        }

        return false;
    }

    @Override
    public void onKitchenDeleteListener(String kitchenID) {
        goToAllKitchensActivity();
    }

    @Override
    public void onKitchenDeleteFailureListener(Exception e) {
        Toast.makeText(getApplicationContext(), R.string.delete_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOwnerIDUpdateListener(String ownerID) {
        displayUIForUserLevel();
    }

    @Override
    public void onKitchenActiveMembersUpdateListener(List<String> userID) {
        // Should be true only when the user has just left the kitchen
        if (!userID.contains(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
            goToAllKitchensActivity();
        }

        pageViewModel.refreshResidents(userID);
    }

    @Override
    public void onKitchenPendingMembersUpdateListener(List<String> userID) {
        displayUIForUserLevel();
        pageViewModel.refreshPendingResidents(userID);
    }

    @Override
    public void onKitchenBannedMembersUpdateListener(List<String> userID) {
        displayUIForUserLevel();
        pageViewModel.refreshBannedResidents(userID);
    }

    public void onResidentsListViewChanges() {
        residentsListViewAdapter.notifyDataSetChanged();
    }

    public void onPendingResidentsListViewChanges() {
        pendingListViewAdapter.notifyDataSetChanged();
    }

    public void onBannedResidentsListViewChanges() {
        bannedListViewAdapter.notifyDataSetChanged();
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    public void removeActiveResidents(int index) {
        boolean success = pageViewModel.removeActiveResidents(index);

        String message;
        if(!success) {
            message = getString(R.string.remove_user_error);
        } else {
            message = getString(R.string.remove_user_success);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void banActiveResidents(int index) {
        boolean success = pageViewModel.banActiveResidents(index);

        String message;
        if(!success) {
            message = getString(R.string.ban_user_error);
        } else {
            message = getString(R.string.ban_user_success);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void approveResidentsRequest(int index) {
        boolean succeed = pageViewModel.approveResidentsRequest(index);

        String message;
        if(!succeed) {
            message = getString(R.string.accept_user_error);
        } else {
            message = getString(R.string.accept_user_success);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void rejectResidentsRequest(int index) {
        boolean succeed = pageViewModel.rejectResidentsRequest(index);

        String message;
        if(!succeed) {
            message = getString(R.string.reject_user_error);
        } else {
            message = getString(R.string.reject_user_success);
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void removeBannedResidents(int index) {
        pageViewModel.removeBannedResidents(index);
    }
}