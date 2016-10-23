package jmt.gui;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.gui.jwat.MainJwatWizard;
import jmt.util.ShortDescriptionButtonMatcher;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Piotr Tokaj
 *
 */
public class JwatGuiTest {

	private FrameFixture window;

	@Before
	public void setUp() {
		window = new FrameFixture(
				GuiActionRunner.execute(new GuiQuery<GraphStartScreen>() {
					protected GraphStartScreen executeInEDT() {
						return new GraphStartScreen();
					}
				}));
		window.show();
	}

	/**
	 * Checks that, on the main JMT window, the user can press the "JWAT"
	 * button, and that this will open a new window of type @MainJwatWizard
	 * (which is the JWAT frame).
	 */
	@Test
	public void mainJWATWindowDisplaysCorrectly() {
		window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JWAT_SHORT_DESCRIPTION)).click();
		FrameFixture jwat = WindowFinder.findFrame(MainJwatWizard.class).using(window.robot);
		jwat.show();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
}
