package jmt.jmarkov.SpatialQueue;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.jmarkov.QueueTypeDialog;
import jmt.util.ShortDescriptionButtonMatcher;
import jmt.util.TextButtonMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpatialQueueGuiTest {
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
     * button, and that this will open a new window of to choose the queue type
     * and that this will then open the Spatial Queue window.
     */
    @Test
    public void mainSpatialQueueWindowDisplaysCorrectly() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.show();

    }

    @After
    public void tearDown() {
        window.cleanUp();
    }
}
