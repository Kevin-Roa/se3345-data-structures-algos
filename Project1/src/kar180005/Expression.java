// Starter code for Project 1 provided by Prof. Sridhar Alagar
// Kevin Roa

package kar180005;

import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * Class to store a node of expression tree For each internal node, element
 * contains a binary operator List of operators: +|*|-|/|%|^ Other tokens: (|)
 * Each leaf node contains an operand (long integer)
 */

public class Expression {
    public enum TokenType {
        // NIL is a special token that can be used to mark bottom of stack
        // + * - / % ^ ( ) 0-9
        PLUS, TIMES, MINUS, DIV, MOD, POWER, OPEN, CLOSE, NIL, NUMBER
    }

    public static class Token {
        TokenType token;
        int priority; // for precedence of operator
        Long number; // used to store number of token = NUMBER
        String string;

        Token(TokenType op, int pri, String tok) {
            token = op;
            priority = pri;
            number = null;
            string = tok;
        }

        // Constructor for number. To be called when other options have been exhausted.
        Token(String tok) {
            token = TokenType.NUMBER;
            number = Long.parseLong(tok);
            string = tok;
        }

        boolean isOperand() {
            return token == TokenType.NUMBER;
        }

        public long getValue() {
            return isOperand() ? number : 0;
        }

        public String toString() {
            return string;
        }
    }

    Token element;
    Expression left, right;

    // Create token corresponding to a string
    // tok is "+" | "*" | "-" | "/" | "%" | "^" | "(" | ")"| NUMBER
    // NUMBER is either "0" or "[-]?[1-9][0-9]*
    static Token getToken(String tok) {
        switch (tok) {
            // Priority depends on order of operations (PEMDAS)
            // |"+"|"-"|"*"|"/"|"%"|"^"|"("|")"|
            // | 1 | 1 | 2 | 2 | 2 | 3 | 4 | 4 |
            // Higher value = higher priority
            // Equal value = left associative, eval from left to right
            // Priority for ( ) dont matter because they are handled differently
            case "+":
                return new Token(TokenType.PLUS, 1, tok);
            case "*":
                return new Token(TokenType.TIMES, 2, tok);
            case "-":
                return new Token(TokenType.MINUS, 1, tok);
            case "/":
                return new Token(TokenType.DIV, 2, tok);
            case "%":
                return new Token(TokenType.MOD, 2, tok);
            case "^":
                return new Token(TokenType.POWER, 3, tok);
            case "(":
                return new Token(TokenType.OPEN, 4, tok);
            case ")":
                return new Token(TokenType.CLOSE, 4, tok);
            // Number
            default:
                return new Token(tok);
        }
    }

    private Expression() {
        element = null;
    }

    private Expression(Token oper, Expression left, Expression right) {
        this.element = oper;
        this.left = left;
        this.right = right;
    }

    private Expression(Token num) {
        this.element = num;
        this.left = null;
        this.right = null;
    }

    // Given a list of tokens corresponding to an infix expression,
    // return the expression tree corresponding to it.
    // @param exp A list of tokens in infix order
    // @return An Expression tree corresponding to the given list of tokens
    // @see Token
    // @see Expression
    public static Expression infixToExpression(List<Token> exp) {
        // Convert to postfix notation because it is easier to work with
        // Hopefully this is allowed because it is by far the simplist solution
        // There is no need to worry about precedence or order of operations
        List<Token> postfix = infixToPostfix(exp);

        return postfixToExpression(postfix);
    }

    // Given a list of tokens corresponding to a postfix expression,
    // Return the expression tree corresponding to it.
    // @param exp A list of tokens in postfix order
    // @return An Expression tree corresponding to the given list of tokens
    // @see Token
    // @see Expression
    public static Expression postfixToExpression(List<Token> exp) {
        // Dont need to worry about out of bounds because valid value always passed
        Token tok = exp.get(exp.size() - 1);

        // Base case, if operand then leaf
        if (tok.isOperand())
            return new Expression(tok);
        else {
            Expression left, right;

            // Remove last element from token list
            // Ref of exp is passed therefore need to get size of exp every time
            exp.remove(exp.size() - 1);
            // Get right side via recursion
            right = postfixToExpression(exp);

            exp.remove(exp.size() - 1);
            // Get left side via recursion
            left = postfixToExpression(exp);

            return new Expression(tok, left, right);
        }
    }

