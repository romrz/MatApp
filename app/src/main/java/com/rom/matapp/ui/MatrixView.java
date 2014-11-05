package com.rom.matapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.rom.matapp.R;
import com.rom.matapp.WorkSpaceActivity;
import com.rom.matapp.mat.Matrix;
import com.rom.matapp.utils.MyUtils;

/**
 * The MatrixView is the visual representation of a Matrix
 */
public class MatrixView extends GridLayout {

    // States of the view
    private boolean enabled = false;
    private boolean focused = false;
    private boolean contracted = false;

    private Matrix mMatrix = null;

    // Variable for maintaining the Matrix Fields width the same
    private int currentFieldWidth = 0;

    // Gesture Detector for this Matrix View
    private GestureDetector mGestureDetector;

    /**
     * Constructor. Initializes with the Context and the specified rows an cols
     *
     * @param context
     * @param rows
     * @param cols
     */
    public MatrixView(Context context, int rows, int cols) {
        super(context, null, R.attr.matrixViewStyle);

        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        setMatrixFields(rows, cols); // Creates the MatrixFields
    }

    /**
     * Sets the column and row count to this view and creates the matrix fields
     *
     * @param cols
     * @param rows
     */
    public void setMatrixFields(int rows, int cols) {

        setRowCount(rows);
        setColumnCount(cols);

        // Matrix Fields margin
        int margin = (int) getResources().getDimension(R.dimen.matrix_field_margin);

        MatrixField field;

        int currentId = MyUtils.generateViewId();

        // Aux for creating and adding the matrix fields
        LayoutParams layoutParams;

        int fields = rows * cols;
        for (int i = 0; i < fields; i++) {
            // Creates the layout params for each matrix fields
            layoutParams = new LayoutParams();
            layoutParams.setMargins(margin, margin, margin, margin);

            int nextId = MyUtils.generateViewId();

            field = new MatrixField(getContext());
            field.setId(currentId);

            if (i < fields - 1)
                field.setNextFocusDownId(nextId);
            else
                field.setImeOptions(EditorInfo.IME_ACTION_DONE);

            currentId = nextId;

            // Creates the matrix field and adds it to the MatrixView
            addView(field, layoutParams);
        }

        // Adds a TextView to show the Matrix Name when the MatrixView is contracted
        // It must be the last view and it's initially hidden
        layoutParams = new LayoutParams();
        layoutParams.setMargins(10, 10, 10, 10);

        TextView t = new TextView(getContext());
        t.setVisibility(View.GONE);
        t.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Passes the event to its parent
                if( !((View) v.getParent()).isEnabled() )
                    ((MatrixView) v.getParent()).onTouchEvent(event);
                return false;
            }
        });

        addView(t, layoutParams);
    }

    /**
     * Sets the source Matrix
     * @param m
     */
    public void setMat(Matrix m) {
        mMatrix = m;
        fillMatrixView();
    }

    /**
     * Gets the source Matrix
     * @return
     */
    public Matrix getMat() {
        return mMatrix;
    }

    /**
     * Gets the WorkSpaceActivity
     *
     * @return The activity WorkSpaceActivity
     */
    public WorkSpaceActivity getWorkSpace() {
        return (WorkSpaceActivity) getContext();
    }

    /**
     * Sets the name to this matrix
     *
     * @param name
     */
    public void setName(String name) {
        mMatrix.setName(name);
    }

    /**
     * Gets the name of this matrix
     *
     */
    public String getName() {
        return mMatrix.getName();
    }

    /**
     * Enables or disables the Matrix View
     *
     * @param e State
     */
    public void setEnabled(boolean e) {

        int c = getChildCount() - 1;
        for (int i = 0; i < c; i++)
            getChildAt(i).setEnabled(e);

        if (e) {
            // Makes the bottom menu invisible when a MatrixView is enabled
            getWorkSpace().findViewById(R.id.menu).setVisibility(View.GONE);
        } else {
            // Removes the focus from the MatrixFields by giving it to the main layout
            getWorkSpace().findViewById(R.id.MainLayout).requestFocus();
            // Makes the bottom menu visible
            getWorkSpace().findViewById(R.id.menu).setVisibility(View.VISIBLE);

            // Saves the matrix fields values
            saveMatrix();
        }

        enabled = e;
    }

    /**
     * Returns the enable state
     *
     * @return enable state
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles the enabled state of this View
     */
    public void toggleEnabled() {
        setEnabled(!enabled);
    }

    /**
     * Saves the values from the MatrixFields to the Matrix
     */
    private void saveMatrix() {
        if(mMatrix == null) return;

        int c = getChildCount() - 1;
        int rows = mMatrix.getRows();
        int cols = mMatrix.getCols();
        for(int i = 0; i < c; i++)
            mMatrix.setValue(((MatrixField) getChildAt(i)).getValue(), i / cols, i % cols);

    }

    /**
     * Fills the Matrix Fields with the values from the Matrix
     */
    private void fillMatrixView() {
        if(mMatrix == null) return;

        int c = getChildCount() - 1;
        int rows = mMatrix.getRows();
        int cols = mMatrix.getCols();
        for(int i = 0; i < c; i++)
            ((MatrixField) getChildAt(i)).setValue(mMatrix.getValue(i / cols, i % cols));
    }

    /**
     * Sets the focused state
     *
     * @param focus
     */
    public void setFocused(boolean focus) {
        if (focus == focused || getWorkSpace().getActionMode() != null)
            return;

        if (focus) {
            ((ExpressionView) getParent().getParent()).setChildFocused(this);
            setBackgroundResource(R.drawable.matrix_brackets_focused);
            getWorkSpace().invalidateOptionsMenu();
            //scrollContainer();
        } else {
            setBackgroundResource(R.drawable.matrix_brackets_normal);
        }

        focused = focus;
    }

    /**
     * Returns the focused state
     *
     * @return
     */
    public boolean getFocused() {
        return focused;
    }

    /**
     * Contracts or expands the MatrixView
     *
     * @param contract True to contract the MatrixView or false to expand it.
     */
    public void setContracted(boolean contract) {
        if(contract == contracted) return; // Do nothing if is in the same state

        ((TextView) getChildAt(getChildCount() - 1)).setText(mMatrix.getName());

        // Shows or hides the MatrixFields
        int c = getChildCount() - 1;
        for(int i = 0; i < c; i++)
            getChildAt(i).setVisibility(contract ? View.GONE : View.VISIBLE);

        // Shows or hides the TextView containing the Matrix name
        getChildAt(c).setVisibility(contract ? View.VISIBLE : View.GONE);

        // ((View) getParent()).invalidate();
        // ((View) getParent()).requestLayout();

        contracted = contract;
    }

    /**
     * Toggles the contracted state of the MatrixView
     */
    public void toggleContracted() {
        setContracted(!contracted);
    }

    /**
     * Adjust the Matrix Fields' sizes for keeping the same size on all of them
     *
     * @param width
     */
    public void changeMatrixFieldWidth(int width) {
        currentFieldWidth = findMaxWidth(width);

        // Change the fields size
        int c = getChildCount() - 1;
        for (int i = 0; i < c; i++)
            ((MatrixField) getChildAt(i)).setMinWidth(currentFieldWidth);

    }

    /**
     * Finds the widest MatrixField
     *
     * @param width
     * @return the max width
     */
    private int findMaxWidth(int width) {
        int maxWidth = width;

        MatrixField field;

        int c = getChildCount() - 1;
        for (int i = 0; i < c; i++) {
            field = (MatrixField) getChildAt(i);

            if (field.getFieldWidth() > maxWidth)
                maxWidth = field.getFieldWidth();
        }

        return maxWidth;
    }

    /**
     * Prepares the matrix to delete
     */
    public void onDelete() {
        mGestureDetector = null;
        getWorkSpace().deleteMatrix(mMatrix.getId());
        mMatrix = null;
    }

    /**
     * This string is useful for identify the Matrix it represents because this string
     * is used in the evaluating and expression solving
     */
    @Override
    public String toString() {
        return "[" + getId() + "]";
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


    }

    /**
     * Handles the touch events in this view trough a GestureDetector
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /* Passes the event to its parent.
           This is done for keeping the focused state alright.
           I mean, if the Matrix Field is focused, also must the Expression View containing it
           focused. */
        ((ExpressionView) getParent().getParent()).getTouchEvent(event);

        // Passes the event to the Gesture Dectector
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * Private class to manage the gestures
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        // Focuses the Matrix View when the users tap up it
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            setFocused(true);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            toggleContracted();
            return false;
        }

        /**
         * Enables the Matrix View and shows its Action Mode Menu
         */
        @Override
        public void onLongPress(MotionEvent e) {
            if (getWorkSpace().getActionMode() != null) return;

            // Focus the Matrix View
            setFocused(true);

            // Starts the Action Mode
            getWorkSpace().setActionMode(startActionMode(new MatrixAMC()));

            setSelected(true);

            if(!((ExpressionView) getParent().getParent()).isResult())
                setEnabled(true);
        }
    }

    /**
     * ActionMode.Callback to manage the contextual menus for the matrix views
     */
    private class MatrixAMC implements ActionMode.Callback {

        // Inflates the contextual menu for Matrix View when the ActionMode is created
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();

            if(((ExpressionView) getParent().getParent()).isResult())
                inflater.inflate(R.menu.contextual_menu_res_matrix, menu);
            else
                inflater.inflate(R.menu.contextual_menu_matrix, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        // Respond to the items clicked by the user
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // If the Matrix View is not focused, do nothing
            if (!focused) return false;

            switch (item.getItemId()) {
                case R.id.copy_matrix:
                case R.id.paste_matrix:
                case R.id.toggle_enable_matrix:
                    return true;
                case R.id.change_matrix_name:

                    new ChangeMatrixNameDialogFragment().show(getWorkSpace().getFragmentManager(), "Change name");

                    return true;
                case R.id.delete_matrix:

                    //getWorkSpace().deleteMatrix(getId());
                    ((ExpressionView) getParent().getParent()).deleteChild(getId());
                    getWorkSpace().getActionMode().finish(); // Close the Action Mode

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            setEnabled(false);
            getWorkSpace().setActionMode(null);
        }
    }
}
