package com.vlath.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/**
 * Created by Vlad on 6/14/2017.
 */

public class PCKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private static final float[] sNoneColorArray = {
            1.0f, 0, 0, 0, 0, // red
            0, 1.0f, 0, 0, 0, // green
            0, 0, 1.0f, 0, 0, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sIncreaseContrastColorArray = {
            2.0f, 0, 0, 0, -160.f, // red
            0, 2.0f, 0, 0, -160.f, // green
            0, 0, 2.0f, 0, -160.f, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sNegativeColorArray = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sBlueBlackColorArray = {
            -1.0f, 0, 0, 0, 100, // red
            0, -1.0f, 0, 0, 200, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sBlueWhiteColorArray = {
            2.0f, 0, 0, 0, 100, // red
            0, 2.0f, 0, 0, 200, // green
            0, 0, 2.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sRedWhiteColorArray = {
            3.0f, 0, 0, 0, 200, // red
            0, 3.0f, 0, 0, 10, // green
            0, 0, 3.0f, 0, 10, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sRedBlackColorArray = {
            -1.0f, 0, 0, 0, 200, // red
            0, -1.0f, 0, 0, 10, // green
            0, 0, -1.0f, 0, 10, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sOrangeBlackColorArray = {
            1.0f, 0, 0, 0, 245, // red
            0, 1.0f, 0, 0, 190, // green
            0, 0, 1.0f, 0, 39, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private KeyboardView kv;
    private Keyboard qwertyKeyboard;
    private Keyboard symbolsKeyboard;
    private Keyboard currentKeyboard;
    private Keyboard numKeyboard;
    private Keyboard mSymShiftKeyboard;
    private boolean firstCaps = false;
    private boolean isSysmbols = false;
    private boolean shiftSim = false;
    private boolean isDpad = false;
    private boolean isProgramming = false;
    private InputMethodManager mServ;
    private float[] mDefaultFilter;
    long shift_pressed=0;
    @Override
    public void onPress(int primaryCode) {
       if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("vib", false)) {
           Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
           v.vibrate(40);
       }

    }


    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getBaseContext().setTheme(R.style.Black);
        /* Initialize the keyboards */
        qwertyKeyboard = new Keyboard(this, R.xml.qwerty);
        symbolsKeyboard = new Keyboard(this, R.xml.symbols);
        numKeyboard = new Keyboard(this, R.xml.numbers);
        mSymShiftKeyboard = new Keyboard(this, R.xml.symbols2);
       if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("bord",false)){
           kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard_key_back, null);
       }
       else {
           kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard, null);
       }
    }

    @Override
    public View onCreateInputView() {
        kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard, null); // Inflate the main keyboard view
        mServ = (InputMethodManager) kv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        kv.setKeyboard(currentKeyboard);
        kv.setOnKeyboardActionListener(this);
        capsOnFirst();
        setTheme();

        /** Set the theme. Because we don't want to bother about changing the app theme, we use color filters.
         * The filter for the theme is set in the setTheme() function.
         * We then set the filter on the paint and apply the paint to the KeyboardView.
         * This operation is repeated in the onStartInput() function, to make sure that the keyboard color
         * matches the theme selected by the user.
         * */

        Paint mPaint = new Paint();
        ColorMatrixColorFilter filterInvert = new ColorMatrixColorFilter(mDefaultFilter);
        mPaint.setColorFilter(filterInvert);
        kv.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);

        return kv;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();


        /** Here we handle the key events. */

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                ic.commitText("", 1);
                break;
            case Keyboard.KEYCODE_SHIFT:

                /** We need to check whether we are on symbols layout or not.
                 * Then, perform the operation accordingly.
                 * Also, we check for double tab on the shift, and, if detected
                 * We set a global variable that tells us that the Shift is in the lock position.
                 * */

