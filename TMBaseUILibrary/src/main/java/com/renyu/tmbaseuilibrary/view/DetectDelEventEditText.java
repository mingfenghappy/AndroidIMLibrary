package com.renyu.tmbaseuilibrary.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Created by Administrator on 2018/4/13.
 */
public class DetectDelEventEditText extends EditText implements View.OnKeyListener,
        EditableInputConnection.OnDelEventListener {
    private DelEventListener delEventListener;

    public DetectDelEventEditText(Context context) {
        super(context);
        init();
    }

    public DetectDelEventEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetectDelEventEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnKeyListener(this);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        super.onCreateInputConnection(outAttrs);
        EditableInputConnection editableInputConnection = new EditableInputConnection(this);
        outAttrs.initialSelStart = getSelectionStart();
        outAttrs.initialSelEnd = getSelectionEnd();
        outAttrs.initialCapsMode = editableInputConnection.getCursorCapsMode(getInputType());

        editableInputConnection.setDelEventListener(this);

        return editableInputConnection;
    }

    public void setDelListener(DelEventListener l) {
        delEventListener = l;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return delEventListener != null && keyCode == KeyEvent.KEYCODE_DEL && event
                .getAction() == KeyEvent.ACTION_DOWN && delEventListener.delEvent();
    }

    @Override
    public boolean onDelEvent() {
        return delEventListener != null && delEventListener.delEvent();
    }

    public interface DelEventListener {
        boolean delEvent();
    }
}
