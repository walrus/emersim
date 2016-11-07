package jmt.jmarkov;

import jmt.jmarkov.SpatialQueue.Gui.SpatialQueueFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by joshuazeltser on 26/10/2016.
 */
public class QueueTypeDialog extends JFrame {

    public QueueTypeDialog() {
        init();
        show();
    }

    private void init() {
        JPanel buttons = new JPanel(new GridLayout(1,0));

        JButton queue = new JButton("Queue");
        buttons.add(queue);
        queue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MMQueues.main(null);
                dispose();
            }
        });

        JButton spatial = new JButton("Spatial Queue");
        buttons.add(spatial);
        spatial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SpatialQueueFrame();
                dispose();
            }
        });

        add(buttons);
        setTitle("Which type of queue:");

        Dimension d = new Dimension(250,180);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
