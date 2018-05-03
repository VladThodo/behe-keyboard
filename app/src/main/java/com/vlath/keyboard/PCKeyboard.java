package com.vlath.keyboard;

/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.*;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.PopupWindow;
import java.util.ArrayList;
import java.util.List;

/** Main class  of the keyboard, extending InputMethodService. Here we handle all the user interaction with the keyboard itself. */

public class PCKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, SpellCheckerSession.SpellCheckerSessionListener {


    /**
     * As we don't want to bother changing the app theme, we use filters to theme the keyboard.
     * Each filter needs an array of colors. The arrays are declared below.
     */

    // TODO: Add the arrays in a separate, static class, so they are eay to access and modify

    private static final float[] sNoneColorArray = {
            1.0f, 0, 0, 0, 0, // red
            0, 1.0f, 0, 0, 0, // green
            0, 0, 1.0f, 0, 0, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };

    private static final float[] sNegativeColorArray = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sBlueBlackColorArray = {
            -0.6f, 0, 0, 0, 41, // red
            0, -0.6f, 0, 0, 128, // green
            0, 0, -0.6f, 0, 185, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sBlueWhiteColorArray = {
            1.0f, 0, 0, 0, 41, // red
            0, 1.0f, 0, 0, 128, // green
            0, 0, 1.0f, 0, 185, // blue
            0, 0, 0, 1.0f, 1 // alpha
    };
    private static final float[] sRedWhiteColorArray = {
            1.0f, 0, 0, 0, 192, // red
            0, 1.0f, 0, 0, 57, // green
            0, 0, 1.0f, 0, 43, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sRedBlackColorArray = {
            -0.6f, 0, 0, 0, 192, // red
            0, -0.6f, 0, 0, 57, // green
            0, 0, -0.6f, 0, 43, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sOrangeBlackColorArray = {
            1.0f, 0, 0, 0, 230, // red
            0, 1.0f, 0, 0, 126, // green
            0, 0, 1.0f, 0, 34, // blue
            0, 0, 0, 1.0f, 0 // alpha
    };
    private static final float[] sMaterialDarkColorArray = {
            1.0f, 0, 0, 0, 55, // red
            0, 1.0f, 0, 0, 71, // green
            0, 0, 1.0f, 0, 79, // blue
            0, 0, 0, 1.0f, 1 // alpha
    };

    static final boolean PROCESS_HARD_KEYS = true;

    private InputMethodManager mInputMethodManager;

    private CustomKeyboard mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mStandardKeyboard;



    private String mWordSeparators;

    private SpellCheckerSession mScs;
    private List<String> mSuggestions;

    private boolean firstCaps = false;
    private boolean isSysmbols = false;
    private boolean shiftSim = false;
    private boolean isDpad = false;
    private boolean isProgramming = false;
    private InputMethodManager mServ;
    private float[] mDefaultFilter;
    long shift_pressed=0;

    private short rowNumber = 4;
    private CustomKeyboard kv;

    private LatinKeyboard currentKeyboard;
    private LatinKeyboard mCurKeyboard;
    private LatinKeyboard standardKeyboard;

    private int standardKeyboardID = R.xml.qwerty;
    /**
     * Main initialization of the input method component. Be sure to call
     * to super class.
     */

    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        final TextServicesManager tsm = (TextServicesManager) getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        mScs = tsm.newSpellCheckerSession(null, null, this, true);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mStandardKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mStandardKeyboard = new LatinKeyboard(this, standardKeyboardID);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols2);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     *
     * We also s
     */
    @Override public View onCreateInputView() {
        mInputView = (CustomKeyboard) getLayoutInflater().inflate(
                R.layout.keyboard, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setPreviewEnabled(false);
        setLatinKeyboard(mStandardKeyboard);
        return mInputView;
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {

        mInputView.setKeyboard(nextKeyboard);

    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        setTheme();
        Paint mPaint = new Paint();
        ColorMatrixColorFilter filterInvert = new ColorMatrixColorFilter(mDefaultFilter);
        mPaint.setColorFilter(filterInvert);
        mCandidateView.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);


        return mCandidateView;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     *
     *
     * And we have to reinitialize all we've one to make sure the keyboard aspect matches
     * The one selected in settings.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        setTheme();

        mComposing.setLength(0);
        updateCandidates();
        /**
         * Some code on here is based on the SoftKeyboard Sample. I don't fully understand it.
         * I need to look it up and delete any unnecessary stuff.
         * */
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }
        mCompletions = null;

        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("bord",false)){
            kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard_key_back, null);
        }
        else {
            kv = (CustomKeyboard) getLayoutInflater().inflate(R.layout.keyboard, null);
        }
        setStandardKeyboard();
        setInputType();
        Paint mPaint = new Paint();
        ColorMatrixColorFilter filterInvert = new ColorMatrixColorFilter(mDefaultFilter);
        mPaint.setColorFilter(filterInvert);
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);

