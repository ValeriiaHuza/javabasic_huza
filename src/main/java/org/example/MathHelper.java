package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class MathHelper {

    public void run() {
        Connection connection = null;

        try {
            connection = DatabaseConnector.getConnection();
            if (connection != null) {
                System.out.println("--Підключення до бази даних успішне--");
            } else {
                System.out.println("--Помилка підключення--");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //create db table if they are not exist
        DataBaseFunction.createDBTable(connection);

        //main program body
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("Введіть номер дії, яку ви хочете виконати: ");
            System.out.println("1 - Ввести рівняння, перевірити його коректність та запропонувати його можливий корінь.");
            System.out.println("2 - Знайти всі рівняння, що мають зазначений корінь.");
            System.out.println("3 - Знайти всі рівняння, що мають один із зазначених коренів.");
            System.out.println("4 - Знайти всі рівняння, що мають рівно один корінь.");
            System.out.println("5 - Знайти всі рівняння, що не мають коренів.");
            System.out.println("0 - Для завершення програми");

            String input = scanner.nextLine();

            switch (input) {
                case "1": {
                    actionWithEquation(scanner, connection);
                    break;
                }
                case "2": {
                    System.out.println("Пошук рівнянь, що мають зазначений корінь:");
                    System.out.println("Введіть корінь");
                    if (scanner.hasNext()) {
                        String inputRoot = scanner.nextLine();
                        ArrayList<String> result = DataBaseFunction.findEquationByRoot(inputRoot, connection);

                        if (result.size() == 0) {
                            System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                            System.out.println();
                        } else {
                            System.out.println("Рівняння - " + result);
                        }
                    }
                    break;
                }
                case "3": {
                    System.out.println("Пошук рівнянь, що мають один із зазначених коренів:");
                    System.out.println("Введіть корені через ПРОБІЛ");
                    if (scanner.hasNext()) {
                        String inputRoot = scanner.nextLine();
                        ArrayList<String> result = DataBaseFunction.findEquationByListRoot(inputRoot, connection);

                        if (result.size() == 0) {
                            System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                            System.out.println();
                        } else {
                            System.out.println("Рівняння - " + result);
                        }
                    }
                    break;
                }
                case "4": {
                    System.out.println("Пошук рівнянь, що мають рівно один корінь:");
                    ArrayList<String> res = DataBaseFunction.findEquationWithOneRoot(connection);

                    if (res.size() == 0) {
                        System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                        System.out.println();
                    } else {
                        System.out.println("Рівняння - " + res);
                    }
                    break;
                }
                case "5": {
                    System.out.println("Пошук рівнянь, що не мають збережених коренів:");
                    ArrayList<String> res = DataBaseFunction.findEquationWithoutRoot(connection);

                    if (res.size() == 0) {
                        System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                        System.out.println();
                    } else {
                        System.out.println("Рівняння - " + res);
                    }
                    break;
                }
                case "0": {
                    return;
                }
                default: {
                    System.out.println("--Введена дія некоректна--");
                    break;
                }
            }
        }
    }

    private void actionWithEquation(Scanner scanner, Connection connection) {
        System.out.println("Введіть ваше рівняння: ");
        String equation = scanner.nextLine();

        //перевірити коректність рівняння
        boolean correct = checkEquation(equation);

        long equationID;
        if (correct) {

            System.out.println("--Введене рівняння коректне!--");
            equationID = DataBaseFunction.insertToEquation(connection, equation);

            //перевірити введений корінь рівняння
            System.out.println("Введіть корінь рівняння і перевірте чи правильний він");
            while (scanner.hasNext()) {
                String res = scanner.nextLine();

                boolean correctRoot = checkRoot(equation, res);

                if (correctRoot) {
                    DataBaseFunction.insertToRoot(connection, equationID, res);
                }

                System.out.println("Хочете перевірити ще один корінь? Якщо так - натисніть 1, якщо ні - будь-який інший знак.");

                String finish = scanner.nextLine();
                if (!finish.equals("1")) {
                    break;
                } else {
                    System.out.println("Введіть корінь рівняння і перевірте чи правильний він");
                }
            }
        } else {
            System.out.println("--Введене рівняння некоректне, перевіртне його!--");
        }
    }


    public boolean checkEquation(String equation) {

        //розбити рівняння на складові
        ArrayList<String> tokens = createToken(equation);

        return tokens.size() != 0 && tokens.contains("=") && tokens.contains("x");
    }


    private ArrayList<String> createToken(String input) {

        //result
        ArrayList<String> res = new ArrayList<>();

        //symbols for easier check
        ArrayList<Character> symbols = new ArrayList<>(Arrays.asList('+', '=', '*', '/'));

        //Stack for checking if brackets are balanced
        Stack<Character> balanced = new Stack<>();

        StringBuilder number = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);

            //check if last symbol not digit or not )
            if (i == input.length() - 1) {
                if (character != ')' && !Character.isDigit(character)) {
                    return new ArrayList<>();
                }
            }

            //if character is digit write it to number and go to check next
            if (Character.isDigit(character)) {
                number.append(character);
            }
            //if ch==x then just add it to tokens list
            else if (character == 'x') {
                res.add(character + "");
            }
            //check if ch=. then it should be part of number
            else if (character == '.' && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.length() > 0) {
                number.append(character);
            }
            //check if character is symbol of operation
            else if (character == '+' || character == '/' || character == '*' || character == '=') {
                //add number to tokens list
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }
                //if 2 symbols of operation is together than this equation is incorrect
                if (i > 0 && (symbols.contains(input.charAt(i - 1)) || input.charAt(i - 1) == '-')) {
                    return new ArrayList<>();
                }
                res.add(String.valueOf(character));
            }
            //check if ch== -
            else if (character == '-') {

                //check - at the beginning
                if (i == 0) {
                    if (input.length() > 1 && Character.isDigit(input.charAt(1))) {
                        number.append(character);
                    } else {
                        return new ArrayList<>();
                    }
                }
                //check if before some symbols of operation
                else if (i != 0 && symbols.contains(input.charAt(i - 1)) && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.toString().equals("")) {

                    number.append(character);
                } else if (i != 0 && input.charAt(i - 1) == '(' && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.toString().equals("")) {

                    number.append(character);
                } else {
                    if (number.length() > 0) {
                        res.add(number.toString());
                        number = new StringBuilder();
                    }

                    if (i < input.length() - 1 && input.charAt(i + 1) == '-') {
                        return new ArrayList<>();
                    }
                    res.add(character + "");
                }
            } else if (character == '(') {
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }

                balanced.push(character);
                res.add(character + "");
            } else if (character == ')') {
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }

                if (balanced.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    balanced.pop();
                }

                res.add(character + "");

            } else {
                return new ArrayList<>();
            }
        }

        if (!balanced.isEmpty()) {
            return new ArrayList<>();
        }

        if (number.length() > 0) {
            res.add(number.toString());
        }

        return res;
    }

    public boolean checkRoot(String equation, String res) {

        ArrayList<String> input = createToken(equation);

        List<String> left = input.subList(0, input.indexOf("="));
        List<String> right = input.subList(input.indexOf("=") + 1, input.size());

        ArrayList<String> leftSide = transformToRPNExpression(left);
        ArrayList<String> rightSide = transformToRPNExpression(right);

        double leftRes = calculateWithRPN(leftSide, res);
        double rightRes = calculateWithRPN(rightSide, res);

        if (Math.abs(leftRes - rightRes) <= Math.pow(10, -9)) {
            System.out.println("--Корінь правильний!!--");
            return true;
        }

        System.out.println("--Введений корінь неправильний!--");
        return false;
    }

    private double calculateWithRPN(ArrayList<String> tokens, String x) {

        Stack<String> stack = new Stack<>();

        if (tokens.size() == 1) {
            if (tokens.get(0).equals("x")) {
                tokens.set(0, x);
            }

            return Double.parseDouble(tokens.get(0));
        }

        for (String t : tokens) {
            switch (t) {
                case "+": {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) + Double.parseDouble(second);
                    stack.push("" + res);
                    break;
                }
                case "-": {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = -Double.parseDouble(first) + Double.parseDouble(second);
                    stack.push("" + res);
                    break;
                }
                case "/": {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(second) / Double.parseDouble(first);
                    stack.push("" + res);
                    break;
                }
                case "*": {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) * Double.parseDouble(second);
                    stack.push("" + res);
                    break;
                }
                case "x": {
                    stack.push(x);
                    break;
                }
                default: {
                    stack.push(t);
                    break;
                }
            }
        }

        return Double.parseDouble(stack.pop());
    }

    //make poliz
    private ArrayList<String> transformToRPNExpression(List<String> input) {
        ArrayList<String> resultList = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        ArrayList<String> mathOperations = new ArrayList<>(Arrays.asList("+", "*", "/", "-"));

        for (String token : input) {

            if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                System.out.println(token);

                while (!stack.peek().equals("(")) {
                    resultList.add(stack.pop());
                }
                stack.pop();
            } else if (mathOperations.contains(token)) {
                if (stack.isEmpty()) {
                    stack.push(token);
                } else if (getPriority(token) > getPriority(stack.peek())) {
                    stack.push(token);
                } else {
                    while (stack.size() > 0 && getPriority(stack.peek()) >= getPriority(token)) {
                        resultList.add(stack.pop());
                    }
                    stack.push(token);
                }
            } else {
                resultList.add(token);
            }


        }

        while (stack.size() > 0) {
            resultList.add(stack.pop());
        }

        return resultList;
    }

    private int getPriority(String op) {
        if (op.equals("-") || op.equals("+")) {
            return 1;
        } else if (op.equals("*") || op.equals("/")) {
            return 2;
        }

        return 0;
    }
}
