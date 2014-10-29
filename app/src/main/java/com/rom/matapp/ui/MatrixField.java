package com.rom.matapp.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.rom.matapp.R;

/**
 * Custom EditText to represent a Matrix Field
 */
public class MatrixField extends EditText {

    public MatrixField(Context context) {
        super(context, null, R.attr.matrixFieldStyle);

        // Prevent the Selection ActionMode of being started
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    /**
     * Set the value to this Matrix Field
     */
    public void setValue(double value) {
        setText("" + value);
    }

    /**
     * Obtains the double value of this MatrixField
     */
    public double getValue() {
        return Double.valueOf(getText().toString());
    }

    /**
     * Calculates the MatrixField width
     */
    public int getFieldWidth() {
        Rect bounds = new Rect();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), bounds);

        return getCompoundPaddingLeft() + getCompoundPaddingRight() + bounds.width();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        // Passes the event to its parent
        if( !((View) getParent()).isEnabled() )
            ((MatrixView) getParent()).onTouchEvent(event);

        return true;
    }

    /**
     * All the Matrix Fields must remain at the same size.
     * When a Matrix Field changes its size, this method checks if the new sizes is bigger
     * or lower than the other's and adjust the sizes.
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        MatrixView parent = (MatrixView) getParent();

        if(parent != null) {
            int newSize = getFieldWidth();

            setMinWidth(newSize);
            parent.changeMatrixFieldWidth(newSize);
        }
    }
}