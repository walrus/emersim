package jmt.jmarkov.SpatialQueue.Map;

import jmt.jmarkov.SpatialQueue.Simulation.ClientRegion;
import jmt.jmarkov.SpatialQueue.Simulation.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegionSettingsFrame extends JFrame {

    /*
     * Provides a dialog to change region properties like name and arrival rate lambda
     */

    private Graphic graphic;

    private ClientRegion cr;

    public RegionSettingsFrame(Graphic graphic, ClientRegion cr) {
        this.graphic = graphic;
        this.cr = cr;
        init();
        show();
    }

    public RegionSettingsFrame(Graphic graphic) {
        this.graphic = graphic;
        initServer();
        show();
    }



    private void init() {
        this.setLayout(new GridLayout(0,2));
        JPanel mainPanel = new JPanel(new GridLayout(3, 0));
        JPanel labelPanel = new JPanel(new GridLayout(3,1));

        JLabel lambdaLabel = new JLabel("Throughput");
        JLabel nameLabel = new JLabel("Name");
        JButton delete = new JButton("Delete Region");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphic.remove();
                dispose();
            }
        });
        labelPanel.add(nameLabel);
        labelPanel.add(lambdaLabel);
        labelPanel.add(delete);


        final JTextField lambda = new JTextField();
        lambda.setText(String.valueOf(cr.getLambda()));

        final JTextField newName = new JTextField(graphic.getName());

        JButton save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphic.rename(newName.getText());
                cr.setLambda(Double.parseDouble(lambda.getText()));
                dispose();
            }
        });

        setTitle("Region Settings");

        mainPanel.add(newName);
        mainPanel.add(lambda);
        mainPanel.add(save);


        add(labelPanel);
        add(mainPanel);

        Dimension d = new Dimension(300, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private void initServer() {
        this.setLayout(new GridLayout(0,2));
        JPanel mainPanel = new JPanel(new GridLayout(2, 0));
        JPanel labelPanel = new JPanel(new GridLayout(2,1));

        JLabel nameLabel = new JLabel("Name");
        JButton delete = new JButton("Delete Server");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphic.remove();
                dispose();
            }
        });
        labelPanel.add(nameLabel);
        labelPanel.add(delete);


        final JTextField newName = new JTextField(graphic.getName());

        JButton save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphic.rename(newName.getText());
                dispose();
            }
        });

        setTitle("Region Settings");

        mainPanel.add(newName);
        mainPanel.add(save);


        add(labelPanel);
        add(mainPanel);

        Dimension d = new Dimension(300, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
