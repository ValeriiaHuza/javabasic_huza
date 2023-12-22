package org.example;

import java.sql.*;

public class DataBaseFunction {

    public static void createDBTable(Connection connection) {
        try {
            Statement equation = connection.createStatement();
            String createEquationQuery = "CREATE TABLE IF NOT EXISTS equation ("
                    + "id SERIAL PRIMARY KEY,"
                    + "body VARCHAR(255) )";

            equation.executeUpdate(createEquationQuery);

            Statement root = connection.createStatement();
            String createRootQuery = "CREATE TABLE IF NOT EXISTS root ("
                    + "id SERIAL PRIMARY KEY,"
                    + "id_equation INTEGER,"
                    + "root_t VARCHAR(255) )";

            root.executeUpdate(createRootQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void findEquationByListRoot(String res, Connection connection) {
        String[] splitArray = res.split(" ");
        StringBuilder array = new StringBuilder("(");

        for (int i = 0; i < splitArray.length; i++) {
            try {
                Double.parseDouble(splitArray[i]);
                if (i == splitArray.length - 1) {
                    array.append("?);");
                } else {
                    array.append("?, ");
                }
            } catch (Exception e) {
                return;
            }
        }
        System.out.println(array);

        try {

            String findEquation = "SELECT DISTINCT body " +
                    "FROM equation " +
                    "JOIN root ON equation.id = root.id_equation " +
                    "WHERE root.root_t IN " + array;

            PreparedStatement preparedStatement = connection.prepareStatement(findEquation);

            for (int i = 1; i <= splitArray.length; i++) {
                preparedStatement.setString(i, splitArray[i - 1]);
            }
            ResultSet result = preparedStatement.executeQuery();

            int count = 0;
            while (result.next()) {
                String equation = result.getString("body");
                System.out.println("Рівняння - " + equation);
                count++;
            }
            if (count == 0) {
                System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void findEquationWithoutRoot(Connection connection) {
        try {
            String findEquation = "SELECT body " +
                    "FROM equation " +
                    "LEFT JOIN root ON equation.id = root.id_equation " +
                    "WHERE root.id_equation IS NULL;";

            PreparedStatement preparedStatement = connection.prepareStatement(findEquation);
            ResultSet result = preparedStatement.executeQuery();

            int count = 0;
            while (result.next()) {
                String equation = result.getString("body");
                System.out.println("Рівняння - " + equation);
                count++;
            }
            if (count == 0) {
                System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void findEquationWithOneRoot(Connection connection) {

        try {

            String findEquation = "SELECT body " +
                    "FROM equation " +
                    "JOIN root ON equation.id = root.id_equation " +
                    "GROUP BY equation.id " +
                    "HAVING COUNT(root.root_t)=1;";

            PreparedStatement preparedStatement = connection.prepareStatement(findEquation);
            ResultSet result = preparedStatement.executeQuery();

            int count = 0;
            while (result.next()) {
                String equation = result.getString("body");
                System.out.println("Рівняння - " + equation);
                count++;
            }

            if (count == 0) {
                System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void findEquationByRoot(String res, Connection connection) {
        try {
            String findEquation = "SELECT body " +
                    "FROM equation " +
                    "JOIN root ON equation.id = root.id_equation " +
                    "WHERE root.root_t = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(findEquation);
            preparedStatement.setString(1, res);

            ResultSet result = preparedStatement.executeQuery();
            int count = 0;
            while (result.next()) {
                String equation = result.getString("body");
                System.out.println("Рівняння - " + equation);
                count++;
            }
            if (count == 0) {
                System.out.println("Рівнянь, що відповідають параметрам у базі даних немає.");
                System.out.println();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static long insertToEquation(Connection connection, String input) {

        try {

            String checkIfExists = "SELECT id FROM equation WHERE body = ?";

            String insert = "INSERT INTO equation (body) VALUES (?) RETURNING id";

            PreparedStatement check = connection.prepareStatement(checkIfExists);
            PreparedStatement insertToDB = connection.prepareStatement(insert);

            check.setString(1, input);

            ResultSet result = check.executeQuery();

            if (result.next()) {
                System.out.println("--Таке рівняння вже є в базі даних.--");
                return result.getLong("id");
            } else {
                insertToDB.setString(1, input);

                ResultSet insertResult = insertToDB.executeQuery();

                if (insertResult.next()) {

                    long id = insertResult.getLong("id");
                    System.out.println("--Рівняння успішно додано до бази даних!--");
                    return id;
                } else {
                    System.out.println("--Виникла помилка при додаванні рівняння в базу даних.--");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void insertToRoot(Connection connection, long equationID, String res) {

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

            if (rowCount == 0) {
                insertToDB.setLong(1, equationID);
                insertToDB.setString(2, res);

                int affected = insertToDB.executeUpdate();

                if (affected > 0) {

                    System.out.println("--Корінь рівняння успішно додано до бази даних!--");

                } else {
                    System.out.println("--Виникла помилка при додаванні кореня до бази даних.--");
                }
            } else {
                System.out.println("--Такий корінь вже є в базі даних.--");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
