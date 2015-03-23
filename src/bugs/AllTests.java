package bugs;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses(value= {TokenTest.class,
                      RecognizerTest.class,
                      ParserTest.class,
                      tree.TreeTest.class,
                      TreeParserTest.class,
                      RecognizerTest1.class,
                      RecognizerTest2.class,
                      RecognizerTest3.class,
                      BugTest.class})
public class AllTests {
    // Empty class
}