    // Given a list of tokens corresponding to an infix expression,
    // return its equivalent postfix expression as a list of tokens.
    // @param exp A list of tokens in infix order
    // @return A list of tokens in postfix order
    // @see Token
    public static List<Token> infixToPostfix(List<Token> exp) {
        // Converted list
        List<Token> output = new LinkedList<>();
        // Stack to temporarily hold operators
        Deque<Token> stack = new ArrayDeque<>();

        // Loop through each token in the given input
        exp.forEach(tok -> {
            // If number then add it to output list
            if (tok.isOperand())
                output.add(tok);
            // If ( then add it to operator stack
            else if (tok.token == TokenType.OPEN)
                stack.push(tok);
            // If )
            else if (tok.token == TokenType.CLOSE) {
                // Add everything on operator stack to output list until (
                while (stack.peek().token != TokenType.OPEN)
                    output.add(stack.pop());
                // Remove ) from operator stack
                stack.pop();
            }
            // If operator
            else {
                // Add the operators to the output list until get to operator with lower
                // precedence
                Token top = stack.peek();
                while (top != null && (top.token != TokenType.OPEN && top.priority >= tok.priority)) {
                    output.add(stack.pop());
                    top = stack.peek();
                }
                // Add the current operator to operator stack
                stack.push(tok);
            }
        });
        // Add the remaining operators to the output list
        stack.forEach(op -> {
            output.add(op);
        });

        return output;
    }

    // Given a postfix expression, evaluate it and return its value.
    // @param exp A list of tokens in postfix order
    // @return The result of evaluating the expression
    // @see token
    public static long evaluatePostfix(List<Token> exp) {
        // Stack to temporarily hold tokens
        Deque<Token> stack = new ArrayDeque<>();

        exp.forEach(tok -> {
            if (tok.token == TokenType.NUMBER)
                stack.push(tok);
            else {
                // Values to operate on
                long val2 = stack.pop().number;
                long val1 = stack.pop().number;

                stack.push(longToToken(performOperation(tok.token, val1, val2)));
            }
        });
        return stack.pop().number;
    }

    // Given an expression tree, evaluate it and return its value.
    // @param tree A valid Expression tree
    // @return The result of evaluating the tree
    // @see Expression
    public static long evaluateExpression(Expression tree) {
        // Base case, if leaf then return the value
        if (tree.left == null && tree.right == null)
            return tree.element.getValue();
        else {
            // Recursively evaluate the left and right subtrees
            long left = evaluateExpression(tree.left);
            long right = evaluateExpression(tree.right);

            return performOperation(tree.element.token, left, right);
        }
    }

    // Perform an operation given the operator and two long values
    // @param op The operator to be used in the evaluation
    // @param val1 The left side of the expression
    // @param val2 The right side of the expression
    // @return The result of evaluating val1 op val2
    // @see TokenType
    public static long performOperation(TokenType op, long val1, long val2) {
        // Perform the calculation given a certain operator
        switch (op) {
            case PLUS:
                return val1 + val2;
            case MINUS:
                return val1 - val2;
            case TIMES:
                return val1 * val2;
            case DIV:
                return val1 / val2;
            case MOD:
                return val1 % val2;
            case POWER:
                return (long) Math.pow(val1, val2);
            // Default case for if given OPEN CLOSE NIL NUMBER
            default:
                return 0;
        }
    }

    // Create a new NUMBER token given a long value
    // @param val A number to be turned into a token
    // @return A new token object
    // @see Token
    public static Token longToToken(long val) {
        return new Token(Long.toString(val));
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner in;

        // Read file passed on execution
        if (args.length > 0)
            in = new Scanner(new File(args[0]));
        // If no file passed, read user input
        else
            in = new Scanner(System.in);

        // Counter for # of expressions
        int count = 1;

        // Loop through every line in input
        while (in.hasNext()) {
            // Hold infix notation of input operation
            List<Token> infix = new LinkedList<>();

            // Split the string at every operator, keep the operator in the array
            // This instead of .split("") because numbers need to stay together
            // Ex: "11*(46-45)^241-11" -> [ 11, *, (, 46, -, 45, ), ^, 241, -, 11 ]
            String[] tokens = in.nextLine().split("(?<=[-+*/%()^])|(?=[-+*/%()^])");
            for (String tok : tokens)
                infix.add(getToken(tok));

            // If input has at least 3 tokens (a op b...)
            if (infix.size() >= 3) {
                System.out.println("Expression number: " + count);
                System.out.println("Infix expression: " + infix);

                // Convert infix to expression tree and postfix
                Expression exp = infixToExpression(infix);
                List<Token> post = infixToPostfix(infix);

                // Evaluate expression tree and postfix
                long eval = evaluateExpression(exp);
                long pval = evaluatePostfix(post);

                System.out.println("Postfix expression: " + post);
                System.out.println("Postfix eval: " + pval + " Exp eval: " + eval + "\n");

                // increment expression count
                count++;
            }
        }
    }
}
