package jmt.gui;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.gui.jsimgraph.mainGui.MainWindow;
import jmt.util.ShortDescriptionButtonMatcher;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Piotr Tokaj
 */
public class JSIMgraphGuiTest {
	
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
	 * Checks that, on the main JMT window, the user can press the "JSIMgraph"
	 * button, and that this will open a new window of type @MainWindow
	 * (which is the JSIMgraph frame).
	 */
	@Test
	public void mainJSIMgraphWindowDisplaysCorrectly() {
		window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMODEL_SHORT_DESCRIPTION)).click();
		FrameFixture jsimGraph = WindowFinder.findFrame(MainWindow.class).using(window.robot);
		jsimGraph.show();
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}

}
