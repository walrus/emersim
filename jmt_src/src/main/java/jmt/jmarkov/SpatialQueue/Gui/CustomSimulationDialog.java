package jmt.jmarkov.SpatialQueue.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by joshuazeltser on 22/11/2016.
 */
public class CustomSimulationDialog extends JDialog {

    private String serverName;
    private String clientName;

    private SpatialQueueFrame aFrame;

    /** Creates the reusable dialog. */
    public CustomSimulationDialog(SpatialQueueFrame aFrame) {
        super(aFrame, true);
        this.aFrame = aFrame;


        serverName = "";
        clientName = "";

        JPanel p = new JPanel(new BorderLayout(5,10));

        JPanel labels = new JPanel(new GridLayout(0,1,2,5));
        labels.add(new JLabel("Server", SwingConstants.RIGHT));
        labels.add(new JLabel("Client", SwingConstants.RIGHT));
        p.add(labels, BorderLayout.WEST);

        JPanel params = new JPanel(new GridLayout(0,1,2,5));
        final JTextField server = new JTextField();
        params.add(server);

        final JTextField client = new JTextField();
        params.add(client);

        p.add(params, BorderLayout.CENTER);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        this.add(p);

        int result = JOptionPane.showConfirmDialog(
                null, p, "Custom Simulation Setup", JOptionPane.OK_CANCEL_OPTION);

        clientName = client.getText().toString();
        serverName = server.getText().toString();

        if (result == 0) {
            aFrame.dispose();
            aFrame = null;
            SpatialQueueFrame newSqf = new SpatialQueueFrame();
            newSqf.setCustomLabels(clientName, serverName);
            if (clientName.equals("") || serverName.equals("")) {
                newSqf.setCustomLabels("Client", "Server");
            }
        }

        //Register an event handler that reacts to option pane state changes.
//        optionPane.addPropertyChangeListener(this);
        this.pack();
        this.setLocationRelativeTo(aFrame);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SpatialQueueFrame mf = new SpatialQueueFrame();
        CustomSimulationDialog jobsDialog = new CustomSimulationDialog(mf);

    }
}