                if (isSysmbols) {
                    if (!shiftSim) {
                        currentKeyboard = new Keyboard(this, R.xml.symbols2);
                        kv.setKeyboard(currentKeyboard);
                        shiftSim = true;
                    } else {
                        currentKeyboard = new Keyboard(this, R.xml.symbols);
                        kv.setKeyboard(currentKeyboard);
                        shiftSim = false;
                    }
                } else {
                    if (shift_pressed + 200 > System.currentTimeMillis()){
                        Variables.setShiftOn();
                        setCapsOn(true);
                        kv.draw(new Canvas());
                    }
                    else{
                       if(Variables.isShift()){
                           Variables.setShiftOff();
                           firstCaps = false;
                           setCapsOn(firstCaps);
                           shift_pressed = System.currentTimeMillis();
                      }
                        else{
                           firstCaps = !firstCaps;
                           setCapsOn(firstCaps);
                           shift_pressed = System.currentTimeMillis();
                       }
                    }


                }
                break;
            case Keyboard.KEYCODE_DONE:

               /** Handle the 'done' action accordingly to the IME Options. */

                handleAction();
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:

               /** Switch between qwerty/symbols layout. */

                if (!isSysmbols) {
                    isSysmbols = !isSysmbols;
                    currentKeyboard = new Keyboard(this, R.xml.symbols);
                    kv.setKeyboard(currentKeyboard);
                } else {
                    isSysmbols = !isSysmbols;
                    currentKeyboard = new Keyboard(this, R.xml.qwerty);
                    kv.setKeyboard(currentKeyboard);
                }
                break;

            case CustomKeyboard.KEYCODE_LANGUAGE_SWITCH:

             /** Language Switch is a custom value defined in the CustomKeyboard class.
              * We use it to switch between qwerty/arrow keys/programming layouts. */

                if (isDpad || isProgramming) {
                    if (isProgramming) {
                        currentKeyboard = new Keyboard(this, R.xml.qwerty);
                        kv.invalidateAllKeys();
                        kv.setKeyboard(currentKeyboard);
                        isProgramming = false;
                    }
                    if (isDpad) {
                        currentKeyboard = new Keyboard(this, R.xml.programming);
                        kv.invalidateAllKeys();
                        kv.setKeyboard(currentKeyboard);
                        isDpad = false;
                        isProgramming = true;
                    }
                } else {
                    currentKeyboard = new Keyboard(this, R.xml.arrow_keys);
                    kv.invalidateAllKeys();
                    kv.setKeyboard(currentKeyboard);
                    isDpad = true;
                }
                break;
            case -108:

              /** Another custom keycode. */

              // TODO: declare custom code in the CustomKeyboard class

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                break;

            case -111:

                // TODO: declare custom code in the CustomKeyboard class

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                break;
            case -107:

                // TODO: declare custom code in the CustomKeyboard class

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
                break;
            case -109:

                // TODO: declare custom code in the CustomKeyboard class

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
                break;
            case -112:

