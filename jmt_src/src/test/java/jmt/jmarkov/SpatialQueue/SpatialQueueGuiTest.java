package jmt.jmarkov.SpatialQueue;

import jmt.gui.common.startScreen.GraphStartScreen;
import jmt.jmarkov.QueueTypeDialog;
import jmt.jmarkov.SpatialQueue.Gui.SimulationSizeDialog;
import jmt.jmarkov.SpatialQueue.Gui.SpatialQueueFrame;
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

import java.awt.*;
import java.awt.event.InputEvent;

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
    @Test //done
    public void mainSpatialQueueWindowDisplaysCorrectly() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.show();

    }

    @Test //done
    public void startButtonIsDisabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Start")).requireDisabled();
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
        jmch.button(new TextButtonMatcher("Add Server")).requireEnabled();
        jmch.show();
    }

    @Test
    public void clientButtonIsDisabledBeforeSimulation() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Client")).requireDisabled();
        jmch.show();
    }

    @Test
    public void receiverButtonDisabledAfterClick() {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        jmch.button(new TextButtonMatcher("Add Server")).requireDisabled();
        jmch.show();
    }

    @Test
    public void clientEnabledAfterServerSet() throws AWTException {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        Robot robot = new Robot();
        robot.mouseMove(400, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        jmch.button(new TextButtonMatcher("Add Client")).requireEnabled();
        jmch.show();
    }

    @Test
    public void clientAndStartEnabledAfterClientSet() throws AWTException {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        Robot robot = new Robot();
        robot.mouseMove(400, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        jmch.button(new TextButtonMatcher("Add Client")).click();

        robot.mouseMove(500, 350);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(600, 300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 340);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jmch.button(new TextButtonMatcher("Add Client")).requireEnabled();
        jmch.button(new TextButtonMatcher("Start")).requireEnabled();

    }

    @Test
    public void afterStartPauseAndStopEnabledStartDisabled() throws AWTException {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        Robot robot = new Robot();
        robot.mouseMove(400, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        jmch.button(new TextButtonMatcher("Add Client")).click();

        robot.mouseMove(500, 350);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(600, 300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 340);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jmch.button(new TextButtonMatcher("Start")).click();

        DialogFixture startMenu = WindowFinder.findDialog(SimulationSizeDialog.class).using(window.robot);
        startMenu.button(new TextButtonMatcher("Enter")).click();




        jmch.button(new TextButtonMatcher("Pause")).requireEnabled();
        jmch.button(new TextButtonMatcher("Stop")).requireEnabled();
        jmch.button(new TextButtonMatcher("Start")).requireDisabled();

    }

    @Test
    public void afterPauseStartAndStopEnabled() throws AWTException {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        Robot robot = new Robot();
        robot.mouseMove(400, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        jmch.button(new TextButtonMatcher("Add Client")).click();

        robot.mouseMove(500, 350);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(600, 300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 340);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jmch.button(new TextButtonMatcher("Start")).click();

        DialogFixture startMenu = WindowFinder.findDialog(SimulationSizeDialog.class).using(window.robot);
        startMenu.button(new TextButtonMatcher("Enter")).click();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jmch.button(new TextButtonMatcher("Pause")).click();


        jmch.button(new TextButtonMatcher("Pause")).requireDisabled();
        jmch.button(new TextButtonMatcher("Stop")).requireEnabled();
        jmch.button(new TextButtonMatcher("Start")).requireEnabled();
    }

    @Test
    public void abilityToCreateMultipleRegions() throws AWTException {
        window.button(new ShortDescriptionButtonMatcher(GraphStartScreen.JMCH_SHORT_DESCRIPTION)).click();
        FrameFixture optionDialog = WindowFinder.findFrame(QueueTypeDialog.class).using(window.robot);
        optionDialog.button(new TextButtonMatcher("Spatial Queue")).click();
        FrameFixture jmch = WindowFinder.findFrame(SpatialQueueFrame.class).using(window.robot);
        jmch.button(new TextButtonMatcher("Add Server")).click();
        Robot robot = new Robot();
        robot.mouseMove(400, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        jmch.button(new TextButtonMatcher("Add Client")).click();

        robot.mouseMove(500, 350);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(600, 300);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(500, 340);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jmch.button(new TextButtonMatcher("Add Client")).click();

        robot.mouseMove(800, 200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(700, 400);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(800, 100);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(400, 400);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.mouseMove(800, 190);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jmch.button(new TextButtonMatcher("Start")).requireEnabled();
    }


    @After
    public void tearDown() {
        window.cleanUp();
    }
}
