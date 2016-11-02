package jmt.jmarkov.SpatialQueue;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.jmarkov.QueueTypeDialog;
import jmt.util.ShortDescriptionButtonMatcher;
import jmt.util.TextButtonMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertTrue;

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

    @Test
    public void startButtonIsEnabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).requireEnabled();
        jmch.show();
    }

    @Test
    public void pauseButtonIsDisabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Pause")).requireDisabled();
        jmch.show();
    }

    @Test
    public void stopButtonIsDisabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Stop")).requireDisabled();
        jmch.show();
    }

    @Test
    public void receiverButtonIsEnabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Receiver")).requireEnabled();
        jmch.show();
    }

    @Test
    public void clientButtonIsEnabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Client")).requireEnabled();
        jmch.show();
    }

    @Test
    public void startButtonIsDisabledAfterClicked() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).click();
        jmch.button(new TextButtonMatcher("Start")).requireDisabled();
    }

    @Test
    public void pauseAndStopButtonsEnabledAfterStartClicked() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).click();
        jmch.button(new TextButtonMatcher("Pause")).requireEnabled();
        jmch.button(new TextButtonMatcher("Stop")).requireEnabled();
    }

    @Test
    public void pauseButtonAndStopOnlyEnabledAfterPauseClicked() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).click();
        jmch.button(new TextButtonMatcher("Pause")).click();
        jmch.button(new TextButtonMatcher("Start")).requireEnabled();
        jmch.button(new TextButtonMatcher("Pause")).requireDisabled();
        jmch.button(new TextButtonMatcher("Stop")).requireEnabled();
    }

    @Test
    public void stopAndPauseDisabledAndStartEnabledAfterStop() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).click();
        jmch.button(new TextButtonMatcher("Stop")).click();
        jmch.button(new TextButtonMatcher("Start")).requireEnabled();
        jmch.button(new TextButtonMatcher("Pause")).requireDisabled();
        jmch.button(new TextButtonMatcher("Stop")).requireDisabled();
    }




    @After
    public void tearDown() {
        window.cleanUp();
    }
}
