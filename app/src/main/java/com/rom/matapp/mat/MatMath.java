package com.rom.matapp.mat;

/**
 * Created by rom on 21/08/14.
 */

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This class provides methods for matrix operations
 */
public class MatMath {

    /**
     * Adds two matrices
     *
     * @param m1
     * @param m2
     * @return The result matrix or null if the dimensions don't match
     */
    public static Matrix add(final Matrix m1, final Matrix m2) {

        if (m1.getRows() != m2.getRows() || m1.getCols() != m2.getCols())
            return null;

        int rows = m1.getRows();
        int cols = m1.getCols();

        double result[][] = new double[rows][cols];

        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                result[row][col] = m1.getValue(row, col) + m2.getValue(row, col);

        return new Matrix(result);
    }

    /**
     * Subtract matrix 2 form matrix 1
     *
     * @param m1
     * @param m2
     * @return The result matrix or null if the dimensions don't agree
     */
    public static Matrix subtract(Matrix m1, Matrix m2) {

        if (m1.getRows() != m2.getRows() || m1.getCols() != m2.getCols())
            return null;

        int rows = m1.getRows();
        int cols = m1.getCols();

        double[][] result = new double[rows][cols];

        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                result[row][col] = m1.getValue(row, col) - m2.getValue(row, col);

        return new Matrix(result);
    }

    /**
     * Multiply matrix 1 by matrix 2
     *
     * @param m1 Matrix 1
     * @param m2 Matrix 2
     * @return The result matrix or null if the dimensions don't agree
     */
    public static Matrix multiply(Matrix m1, Matrix m2) {

        if (m1.getCols() != m2.getRows()) return null;

        int rows1 = m1.getRows();
        int cols1 = m1.getCols();
        int cols2 = m2.getCols();

        double result[][] = new double[rows1][cols2];

        for (int row = 0; row < rows1; row++)
            for (int col = 0; col < cols2; col++)
                for (int cr = 0; cr < cols1; cr++)
                    result[row][col] += m1.getValue(row, cr) * m2.getValue(cr, col);

        return new Matrix(result);
    }

    /**
     * Inverses the matrix. Inverses the matrix by the LU decomposition approach
     *
     * @param m Matrix
     * @return The result matrix or null if there was any error
     */
    public static Matrix inverse(Matrix m) {

        if (!m.isSquare()) return null;

        int n = m.getRows();

        double result[][] = new double[n][n];

        Matrix b = new Matrix(n, 1);
        Matrix LU = LUDecompose(m);
        Matrix aux;

        for (int col = 0; col < n; col++) {

            for (int k = 0; k < n; k++)
                if (col == k)
                    b.setValue(1, k, 0);
                else
                    b.setValue(0, k, 0);

            aux = LUSubstitute(LU, b);

            for (int k = 0; k < n; k++)
                result[k][col] = aux.getValue(k, 0);
        }

        return new Matrix(result);
    }

    /**
     * Decomposes the matrix in LU. Auxiliary method for inverse a matrix.
     *
     * @param m Matrix
     * @return A matrix containing the L and U matrices or null if there were any errors
     */
    public static Matrix LUDecompose(Matrix m) {

        if (!m.isSquare()) return null;

        int n = m.getRows();

        double result[][] = m.getData().clone();
        double factor;

        for (int col = 0; col < n - 1; col++)
            for (int row = col + 1; row < n; row++) {

                factor = result[row][col] / result[col][col];
                // L storage
                result[row][col] = factor;

                for (int k = col + 1; k < n; k++)
                    result[row][k] -= result[col][k] * factor;
            }

        return new Matrix(result);
    }

    /**
     * Auxiliary method to calculate the inverse of a matrix
     *
     * @param a Matrix A
     * @param b Matrix b
     * @return The result matrix or null if there was any errors
     */
    private static Matrix LUSubstitute(Matrix a, Matrix b) {

        if (!a.isSquare() || b.getCols() != 1 || a.getRows() != b.getRows()) return null;

        int n = a.getRows();

        double x[][] = new double[n][1];
        double sum;

        for (int row = 1; row < n; row++) {
            sum = b.getValue(row, 0);

            for (int col = 0; col < row; col++)
                sum -= a.getValue(row, col) * b.getValue(col, 0);

            b.setValue(sum, row, 0);
        }

        x[n - 1][0] = b.getValue(n - 1, 0) / a.getValue(n - 1, n - 1);
        for (int row = n - 2; row >= 0; row--) {
            sum = 0;

            for (int col = row + 1; col < n; col++)
                sum += a.getValue(row, col) * x[col][0];

            x[row][0] = (b.getValue(row, 0) - sum) / a.getValue(row, row);
        }

        return new Matrix(x);
    }
}