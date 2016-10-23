package jmt.gui;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.gui.jsimwiz.JSIMMain;
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
public class JSIMwizGuiTest {
	
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
	 * Checks that, on the main JMT window, the user can press the "JSIMwiz"
	 * button, and that this will open a new window of type @JSIMMain
	 * (which is the JSIMwiz frame).
	 */
	@Test
	public void mainJSIMwizWindowDisplaysCorrectly() {
		window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JSIM_SHORT_DESCRIPTION)).click();
		FrameFixture jsimWiz = WindowFinder.findFrame(JSIMMain.class).using(window.robot);
		jsimWiz.show();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}

}
