package org.example;

import java.sql.*;
import java.util.*;

public class MathHelper {

    public void run(){
        Connection connection = null;

        try {
            connection = DatabaseConnector.getConnection();
            if (connection!= null) {
                System.out.println("Підключення до бази даних успішне");
            }
            else {
                System.out.println("Помилка підключення");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //create db table if they are not exist
        DataBaseFunction.createDBTable(connection);

        //main program body
        Scanner scanner = new Scanner(System.in);

        while (true){

            System.out.println("Введіть номер дії, яку ви хочете виконати: ");
            System.out.println("1 - Ввести рівняння, перевірити його коректність та запропонувати його можливий корінь.");
            System.out.println("2 - Знайти всі рівняння, що мають заданий корінь.");
            System.out.println("3 - Знайти всі рівняння, що мають один із зазначених коренів.");
            System.out.println("4 - Знайти всі рівняння, що мають рівно один корінь.");
            System.out.println("5 - Знайти всі рівняння, що не мають коренів.");
            System.out.println("0 - Для завершення програми");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> actionWithEquation(scanner,connection);
                case "2" -> {
                    System.out.println("Пошук рівнянь, що мають зазначений корінь:");
                    System.out.println("Введіть корінь");
                    if (scanner.hasNext()) {
                        String res = scanner.nextLine();

                        DataBaseFunction.findEquationByRoot(res, connection);
                    }
                }
                case "3" -> {
                    System.out.println("Пошук рівнянь, що мають один із зазначених коренів:");
                    System.out.println("Введіть корені через ПРОБІЛ");
                    if (scanner.hasNext()) {
                        String res = scanner.nextLine();

                        DataBaseFunction.findEquationByListRoot(res, connection);
                    }
                }
                case "4" -> {
                    System.out.println("Пошук рівнянь, що мають рівно один корінь:");
                    DataBaseFunction.findEquationWithOneRoot(connection);
                }
                case "5" -> {
                    System.out.println("Пошук рівнянь, що не мають збережених коренів:");
                    DataBaseFunction.findEquationWithoutRoot(connection);
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Введена дія некоректна");
            }
        }
    }

    private void actionWithEquation(Scanner scanner, Connection connection) {
        System.out.println("Введіть ваше рівняння: ");
        String equation = scanner.nextLine();

        //розбити рівняння на складові
        ArrayList<String> allTokens = createToken(equation);

        //перевірити коректність рівняння
        boolean correct = checkEquation(allTokens);

        long equationID;
        if (correct) {

            System.out.println("--Введене рівняння коректне!--");
            equationID = DataBaseFunction.insertToEquation(connection, equation);

            //перевірити введений корінь рівняння
            System.out.println("Введіть корінь рівняння і перевірте чи правильний він");
            while (scanner.hasNext()) {
                String res = scanner.nextLine();

                checkRoot(allTokens, res, equationID, connection);

                System.out.println("Хочете перевірити ще один корінь? Якщо так - натисніть 1, якщо ні - будь-який інший знак.");

                String finish = scanner.nextLine();
                if (!finish.equals("1")){
                    break;
                }
                else {
                    System.out.println("Введіть корінь рівняння і перевірте чи правильний він");
                }
            }
        }
        else {
            System.out.println("--Введене рівняння некоректне, перевіртне його!--");
        }
    }





    private boolean checkEquation( ArrayList<String> tokens) {
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

        for (int i = 0; i< input.length(); i++) {
            char ch = input.charAt(i);

            //check if last symbol not digit or not )
            if (i==input.length()-1 ){
                if (ch!=')' && !Character.isDigit(ch)) {
                    return new ArrayList<>();
                }
            }

            //if ch is digit write it to number and go to check next
            if (Character.isDigit(ch)) {
                number.append(ch);
            }
            //if ch==x then just add it to tokens list
            else if (ch=='x'){
                res.add(ch + "");
            }
            //check if ch=. then it should be part of number
            else if (ch == '.' && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.length()>0) {
                number.append(ch);
            }
            //check if ch is symbol of operation
            else if (ch == '+' || ch == '/' || ch == '*' || ch == '=') {
                //add number to tokens list
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }
                //if 2 symbols of operation is together than this equation is incorrect
                if(i>0 && (symbols.contains(input.charAt(i - 1)) || input.charAt(i-1)=='-')){
                    return new ArrayList<>();
                }
                res.add(String.valueOf(ch));
            }
            //check if ch== -
            else if (ch == '-') {

                //check - at the beginning
                if (i==0){
                    if(input.length()>1 && Character.isDigit(input.charAt(1))) {
                        number.append(ch);
                    }
                    else {
                        return new ArrayList<>();
                    }
                }
                //check if before some symbols of operation
                else if (i != 0 && symbols.contains(input.charAt(i - 1)) && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.toString().equals("")) {

                    number.append(ch);
                }
                else if (i != 0 && input.charAt(i - 1) =='(' && i < input.length() - 1 && Character.isDigit(input.charAt(i + 1)) && number.toString().equals("")) {

                    number.append(ch);
                }
                else  {
                    if (number.length() > 0) {
                        res.add(number.toString());
                        number = new StringBuilder();
                    }

                    if (i<input.length()-1 && input.charAt(i+1)=='-'){
                        return new ArrayList<>();
                    }
                    res.add(ch + "");
                }
            } else if (ch == '(') {
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }

                balanced.push(ch);
                res.add(ch + "");
            } else if (ch == ')') {
                if (number.length() > 0) {
                    res.add(number.toString());
                    number = new StringBuilder();
                }

                if (balanced.isEmpty()){
                    return new ArrayList<>();
                }
                else {
                    balanced.pop();
                }

                res.add(ch + "");

            } else {
                return new ArrayList<>();
            }
        }

        if(!balanced.isEmpty()){
            return new ArrayList<>();
        }

        if(number.length()>0){
            res.add(number.toString());
        }

        return res;
    }

