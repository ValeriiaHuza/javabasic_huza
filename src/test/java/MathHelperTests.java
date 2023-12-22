import org.example.MathHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class MathHelperTests {

    public static MathHelper mh;

    @BeforeAll
    public static void configureBeforeTests(){
        mh = new MathHelper();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2*x+5=17", "3*x=12", "x/2=3", "5=2*x-1", "2*x*x=10", "-1.3*5/x=1.2", "2*(x+5+x)+5=10" })
    public void checkTrueEquationTest(String equation){
        boolean correct = mh.checkEquation(equation);
        assertTrue(correct, "Рівняння" + equation);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2*+x+5=17", "(3*x=12", "x/2.=3", "2*x-1", "2=1+1", "(3+5)*8*x)=6", " ", "3"})
    public void checkFalseEquationTest(String equation){
        boolean correct = mh.checkEquation(equation);
        assertFalse(correct, "Рівняння" + equation);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1","1.3","5","5.9"})
    public void checkFalseRootForEquation1(String root){
        String equation ="2*x+5=17";
        boolean correctRoot = mh.checkRoot(equation,root);
        assertFalse(correctRoot, "Рівняння - " + equation + ", корінь - " + root);
    }

    @Test
    public void checkTrueRootForEquation1(){
        String equation ="2*x+5=17";
        boolean correctRoot = mh.checkRoot(equation,"6");
        assertTrue(correctRoot, "Рівняння - " + equation + ", корінь - 6");
    }

    @Test
    public void checkTrueRootForEquation2(){
        String equation ="-1.3*5/x=1.2";
        boolean correctRoot = mh.checkRoot(equation,"-5.41666667");
        assertTrue(correctRoot, "Рівняння - " + equation + ", корінь - -5.41666667");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.2360679775","-2.2360679775"})
    public void checkTrueRootForEquation3(String root){
        String equation ="2*x*x=10";
        boolean correctRoot = mh.checkRoot(equation,root);
        assertTrue(correctRoot, "Рівняння - " + equation + ", корінь - " + root);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1","1.5","2.236","-2.2","0"})
    public void checkFalseRootForEquation3(String root){
        String equation ="2*x*x=10";
        boolean correctRoot = mh.checkRoot(equation,root);
        assertFalse(correctRoot, "Рівняння - " + equation + ", корінь - " + root);
    }
}