                // TODO: declare custom code in the CustomKeyboard class

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(100, 100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE, 0));
                break;
            case -113:

                // TODO: declare custom code in the CustomKeyboard class

                if (Variables.isCtrl()) {
                    Variables.setCtrlOff();
                    kv.draw(new Canvas());
                } else {
                    Variables.setCtrlOn();
                    kv.draw(new Canvas());
                }
                break;
            case -114:

                // TODO: declare custom code in the CustomKeyboard class

                if (Variables.isAlt()) {
                    Variables.setAltOff();
                    kv.draw(new Canvas());
                } else {
                    Variables.setAltOn();
                    kv.draw(new Canvas());
                }
                break;
            case -117:

                /** This key enables the user to switch rapidly between qwerty/arrow keys layouts.*/

                // TODO: declare custom code in the CustomKeyboard class

                currentKeyboard = qwertyKeyboard;
                kv.setKeyboard(currentKeyboard);
                isDpad = false;
                break;
            case -121:
               /** Procces DEL key*/

               if(Variables.isAnyOn()){
                  if(Variables.isCtrl() && Variables.isAlt()) {
                      getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
                  }
                  if(Variables.isAlt()){
                      getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, KeyEvent.META_ALT_ON));
                  }
                  if(Variables.isCtrl()){
                      getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, KeyEvent.META_CTRL_ON));
                  }
               }
               else{
                   getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL , 0));
                   getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0));
               }
                break;
            case -122:
                if(Variables.isAnyOn()){
                    if(Variables.isCtrl() && Variables.isAlt()) {
                        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
                    }
                    if(Variables.isAlt()){
                        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB, 0, KeyEvent.META_ALT_ON));
                    }
                    if(Variables.isCtrl()){
                        getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB, 0, KeyEvent.META_CTRL_ON));
                    }
                }
                else{
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB , 0));
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB, 0));
                }
                break;
            default:

                if (Variables.isAnyOn()) {
                    processKeyCombo(primaryCode);
                } else {
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && firstCaps || Character.isLetter(code) && Variables.isShift()) {
                        code = Character.toUpperCase(code);
                    }
                    ic.commitText(String.valueOf(code), 1);
                    firstCaps = false;
                    setCapsOn(false);
                }
        }
        try {

            /** Some text processing. Helps some guys improve their writing skills, huh*/

            //TODO: Handle this better
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("caps",true)) {
                if (ic.getTextBeforeCursor(2, 0).toString().contains(". ")) {
                    setCapsOn(true);
                    firstCaps = true;
                }
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        /** Here it gets tricky. We repeat the same operations that are in the onCreateInputView() function.
         * Although, this needs better handling.
         * */

        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("bord",false)){
            kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard_key_back, null);
        }
        else {
            kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard, null);
        }
        setInputType();
        setTheme();
        Paint mPaint = new Paint();
        ColorMatrixColorFilter filterInvert = new ColorMatrixColorFilter(mDefaultFilter);
        mPaint.setColorFilter(filterInvert);
        kv.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
        kv.setKeyboard(currentKeyboard);
        capsOnFirst();
        kv.setOnKeyboardActionListener(this);
        setInputView(kv);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();

        /** Make sure that we don't start in the Dpad mode or symbols mode unless the user requests it. */

        isDpad = false;
        isSysmbols = false;

        /** Also, disable the global caps*/

        Variables.setShiftOff();

    }

    private void handleAction() {
        EditorInfo curEditor = getCurrentInputEditorInfo();
        switch (curEditor.imeOptions & EditorInfo.IME_MASK_ACTION) {
            case EditorInfo.IME_ACTION_DONE:
                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_DONE);
                break;
            case EditorInfo.IME_ACTION_GO:
                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_GO);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_NEXT);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_SEARCH);
                break;
            case EditorInfo.IME_ACTION_SEND:
                if (curEditor.imeOptions == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                    getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_SEND);

                } else {
                    getCurrentInputConnection().sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    getCurrentInputConnection().sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                }
                break;
            default:
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                break;
        }
    }

    private int getCursorCapsMode(InputConnection ic, EditorInfo attr) {

       /** A rudimentary method to find out whether we should start with caps on or not.
        * */
       // TODO: Perform additional checks.

        int caps = 0;
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
            caps = ic.getCursorCapsMode(attr.inputType);
        }
        return caps;
    }

    private void setInputType() {

        /** Checks the preferences for the default keyboard layout.
        * If qwerty, we start out whether in qwerty or numbers, depending on the input type.
        * */

       EditorInfo attribute = getCurrentInputEditorInfo();
        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("start", "1").equals("1")) {
            switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
                case InputType.TYPE_CLASS_NUMBER:
                case InputType.TYPE_CLASS_DATETIME:
                case InputType.TYPE_CLASS_PHONE:
                    currentKeyboard = new Keyboard(this, R.xml.numbers);
                    break;
                case InputType.TYPE_CLASS_TEXT:
                    int webInputType = attribute.inputType & InputType.TYPE_MASK_VARIATION;

                    if (webInputType == InputType.TYPE_TEXT_VARIATION_URI ||
                            webInputType == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT ||
                            webInputType == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            || webInputType == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
                        currentKeyboard = new Keyboard(this, R.xml.qwerty);
                    } else {
                        currentKeyboard = new Keyboard(this, R.xml.qwerty);
                    }

                    break;

                default:
                    currentKeyboard = new Keyboard(this, R.xml.qwerty);;
                    break;
            }
        } else {
            setDefaultKeyboard();
        }
        if (kv != null) {
            kv.setKeyboard(currentKeyboard);
        }
    }

    private void capsOnFirst() {

        /** Huh, a method that calls getCursorCapsMode() and performs a check.
        * Accordingly to the official android documentation, if the caps mode is not equal to 0,
        * We should start in caps mode. Although, tests have proven that additionally checks are needed.
        * I'll see what I can do on this.
        * */
      if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("caps",true)){
          if (getCursorCapsMode(getCurrentInputConnection(), getCurrentInputEditorInfo()) != 0) {
              firstCaps = true;
              setCapsOn(true);
          }
      }
      else {
          firstCaps = false;
          setCapsOn(false);
      }

      }

    private void setCapsOn(boolean on) {

        /** Simple function that enables us to rapidly set the keyboard shifted or not.
         * */
        if(Variables.isShift()){
            currentKeyboard.setShifted(true);
            kv.invalidateAllKeys();
        }
        else {
            currentKeyboard.setShifted(on);
            kv.invalidateAllKeys();
        }

    }

    private void processKeyCombo(int keycode) {
        if (Variables.isAnyOn()) {
            if (Variables.isCtrl() && Variables.isAlt()) {
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, getHardKeyCode(keycode), 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, getHardKeyCode(keycode), 0, KeyEvent.META_CTRL_ON | KeyEvent.META_ALT_ON));
            } else {
                if (Variables.isCtrl()) {
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, getHardKeyCode(keycode), 0, KeyEvent.META_CTRL_ON));
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, getHardKeyCode(keycode), 0, KeyEvent.META_CTRL_ON));
                }
                if (Variables.isAlt()) {
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, getHardKeyCode(keycode), 0, KeyEvent.META_ALT_ON));
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(100, 100, KeyEvent.ACTION_UP, getHardKeyCode(keycode), 0, KeyEvent.META_ALT_ON));
                }
            }
        }

    }

    private int getHardKeyCode(int keycode) {
        char code = (char) keycode;
        switch (String.valueOf(code)) {
            case "a":
                return KeyEvent.KEYCODE_A;
            case "b":
                return KeyEvent.KEYCODE_B;
            case "c":
                return KeyEvent.KEYCODE_C;

            case "d":
                return KeyEvent.KEYCODE_D;

            case "e":
                return KeyEvent.KEYCODE_E;

            case "f":
                return KeyEvent.KEYCODE_F;


            case "g":
                return KeyEvent.KEYCODE_G;

            case "h":
                return KeyEvent.KEYCODE_H;

            case "i":
                return KeyEvent.KEYCODE_I;

            case "j":
                return KeyEvent.KEYCODE_J;


            case "k":
                return KeyEvent.KEYCODE_K;

            case "l":
                return KeyEvent.KEYCODE_L;

            case "m":
                return KeyEvent.KEYCODE_M;

            case "n":
                return KeyEvent.KEYCODE_N;

            case "o":
                return KeyEvent.KEYCODE_O;

            case "p":
                return KeyEvent.KEYCODE_P;


            case "q":
                return KeyEvent.KEYCODE_Q;

            case "r":
                return KeyEvent.KEYCODE_R;


            case "s":
                return KeyEvent.KEYCODE_S;

            case "t":
                return KeyEvent.KEYCODE_T;

            case "u":
                return KeyEvent.KEYCODE_U;

            case "v":
                return KeyEvent.KEYCODE_V;


            case "w":
                return KeyEvent.KEYCODE_W;

            case "x":
                return KeyEvent.KEYCODE_X;

            case "y":
                return KeyEvent.KEYCODE_Y;

            case "z":
                return KeyEvent.KEYCODE_Z;
            default:
                return keycode;
        }
    }

    public void setTheme() {
        switch (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("theme", "2")) {
            case "1":
                mDefaultFilter = sNoneColorArray;
                break;
            case "2":
                mDefaultFilter = sNegativeColorArray;
                break;
            case "3":
                mDefaultFilter = sBlueWhiteColorArray;
                break;
            case "4":
                mDefaultFilter = sBlueBlackColorArray;
                break;
            case "5":
                mDefaultFilter = sRedWhiteColorArray;
                break;
            case "6":
                mDefaultFilter = sRedBlackColorArray;
                break;
            case "7":
                mDefaultFilter = sOrangeBlackColorArray;
                break;

        }
    }

    public void setDefaultKeyboard() {
        switch (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("start", "1")) {
            case "1":
                currentKeyboard = qwertyKeyboard;
                break;
            case "2":
                currentKeyboard = new Keyboard(this, R.xml.arrow_keys);
                break;
            case "3":
                currentKeyboard = new Keyboard(this, R.xml.programming);
                break;
        }
    }
}


