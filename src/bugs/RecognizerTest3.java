package bugs;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class RecognizerTest3 {

    @Test
    public final void testLongProgram() {
        Recognizer r = new Recognizer(
//                        "// Example Bugs program by David Matuszek\n" + 
//        		"/* Nonsense program to test out the recognizer */\n" + 
        		"Allbugs {\n" + 
        		"    var abc\n" + 
        		"   \n" + 
        		"    define forward using n {\n" + 
        		"        move n // random pointless comment \n" + 
        		"        return -n\n" + 
        		"    }\n" + 
        		"    define abc123 {\n" + 
        		"        abc = 123\n" + 
        		"    }\n" + 
        		"}\n" + 
        		"\n" + 
        		"Bug Sally {\n" + 
        		"    var a, b, c\n" + 
        		"    var x, y\n" + 
        		"    \n" + 
        		"    initially {\n" + 
        		"        x = -50\n" + 
        		"        color red\n" + 
        		"        line 0, 0, 25.3, 100/3\n" + 
        		"    }\n" + 
        		"    \n" + 
        		"    y = 2 + 3 * a - b / c\n" + 
        		"    y = ((2+3)*a)-(b/c)\n" + 
        		"    loop{\n" + 
        		"        y = y / 2.0\n" + 
        		"        exit if y<=0.5\n" + 
        		"    }\n" + 
        		"    switch {\n" + 
        		"    }\n" + 
        		"    switch {\n" + 
        		"        case x < y\n" + 
        		"            moveto 3, x+y\n" + 
        		"            turn x-y\n" + 
        		"        case a <= x < y = z !=a >= b > c\n" + 
        		"            turnto -abc123() + forward(x)\n" + 
        		"    }\n" + 
        		"    do forward(a)\n" + 
        		"}\n" + 
        		"Bug henry {\n" + 
        		"    x = Sally.x\n" + 
        		"    y = -Sally.y + 100\n" + 
        		"}\n");
        assertTrue(r.isProgram());
    }
}