import java.util.*;

/**
 * This class implements an LR1 parser that checks a 'program' given as a command line argument for syntax errors based on its defined grammar.
 * While the expression is valid, the program evaluates it, printing the state of the stack on every step of parsing.
 * A wholly valid expression will have its value displayed at the end. Otherwise, "Invalid Expression" will print.
 *
 * @author Abe Ramseyer
 */
public class LR1 {
    /**
     * tracks whether the parsers has reached an accepted state
     */
    static boolean accepted = false;

    /**
     * holds the stack that contains information about the current state, value, and symbol
     */
    static Stack<ParseToken> stack = new Stack<>();

    /**
     * holds the input code
     */
    static Queue<String> code = new LinkedList<>();

    /**
     * used for ensuring a number in the code doesn't start with '0'
     */
    static final String validNumber = "([1-9]\\d*|0)";


    public static void main(String[] args) {
        // basic error checking
        if (args.length != 1 || args[0].equals("")) {
            System.err.println("Usage: java LR1 <code>");
            System.exit(1);
        }
        // create a string tokenizer and add all its tokens to the queue
        StringTokenizer tokenizer = new StringTokenizer(args[0].endsWith("$") ? args[0] : args[0]+"$", "+-*/()$", true);
        while(tokenizer.hasMoreTokens())
            code.offer(tokenizer.nextToken());

        // manually push the starting state to the stack
        push(new ParseToken("", 0));

        while(!accepted) {
            // determine which state we are in and jump to the method handling it
            switch(peekStack().state) {
                // all 4 of these states have the same behavior in the parsing table, so they have been combined
                case 0:
                case 4:
                case 6:
                case 7:
                    zero();
                    break;
                case 1:
                    one();
                    break;
                case 2:
                    two();
                    break;
                case 3:
                    three();
                    break;
                case 5:
                    five();
                    break;
                case 8:
                    eight();
                    break;
                case 9:
                    nine();
                    break;
                case 10:
                    ten();
                    break;
                case 11:
                    eleven();
                    break;
            }
        }
        System.out.println("Valid Expression, value = " + pop().value);
    }

    /**
     * state zero
     */
    static void zero() {
        switch(peekForSymbol()) {
            case "n":
                push(new ParseToken(code.peek(), 5, code.poll()));
                break;
            case "(":
                push(new ParseToken(code.poll(), 4));
                break;
            default: no();
        }
    }