    private void checkRoot(ArrayList<String> input, String res, long equationID, Connection connection) {

        System.out.println( "Розбили вхідний рядок на токени - " + input);

        List<String> left = input.subList(0,input.indexOf("="));
        List<String> right = input.subList(input.indexOf("=")+1,input.size());

        System.out.println(left);
        System.out.println(right);
        ArrayList<String> leftSide = makePoliz(left);
        ArrayList<String> rightSide = makePoliz(right);

        double leftRes = calculatePoliz(leftSide,res);
        System.out.println("Ліва частина - " + leftRes);
        double rightRes = calculatePoliz(rightSide,res);

        System.out.println("Права частина - " + rightRes);

        System.out.println(leftRes-rightRes);
        System.out.println(Math.pow(10,-9));
        if (Math.abs(leftRes-rightRes)<= Math.pow(10,-9)){
            System.out.println("Корінь правильний!!");
            DataBaseFunction.insertToRoot(connection, equationID,res );
        }

    }

    private double calculatePoliz(ArrayList<String> tokens, String x) {

        Stack<String> stack = new Stack<>();

        if (tokens.size()==1){
            return Double.parseDouble(tokens.get(0));
        }

        for (String t : tokens ){

            switch (t) {
                case "+" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) + Double.parseDouble(second);

                    System.out.println(first + " + " + second);
                    System.out.println("Res = " + res);
                    stack.push("" + res);
                }
                case "-" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = -Double.parseDouble(first) + Double.parseDouble(second);

                    System.out.println(second + " - " + first);
                    System.out.println("Res = " + res);
                    stack.push("" + res);
                }
                case "/" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(second) / Double.parseDouble(first);

                    System.out.println(second + " / " + first);
                    System.out.println("Res = " + res);
                    stack.push("" + res);
                }
                case "*" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) * Double.parseDouble(second);

                    System.out.println(first + " * " + second);
                    System.out.println("Res = " + res);
                    stack.push("" + res);
                }
                case "x" -> stack.push(x);
                default -> stack.push(t);
            }
        }

        return Double.parseDouble(stack.pop());
    }

    private ArrayList<String> makePoliz(List<String> input) {
        ArrayList<String> st = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        ArrayList<String> symbols = new ArrayList<>(Arrays.asList("+", "*", "/","-"));

        for (String t : input){

            if (t.equals("(")){
                stack.push(t);
                // System.out.println(t);
                // System.out.println("Рядок - " + st);
                // System.out.println("Стек - " + stack);
            }
            else if (t.equals(")")){
                System.out.println(t);

                while (!stack.peek().equals("(")){
                    st.add(stack.pop());
                }
                stack.pop();

                //System.out.println("Рядок - " + st);
                //System.out.println("Стек - " + stack);
            }
            else if (symbols.contains(t)){
                //System.out.println(t);

                if (stack.isEmpty()){
                    stack.push(t);
                }
                else if ( priority(t) > priority(stack.peek())){
                    stack.push(t);
                }
                else {
                    while (stack.size()>0 && priority(stack.peek())>=priority(t)){
                        st.add(stack.pop());
                    }
                    stack.push(t);
                }
                //System.out.println("Рядок - " + st);
                //System.out.println("Стек - " + stack);
            }
            else {
                //System.out.println(t);

                st.add(t);
            }


        }

        //System.out.println("Рядок - " + st);
        //System.out.println("Стек - " + stack);

        while (stack.size()>0){
            st.add(stack.pop());
        }

        System.out.println("Поліз - " + st);
        return st;
    }

    private int priority(String op){
        if(op.equals("-") || op.equals("+")){
            return 1;
        }
        else if(op.equals("*") || op.equals("/")){
            return 2;
        }

        return 0;
    }
}
