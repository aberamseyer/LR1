import java.util.*;

/**
 * This class implements an LR1 parser that checks a 'program' given as a command line argument for syntax errors based on its defined grammar.
 * If the expression is valid, the program evaluates it, printing the state of the stack on every step of parsing. Otherwise, it print
 * Prints "Yes" if syntax follows the given ruleset, otherwise prints "No"
 *
 * @author Abe Ramseyer
 */
public class LR1 {
    static boolean accepted = false;
    static final String operators = "+-*/()$";
    static Stack<struct> stack = new Stack<>();
    static Queue<String> code = new LinkedList<>();

    public static void main(String[] args) {
        if (args.length != 1 || args[0].equals("")) {
            System.err.println("Usage: java LR1 <code>");
            System.exit(1);
        }
        StringTokenizer tokenizer = new StringTokenizer(args[0]+"$", operators, true);
        while(tokenizer.hasMoreTokens())
            code.offer(tokenizer.nextToken());

        stack.push(new struct("", 0, 0));
        try {
            while(!accepted) {
                switch(state()) {
                    case 0:
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
                    case 4:
                        four();
                        break;
                    case 5:
                        five();
                        break;
                    case 6:
                        six();
                        break;
                    case 7:
                        seven();
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
                print();
            }
        } catch (EmptyStackException e) {
            no();
        }
        System.out.println("Valid Expression, value = " + stack.pop().value);
    }

    static void zero() {
        switch(peekAsSymbol()) {
            case "n":
                stack.push(new struct(code.peek(), 5, code.poll()));
                break;
            case "(":
                stack.push(new struct(code.poll(), 4, 0));
                break;
            default: no();
        }
    }
    static void one() {
        switch (peekAsSymbol()) {
            case "+":
            case "-":
                stack.push(new struct(code.poll(), 6, 0));
                break;
            case "$":
                accepted = true;
                break;
            default: no();
        }
    }
    static void two() {
        switch (peekAsSymbol()) {
            case "+":
            case "-":
                reduce("E", "T");
                break;
            case "*":
            case "/":
                stack.push(new struct(code.poll(), 7, 0));
                break;
            case ")":
            case "$":
                reduce("E", "T");
                break;
            default: no();
        }
    }
    static void three() {
        switch (peekAsSymbol()) {
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
    static void four() {
        zero();
    }
    static void five() {
        switch (peekAsSymbol()) {
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
    static void six() {
        four();	// they're the same
    }
    static void seven() {
        six(); // stoooop
    }
    static void eight() {
        switch (peekAsSymbol()) {
            case "+":
            case "-":
                stack.push(new struct(code.poll(), 6, 0));
                break;
            case ")":
                stack.push(new struct(code.poll(), 11, 0));
                break;
            default: no();
        }
    }
    static void nine() {
        switch (peekAsSymbol()) {
            case "+":
            case "-":
            case ")":
            case "$":
                reduce("E", "E+-T"); // other case handled in reduce method
                break;
            case "*":
            case "/":
                stack.push(new struct(code.poll(), 7, 0));
                break;
            default: no();
        }
    }
    static void ten() {
        switch (peekAsSymbol()) {
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
    static void eleven() {
        switch (peekAsSymbol()) {
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

    static void reduce(String generated, String from) {
        double newValue = 0;
        struct t, e, f;
        String op;
        switch (from) {
            // case "E-T":
            case "E+-T":
                t = stack.pop();
                op = stack.pop().symbol;
                e = stack.pop();
                if (op.equals("+"))
                    newValue = e.value + t.value;
                else if (op.equals("-"))
                    newValue = e.value - t.value;
                break;
            case "T*/F":
                f = stack.pop();
                op = stack.pop().symbol;
                t = stack.pop();
                if (op.equals("*"))
                    newValue = t.value * f.value;
                else if (op.equals("/"))
                    newValue = t.value / f.value;
                break;
            case "F":
            case "n":
            case "T":
                newValue = stack.pop().value;
                break;
            case "(E)":
                stack.pop();
                newValue = stack.pop().value;
                stack.pop();
                break;
        }
        int newState = -1;
        switch(stack.peek().state) {
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
        if (newState == -1)
            no();
        stack.push(new struct(generated, newState, newValue));
    }

    static int state() {
        return stack.peek().state;
    }


    static void print() {
        stack.stream().forEach(System.out::print);
        System.out.print("\t\t");
        code.stream().map(t -> t + " ").forEach(System.out::print);
        System.out.println();
    }

    static String peekAsSymbol() {
        String token = code.peek();
        if (token.matches("[1-9]\\d*"))
            token = "n";

        return token;
    }

    static void no() {
        System.out.println("Invalid Expression");
        System.exit(0);
    }

    static class struct {
        String symbol;
        int state;
        double value;

        struct(String symbol, int state, double value) {
            this.symbol = symbol;
            this.state = state;
            this.value = value;
        }
        struct(String symbol, int state, String value) {
            this.symbol = symbol;
            this.state = state;
            this.value = Double.parseDouble(value);
        }

        public String toString() {
            return "[" + (symbol.equals("") ? "-" : symbol) + ":" + this.state + "]";
        }
    }
}