    /**
     * state one
     */
    static void one() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
                push(new ParseToken(code.poll(), 6));
                break;
            case "$":
                accepted = true;
                break;
            default: no();
        }
    }

    /**
     * state two
     */
    static void two() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
                reduce("E", "T");
                break;
            case "*":
            case "/":
                push(new ParseToken(code.poll(), 7));
                break;
            case ")":
            case "$":
                reduce("E", "T");
                break;
            default: no();
        }
    }

    /**
     * state three
     */
    static void three() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
            case "*":
            case "/":
            case ")":
            case "$":
                reduce("T", "F");
                break;
            default: no();
        }
    }

    /**
     * state five
     */
    static void five() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
            case "*":
            case "/":
            case ")":
            case "$":
                reduce("F", "n");
                break;
            default: no();
        }
    }

    /**
     * state eight
     */
    static void eight() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
                push(new ParseToken(code.poll(), 6));
                break;
            case ")":
                push(new ParseToken(code.poll(), 11));
                break;
            default: no();
        }
    }

    /**
     * state nine
     */
    static void nine() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
            case ")":
            case "$":
                reduce("E", "E+-T"); // other case handled in reduce method
                break;
            case "*":
            case "/":
                push(new ParseToken(code.poll(), 7));
                break;
            default: no();
        }
    }

    /**
     * state ten
     */
    static void ten() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
            case "*":
            case "/":
            case ")":
            case "$":
                reduce("T", "T*/F");
                break;
            default: no();
        }
    }

    /**
     * state eleven
     */
    static void eleven() {
        switch (peekForSymbol()) {
            case "+":
            case "-":
            case "*":
            case "/":
            case ")":
            case "$":
                reduce("F", "(E)");
                break;
            default: no();
        }
    }

    /**
     * reduces the stack based on predefined rules of form 'T -> T*F'
     * @param generated what is generated from the reduction: 'T' in the above example
     * @param from  what is removed from the stack from the reduction: 'T*F' in the above example
     */
    static void reduce(String generated, String from) {
        // the value that will hold the result of any expression computation
        double newValue = 0;
        // variables for holding the objects that are popped of the stack
        ParseToken t, e, f, n;
        // the operator that is found in the stack, e.g. '+' or '-'
        String op;
        switch (from) {
            // 'E+T' and 'E-T' follow the same reduction pattern, so they were combined into this one rule
            case "E+-T":
                t = checkedPop("T");
                op = checkedPop("+-").symbol;
                e = checkedPop("E");
                if (op.equals("+"))
                    newValue = e.value + t.value;
                else if (op.equals("-"))
                    newValue = e.value - t.value;
                else
                    no();
                break;
            // same as above for 'T*F' and 'T/F'
            case "T*/F":
                f = checkedPop("F");
                op = checkedPop("*/").symbol;
                t = checkedPop("T");
                if (op.equals("*"))
                    newValue = t.value * f.value;
                else if (op.equals("/"))
                    newValue = t.value / f.value;
                else
                    no();
                break;
            case "F":
                newValue = checkedPop("F").value;
                break;
            case "n":
                n = pop();
                if (n.symbol.matches(validNumber))
                    newValue = n.value;
                else
                    no();
                break;
            case "T":
                newValue = checkedPop("T").value;
                break;
            case "(E)":
                checkedPop(")");
                newValue = checkedPop("E").value;
                checkedPop("(");
                break;
            default: no();
        }
        // the state that we will be going to when the reduction is complete
        int newState = -1;
        // check the current state of the stack and set the next state according to the parsing table
        switch(peekStack().state) {
            case 0:
                newState = "E".equals(generated) ? 1 :
                        "T".equals(generated) ? 2 :
                                "F".equals(generated) ? 3 : -1;
                break;
            case 4:
                newState = "E".equals(generated) ? 8 :
                        "T".equals(generated) ? 2 :
                                "F".equals(generated) ? 3 : -1;
                break;
            case 6:
                newState = "T".equals(generated) ? 9 :
                        "F".equals(generated) ? 3 : -1;
                break;
            case 7:
                newState = "F".equals(generated) ? 10 : -1;
                break;
        }
        // no entry existed in the parsing table for the state seen
        if (newState == -1)
           no();
        // push the new reduced token into the stack
        push(new ParseToken(generated, newState, newValue));
    }

    /**
     * automagically prints the token stack and code queue without removing anything from them
     */
    static void print() {
        stack.stream()
                .forEach(System.out::print);
        System.out.print("\t\t");
        code.stream()
                .map(t -> t + " ")
                .forEach(System.out::print); // I'm coming for you, ML!
        System.out.println();
    }

    /**
     * translates an item in the queue to a number if necessary
     * @return the item
     */
    static String peekForSymbol() {
        String token = code.peek();
        if (token.matches(validNumber))
            token = "n";

        return token;
    }

    /**
     * wrapper for pushing an item onto the code stack and printing it out
     * @param parseToken the token to push onto the stack
     */
    static void push(ParseToken parseToken) {
        stack.push(parseToken);
        print();
    }

    /**
     * Pops a token from the stack. If the token doesnt match one of the expected symbol(s), quit
     * @param expected a string of symbols that are valid results
     */
    static ParseToken checkedPop(String expected) {
        ParseToken t = pop();
        for (char c : expected.toCharArray())
            if (t.symbol.indexOf(c) != -1)
                return t;
        no();
        return null;
    }

    /**
     * wrapper for popping an item from the code stack, catching errors, and printing it out
     * @return  the token popped from the stack
     */
    static ParseToken pop() {
        peekStack(); // error handling will be taken care of here if the stack is empty
        ParseToken toReturn = stack.pop();
        print();
        return toReturn;
    }

    /**
     * Wrapper for peeking at the stack to catch errors
     * @return  the item from the top of the stack
     */
    static ParseToken peekStack() {
        ParseToken toReturn = null;
        try {
            toReturn = stack.peek();
        } catch (EmptyStackException e) {
            no();
        }
        return toReturn;
    }


    /**
     * called when there's an error. Stops the program
     */
    static void no() {
        System.out.println("Invalid Expression");
        System.exit(0);
    }

    /**
     * class that defines a single item in the stack
     */
    static class ParseToken {
        /**
         * an item from the code queue or a letter from reduction
         */
        String symbol;

        /**
         * the state that the stack is in at this token
         */
        int state;

        /**
         * the value of the stack at this token
         */
        double value;

        ParseToken(String symbol, int state) {
            this.symbol = symbol;
            this.state = state;
        }
        ParseToken(String symbol, int state, double value) {
            this(symbol, state);
            this.value = value;
        }
        ParseToken(String symbol, int state, String value) {
            this(symbol, state);
            this.value = Double.parseDouble(value);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "[" + (symbol.equals("") ? "-" : symbol) + ":" + this.state + "]";
        }
    }
}
