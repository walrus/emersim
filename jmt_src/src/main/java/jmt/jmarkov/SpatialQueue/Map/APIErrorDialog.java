package jmt.jmarkov.SpatialQueue.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class APIErrorDialog extends JFrame {

    public APIErrorDialog() {
        /*
        * Dialog to notify the user when directions cannot be calculated for a
        * particular route.
        */

        init();
        show();
    }

    private void init() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel errorLabel = new JLabel("A route could not be calculated for this journey.");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createVerticalGlue());

        JLabel errorLabel2 = new JLabel("Please choose another travel mode and try again.");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(errorLabel2);
        mainPanel.add(Box.createVerticalGlue());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        closeButton.setPreferredSize(new Dimension(90, 30));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(closeButton);

        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        setTitle("Route not found");

        Dimension d = new Dimension(600, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
