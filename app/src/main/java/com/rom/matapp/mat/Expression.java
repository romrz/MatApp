package com.rom.matapp.mat;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by rom on 22/08/14.
 */
public class Expression {

    private WorkSpace mWorkSpace;

    // The string representing this expression
    private String mExpression;

    private int mStatus;

    public Expression(WorkSpace ws, String expression) {
        mWorkSpace = ws;
        mExpression = expression;
        mStatus = 1;
    }

    public int getStatus() {
        return mStatus;
    }

    // Checks which operators has higher precedence
    private boolean hasHigherPrecedence(String op1, String op2) {
        return operatorPriority(op1) > operatorPriority(op2);
    }

    /**
     * Gets the priority of the operator.
     * 0 for a lower priority.
     */
    private int operatorPriority(String operator) {

        if (operator.equals("(") || operator.equals(")"))
            return 0;
        else if (operator.equals("+") || operator.equals("-"))
            return 1;
        else if (operator.equals("*"))
            return 2;
        else if (operator.equals("^-1"))
            return 3;
        else
            return 0;
    }

    // Gets the int value of num in token "[num]"
    private int getIdFromToken(String token) {
        return Integer.parseInt(token.substring(1, token.length() - 1));
    }

    /**
     * Resolves this expression
     *
     * @return The result matrix or null if there was any problem
     */
    public Matrix resolve() {
        return evaluate(toPostfix());
    }

    /**
     * Converts the infix expression to postfix expression.
     *
     * @return A postfix expression string or null if there was any error
     */
    private String toPostfix() {

        String postfix = "";
        Deque<String> tempStack = new ArrayDeque<String>();

        String tokens[] = mExpression.split(" ");

        for(String token : tokens) {

            if(token.equals("+") || token.equals("-") || token.equals("*") || token.equals("^-1")) {

                while (!tempStack.isEmpty() && !hasHigherPrecedence(token, tempStack.peek())) {
                    if (postfix.length() > 0)
                        postfix += " ";

                    postfix += tempStack.pop();
                }

                tempStack.push(token);

            } else if(token.equals("(")) {

                tempStack.push(token);

            } else if(token.equals(")")) {

                while (!tempStack.isEmpty()) {
                    String operator = tempStack.pop();

                    if (operator.equals("("))
                        break;
                    else if (tempStack.isEmpty())
                        return null;
                    else
                        postfix += " " + operator;

                }

            }
            // The matrices are represented in string format as "[matrix_id]"
            // therefore it checks if the token is a matrix and if so, add it to the postfix expression
            else if(token.matches("\\x5B\\d+\\x5D")) {

                if(postfix.length() > 0)
                    postfix += " ";

                postfix += token;

            } else
                return null;

        }

        while(!tempStack.isEmpty()) {

            String operator = tempStack.pop();
            if(operator.equals("("))
                return null;

            postfix += " " + operator;

        }

        return postfix;
    }

    /**
     * Evaluates the given postfix expression
     *
     * @param expression Postfix Expression
     * @return The result matrix or null if there was any error
     */
    private Matrix evaluate(String expression) {

        Deque<Matrix> tempStack = new ArrayDeque<Matrix>();
        String tokens[] = expression.split(" ");

        for(String token : tokens) {

            Matrix m = null;

            if(token.equals("+"))

                if (tempStack.size() >= 2) {
                    if ((m = tempStack.pop().add(tempStack.pop())) == null)
                        mStatus = WorkSpace.RESULT_ERROR_DIM;
                } else
                    mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;
            else if(token.equals("-"))

                if(tempStack.size() >= 2) {
                    if ((m = tempStack.pop().subtract(tempStack.pop(), true)) == null)
                        mStatus = WorkSpace.RESULT_ERROR_DIM;
                } else
                    mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;

            else if(token.equals("*"))

                if(tempStack.size() >= 2) {
                    Matrix tmp = tempStack.pop();
                    if((m = tempStack.pop().multiply(tmp)) == null)
                        mStatus = WorkSpace.RESULT_ERROR_DIM;
                } else
                    mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;

            else if(token.equals("^-1"))

                if(tempStack.size() >= 1) {
                    if ((m = tempStack.pop().inverse()) == null)
                        mStatus = WorkSpace.RESULT_ERROR_SING_MAT;
                } else
                        mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;

            // The matrices are represented in string format as "[matrix_id]"
            // therefore it checks if the token is a matrix and if so, finds the matrix with the id given by the token
            else if(token.matches("\\x5B\\d+\\x5D")) {

                if ((m = mWorkSpace.findMatrixById(getIdFromToken(token))) == null)
                    mStatus = WorkSpace.RESULT_ERROR_MAT_NFOUND;
            }
            else
                mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;

            if(mStatus != WorkSpace.RESULT_OK)
                return null;

            tempStack.push(m);

        }

        if(tempStack.size() != 1) {
            mStatus = WorkSpace.RESULT_ERROR_BAD_EXP;
            return null;
        }

        return new Matrix(tempStack.pop());
    }
}
