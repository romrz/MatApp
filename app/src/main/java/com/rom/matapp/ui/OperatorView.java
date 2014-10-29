package com.rom.matapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.rom.matapp.R;
import com.rom.matapp.WorkSpaceActivity;
import com.rom.matapp.utils.MyUtils;

/**
 * Created by rom on 15/08/14
 */
public class OperatorView extends TextView {

    // Types of operators
    public enum Type { SUM, SUB, MUL, INV, LB, RB };
    private Type mType;

    private boolean mFocused;

    private GestureDetector mGestureDetector;

    public OperatorView(Context context, Type type) {
        super(context, null, R.attr.operatorViewStyle);

        mFocused = false;
        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener());

        setId(MyUtils.generateViewId());
        setType(type);

    }

    /**
     * Sets the text corresponding to the operator
     *
     * @param op
     */
    public void setType(Type op) {

        mType = op;

        switch(op) {
            case SUM:
                setText("+");
                break;
            case SUB:
                setText("-");
                break;
            case MUL:
                setText("*");
                break;
            case INV:
                setText("-1");
                break;
            case LB:
                setText("(");
                break;
            case RB:
                setText(")");
                break;
        }

    }

    public Type getType() {
        return mType;
    }

    /**
     * Focus this Operator View
     *
     * @param focus
     */
    public void setFocused(boolean focus) {
        if(focus == mFocused || getWorkSpace().getActionMode() != null)
            return;

        if(focus) {
            ((ExpressionView) getParent().getParent()).setChildFocused(this);
            setBackgroundColor(Color.argb(255, 255, 255, 255));
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }

        mFocused = focus;
    }

    /**
     * Returns the focused state
     *
     * @return
     */
    public boolean getFocused() {
        return mFocused;
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
     * Prepares the operator to delete
     */
    public void onDelete() {
        mGestureDetector = null;
    }

    @Override
    public String toString() {
        if(mType == Type.INV)
            return "^-1";

        return getText().toString();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        ((HorizontalScrollView) getParent().getParent()).scrollBy(100, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        ((ExpressionView) getParent().getParent()).getTouchEvent(event);

        int action = event.getActionMasked();

        if(action == MotionEvent.ACTION_DOWN)
            setBackgroundColor(Color.argb(100, 255, 255, 255));
        else if(action == MotionEvent.ACTION_UP && !mFocused)
            setBackgroundColor(Color.TRANSPARENT);
        else if(!mFocused)
            setBackgroundColor(Color.TRANSPARENT);


        mGestureDetector.onTouchEvent(event);

        return true;
    }

    /**
     * Class to handle the gestures
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent event) {
            if(getWorkSpace().getActionMode() != null) return;

            setFocused(true);
            getWorkSpace().setActionMode(startActionMode(new OperatorAMC()));
            setSelected(true);
        }
    }

    /**
     * ActionMode.Callback to manage the contextual menus for the Operator views
     */
    private class OperatorAMC implements ActionMode.Callback {

        // Inflates the contextual menu for Operator View when the ActionMode is created
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu_operator, menu);
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
            if(!mFocused) return false;

            switch(item.getItemId()) {
                case R.id.delete_operator:
                    getWorkSpace().getFocusedExpression().deleteChild(getId());
                    getWorkSpace().getActionMode().finish(); // Close the Action Mode
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getWorkSpace().setActionMode(null);
            setFocused(false);
        }
    }
}
