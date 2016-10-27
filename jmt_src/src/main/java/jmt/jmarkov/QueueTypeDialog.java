package jmt.jmarkov;

import jmt.jmarkov.SpatialQueue.SpatialQueueFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by joshuazeltser on 26/10/2016.
 */
public class QueueTypeDialog extends JDialog {

    public QueueTypeDialog() {
        init();
    }

    private void init() {
        final JFrame queueType = new JFrame();

        JPanel buttons = new JPanel(new GridLayout(1,0));

        JButton queue = new JButton("Queue");
        buttons.add(queue);
        queue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MMQueues.main(null);
                queueType.dispose();
            }
        });

        JButton spatial = new JButton("Spatial Queue");
        buttons.add(spatial);
        spatial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SpatialQueueFrame(null);
                queueType.dispose();
            }
        });

        queueType.add(buttons);

//        JPanel labelPanel = new JPanel(new BorderLayout());
//        JLabel label = new JLabel("Which type of queue:");
        queueType.setTitle("Which type of queue:");



        Dimension d = new Dimension(250,180);
        queueType.setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        queueType.pack();
        queueType.setLocationRelativeTo(null);
        queueType.setVisible(true);
    }
}
