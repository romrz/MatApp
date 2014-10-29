package com.rom.matapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.rom.matapp.R;
import com.rom.matapp.WorkSpaceActivity;
import com.rom.matapp.utils.MyUtils;

/**
 * Custom view to show a matrix equation
 * It should contain MatrixView objects
 */
public class ExpressionView extends HorizontalScrollView {

    // Focused state of this expression
    private boolean focused = false;

    // Index of the focused child. -1 if none is focused
    private int childFocused = -1;

    // Linear layout which contains all the children
    private LinearLayout childLayout;

    /*
      The expression from it was created, i.e. the expression whom invoke the resolve method and
      therefore it is the result of that expression.
     */
    private ExpressionView mExpressionParent = null;

    /*
      The result of this expression
     */
    private ExpressionView mExpressionChild = null;

    // Gesture detector for this View
    private GestureDetector mGestureDetector;

    /**
     * Constructor
     *
     * @param context
     */
    public ExpressionView(Context context) {
        super(context, null, R.attr.expressionViewStyle);

        mGestureDetector = new GestureDetector(getWorkSpace(), new MyGestureListener());
        setChildLayout();
        setId(MyUtils.generateViewId());
    }

    /**
     * Adds the child layout to the View
     */
    public void setChildLayout() {
        childLayout = new LinearLayout(getContext(), null, R.attr.expressionChildLayoutStyle);
        addView(childLayout, new LinearLayout.LayoutParams(-2, -2));
    }

    public WorkSpaceActivity getWorkSpace() {
        return (WorkSpaceActivity) getContext();
    }

    /**
     * Returns the index of the focused matrix
     * @return
     */
    public int getChildFocusedIndex() {
        return childFocused;
    }

    public void setChildExpression(ExpressionView expr) {
        mExpressionChild = expr;
    }

    public ExpressionView getExpressionChild() {
        return mExpressionChild;
    }

    public void setParentExpression(ExpressionView expr) {
        mExpressionParent = expr;
    }

    public ExpressionView getExpressionParent() {
        return mExpressionParent;
    }

    /**
     * Returns whether this expression is the result of another expression
     *
     * @return True if this expression has a parent expression or false if not
     */
    public boolean isResult() {
        return mExpressionParent != null;
    }

    public boolean isParent() {
        return mExpressionChild != null;
    }

    /** Sets the focused state
    *
    * @param focus
    */
    public void setFocused(boolean focus) {
        if(focus == focused || getWorkSpace().getActionMode() != null)
            return;

        if(focus) {
            getWorkSpace().setFocusedChild(this);

            if(isResult())
                setBackgroundColor(Color.GREEN);
            else
                setBackgroundResource(R.drawable.focused_expression_view);

            getWorkSpace().invalidateOptionsMenu();
        } else {
            clearFocusChild();

            if(isResult())
                setBackgroundColor(Color.GREEN);
            else
                setBackgroundColor(Color.WHITE);
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
     * Clear the focus on its children and stores the given View position
     *
     * @param view Matrix View or Operator View to be focused
     */
    public void setChildFocused(View view) {
        clearFocusChild();

        for(int i = 0; i < childLayout.getChildCount(); i++)
            if(childLayout.getChildAt(i) == view)
                childFocused = i;
    }

    /**
     * Clear the focus of the focused children
     */
    public void clearFocusChild() {
        if(getWorkSpace().getActionMode() != null)
            return;

        if(childFocused >= 0) {
            View child = childLayout.getChildAt(childFocused);

            if(child instanceof MatrixView)
                ((MatrixView) child).setFocused(false);
            else
                ((OperatorView) child).setFocused(false);
        }

        childFocused = -1;
    }

    /**
     * Returns the focused child
     * @return The focused MatrixView or null if none of its child are focused
     */
    public MatrixView getFocusedMatrix() {
        return (MatrixView) childLayout.getChildAt(childFocused);
    }

    /**
     * Adds a matrix to this expression
     *
     * @param matrixView
     */
    public void addMatrixView(MatrixView matrixView) {

        final int margin = (int) getResources().getDimension(R.dimen.matrix_view_margin);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(margin, margin, margin, margin);

        childLayout.addView(matrixView, params);

        matrixView.setFocused(true);
    }

    /**
     * Adds an operator view to the expression
     *
     * @param operator
     */
    public void addOperator(View operator) {
        if(isResult()) return;

        addOperator(operator, -1, new LinearLayout.LayoutParams(-2, -1));
    }

    /**
     * Adds an operator view to the expression
     *
     * @param operator
     * @param index
     * @param params
     */
    public void addOperator(View operator, int index, ViewGroup.LayoutParams params) {
        if(isResult()) return;

        if(index < 0)
            index = childLayout.getChildCount();

        childLayout.addView(operator, index, params);
    }

    /**
     * Deletes the child with the given id
     */
    public void deleteChild(int id) {
        View child = childLayout.findViewById(id);

        if(child instanceof MatrixView)
            ((MatrixView) child).onDelete();
        else
            ((OperatorView) child).onDelete();

        childLayout.removeView(child);

        childFocused = -1;
    }

    /**
     * Deletes the child in the given index
     */
    public void deleteChildAt(int index) {
        View child = childLayout.getChildAt(index);

        if(child instanceof MatrixView)
            ((MatrixView) child).onDelete();
        else
            ((OperatorView) child).onDelete();

        childLayout.removeView(child);
    }

    /**
     * Removes all the matrices in the expression
     */
    public void delete() {

        int c = childLayout.getChildCount();
        for(int i = 0; i < c; i++)
            deleteChildAt(0);

        removeView(childLayout);

        mExpressionChild = null;
        mExpressionParent = null;
        childLayout = null;
        mGestureDetector = null;
    }

    /**
     * Scrolls the container to the focused Expression
     */
    public void scrollContainer() {
        ScrollView container = (ScrollView) getParent().getParent();

        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                getResources().getDimension(R.dimen.activity_vertical_margin),
                getResources().getDisplayMetrics());

        int bottom = getBottom() + margin - container.getScrollY();

        if(bottom > container.getBottom())
            container.scrollBy(0, bottom - container.getBottom());

    }

    /**
     * Intercepts the touch event.
     * @param event
     */
    public void getTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN && !getFocused())
            setFocused(true);
    }

    @Override
    public String toString() {

        String string = "";
        int c = childLayout.getChildCount();

        for(int i = 0; i < c; i++)
            string += childLayout.getChildAt(i).toString() + " ";

        return string;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getTouchEvent(event);
        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    /**
     * Class to handle gestures on this Expressoin View
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            clearFocusChild();

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(getWorkSpace().getActionMode() != null) return;

            // Starts the action mode for the expression
            clearFocusChild();
            getWorkSpace().setActionMode(startActionMode(new ExpressionAMC()));
            setSelected(true);
        }

    }

    /**
     * ActionMode.Callback to manage the contextual menus for the Expression views
     */
    private class ExpressionAMC implements ActionMode.Callback {

        // Inflates the contextual menu for Expression View when the ActionMode is created
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();

            if(isResult())
                inflater.inflate(R.menu.contextual_menu_res_expression, menu);
            else
                inflater.inflate(R.menu.contextual_menu_expression, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        // Respond to the items clicked by the user
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // If the Expression View is not focused, do nothing
            if(!focused) return false;

            switch(item.getItemId()) {
                case R.id.paste_matrix:
                    return true;
                case R.id.delete_expression:
                    getWorkSpace().deleteExpression();
                    getWorkSpace().getActionMode().finish(); // Close the Action Mode
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getWorkSpace().setActionMode(null);
        }
    }
}