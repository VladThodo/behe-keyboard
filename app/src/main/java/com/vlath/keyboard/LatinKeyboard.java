package com.vlath.keyboard;

/**
 * Created by Vlad on 6/14/2017.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class LatinKeyboard extends Keyboard {

    static final int KEYCODE_OPTIONS = -100;

    static final int KEYCODE_LAYUOUT_SWITCH = -101;

    static final int KEYCODE_DPAD_R = -111;

    static final int KEYCODE_DPAD_L = -108;

    static final int KEYCODE_DPAD_U = -107;

    static final int KEYCODE_DPAD_DO = -109;

    static final int KEYCODE_ESCAPE = -112;

    static final int KEYCODE_CTRL = -113;

    static final int KEYCODE_ALT  = -114;

    static final int KEYCODE_STANDARD_SWITCH = -117;

    static final int KEYCODE_DELL_PROCESS = -121;

    static final int KEYCODE_I_DONT_KNOW_WHY_I_PUT_THAT_HERE = -122;


    private Key mEnterKey;
    private Key mSpaceKey;
    private static short rowNumber = 4;

    /**
     * Stores the current state of the mode change key. Its width will be dynamically updated to
     * match the region of {@link #mModeChangeKey} when {@link #mModeChangeKey} becomes invisible.
     */
    private Key mModeChangeKey;
    /**
     * Stores the current state of the language switch key (a.k.a. globe key). This should be

     * returns true. When this key becomes invisible, its width will be shrunk to zero.
     */
    private Key mLanguageSwitchKey;
    /**
     * Stores the size and other information of {@link #mModeChangeKey} when
     * {@link #mLanguageSwitchKey} is visible. This should be immutable and will be used only as a
     * reference size when the visibility of {@link #mLanguageSwitchKey} is changed.
     */
    private Key mSavedModeChangeKey;
    /**
     * Stores the size and other information of {@link #mLanguageSwitchKey} when it is visible.
     * This should be immutable and will be used only as a reference size when the visibility of
     * {@link #mLanguageSwitchKey} is changed.
     */
    private Key mSavedLanguageSwitchKey;

    public LatinKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public LatinKeyboard(Context context, int layoutTemplateResId,
                         CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                                   XmlResourceParser parser) {
        Key key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        } else if (key.codes[0] == ' ') {
            mSpaceKey = key;
        } else if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
            mModeChangeKey = key;
            mSavedModeChangeKey = new LatinKey(res, parent, x, y, parser);
        } else if (key.codes[0] == LatinKeyboard.KEYCODE_LAYUOUT_SWITCH) {
            mLanguageSwitchKey = key;
            mSavedLanguageSwitchKey = new LatinKey(res, parent, x, y, parser);
        }
        return key;
    }

    /**
     * Dynamically change the visibility of the language switch key (a.k.a. globe key).
     * @param visible True if the language switch key should be visible.
     */
    void setLanguageSwitchKeyVisibility(boolean visible) {
        if (visible) {
            // The language switch key should be visible. Restore the size of the mode change key
            // and language switch key using the saved layout.
            mModeChangeKey.width = mSavedModeChangeKey.width;
            mModeChangeKey.x = mSavedModeChangeKey.x;
            mLanguageSwitchKey.width = mSavedLanguageSwitchKey.width;
            mLanguageSwitchKey.icon = mSavedLanguageSwitchKey.icon;
            mLanguageSwitchKey.iconPreview = mSavedLanguageSwitchKey.iconPreview;
        } else {
            // The language switch key should be hidden. Change the width of the mode change key
            // to fill the space of the language key so that the user will not see any strange gap.
            mModeChangeKey.width = mSavedModeChangeKey.width + mSavedLanguageSwitchKey.width;
            mLanguageSwitchKey.width = 0;
            mLanguageSwitchKey.icon = null;
            mLanguageSwitchKey.iconPreview = null;
        }
    }

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }

        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "ENT";
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "N";
                break;
            case EditorInfo.IME_ACTION_SEARCH:
              //  mEnterKey.icon = "K";
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "HH";
                break;
            default:
             //   mEnterKey.icon = "U";
                mEnterKey.label = null;
                break;
        }
    }

    public void setRowNumber(short number){

        rowNumber = number;

    }



    void setSpaceIcon(final Drawable icon) {
        if (mSpaceKey != null) {
            mSpaceKey.icon = icon;
        }
    }
    public void changeKeyHeight(double height_modifier){
        int height = 0;
        for(Keyboard.Key key : getKeys()) {
            key.height *= height_modifier;
            key.y *= height_modifier;
            height = key.height;
        }
        setKeyHeight(height);
        getNearestKeys(0, 0); //somehow adding this fixed a weird bug where bottom row keys could not be pressed if keyboard height is too tall.. from the Keyboard source code seems like calling this will recalculate some values used in keypress detection calculation
    }


    /** This piece of code is intended to help us to resize the keyboard at runtime,
     *  thus giving us the opportunity to customize its height. It's a bit tricky though.
     *  And StackOverflow inspired me to be honest.
     * **/

    @Override
    public int getHeight(){

        return getKeyHeight() * rowNumber;
    }

    public void setKeyHeight(int height) {
        super.setKeyHeight(height);
    }


    static class LatinKey extends Keyboard.Key {

        public LatinKey(Resources res, Keyboard.Row parent, int x, int y,
                        XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }

        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard.
         */
        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }

}