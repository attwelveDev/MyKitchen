package com.aaronnguyen.mykitchen.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class ButtonRequiringEditText {
    /**
     * All editTexts attached to the button must all be non-empty for the button to be enabled.
     *
     * @param button the button to enable or disable.
     * @param editTexts the editTexts required to be non-empty for the button to be enabled.
     * @param <T> the class of the "button", so preferably a Button or FloatingActionButton.
     */
    public static <T extends View> void attachEditTextsToButton(T button, EditText[] editTexts) {
        for (EditText editText : editTexts) {
            if (editText.getText().toString().isEmpty()) {
                button.setEnabled(false);
            }

            editText.addTextChangedListener(buttonEnabler(button, editTexts));
        }
    }

    /**
     * If the text in a editText is changed, check if all editTexts attached to the button are
     * empty, and enable or disable the button accordingly.
     *
     * @param button the button to enable or disable.
     * @param editTexts a list of editTexts to check for text emptiness.
     * @return a TextWatcher object that enables or disables the button when one of the text of
     * one of the editTexts changes.
     */
    private static <T extends View> TextWatcher buttonEnabler(T button, EditText[] editTexts) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonCheck(button, editTexts);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
    }

    public static <T extends View> void buttonCheck(T button, EditText[] editTexts) {
        for (EditText editText : editTexts) {
            if (editText.getVisibility() == View.GONE) {
                continue;
            }

            if (editText.getText().toString().isEmpty()) {
                button.setEnabled(false);
                return;
            }
        }

        button.setEnabled(true);
    }
}
