package com.aaronnguyen.mykitchen.ui.other.managekitchen;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class AutoHeightAdjustingListView extends ListView {
    public AutoHeightAdjustingListView(Context context) {
        super(context);
    }

    public AutoHeightAdjustingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHeightAdjustingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Make the listView have the same height as the sum of all of the heights of its rows.
     * Reference: <a href="ref">https://stackoverflow.com/questions/11295080/android-wrap-content-is-not-working-with-listview</a>.
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link MeasureSpec}.
     *
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
}
