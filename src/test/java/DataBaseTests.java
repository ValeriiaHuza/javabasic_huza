import org.example.DataBaseFunction;
import org.example.DatabaseConnector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataBaseTests {

    static Connection connection = null;

    @BeforeAll
    public static void configureBeforeTests() {
        try {
            connection = DatabaseConnector.getConnection();
            if (connection != null) {
                System.out.println("--Підключення до бази даних успішне--");
            } else {
                System.out.println("--Помилка підключення--");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //create db table if they are not exist
        DataBaseFunction.createDBTable(connection);
    }

    @Test
    public void testAddEquationToDB() {
        String equation = "x/2=3";
        DataBaseFunction.insertToEquation(connection, equation);
        assertTrue(DataBaseFunction.ifEquationExists(connection, equation));
    }

    @Test
    public void testAddRootToDB() {
        String equation = "x/2=3";
        long equationId = DataBaseFunction.insertToEquation(connection, equation);
        String root = "6";
        DataBaseFunction.insertToRoot(connection, equationId, root);

        assertTrue(DataBaseFunction.ifRootExists(connection, equationId, root));
    }

    @Test
    public void testFindEquationByListRoot() {
        String equation1 = "x/2=3";
        long equationId1 = DataBaseFunction.insertToEquation(connection, equation1);
        String root1 = "6";
        DataBaseFunction.insertToRoot(connection, equationId1, root1);

        String equation2 = "3*x=12";
        long equationId2 = DataBaseFunction.insertToEquation(connection, equation2);
        String root2 = "4";
        DataBaseFunction.insertToRoot(connection, equationId2, root2);

        String input = "6 4";
        ArrayList<String> res = DataBaseFunction.findEquationByListRoot(input, connection);

        assertTrue(res.contains(equation1));
        assertTrue(res.contains(equation2));
    }

    @Test
    public void testFindEquationWithoutRoot() {
        String equation1 = "x*x+4=0";
        DataBaseFunction.insertToEquation(connection, equation1);
        ArrayList<String> res = DataBaseFunction.findEquationWithoutRoot(connection);
        assertTrue(res.contains(equation1));
    }

    @Test
    public void testFindEquationWithOneRoot() {
        String equation1 = "x/2=3";
        long equationId1 = DataBaseFunction.insertToEquation(connection, equation1);
        String root1 = "6";
        DataBaseFunction.insertToRoot(connection, equationId1, root1);

        String equation2 = "3*x=12";
        long equationId2 = DataBaseFunction.insertToEquation(connection, equation2);
        String root2 = "4";
        DataBaseFunction.insertToRoot(connection, equationId2, root2);

        ArrayList<String> res = DataBaseFunction.findEquationWithOneRoot(connection);

        assertTrue(res.contains(equation1));
        assertTrue(res.contains(equation2));
    }

    @Test
    public void testFindEquationByRoot() {
        String equation1 = "x/2=3";
        long equationId1 = DataBaseFunction.insertToEquation(connection, equation1);
        String root1 = "6";
        DataBaseFunction.insertToRoot(connection, equationId1, root1);

        String equation2 = "2*x+5=17";
        long equationId2 = DataBaseFunction.insertToEquation(connection, equation2);
        DataBaseFunction.insertToRoot(connection, equationId2, root1);

        ArrayList<String> res = DataBaseFunction.findEquationByRoot(root1, connection);

        assertTrue(res.contains(equation1));
        assertTrue(res.contains(equation2));
    }

}
