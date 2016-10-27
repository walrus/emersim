package jmt.gui;

import javax.swing.*;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.jmarkov.MMQueues;
import jmt.jmarkov.QueueTypeDialog;
import jmt.util.ShortDescriptionButtonMatcher;
import jmt.util.TextButtonMatcher;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Piotr Tokaj
 *
 */
public class JmchGuiTest {

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
	 * Checks that, on the main JMT window, the user can press the "JMCH"
	 * button, and that this will open a new window of type @MMQueues
	 * (which is the JMCH frame).
	 */
	@Test
	public void mainJMCHWindowDisplaysCorrectly() {
		window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
		FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
		optionDialog.button(new TextButtonMatcher("Queue")).click();
		FrameFixture jmch = WindowFinder.findFrame(MMQueues.class).using(window.robot);
		DialogFixture popup = WindowFinder.findDialog(JDialog.class).using(window.robot);
		popup.button(new TextButtonMatcher("Enter")).click();
		jmch.show();
		
	}
	
	@After
	public void tearDown() {
		window.cleanUp();
	}
}
