package org.example;

import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Connection connection = null;

        try {
            connection = DatabaseConnector.getConnection();
            if (connection!= null) {
                System.out.println("Connected to db successfully");
            }
            else {
                System.out.println("Connection Faild.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement equation = connection.createStatement();

            String createEquationQuery = "CREATE TABLE IF NOT EXISTS equation ("
                    + "id SERIAL PRIMARY KEY,"
                    + "body VARCHAR(255) )";

            equation.executeUpdate(createEquationQuery);
            System.out.println("Table Equation created successfully.");

            Statement root = connection.createStatement();

            String createRootQuery = "CREATE TABLE IF NOT EXISTS root ("
                    + "id SERIAL PRIMARY KEY,"
                    + "id_equation INTEGER,"
                    + "root_t VARCHAR(255) )";

            root.executeUpdate(createRootQuery);
            System.out.println("Table Root created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введіть ваше рівняння: ");
        while (scanner.hasNext()){
            String input = scanner.nextLine();

            //розбити рівняння на складові
            ArrayList<String> allTokens = createToken(input);

            //перевірити коректність рівняння
            boolean correct = checkEquation(allTokens);

            long equationID = -1;
            if (correct) {
                System.out.println("Введене рівняння коректне!");
                equationID = insertToEquation(connection, input);

                //перевірити введений корінь рівняння

                System.out.println("Введіть корінь рівняння і перевірте чи правильний він");
                if (scanner.hasNext()) {
                    String res = scanner.nextLine();

                    checkRoot(allTokens, res, equationID, connection);
                }
            }

            System.out.println("Пошук рівнянь, що мають один із зазначених коренів:");
            System.out.println("Введіть корінь");
            if (scanner.hasNext()) {
                String res = scanner.nextLine();

                findEquationByRoot(res, connection);
            }


        }

    }

    private static void findEquationByRoot(String res, Connection connection) {

        try {

            String findEquation = "SELECT body " +
                                    "FROM equation " +
                                    "JOIN root ON equation.id = root.id_equation " +
                                    "WHERE root.root_t = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(findEquation);

            preparedStatement.setString(1, res);

            ResultSet result = preparedStatement.executeQuery();

            while ( result.next()){
                String equation = result.getString("body");
                System.out.println("Equation - " + equation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static long insertToEquation(Connection connection, String input) {

        try {

            String checkIfExists = "SELECT id FROM equation WHERE body = ?";

            String insert = "INSERT INTO equation (body) VALUES (?) RETURNING id";

            PreparedStatement check = connection.prepareStatement(checkIfExists);
            PreparedStatement insertToDB = connection.prepareStatement(insert);

            check.setString(1, input);

            ResultSet result = check.executeQuery();

            if(result.next()){
                System.out.println("Таке рівняння вже є в базі даних");
                return result.getLong("id");
            }
            else {
                insertToDB.setString(1, input);

                ResultSet insertResult = insertToDB.executeQuery();

                if (insertResult.next()){

                    long id = insertResult.getLong("id");
                    System.out.println("Рівняння успішно додалось!");
                    return id;
                } else {
                    System.out.println("Виникла помилка");
                }
            }

        } catch (SQLException e) {
        e.printStackTrace();
    }

        return -1;
    }

    private static boolean checkEquation( ArrayList<String> tokens) {
        return tokens.size() != 0 && tokens.contains("=") && tokens.contains("x");
    }


    private static ArrayList<String> createToken(String input) {

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

    private static void checkRoot(ArrayList<String> input, String res, long equationID, Connection connection) {

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

        if (Math.abs(leftRes-rightRes)<= Math.pow(10,-9)){
            System.out.println("Корінь правильний!!");
            insertToRoot(connection, equationID,res );
        }

    }

    private static void insertToRoot(Connection connection, long equationID, String res) {

        try {

            String checkIfExists = "SELECT COUNT(*) FROM root WHERE id_equation = ? AND root_t = ?";

            String insert = "INSERT INTO root (id_equation, root_t) VALUES (?,?)";

            PreparedStatement check = connection.prepareStatement(checkIfExists);
            PreparedStatement insertToDB = connection.prepareStatement(insert);

            check.setLong(1, equationID);
            check.setString(2, res);

            ResultSet result = check.executeQuery();
            result.next();

            int rowCount = result.getInt(1);

            if (rowCount==0) {
                insertToDB.setLong(1, equationID);
                insertToDB.setString(2,res);

                int affected  = insertToDB.executeUpdate();

                if (affected>0){

                    System.out.println("Корінь рівняння успішно додався!");

                } else {
                    System.out.println("Виникла помилка");
                }
            }
            else {
                System.out.println("Такий корінь вже є в базі даних");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static double calculatePoliz(ArrayList<String> tokens, String x) {

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
                    stack.push("" + res);
                }
                case "-" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) - Double.parseDouble(second);
                    stack.push("" + res);
                }
                case "/" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) / Double.parseDouble(second);
                    stack.push("" + res);
                }
                case "*" -> {
                    String first = stack.pop();
                    String second = stack.pop();

                    double res = Double.parseDouble(first) * Double.parseDouble(second);
                    stack.push("" + res);
                }
                case "x" -> stack.push(x);
                default -> stack.push(t);
            }
        }

        return Double.parseDouble(stack.pop());
    }

    private static ArrayList<String> makePoliz(List<String> input) {
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

    private static int priority(String op){
        if(op.equals("-") || op.equals("+")){
            return 1;
        }
        else if(op.equals("*") || op.equals("/")){
            return 2;
        }

        return 0;
    }
}