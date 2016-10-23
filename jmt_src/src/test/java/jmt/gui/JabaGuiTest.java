package jmt.gui;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.gui.jaba.JabaWizard;
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
public class JabaGuiTest {

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
	 * Checks that, on the main JMT window, the user can press the "JABA"
	 * button, and that this will open a new window of type @JabaWizard
	 * (which is the JABA frame).
	 */
	@Test
	public void mainJABAWindowDisplaysCorrectly() {
		window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JABA_SHORT_DESCRIPTION)).click();
		FrameFixture jaba = WindowFinder.findFrame(JabaWizard.class).using(window.robot);
		jaba.show();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
}
