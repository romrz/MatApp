package com.rom.matapp.mat;

import java.util.List;
import java.util.LinkedList;

/**
 *
 */
public class WorkSpace {

    public static int RESULT_OK = 1;
    public static int RESULT_ERROR_DIM = 2;
    public static int RESULT_ERROR_SING_MAT = 3;
    public static int RESULT_ERROR_MAT_NFOUND = 4;
    public static int RESULT_ERROR_BAD_EXP = 5;

    // This is where all the matrices are stored
    private List<Matrix> mMatrices;

    private int mStatus = RESULT_OK;

    // Creates a new WorkSpace
    public WorkSpace() {
        mMatrices = new LinkedList<Matrix>();
    }

    public int getStatus() {
        return mStatus;
    }

    public void clearStatus() {
        mStatus = RESULT_OK;
    }

    // Adds a matrix to the matrix list
    public void addMatrix(Matrix matrix) {
        mMatrices.add(matrix);
    }

    /**
     * Finds a matrix in the matrix list given its id
     *
     * @param id
     * @return The matrix or null if it wasn't found
     */
    public Matrix findMatrixById(int id) {
        for(Matrix m : mMatrices)
            if(m.getId() == id)
                return m;

        return null;
    }

    /**
     * Removes a matrix form the workspace
     *
     * @param id
     */
    public void removeMatrix(int id) {
        mMatrices.remove(findMatrixById(id));
    }

    // Resolves the given expression
    public Matrix resolveExpression(String expression) {

        Expression exp = new Expression(this, expression);

        Matrix r = exp.resolve();

        mStatus = exp.getStatus();

        return r;

    }

}
