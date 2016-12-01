package jmt.jmarkov.SpatialQueue.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientErrorDialog extends JFrame {

    public ClientErrorDialog() {
        init();
        show();
    }

    private void init() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel errorLabel = new JLabel("Chosen region must have at least 3 vertices");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(errorLabel);
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
        setTitle("Client Creation Error");

        Dimension d = new Dimension(400, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
