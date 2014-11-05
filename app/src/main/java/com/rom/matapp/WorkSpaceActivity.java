package com.rom.matapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rom.matapp.mat.Matrix;
import com.rom.matapp.mat.WorkSpace;
import com.rom.matapp.ui.ExpressionView;
import com.rom.matapp.ui.MatrixField;
import com.rom.matapp.ui.MatrixView;
import com.rom.matapp.ui.NewMatrixDialogFragment;
import com.rom.matapp.ui.OperatorView;
import com.rom.matapp.utils.MyUtils;

import java.util.concurrent.locks.ReentrantLock;


public class WorkSpaceActivity extends Activity {

    // Constants for menu items ids
    public static final int MI_NEW_EXP = 1;
    public static final int MI_NEW_MAT = 2;

    private WorkSpace mWorkspace;

    private ActionMode mActionMode;

    // ExpressionView's Container
    private ViewGroup mContainer;

    // Current focused child. -1 if none is focused
    private int childFocused = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_space);

        mWorkspace = new WorkSpace();

        mContainer = (ViewGroup) findViewById(R.id.container);

        findViewById(R.id.scroll_container).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                            clearFocusChild();
                            invalidateOptionsMenu();
                        return false;
                    }
                }
        );

        findViewById(R.id.scroll_container).requestFocus();
    }

    /**
     * Creates a new expression
     */
    public void addExpression() {

        final int margin = (int) getResources().getDimension(R.dimen.matrix_view_margin);
        final int index = childFocused >= 0 ? childFocused + 1 : mContainer.getChildCount();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = margin;

        ExpressionView expression = new ExpressionView(this);

        mContainer.addView(expression, index, params);
        expression.setFocused(true);
    }

    /**
     * Adds the result expression to this expression
     */
    public void resolveExpression() {
        if(!isExpressionFocused() || getFocusedExpression().isResult()) return;

        String string = getFocusedExpression().toString();

        Matrix r = mWorkspace.resolveExpression(string);

        int expStatus = mWorkspace.getStatus();

        if(expStatus == WorkSpace.RESULT_OK) {

            final int id = MyUtils.generateViewId();

            MatrixView m = new MatrixView(this, r.getRows(), r.getCols());

            r.setName(MyUtils.generateMatrixName());

            r.setId(id);
            m.setId(id);

            m.setMat(r);
            mWorkspace.addMatrix(r);

            if (getFocusedExpression().isParent())
                updateResultExpression(m);
            else
                addResultExpression(m);

            m = null;
            r = null;
        }
        else {

            int textId;

            switch(expStatus) {
                case WorkSpace.RESULT_ERROR_DIM:
                    textId = R.string.error_wrong_dimen;
                    break;
                case WorkSpace.RESULT_ERROR_SING_MAT:
                    textId = R.string.error_sing_mat;
                    break;
                default:
                    textId = R.string.error_bad_exp;
            }

            Toast.makeText(getApplicationContext(), textId, Toast.LENGTH_SHORT).show();

        }

    }

    public void addResultExpression(MatrixView m) {
        final int margin = (int) getResources().getDimension(R.dimen.matrix_view_margin);
        final int index = childFocused >= 0 ? childFocused + 1 : mContainer.getChildCount();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = margin;

        ExpressionView expression = new ExpressionView(this);
        expression.addMatrixView(m);

        getFocusedExpression().setChildExpression(expression);
        expression.setParentExpression(getFocusedExpression());

        mContainer.addView(expression, index, params);
        expression.setFocused(true);
    }

    public void updateResultExpression(MatrixView m) {

        ExpressionView expr = getFocusedExpression().getExpressionChild();

        expr.deleteChildAt(0);
        expr.addMatrixView(m);
    }

    /**
     * Deletes the focused expression
     */
    public void deleteExpression() {
        if(childFocused < 0) return;

        ExpressionView expression = getFocusedExpression();

        if(expression.isParent()) {
            expression.getExpressionChild().delete();
            mContainer.removeView(expression.getExpressionChild());
        }

        if(expression.isResult())
            expression.getExpressionParent().setChildExpression(null);

        expression.delete();
        mContainer.removeView(expression);

        childFocused = -1;
        invalidateOptionsMenu();
    }

    /**
     * Adds a new matrix view to the focused expression
     *
     * @param rows
     * @param cols
     */
    public void addMatrix(int rows, int cols, String name) {
        if(!isExpressionFocused() || rows < 1 || cols < 1 || getFocusedExpression().isResult())
            return;

        int id = MyUtils.generateViewId();

        MatrixView matrixView = new MatrixView(this, rows, cols);
        Matrix matrix = new Matrix(rows, cols);

        matrix.setName(name);

        matrixView.setId(id);
        matrix.setId(id);

        matrixView.setMat(matrix);

        mWorkspace.addMatrix(matrix);
        getFocusedExpression().addMatrixView(matrixView);
    }

    public void deleteMatrix(int id) {
        mWorkspace.removeMatrix(id);
    }

    /**
     * Focus the given view
     *
     * @param view
     */
    public void setFocusedChild(ExpressionView view) {
        clearFocusChild();

        for(int i = 0; i < mContainer.getChildCount(); i++)
            if(mContainer.getChildAt(i) == view)
                childFocused = i;
    }

    /**
     * Clear the focus of all the expressions
     */
    public void clearFocusChild() {
        if(getActionMode() != null) return;

        if(childFocused >= 0)
            getFocusedExpression().setFocused(false);

        childFocused = -1;
    }

    /**
     * Checks if an expression is focused
     *
     * @return True if an expression is focused. False otherwise
     */
    public boolean isExpressionFocused() {
        return childFocused >= 0;
    }

    /**
     * Returns the Focused Expression
     *
     * @return The focused ExpressionView or null of none of its children is focused
     */
    public ExpressionView getFocusedExpression() {
        return childFocused >= 0 ? (ExpressionView) mContainer.getChildAt(childFocused) : null;
    }

    /**
     * Returns the Focused Matrix
     *
     * @return The focused MatrixView or null of none of its children is focused
     */
    public MatrixView getFocusedMatrix() {
        return childFocused >= 0 ? getFocusedExpression().getFocusedMatrix() : null;
    }

    /**
     * Returns the current Action Mode
     *
     * @return The current action mode or null of there is no action mode
     */
    public ActionMode getActionMode() {
        return mActionMode;
    }

    /**
     * Sets the currently using ActionMode
     *
     * @param actionMode
     */
    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    /**
     * Called when a operator item menu is selected
     *
     * @param view
     */
    public void onAppMenuItemSelected(View view) {
        if(!isExpressionFocused() || getFocusedExpression().isResult()) return;

        switch(view.getId()) {
            case R.id.menu_btn_sum:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.SUM));

                break;
            case R.id.menu_btn_subtraction:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.SUB));

                break;
            case R.id.menu_btn_multiplication:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.MUL));

                break;
            case R.id.menu_btn_inverse:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.INV));

                break;
            case R.id.menu_btn_lbracket:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.LB));

                break;
            case R.id.menu_btn_rbracket:

                getFocusedExpression().addOperator(new OperatorView(this, OperatorView.Type.RB));

                break;
            case R.id.menu_btn_equal:

                resolveExpression();

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mActionMode != null) return true;

        menu.clear();

        menu.add(Menu.NONE, MI_NEW_EXP, Menu.NONE, "New Expression")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        if(isExpressionFocused() && !getFocusedExpression().isResult()) {

            menu.add(Menu.NONE, MI_NEW_MAT, Menu.NONE, "New Matrix")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.work_space, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case MI_NEW_EXP:
                addExpression();

                break;
            case MI_NEW_MAT:
                if(isExpressionFocused())
                    new NewMatrixDialogFragment().show(getFragmentManager(), "New matrix");

                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
