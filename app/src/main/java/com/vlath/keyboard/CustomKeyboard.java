package com.vlath.keyboard;

/**
 * Created by Vlad on 6/14/2017.
 */

import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.inputmethodservice.KeyboardView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.PopupWindow;

import java.lang.reflect.Field;
import java.util.List;

public class CustomKeyboard extends KeyboardView {

    Drawable mTransparent = new ColorDrawable(Color.TRANSPARENT);
    NinePatchDrawable mSpaceBackground = (NinePatchDrawable) getContext().getResources().getDrawable(R.drawable.space);
    NinePatchDrawable mPressedBackground = (NinePatchDrawable) getContext().getResources().getDrawable(R.drawable.press);
    Paint mPaint = new Paint();

    public CustomKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LatinKeyboard getLatinKeyboard(){
        return (LatinKeyboard)getKeyboard();
    }

    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(LatinKeyboard.KEYCODE_OPTIONS, null);
            return true;
        }
        return super.onLongPress(key);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(28);
        mPaint.setColor(Color.parseColor("#a5a7aa"));

        List<Key> keys = getKeyboard().getKeys();

        for(Key key: keys) {

            if(key.label != null) {
                if (key.codes[0] == 32) {
                    mSpaceBackground.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    mSpaceBackground.draw(canvas);
                }
                if (Variables.isAnyOn()) {
                    if (Variables.isCtrl()) {
                        if (key.codes[0] == -113) {
                            mPressedBackground.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                            mPressedBackground.draw(canvas);
                        }
                    }
                    if (Variables.isAlt()){
                        if (key.codes[0] == -114) {
                            mPressedBackground.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                            mPressedBackground.draw(canvas);
                        }
                    }

                }
                else{
                    if(key.codes[0] == -113) {
                        mTransparent.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                        mTransparent.draw(canvas);
                    }
                    if(key.codes[0] == -114) {
                        mTransparent.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                        mTransparent.draw(canvas);
                    }
                }
            }
        }
    }
}