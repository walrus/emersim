package jmt.gui;

import jmt.framework.gui.components.JMTDialog;
import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.manual.PDFViewerBuffer;
import jmt.util.TextButtonMatcher;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MainGuiTest {

	private FrameFixture window;

	@Before
	public void setUp() {
		window = new FrameFixture(
				GuiActionRunner.execute(new GuiQuery<GraphStartScreen>() {
					protected GraphStartScreen executeInEDT() {
						return new GraphStartScreen();
					}
				}));
	}

	@Test
	public void mainWindowDisplaysCorrectly() {
		window.show();
	}
	
	@Test
	public void aboutWindowDisplaysCorrectly() {
		window.show();
		window.button(new TextButtonMatcher("Credits")).click();
		DialogFixture about = WindowFinder.findDialog(JMTDialog.class).using(window.robot);
		about.close();
	}
	
	@Test
	public void introWindowDisplaysCorrectly() {
		window.show();
		window.button(new TextButtonMatcher("Introduction to JMT")).click();
		FrameFixture intro = WindowFinder.findFrame(PDFViewerBuffer.class).using(window.robot);
		intro.close();		
	}

	@After
	public void tearDown() {
		window.cleanUp();
	}
}
