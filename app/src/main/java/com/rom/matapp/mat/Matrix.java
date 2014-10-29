package com.rom.matapp.mat;

/**
 * This class represents a numeric matrix
 */
public class Matrix {

    // Matrix ID
    private int mId;

    // Matrix name
    private String mName;

    // Matrix dimensions
    private int mRows;
    private int mCols;

    private double mValues[][];

    /**
     * Creates a new matrix with the given dimensions
     *
     * @param r Rows
     * @param c Cols
     */
    public Matrix(int r, int c) {
        this(new double[r][c]);
    }

    /**
     * Creates a new matrix with the given values
     *
     * @param data Values
     */
    public Matrix(double data[][]) {

        mRows = data.length;
        mCols = data[0].length;

        mValues = data;
    }

    public Matrix(Matrix m) {
        mRows = m.mRows;
        mCols = m.mCols;

        mValues = new double[mRows][mCols];

        copyValues(m.getData(), mValues);
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public int getRows() {
        return mRows;
    }

    public int getCols() {
        return mCols;
    }

    public double getValue(int row, int col) {
        return mValues[row][col];
    }

    public void setValue(double value, int row, int col) {
        mValues[row][col] = value;
    }

    public double[][] getData() {
        return mValues;
    }

    public boolean isSquare() {
        return mRows == mCols;
    }

    /**
     * Change the matrix dimensions
     *
     * @param newRows New rows
     * @param newCols New cols
     */
    public void changeDimensions(int newRows, int newCols) {

        double newValues[][] = new double[newRows][newCols];

        copyValues(mValues, newValues);

        mValues = newValues;
        mRows = newRows;
        mCols = newCols;

    }

    /**
     * Adds two matrices
     *
     * @param m
     * @return The result matrix or null if the dimensions don't match
     */
    public Matrix add(final Matrix m) {

        if (mRows != m.getRows() || mCols != m.getCols())
            return null;

        int rows = mRows;
        int cols = mCols;

        double result[][] = new double[rows][cols];

        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                result[row][col] = getValue(row, col) + m.getValue(row, col);

        return new Matrix(result);
    }

    /**
     * Subtract the given matrix from this
     *
     * @param m
     * @return The result matrix or null if the dimensions don't match
     */
    public Matrix subtract(final Matrix m) {
        return subtract(m, false);
    }

    /**
     * Subtract the given matrix from this if inverted is false or
     * subtract this from the given matrix if inverted is true
     *
     * @param m
     * @param inverted Indicates if subtracts the given matrix from this or this from the matrix
     * @return The result matrix or null if the dimensions don't match
     */
    public Matrix subtract(final Matrix m, boolean inverted) {

        if (mRows != m.getRows() || mCols != m.getCols())
            return null;

        int rows = mRows;
        int cols = mCols;

        double result[][] = new double[rows][cols];

        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                if(inverted)
                    result[row][col] = m.getValue(row, col) - getValue(row, col);
                else
                    result[row][col] = getValue(row, col) - m.getValue(row, col);

        return new Matrix(result);
    }

    /**
     * Multiply the given matrix with this
     *
     * @param m Matrix
     * @return The result matrix or null if the dimensions don't agree
     */
    public Matrix multiply(final Matrix m) {

        if (getCols() != m.getRows()) return null;

        int rows1 = getRows();
        int cols1 = getCols();
        int cols2 = m.getCols();

        double result[][] = new double[rows1][cols2];

        for (int k = 0; k < rows1; k++)
            for (int i = 0; i < cols2; i++)
                for (int j = 0; j < cols1; j++)
                    result[k][i] += getValue(k, j) * m.getValue(j, i);

        return new Matrix(result);
    }


    public Matrix solveSystem(Matrix mb) {

        // Checks dimensions
        if(!isSquare() || mCols != mb.mRows)
            return null;

        int n = mCols;

        Matrix a = new Matrix(this);
        Matrix b = new Matrix(mb);

        Matrix x = new Matrix(n, 1);

        /* Gauss method */

        // Gets the highest values of each row
        Matrix s = new Matrix(n, 1);

        for(int i = 0; i < n; i++) {
            s.setValue(Math.abs(a.getValue(i, 0)), i, 0);
            for(int j = 1; j < n; j++)
                if(Math.abs(a.getValue(i, j)) > s.getValue(i, 0))
                    s.setValue(Math.abs(a.getValue(i, j)), i, 0);
        }

        double error = 0.01;

        // Eliminate
        if(!eliminate(a, b, s, error))
            return null;

        // Substitute
        substitute(a, b, x);

        return x;
    }

    private boolean eliminate(Matrix a, Matrix b, Matrix s, double error) {

        int n = a.getRows();

        int k;
        for(k = 0; k < n - 1; k ++) {

            pivot(a, b, s, k);
            if(Math.abs(a.getValue(k, k) / s.getValue(k, 0)) < error)
                return false;

            double factor = 0;
            for(int i = k + 1; i < n; i++) {

                factor = a.getValue(i, k) / a.getValue(k, k);
                for(int j = k + 1; j < n; j++)
                    a.setValue(a.getValue(i, j) - factor * a.getValue(k, j), i, j);

                b.setValue(b.getValue(i, 0) - factor * b.getValue(k, 0), i, 0);
            }

        }

        if(Math.abs(a.getValue(k, k) / s.getValue(k, 0)) < error)
            return false;

        return true;

    }

    private void pivot(Matrix a, Matrix b, Matrix s, int k) {

        int n = a.getRows();

        int p = k;
        double big = Math.abs(a.getValue(k, k) / s.getValue(k, 0));

        double aux = 0;
        for(int i = k + 1; i < n; i++) {
            aux = Math.abs(a.getValue(i, k) / s.getValue(i, 0));
            if(aux > big) {
                big = aux;
                p = i;
            }
        }

        if(p != k) {

            for(int i = k; i < n; i++) {
                aux = a.getValue(p, i);
                a.setValue(a.getValue(k, i), p, i);
                a.setValue(aux, k, i);
            }

            aux = b.getValue(p, 0);
            b.setValue(b.getValue(k, 0), p, 0);
            b.setValue(aux, k, 0);

            aux = s.getValue(p, 0);
            s.setValue(s.getValue(k, 0), p, 0);
            s.setValue(aux, k, 0);

        }

    }

    private void substitute(Matrix a, Matrix b, Matrix x) {

        int n = a.getRows();

        x.setValue(b.getValue(n - 1, 0) / a.getValue(n - 1, n - 1), n - 1, 0);

        double sum;
        for(int i = n - 2; i >= 0; i--) {

            sum = 0.0;

            for(int j = i + 1; j < n; j++)
                sum = sum + a.getValue(i, j) * x.getValue(j, 0);

            x.setValue((b.getValue(i, 0) - sum) / a.getValue(i, i), i, 0);

        }

    }

    /**
     * Inverses the matrix. Inverses the matrix by the LU decomposition approach
     *
     * @return The result matrix or null if there was any error
     */
    public Matrix inverse() {

        if (!isSquare()) return null;

        int n = getRows();

        Matrix a = new Matrix(this);
        Matrix b = new Matrix(n, 1);
        Matrix o = new Matrix(n, 1);
        Matrix s = new Matrix(n, 1);
        Matrix r = new Matrix(n, n);

        double error = 0.01;

        if(LUDecompose(a, o, s, error)) {
            Matrix x;

            for (int col = 0; col < n; col++) {

                for (int k = 0; k < n; k++)
                    if (col == k)
                        b.setValue(1, k, 0);
                    else
                        b.setValue(0, k, 0);

                x = LUSubstitute(a, b, o);

                for (int k = 0; k < n; k++)
                    r.setValue(x.getValue(k, 0), k, col);
            }
        }
        else
            return null;

        return r;
    }

    /**
     * Decomposes the matrix in LU.
     *
     * @return A matrix containing the L and U matrices or null if there were any errors
     */
    public boolean LUDecompose(Matrix a, Matrix o, Matrix s, double error) {

        int n = getRows();

        for(int i = 0; i < n; i++) {

            o.setValue(i, i, 0);
            s.setValue(Math.abs(a.getValue(i, 0)), i, 0);

            for(int j = 1; j < n; j++)
                if(Math.abs(a.getValue(i, j)) > s.getValue(i, 0))
                    s.setValue(Math.abs(a.getValue(i, j)), i, 0);

        }

        double factor;

        int k;
        for (k = 0; k < n - 1; k++) {

            pivot2(a, o, s, k);

            if(Math.abs(a.getValue((int)o.getValue(k, 0), k) / s.getValue((int)o.getValue(k, 0), 0)) < error)
                return false;

            for (int i = k + 1; i < n; i++) {


                factor = a.getValue((int)o.getValue(i, 0), k) / a.getValue((int)o.getValue(k, 0), k);

                a.setValue(factor, (int) o.getValue(i, 0), k);

                // U Storage
                for (int j = k + 1; j < n; j++)
                    a.setValue(a.getValue((int)o.getValue(i, 0), j) - factor * a.getValue((int)o.getValue(k, 0), j), (int)o.getValue(i, 0), j);
            }

        }

        if(Math.abs(a.getValue((int)o.getValue(k, 0), k) / s.getValue((int)o.getValue(k, 0), 0)) < error)
            return false;

        return true;

    }

    private void pivot2(Matrix a, Matrix o, Matrix s, int k) {

        int n = a.getRows();

        int p = k;
        double big = Math.abs(a.getValue((int)o.getValue(k, 0), k) / s.getValue((int)o.getValue(k, 0), 0));

        double aux = 0;
        for(int i = k + 1; i < n; i++) {
            aux = Math.abs(a.getValue((int)o.getValue(i, 0), k) / s.getValue((int)o.getValue(i, 0), 0));
            if(aux > big) {
                big = aux;
                p = i;
            }
        }

        aux = o.getValue(p, 0);
        o.setValue(o.getValue(k, 0), p, 0);
        o.setValue(aux, k, 0);

    }

    /**
     * Auxiliary method to calculate the inverse of a matrix
     *
     * @param a Matrix A
     * @param b Matrix b
     * @return The result matrix or null if there was any errors
     */
    private Matrix LUSubstitute(Matrix a, Matrix b, Matrix o) {

        int n = a.getRows();

        double sum;
        double x[][] = new double[n][1];

        for (int i = 1; i < n; i++) {
            sum = b.getValue((int)o.getValue(i, 0), 0);

            for (int j = 0; j < i; j++)
                sum -= a.getValue((int)o.getValue(i, 0), j) * b.getValue((int)o.getValue(j, 0), 0);

            b.setValue(sum, (int)o.getValue(i, 0), 0);
        }

        x[n - 1][0] = b.getValue((int)o.getValue(n-1, 0), 0) / a.getValue((int)o.getValue(n-1, 0), n - 1);
        for (int i = n - 2; i >= 0; i--) {
            sum = 0;

            for (int j = i + 1; j < n; j++)
                sum += a.getValue((int)o.getValue(i, 0), j) * x[j][0];

            x[i][0] = (b.getValue((int)o.getValue(i, 0), 0) - sum) / a.getValue((int)o.getValue(i, 0), i);
        }

        return new Matrix(x);
    }

    /**
     * Copy the values form source to destiny. If destiny's dimensions are lower those values are lost
     * Fills the voids with zeros.
     *
     * @param src Source
     * @param dest Destiny
     */
    private void copyValues(double src[][], double dest[][]) {

        int oldR = src.length;
        int oldC = src[0].length;

        int newR = dest.length;
        int newC = dest[0].length;

        for(int row = 0; row < newR && row < oldR; row++)
            for(int col = 0; col < newC && col < oldC; col++)
                dest[row][col] = src[row][col];

    }

    @Override
    public String toString() {

        String str = "";

        for(int row = 0; row < mRows; row++) {
            for (int col = 0; col < mCols; col++)
                str += mValues[row][col] + " ";

            str += "\n";
        }

        str += "\n";

        return str;
    }
}
