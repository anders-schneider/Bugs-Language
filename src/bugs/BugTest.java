package bugs;

import static org.junit.Assert.*;

import org.junit.Test;

public class BugTest {

	@Test
	public void testStoreAndFetch() {
		Bug bug = new Bug();
		
		bug.store("x", 1.0);
		assertEquals(bug.fetch("x"), 1.0, 0);
		
		try {
			bug.fetch("u");
			fail();
		} catch (IllegalArgumentException e) { }
		
		bug.store("u", 0);
		assertEquals(bug.fetch("u"), 0, 0);
		
		assertEquals(bug.fetch("angle"), 0, 0);
		
		bug.store("angle", 180);
		assertEquals(bug.fetch("angle"), 180, 0);
	}
}
