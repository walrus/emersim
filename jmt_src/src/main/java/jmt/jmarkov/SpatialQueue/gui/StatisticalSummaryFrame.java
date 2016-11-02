package jmt.jmarkov.SpatialQueue.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by Dyl on 02/11/2016.
 */

public class StatisticalSummaryFrame extends JFrame implements ActionListener, PropertyChangeListener {

    public StatisticalSummaryFrame() {
        init();
    }

    public void init(){
        setTitle("Create a new Statistical Summary window");

        // window creation
        Dimension d = new Dimension(1000,800);
        setPreferredSize(d);

        // window settings
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }







    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }


    public static void main(String[] args) {
        new StatisticalSummaryFrame();
    }

}
