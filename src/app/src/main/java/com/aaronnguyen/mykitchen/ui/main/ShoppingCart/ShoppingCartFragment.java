package com.aaronnguyen.mykitchen.ui.main.ShoppingCart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.KitchenFragmentsViewModelFactory;
import com.aaronnguyen.mykitchen.ui.main.KitchenHomeActivity;
import com.aaronnguyen.mykitchen.ui.other.Search.SearchActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author u7648367 Ruixian Wu
 */
public class ShoppingCartFragment extends Fragment {

    private EditText searchEditText;
    private FloatingActionButton addItemBtn;
    protected ExtendedFloatingActionButton buyItemBtn;
    private Button foodGroupBtn;
    private Button storageLocationBtn;

    private PopupMenu categoryPopupMenu;
    ExpandableListView expandableListView;
    CartViewModel cartViewModel;


    Button dateBtn;

    MaterialDatePicker<Long> datePicker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Kitchen kitchen = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.KITCHEN_INTENT_TAG, Kitchen.class);
        User user = getActivity().getIntent().getSerializableExtra(KitchenHomeActivity.USER_INTENT_TAG, User.class);


        cartViewModel = new KitchenFragmentsViewModelFactory(
                kitchen,
                user,
                KitchenFragmentsViewModelFactory.TargetViewModelCode.ShoppingCart
        ).create(CartViewModel.class);
        cartViewModel.setShoppingCartFragment(this);

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
        if (kitchenHomeActivity.cartExpandableListAdapter == null) {
            kitchenHomeActivity.cartExpandableListAdapter = new CartExpandableListAdapter(
                    getContext(),
                    cartViewModel
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStatus) {
        return inflater.inflate(R.layout.activity_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.search_cart_edit_text);
        searchEditText.setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        TextInputLayout searchEditTextContainer = view.findViewById(R.id.search_cart_edit_text_container);
        searchEditTextContainer.setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getCartViewModel().onSearch(s.toString().replaceAll("\\s+","").toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        expandableListView = view.findViewById(R.id.ShoppingCartListView);

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

        Set<CartExpandableListAdapter.Position> checkedPositions = new HashSet<>();

        if (kitchenHomeActivity.cartExpandableListAdapter != null) {
            checkedPositions = kitchenHomeActivity.cartExpandableListAdapter.getCheckedChildren();
        }

        kitchenHomeActivity.cartExpandableListAdapter = new CartExpandableListAdapter(
                getContext(),
                cartViewModel
        );

        kitchenHomeActivity.cartExpandableListAdapter.setCheckedChildren(checkedPositions);

        cartViewModel.setShoppingCartFragment(this);



        expandableListView.setAdapter(kitchenHomeActivity.cartExpandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition);
                }

                lastExpandedPosition = groupPosition;
            }
        });

        buyItemBtn = view.findViewById(R.id.buy_cart_item_btn);
        if (kitchenHomeActivity.cartExpandableListAdapter.noCheckedChildren()) {
            buyItemBtn.setEnabled(false);
        } else {
            buyItemBtn.setEnabled(true);
        }

        ShoppingCartFragment fragment = this;
        buyItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cartViewModel.buyCartItems(
                            kitchenHomeActivity.getKitchen(),
                            kitchenHomeActivity.getCurrentUser(),
                            kitchenHomeActivity.cartExpandableListAdapter.getCheckedChildren());
                } catch (InvalidQuantityException e) {
                    throw new RuntimeException(e);
                }

                kitchenHomeActivity.cartExpandableListAdapter.notifyItemsBought();

                buyItemBtn.setEnabled(false);
            }

        });

        registerForContextMenu(expandableListView);


        addItemBtn = view.findViewById(R.id.add_cart_item_btn);
        addItemBtn.setOnClickListener(v -> showAddItemAlertDialog());

        // Note that this will have to go behind all the set up of the UIs,
        // so that the view model has access to the expandable list
        cartViewModel.startUpFetch();
    }


    /**
     * Creates context menu that allows user to view and edit the fields of a cart item. Item can also be deleted
     * @param menu
     * @param v The view that the context menu belongs to
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ExpandableListView.ExpandableListContextMenuInfo listMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        if (ExpandableListView.getPackedPositionType(listMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(ContextMenu.NONE, 0, ContextMenu.NONE, "Quantity");
            menu.add(ContextMenu.NONE, 1, ContextMenu.NONE, "Expiry days");
            menu.add(ContextMenu.NONE, 2, ContextMenu.NONE, "Storage Location");
            menu.add(ContextMenu.NONE, 3, ContextMenu.NONE, "Add schedule");
            menu.add(ContextMenu.NONE, 4, ContextMenu.NONE, "Schedules");
            menu.add(ContextMenu.NONE, 5, ContextMenu.NONE, "Delete");
        }
    }

    /**
     * On context menu click, gets the position that was clicked on and calls the corresponding alert dialog
     * @param item The context menu item that was selected.
     * @return
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo listMenuInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (listMenuInfo == null) {
            return super.onContextItemSelected(item);
        }

        int groupPosition = ExpandableListView.getPackedPositionGroup(listMenuInfo.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(listMenuInfo.packedPosition);

        if (item.getItemId() == 0){

            showChangeQuantityAlertDialog(groupPosition, childPosition);

        } else if (item.getItemId() == 1) {

            showChangeExpiryDaysAlertDialog(groupPosition, childPosition);
        } else if (item.getItemId() == 2) {

            showChangeStorageLocationAlertDialog(groupPosition, childPosition);

        } else if (item.getItemId() == 3) {
            showAddScheduleAlertDialog(groupPosition, childPosition);
        } else if (item.getItemId() == 4) {

            showCartScheduleAlertDialog(groupPosition, childPosition);

        } else if (item.getItemId() == 5) {
            showDeleteCartItemAlertDialog(groupPosition, childPosition);
        }
        return true;
    }



    /**
     * Shows an alert dialog to change the quantity of the cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    private void showChangeQuantityAlertDialog(int groupPosition, int childPosition) {
        View quantityView = getLayoutInflater().inflate(R.layout.change_quantity_alert_dialog, null);

        EditText quantityEditText = quantityView.findViewById(R.id.quantity_edit_text);

        MaterialAlertDialogBuilder changeQuantityDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        changeQuantityDialogBuilder.setView(quantityView).setPositiveButton(R.string.confirm, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        //
        androidx.appcompat.app.AlertDialog changeQuantityDialog = changeQuantityDialogBuilder.create();
        //
        changeQuantityDialog.setOnShowListener(dialog -> {

            Button positiveButton = changeQuantityDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String quantityS = quantityEditText.getText().toString();

                if(!quantityS.matches("[0-9]+$")) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityS);

                if(cartViewModel.isCartItemScheduleEmpty(groupPosition,childPosition)) {
                    if(quantity <= 0) {
                        Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    if(quantity < 0) {
                        Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

                try {
                    cartViewModel.changeCartItemQuantity(
                            this,
                            kitchenHomeActivity.getKitchen(),
                            kitchenHomeActivity.getCurrentUser(),
                            groupPosition,
                            childPosition,
                            quantity
                    );
                } catch (InvalidQuantityException e) {
                    throw new RuntimeException(e);
                }

                dialog.dismiss();
            });
        });
        changeQuantityDialog.show();

    }

    /**
     * Shows an alert dialog to change the expiry days of the cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    public void showChangeExpiryDaysAlertDialog(int groupPosition, int childPosition) {
        View expiryView = getLayoutInflater().inflate(R.layout.change_expiry_days_alert_dialog, null);


        EditText expiryEditText = expiryView.findViewById(R.id.expiry_edit_text);

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

        TextView currentExpiryLbl = expiryView.findViewById(R.id.current_expiry);
        currentExpiryLbl.setText(currentExpiryLbl.getText().toString()
                + kitchenHomeActivity.cartExpandableListAdapter.getItemExpiryDays(groupPosition, childPosition));

        MaterialAlertDialogBuilder changeExpiryDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        changeExpiryDialogBuilder.setView(expiryView).setPositiveButton(R.string.confirm, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        androidx.appcompat.app.AlertDialog changeExpiryDialog = changeExpiryDialogBuilder.create();

        changeExpiryDialog.setOnShowListener(dialog -> {

            Button positiveButton = changeExpiryDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String expiryDaysS = expiryEditText.getText().toString();

                if(!expiryDaysS.matches("[0-9]+$")) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                int expiryDays = Integer.parseInt(expiryDaysS);

                if(expiryDays <= 0) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    cartViewModel.changeCartItemExpiry(
                            this,
                            kitchenHomeActivity.getKitchen(),
                            kitchenHomeActivity.getCurrentUser(),
                            groupPosition,
                            childPosition,
                            expiryDays
                    );
                } catch (InvalidQuantityException e) {
                    throw new RuntimeException(e);
                }

                dialog.dismiss();
            });
        });
        changeExpiryDialog.show();
    }

    /**
     * Shows an alert dialog to change the storage location of the cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    private void showChangeStorageLocationAlertDialog(int groupPosition, int childPosition) {
        View storageLocation = getLayoutInflater().inflate(R.layout.change_storage_location_alert_dialog, null);

        storageLocationBtn = storageLocation.findViewById(R.id.storage_location_btn);
        storageLocationBtn.setOnClickListener(v1 -> showCategoryPopupMenu(storageLocationBtn));


        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

        TextView currentLocationLbl = storageLocation.findViewById(R.id.current_location);
        currentLocationLbl.setText(currentLocationLbl.getText().toString()
                + kitchenHomeActivity.cartExpandableListAdapter.getItemStorageLocation(groupPosition, childPosition));

        MaterialAlertDialogBuilder storageLocationDialogBuilder = new MaterialAlertDialogBuilder(getActivity());

        ShoppingCartFragment cartFragment = this;

        storageLocationDialogBuilder.setView(storageLocation).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        storageLocationDialogBuilder.setPositiveButton(R.string.confirm, (d, w) -> {
            try {
                cartViewModel.changeCartItemStorageLocation(
                        cartFragment,
                        kitchenHomeActivity.getKitchen(),
                        kitchenHomeActivity.getCurrentUser(),
                        storageLocationBtn.getText().toString(),
                        groupPosition,
                        childPosition);
            } catch (InvalidQuantityException e) {
                throw new RuntimeException(e);
            }

        });


        androidx.appcompat.app.AlertDialog storageLocationDialog = storageLocationDialogBuilder.create();
        storageLocationDialog.show();

    }

    /**
     * Shows an alert dialog to delete the selected cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    private void showDeleteCartItemAlertDialog(int groupPosition, int childPosition) {
        MaterialAlertDialogBuilder confirmRemoveBuilder = new MaterialAlertDialogBuilder(getActivity());
        confirmRemoveBuilder.setTitle("Are you sure you want to remove this cart item?");

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
        ShoppingCartFragment cartFragment = this;

        confirmRemoveBuilder.setNegativeButton(R.string.cancel, (d, w) -> d.cancel());
        confirmRemoveBuilder.setPositiveButton(R.string.rm_btn, (d, w) -> {
            cartViewModel.removeCartItem(
                    cartFragment,
                    kitchenHomeActivity.getKitchen(),
                    kitchenHomeActivity.getCurrentUser(),
                    groupPosition,
                    childPosition);

            kitchenHomeActivity.cartExpandableListAdapter.notifyItemDeletion(groupPosition, childPosition);

        });
        androidx.appcompat.app.AlertDialog confirmRemoveDialog = confirmRemoveBuilder.create();
        confirmRemoveDialog.show();

    }

    /**
     * Shows an alert dialog that displays all the schedules of the selected cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    public void showCartScheduleAlertDialog(int groupPosition, int childPosition) {
        View cartScheduleView = getLayoutInflater().inflate(R.layout.cart_item_schedule, null);
        TextView noScheduleTextView = cartScheduleView.findViewById(R.id.cart_no_schedules_lbl);


        CartScheduleListAdapter scheduleListAdapter = new CartScheduleListAdapter(getContext(), cartViewModel, groupPosition, childPosition);

        if (scheduleListAdapter.getCount() > 0) {
            noScheduleTextView.setVisibility(View.GONE);
        } else {
            noScheduleTextView.setVisibility(View.VISIBLE);
        }

        MaterialAlertDialogBuilder cartScheduleDialogBuilder = new MaterialAlertDialogBuilder(getActivity());

        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
        ShoppingCartFragment cartFragment = this;

        cartScheduleDialogBuilder.setView(cartScheduleView)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setTitle("Schedules")
                .setAdapter(scheduleListAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int scheduleIndex) {
                        MaterialAlertDialogBuilder deleteScheduleDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
                        deleteScheduleDialogBuilder.setTitle("Delete schedule?")
                                .setPositiveButton(R.string.confirm, (d, w) -> {
                                    cartViewModel.removeCartSchedule(
                                            kitchenHomeActivity.getKitchen(),
                                            kitchenHomeActivity.getCurrentUser(),
                                            groupPosition,
                                            childPosition,
                                            scheduleIndex);

                                })
                                .setNegativeButton(R.string.cancel, (d, w) -> dialog.cancel());

                        androidx.appcompat.app.AlertDialog deletScheduleDialog = deleteScheduleDialogBuilder.create();
                        deletScheduleDialog.show();

                    }
                });

        androidx.appcompat.app.AlertDialog cartScheduleDialog = cartScheduleDialogBuilder.create();

        cartScheduleDialog.show();

    };

    /**
     * Shows an alert dialog to add a schedule to the selected cart item
     * @param groupPosition of cart item selected
     * @param childPosition of cart item selected
     */
    private void showAddScheduleAlertDialog(int groupPosition, int childPosition) {
        View addScheduleView = getLayoutInflater().inflate(R.layout.add_cart_schedule_dialog, null);

        EditText quantityEditText = addScheduleView.findViewById(R.id.quantity_edit_text);
        dateBtn = addScheduleView.findViewById(R.id.date_btn);
        CheckBox repeatCheckbox = addScheduleView.findViewById(R.id.days_repeating_checkbox);
        EditText daysReoccurringEditText = addScheduleView.findViewById(R.id.days_repeating_edit_text);
        TextInputLayout daysReoccurringContainer = addScheduleView.findViewById(R.id.days_repeating_edit_text_container);


        dateBtn.setOnClickListener(v1 -> {
            showDatePickerDialog();
        });

        repeatCheckbox.setOnClickListener(v1 -> {
            daysReoccurringContainer.setEnabled(repeatCheckbox.isChecked());
        });

        MaterialAlertDialogBuilder addScheduleDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        addScheduleDialogBuilder.setView(addScheduleView).setPositiveButton(R.string.add_btn, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        androidx.appcompat.app.AlertDialog addScheduleDialog = addScheduleDialogBuilder.create();
        addScheduleDialog.setOnShowListener(dialog -> {
            Button positiveButton = addScheduleDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String quantityS = quantityEditText.getText().toString();

                if(!quantityS.matches("[0-9]+$")) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityS);

                if(quantity <= 0) {
                    Toast.makeText(getContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                String daysReoccurringS = daysReoccurringEditText.getText().toString();

                if(repeatCheckbox.isChecked() && !daysReoccurringS.matches("[0-9]+$")) {
                    Toast.makeText(getContext(), R.string.invalid_repeat_after_days_toast, Toast.LENGTH_LONG).show();
                    return;
                }

                int daysReoccurring = 0;

                if (repeatCheckbox.isChecked()) {
                    daysReoccurring = Integer.parseInt(daysReoccurringS);

                    if(daysReoccurring <= 0) {
                        Toast.makeText(getContext(), R.string.invalid_repeat_after_days_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                Date date = new Date();

                try {
                    date = (CartSchedule.df).parse(dateBtn.getText().toString());
                } catch (ParseException e) {
                    Toast.makeText(getContext(), R.string.missing_date, Toast.LENGTH_LONG).show();
                    return;
                }

                KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();

                cartViewModel.addCartSchedule(
                        this,
                        kitchenHomeActivity.getKitchen(),
                        kitchenHomeActivity.getCurrentUser(),
                        groupPosition,
                        childPosition,
                        quantity,
                        date,
                        daysReoccurring
                );

                dialog.dismiss();
            });


            daysReoccurringContainer.setEnabled(false);

        });
        addScheduleDialog.show();

    }



    /**
     * Shows an alert dialog to add a cart item
     */
    private void showAddItemAlertDialog() {
        View addItemView = getLayoutInflater().inflate(R.layout.add_cart_item_alert_dialog, null);

        EditText foodNameEditText = addItemView.findViewById(R.id.cart_food_name_edit_text);

        EditText quantityEditText = addItemView.findViewById(R.id.cart_quantity_edit_text);

        Button expireRefBtn = addItemView.findViewById(R.id.expire_ref_btn);
        Log.i("ACT", "Opening Search Activity");
        expireRefBtn.setOnClickListener(v -> {
            Log.i("Fragment", "Opening Search Activity");

            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        foodGroupBtn = addItemView.findViewById(R.id.cart_food_gp_btn);
        EditText expiresAfterEditText = addItemView.findViewById(R.id.expires_after_edit_text);
        storageLocationBtn = addItemView.findViewById(R.id.cart_storage_loc_btn);

        MaterialAlertDialogBuilder addItemDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        addItemDialogBuilder.setView(addItemView).setPositiveButton(R.string.add_btn, (dialog, which) -> {}).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        androidx.appcompat.app.AlertDialog addItemDialog = addItemDialogBuilder.create();

        addItemDialog.setOnShowListener(dialog -> {
            Button positiveButton = addItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String foodNameText = foodNameEditText.getText().toString();
                String quantityText = quantityEditText.getText().toString();
                String expiresAfterText = expiresAfterEditText.getText().toString();
                String foodGroupText = foodGroupBtn.getText().toString();
                String storageLocText = storageLocationBtn.getText().toString();

                if (foodNameText.isEmpty() || quantityText.isEmpty() || expiresAfterText.isEmpty() || foodGroupText.isEmpty()|| storageLocText.isEmpty()) {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.fill_in_all_fields_toast, Toast.LENGTH_LONG).show();
                    return;
                }


                // Ensure expiresAfter is a number
                for (Character character : expiresAfterText.toCharArray()) {
                    if (!Character.isDigit(character)) {
                        Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_expires_after_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                // Ensure quantityText is a number
                for (Character character : quantityText.toCharArray()) {
                    if (!Character.isDigit(character)) {
                        Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                int expiresAfter = Integer.parseInt(expiresAfterText);

                int quantity = Integer.parseInt(quantityText);

                if (expiresAfter <= 0) {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_expires_after_toast, Toast.LENGTH_LONG).show();
                }
                else if (quantity <= 0) {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.invalid_quantity_toast, Toast.LENGTH_LONG).show();
                } else {
                    KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
                    try {
                        cartViewModel.addCartItem(
                                this,
                                kitchenHomeActivity.getKitchen(),
                                foodNameText,
                                foodGroupText,
                                expiresAfter,
                                quantity,
                                storageLocText,
                                kitchenHomeActivity.getCurrentUser()
                        );
                    } catch (InvalidQuantityException e) {
                        throw new RuntimeException(e);
                    }

                    dialog.dismiss();
                }
            });
        });


        foodGroupBtn.setOnClickListener(v1 -> showCategoryPopupMenu(foodGroupBtn));
        storageLocationBtn.setOnClickListener(v1 -> showCategoryPopupMenu(storageLocationBtn));

        addItemDialog.show();
    }

    /**
     * Show dialog that allows user to pick a date
     */
    private void showDatePickerDialog() {
        if (datePicker != null && datePicker.isVisible()) {
            return;
        }
        MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
        datePickerBuilder.setTitleText("Select schedule date");
        datePickerBuilder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        datePicker = datePickerBuilder.build();
        datePicker.addOnPositiveButtonClickListener(aLong -> dateBtn.setText((CartSchedule.df).format(new Date(aLong))));
        datePicker.show(requireActivity().getSupportFragmentManager(), "");
    }



    /**
     * Show a popup menu depending on which 'category' button is clicked.
     * It is either a food group or storage location menu.
     *
     * @param button the button to anchor the popup menu to.
     *
     * @author u7333216 Aaron Nguyen
     */
    private void showCategoryPopupMenu(Button button) {
        String[] menuItems;

        if (button.equals(foodGroupBtn)) {
            menuItems = new String[]{"Dairy", "Fruit", "Grain", "Protein", "Vegetable", "Other"};
        } else if (button.equals(storageLocationBtn)) {
            menuItems = new String[]{"Freezer", "Fridge", "Pantry", "Other"};
        } else {
            return;
        }

        // Set up a popup menu that appears next to the clicked button
        categoryPopupMenu = new PopupMenu(getActivity(), button);

        for (int i = 0; i < menuItems.length; i++) {
            categoryPopupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, menuItems[i]);
        }

        categoryPopupMenu.setOnMenuItemClickListener(item -> {
            int itemIndex = item.getItemId();
            if (itemIndex >= 0 && itemIndex < menuItems.length) {
                button.setText(menuItems[itemIndex]);
                return true;
            } else {
                return false;
            }
        });

        categoryPopupMenu.show();
    }

    public CartViewModel getCartViewModel() {
        return cartViewModel;
    }

    public void notifyListViewChanges() {
        KitchenHomeActivity kitchenHomeActivity = (KitchenHomeActivity) getActivity();
        kitchenHomeActivity.cartExpandableListAdapter.notifyDataSetChanged();
    }
}