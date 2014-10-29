package com.rom.matapp.mat;

/**
 * Created by rom on 9/10/14.
 */
public class MatTest {

    public static void main(String args[]) {

        WorkSpace ws = new WorkSpace();

        double dataA[][] = {
                {1, 2, 3},
                {2, 4, 6},
                {4, 8, 12}};

        double dataB[][] = {
                {1},
                {10},
                {2}};


        Matrix A = new Matrix(dataA);
        Matrix B = new Matrix(dataB);

        A.setId(1);
        B.setId(2);

        ws.addMatrix(A);
        ws.addMatrix(B);

        Matrix result = ws.resolveExpression("[1] ^-1 * [2]");

        System.out.println("Status: " + ws.getStatus());

        if(ws.getStatus() == WorkSpace.RESULT_OK)
            System.out.println(result.toString());

        ws.clearStatus();

        //System.out.println(A.solveSystem(B).toString());

    }

}
