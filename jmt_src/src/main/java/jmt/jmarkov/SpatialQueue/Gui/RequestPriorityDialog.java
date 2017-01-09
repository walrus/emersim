package jmt.jmarkov.SpatialQueue.Gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by daniel on 09/01/17.
 */
public class RequestPriorityDialog {

    //Name of custom server and clients
    private int numPriorities;

    //Dialog that is produced when custom dialog selected
    public RequestPriorityDialog(SpatialQueueFrame aFrame) {

        numPriorities = 0;

        JPanel p = new JPanel(new BorderLayout(5, 10));

        //create labels for server and client input
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 5));
        labels.add(new JLabel("Priority levels", SwingConstants.RIGHT));
        p.add(labels, BorderLayout.WEST);

        // create text fields so the user can input their chosen names
        JPanel params = new JPanel(new GridLayout(0, 1, 2, 5));

        final JTextField priorityField = new JTextField();
        params.add(priorityField);

        p.add(params, BorderLayout.CENTER);

        // create the dialog box
        int result = JOptionPane.showConfirmDialog(
                aFrame, p, "Number of priority levels to simulate", JOptionPane.OK_CANCEL_OPTION);

        try {
            numPriorities = Integer.parseInt(priorityField.getText().toString());
        } catch (NumberFormatException e) {
            numPriorities = 0;
        }
        //if ok is clicked save the settings and close the dialog
        if (result == 0) {
            //aFrame.dispose();
            GuiComponents.priorityLevels = numPriorities;
        }
    }
}