        kv.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
        currentKeyboard.setRowNumber(getRowNumber());

        kv.setKeyboard(currentKeyboard);

        capsOnFirst();
        kv.setOnKeyboardActionListener(this);

        mPredictionOn = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("pred", false);
        mCompletionOn = false;

        mCandidateView.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);

        setInputView(kv);
        kv.getLatinKeyboard().changeKeyHeight(getHeightKeyModifier());

        setCandidatesView(mCandidateView);

    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        mCurKeyboard = mStandardKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mStandardKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }


    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(mComposing.toString())}, 5);
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        mSuggestions = suggestions;
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (kv.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (mPredictionOn && !mWordSeparators.contains(String.valueOf((char)primaryCode))) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        }
        if(mPredictionOn && mWordSeparators.contains(String.valueOf((char)primaryCode))){
            char code = (char) primaryCode;
            if (Character.isLetter(code) && firstCaps || Character.isLetter(code) && Variables.isShift()) {
                code = Character.toUpperCase(code);
            }
            getCurrentInputConnection().setComposingRegion(0,0);
            getCurrentInputConnection().commitText(String.valueOf(code), 1);
            firstCaps = false;
            setCapsOn(false);
        }
        if(!mPredictionOn){
            char code = (char) primaryCode;
            if (Character.isLetter(code) && firstCaps || Character.isLetter(code) && Variables.isShift()) {
                code = Character.toUpperCase(code);
            }
            getCurrentInputConnection().setComposingRegion(0,0);
            getCurrentInputConnection().commitText(String.valueOf(code), 1);
            firstCaps = false;
            setCapsOn(false);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(String s) {

       if(s.contains(". ") || s.contains("? ") || s.contains("! ")){
           return true;
       }

       return false;

    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {

            if (mPredictionOn && mSuggestions != null && index >= 0) {
                mComposing.replace(0, mComposing.length(), mSuggestions.get(index));
            }
            commitTyped(getCurrentInputConnection());

        }
    }

    public void onPress(int primaryCode) {
        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("vib", false)) {
            Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(40);
        }
    }


    public void onRelease(int primaryCode) {

    }
    /**
     * http://www.tutorialspoint.com/android/android_spelling_checker.htm
     * Sort of copy-paste, huh.
     *
     * I need to find time to refine this code
     *
     *
     * @param results results
     */
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        final StringBuilder sb = new StringBuilder();

        for (SuggestionsInfo result : results) {
            // Returned suggestions are contained in SuggestionsInfo
            final int len = result.getSuggestionsCount();
            sb.append('\n');

            for (int j = 0; j < len; ++j) {
                sb.append(",").append(result.getSuggestionAt(j));
            }

            sb.append(" (").append(len).append(")");
        }
    }

    private void dumpSuggestionsInfoInternal(
            final List<String> sb, final SuggestionsInfo si, final int length, final int offset) {
        // Returned suggestions are contained in SuggestionsInfo
        final int len = si.getSuggestionsCount();
        for (int j = 0; j < len; ++j) {
            sb.add(si.getSuggestionAt(j));
        }
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
       try {
           final List<String> sb = new ArrayList<>();
           for (final SentenceSuggestionsInfo ssi : results) {
               for (int j = 0; j < ssi.getSuggestionsCount(); ++j) {
                   dumpSuggestionsInfoInternal(
                           sb, ssi.getSuggestionsInfoAt(j), ssi.getOffsetAt(j), ssi.getLengthAt(j));
               }
           }

           setSuggestions(sb, true, true);
       }
       catch(Exception ignored){}

    }
    private void setCapsOn(boolean on) {

        /** Simple function that enables us to rapidly set the keyboard shifted or not.
         * */
        if(Variables.isShift()){
            kv.getKeyboard().setShifted(true);
            kv.invalidateAllKeys();
        }
        else {
            kv.getKeyboard().setShifted(on);
            kv.invalidateAllKeys();
        }

    }
    private void processKeyCombo(int keycode) {
       /** Ass the function name says, we process key combinations here*/

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
       /** Seems like the actual soft key code doesn't match the hard key code*/
        PopupWindow p = new PopupWindow();
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

                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_SEND);

                break;
            default:

                break;
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
            case "8":
                mDefaultFilter = sMaterialDarkColorArray;
                break;

        }
    }
    private void setInputType() {

        /** Checks the preferences for the default keyboard layout.
         * If standard, we start out whether in standard or numbers, depending on the input type.
         * */

        EditorInfo attribute = getCurrentInputEditorInfo();

        if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("start", "1").equals("1")) {
            switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
                case InputType.TYPE_CLASS_NUMBER:
                case InputType.TYPE_CLASS_DATETIME:
                case InputType.TYPE_CLASS_PHONE:
                    currentKeyboard = new LatinKeyboard(this, R.xml.numbers);
                    break;
                case InputType.TYPE_CLASS_TEXT:
                    int webInputType = attribute.inputType & InputType.TYPE_MASK_VARIATION;

                    if (webInputType == InputType.TYPE_TEXT_VARIATION_URI ||
                            webInputType == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT ||
                            webInputType == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            || webInputType == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
                        currentKeyboard = new LatinKeyboard(this, standardKeyboardID);
                    } else {
                        currentKeyboard = new LatinKeyboard(this, standardKeyboardID);
                    }

                    break;

                default:
                    currentKeyboard = new LatinKeyboard(this, standardKeyboardID);
                    break;
            }
        } else {
            setDefaultKeyboard();
        }
        if (kv != null) {
            kv.setKeyboard(currentKeyboard);
        }
    }
    public void setDefaultKeyboard() {
        switch (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("start", "1")) {
            case "1":
                currentKeyboard = standardKeyboard;
                break;
            case "2":
                currentKeyboard = new LatinKeyboard(this, R.xml.arrow_keys);
                setRowNumber(4);
                currentKeyboard.setRowNumber(getRowNumber());
                break;
            case "3":
                currentKeyboard = new LatinKeyboard(this, R.xml.programming);
                setRowNumber(5);
                currentKeyboard.setRowNumber(getRowNumber());
                break;
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
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();


        /** Here we handle the key events. */

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
               handleBackspace();
                break;
            case Keyboard.KEYCODE_SHIFT:

                /** We need to check whether we are on symbols layout or not.
                 * Then, perform the operation accordingly.
                 * Also, we check for double tab on the shift, and, if detected
                 * We set a global variable that tells us that the Shift is in the lock position.
                 * */

                if (isSysmbols) {
                    if (!shiftSim) {
                        currentKeyboard = new LatinKeyboard(this, R.xml.symbols2);
                        kv.setKeyboard(currentKeyboard);
                        shiftSim = true;
                    } else {
                        currentKeyboard = new LatinKeyboard(this, R.xml.symbols);
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
            case 10:

                /** Handle the 'done' action accordingly to the IME Options. */

                EditorInfo curEditor = getCurrentInputEditorInfo();
                switch (curEditor.imeOptions & EditorInfo.IME_MASK_ACTION) {
                    case EditorInfo.IME_ACTION_DONE:
                        keyDownUp(66);
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_GO);
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        keyDownUp(66);
                        break;
                    case EditorInfo.IME_ACTION_SEARCH:
                        getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_SEARCH);
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        keyDownUp(66);
                        break;
                    default:
                       keyDownUp(66);
                        break;
                }
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:

                /** Switch between standard/symbols layout. */

                if (!isSysmbols) {
                    isSysmbols = !isSysmbols;
                    currentKeyboard = new LatinKeyboard(this, R.xml.symbols);
                    kv.setKeyboard(currentKeyboard);
                } else {
                    isSysmbols = false;
                    currentKeyboard = new LatinKeyboard(this, standardKeyboardID);
                    kv.setKeyboard(currentKeyboard);
                }
                kv.getLatinKeyboard().changeKeyHeight(getHeightKeyModifier());
                break;

            case LatinKeyboard.KEYCODE_LAYUOUT_SWITCH:

                /** Language Switch is a custom value defined in the LatinKeyboard class.
                 * We use it to switch between standard/arrow keys/programming layouts. */

                if (isDpad || isProgramming) {
                    if (isProgramming) {
                        currentKeyboard = new LatinKeyboard(this, standardKeyboardID);
                        kv.invalidateAllKeys();
                        currentKeyboard.setRowNumber(getStandardRowNumber());
                        kv.setKeyboard(currentKeyboard);
                        isProgramming = false;
                        isDpad = false;
                    }

                    if (isDpad) {
                        currentKeyboard = new LatinKeyboard(this, R.xml.programming);
                        kv.invalidateAllKeys();
                        setRowNumber(5);
                        currentKeyboard.setRowNumber(getRowNumber());
                        kv.setKeyboard(currentKeyboard);
                        isDpad = false;
                        isProgramming = true;
                    }
                } else {
                    currentKeyboard = new LatinKeyboard(this, R.xml.arrow_keys);
                    kv.invalidateAllKeys();
                    setRowNumber(4);
                    currentKeyboard.setRowNumber(getRowNumber());
                    kv.setKeyboard(currentKeyboard);
                    isDpad = true;
                }
                kv.getLatinKeyboard().changeKeyHeight(getHeightKeyModifier());
                break;
            case LatinKeyboard.KEYCODE_DPAD_L:

                /** Another custom keycode. */



                    getCurrentInputConnection().sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                    getCurrentInputConnection().sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));

                break;

            case LatinKeyboard.KEYCODE_DPAD_R:

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                break;
            case LatinKeyboard.KEYCODE_DPAD_U:

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
                break;
            case LatinKeyboard.KEYCODE_DPAD_DO:

                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
                break;
            case LatinKeyboard.KEYCODE_ESCAPE:


                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(100, 100, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0));
                getCurrentInputConnection().sendKeyEvent(
                        new KeyEvent(100, 100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE, 0));
                break;
            case LatinKeyboard.KEYCODE_CTRL:

                if (Variables.isCtrl()) {
                    Variables.setCtrlOff();
                    kv.draw(new Canvas());
                } else {
                    Variables.setCtrlOn();
                    kv.draw(new Canvas());
                }
                break;
            case LatinKeyboard.KEYCODE_ALT:

                if (Variables.isAlt()) {
                    Variables.setAltOff();
                    kv.draw(new Canvas());
                } else {
                    Variables.setAltOn();
                    kv.draw(new Canvas());
                }
                break;
            case LatinKeyboard.KEYCODE_STANDARD_SWITCH:

                /** This key enables the user to switch rapidly between standard/arrow keys layouts.*/

                currentKeyboard = new LatinKeyboard(getBaseContext(), standardKeyboardID);
                currentKeyboard.setRowNumber(getStandardRowNumber());
                kv.setKeyboard(currentKeyboard);
                kv.getLatinKeyboard().changeKeyHeight(getHeightKeyModifier());
                isDpad = false;
                break;
            case LatinKeyboard.KEYCODE_DELL_PROCESS:
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
            case LatinKeyboard.KEYCODE_I_DONT_KNOW_WHY_I_PUT_THAT_HERE:

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
                    handleCharacter(primaryCode, keyCodes);
                }
        }
        try {

            /** Some text processing. Helps some guys improve their writing skills, huh*/

            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("caps",true)) {
                if (isWordSeparator(ic.getTextBeforeCursor(2, 0).toString())) {
                    setCapsOn(true);
                    firstCaps = true;
                }
            }
        } catch (Exception e) {
        }
    }

    public short getRowNumber(){

        return rowNumber;

    }
    public void setRowNumber(int number){

        rowNumber = (short) number;
    }
    public short getStandardRowNumber(){

        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("arr_qrt", false) && PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("nbr_qrt", false)){
            return 5;
        }
        else{
            if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("arr_qrt", false)){
                return 4;
            }
            else if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("nbr_qrt", false)){
                return 5;
            }
            else {
                 return 4;
            }
        }

    }
    public void setStandardKeyboard(){

        int layout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("layout", "1"));

        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("arr_qrt", false) && PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("nbr_qrt", false)){
            switch (layout) {
                case 2:
                    standardKeyboardID = R.xml.azerty_arrow_numbers;
                    break;
                case 3:
                    standardKeyboardID = R.xml.qwertz_arrow_numbers;
                    break;
                default:
                    standardKeyboardID = R.xml.qwerty_arrow_numbers;
            }
            setRowNumber(5);
        }
        else{
            if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("arr_qrt", false)){
                switch (layout) {
                    case 2:
                        standardKeyboardID = R.xml.azerty_arrows;
                        break;
                    case 3:
                        standardKeyboardID = R.xml.qwertz_arrows;
                        break;
                    default:
                        standardKeyboardID = R.xml.qwerty_arrows;
                }
                setRowNumber(4);
            }
            else if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("nbr_qrt", false)){
                switch (layout) {
                    case 2:
                        standardKeyboardID = R.xml.azerty_numbers;
                        break;
                    case 3:
                        standardKeyboardID = R.xml.qwertz_numbers;
                        break;
                    default:
                        standardKeyboardID = R.xml.qwerty_numbers;
                }
                setRowNumber(5);
            }
            else {
                switch (layout) {
                    case 2:
                        standardKeyboardID = R.xml.azerty;
                        break;
                    case 3:
                        standardKeyboardID = R.xml.qwertz;
                        break;
                    default:
                        standardKeyboardID = R.xml.qwerty;
                }
                setRowNumber(4);
            }
        }
    }
    public double getHeightKeyModifier() {

        return (double)PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("height", 50) / (double)50;
    }
}
