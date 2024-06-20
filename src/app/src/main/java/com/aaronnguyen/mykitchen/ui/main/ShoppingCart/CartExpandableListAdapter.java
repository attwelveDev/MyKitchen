package com.aaronnguyen.mykitchen.ui.main.ShoppingCart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.aaronnguyen.mykitchen.R;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * List adapter for ShoppingCartFragment's list of cart items
 * @author u7648367 Ruixian Wu
 */

public class CartExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;

    /**
     * Map whose keys are the item categories, and the value is the list of the item names of items whose item type is of that item category
     */
    private Map<String, List<String>> itemCollection;
    /**
     * Map whose keys are the item categories, and the value is the list of the quantities of items whose item type is of that item category
     */
    private Map<String, List<String>> itemQuantity;
    /**
     * Map whose keys are the item categories, and the value is the list of the expiry days of items whose item type is of that item category
     */
    private Map<String, List<String>> itemExpiryDays;
    /**
     * Map whose keys are the item categories, and the value is the list of the storage locations of items whose item type is of that item category
     */
    private Map<String, List<String>> itemStorageLocation;
    /**
     * List of all item categories
     */
    private final List<String> groupList;

    /**
     * Position of all the list view children who's checkboxes are currently ticked
     */
    private Set<Position> checkedChildren = new HashSet<Position>();

    private CartViewModel cartViewModel;

    public CartExpandableListAdapter(Context context, CartViewModel viewModel) {
        this.cartViewModel = viewModel;
        this.context = context;
        this.groupList = viewModel.getGroupList();
        this.itemCollection = viewModel.itemCollection;
        this.itemQuantity = viewModel.itemQuantity;
        this.itemExpiryDays = viewModel.itemExpiryDays;
        this.itemStorageLocation = viewModel.itemLocation;
    }



    @Override
    public int getGroupCount() {
        return itemCollection.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(itemCollection.get(groupList.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemCollection.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     *
     * @param groupPosition the position of the group for which we are creating a View
     * @param isExpanded whether the group is expanded
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryName = getGroup(groupPosition).toString();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }
        TextView item = convertView.findViewById(R.id.food_categories);
        item.setText(categoryName);

        TextView childCountTextView = convertView.findViewById(R.id.child_count_lbl);
        childCountTextView.setText(String.valueOf(getChildrenCount(groupPosition)));

        return convertView;
    }

    /**
     *
     * @param groupPosition the group position that the child belongs to
     * @param childPosition the position of the child within the group
     * @param isLastChild true if the child is the last child within the group, false otherwise
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String foodName = getChild(groupPosition, childPosition).toString();
        String quantity = getItemQuantity(groupPosition, childPosition);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_shopping_list, null);
        }

        TextView item = convertView.findViewById(R.id.cart_food_name);
        TextView quantityView = convertView.findViewById(R.id.cart_quantity_text);

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        Position pos = new Position(groupPosition, childPosition);

        if (Integer.parseInt(quantity) == 0) {
            checkedChildren.remove(pos);
            checkBox.setEnabled(false);
        } else {
            checkBox.setEnabled(true);
        }

        checkBox.setChecked(checkedChildren.contains(pos));
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkedChildren.add(pos);
                } else {
                    checkedChildren.remove(pos);
                }

                cartViewModel.cartViewFragment.buyItemBtn.setEnabled(!noCheckedChildren());
            }
        });


        item.setText(foodName);
        quantityView.setText("x " + quantity);

        return convertView;
    }

    public String getItemQuantity(int groupPosition, int childPosition) {
        return itemQuantity.get(groupList.get(groupPosition)).get(childPosition);
    }
    public String getItemExpiryDays(int groupPosition, int childPosition) {
        return itemExpiryDays.get(groupList.get(groupPosition)).get(childPosition);
    }
    public String getItemStorageLocation(int groupPosition, int childPosition) {
        return itemStorageLocation.get(groupList.get(groupPosition)).get(childPosition);
    };


    /**
     * Returns whether a child is selectable. All children in this adapter are selectable
     * @param groupPosition position of the group that contains the child
     * @param childPosition position of the child within the group
     * @return
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    public void setCheckedChildren(Set<Position> newCheckedChildren) {
        this.checkedChildren = newCheckedChildren;
    }

    /**
     * Updates the list of checked children positions when a child has been deleted
     * @param deletedGroupPos
     * @param deletedChildPos
     */
    public void notifyItemDeletion(int deletedGroupPos, int deletedChildPos) {
        Set<Position> newCheckedChildren = new HashSet<>();
        for (Position pos : checkedChildren) {
            if (pos.getGroupPosition() == deletedGroupPos && pos.getChildPosition() > deletedChildPos) {
                newCheckedChildren.add(new Position(deletedGroupPos, pos.getChildPosition() - 1));
            } else if (!(pos.getGroupPosition() == deletedGroupPos && pos.getChildPosition() == deletedChildPos)) {
                newCheckedChildren.add(pos);
            }
        }

        checkedChildren = newCheckedChildren;
    }

    /**
     * Notify the adapter that all checked items have been removed from the Cart
     */
    public void notifyItemsBought() {
        checkedChildren = new HashSet<>();
    }

    public Set<Position> getCheckedChildren() {
        return checkedChildren;
    }

    public boolean noCheckedChildren() {
        return checkedChildren.isEmpty();
    }

    /**
     * Position of a child in the expandable list adapter
     */
    public class Position {
        int groupPosition;
        int childPosition;

        public Position(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }

        public int getGroupPosition() {
            return groupPosition;
        }

        public int getChildPosition() {
            return childPosition;

        }

        /**
         * Enables us to remove a Position from a HashSet of positions
         * @return
         */
        @Override
        public int hashCode() {
            return Integer.parseInt("" + groupPosition + childPosition);

        }

    }

}
