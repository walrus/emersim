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

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private TextField textField2;

    private JPanel selectionP;
    private GridBagConstraints c;
    private JTextField textField;


    private JOptionPane optionPane;


    private String receiverName;
    private String clientName;

    private SpatialQueueFrame aFrame;


    /** Creates the reusable dialog. */
    public CustomSimulationDialog(SpatialQueueFrame aFrame) {
        super(aFrame, true);
        this.aFrame = aFrame;


        receiverName = "";
        clientName = "";

        setTitle("Custom Simulation Setup");
        textField = new JTextField(10);
        textField.setEnabled(true);
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                receiverName += e.getKeyChar();
            }
        });
        textField.setText("Receiver");

        textField2 = new TextField(10);
        textField2.setEnabled(true);
        textField2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                clientName += e.getKeyChar();
            }
        });
        textField2.setText("Client");


        //adding to panel
        selectionP = new JPanel(new GridLayout(2,1));

        selectionP.add(textField);

        selectionP.add(textField2);


        JButton enter = new JButton("Enter");

        enter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                aFrame.dispose();
//                aFrame = new SpatialQueueFrame();
//                aFrame.guiComponents.setSimReceiver(receiverName);
//                aFrame.guiComponents.setSimClient(clientName);


            }
        });

        //Create an array of the text and components to be displayed.
        String msgString1 = "Enter names of Receiver and Client:";

        Object[] array = { msgString1, selectionP };

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = { enter };

        //Create the JOptionPane.
        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);


        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window,
				 * we're going to change the JOptionPane's
				 * value property.
				 */
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });



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
