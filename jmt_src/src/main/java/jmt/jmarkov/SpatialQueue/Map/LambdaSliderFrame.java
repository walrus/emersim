package jmt.jmarkov.SpatialQueue.Map;

import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.utils.Formatter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Dictionary;

public class LambdaSliderFrame extends JFrame {

    private Entity entity;
    private boolean isClient;
    private GridBagConstraints c = new GridBagConstraints();
    private JSlider lambdaS;
    private double lambdaMultiplier = 1; //lambda slide bar multiplier
    private JLabel lambdaL;
    private String lambdaStrS = "Avg. Arrival Rate (lambda) = ";
    private String lambdaStrE = " cust./s";
    private boolean lambdaSChange = true;
    private int lambdaMultiplierChange = 0; //for the lambda slide bar
    private JSlider sS;
    private double lambda;


    public LambdaSliderFrame(Entity entity){
        this.entity = entity;
        if (entity instanceof ClientEntity) {
            init();
            show();
        }
    }

    private void init() {
        JPanel mainPanel = new JPanel(new GridLayout(1,0));

        //JPanel renamePanel = createRenamePanel();
        JPanel lambdaPanel = createLambdaPanel();

        //mainPanel.add(renamePanel);
        mainPanel.add(lambdaPanel);


        add(mainPanel);
        setTitle(entity.getName());

        Dimension d = new Dimension(400, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);


    }

    private JPanel createLambdaPanel() {
        JPanel parametersP = new JPanel();
        JPanel lambdaPanel = new JPanel();
        lambdaL = new JLabel();
        lambdaMultiplier = 1; //lambda slide bar multiplier
        int lambdaMultiplierChange = 0;
        DrawConstrains dCst = new DrawNormal();
        int LAMBDA_I = 50;


        lambdaPanel.setLayout(new GridLayout(2, 1));
        lambdaS = new JSlider();


        c.weightx = 0.5;

        parametersP.add(lambdaPanel, c);

        c.gridx = 1;
        c.weightx = 0;
        parametersP.add(getSplitter(10, 1), c);
        c.weightx = 0.5;

        lambdaL.setAlignmentX(SwingConstants.CENTER);
        lambdaPanel.add(lambdaL);
        lambdaMultiplier = 0.01;
        lambdaMultiplierChange = 0;
        lambdaS.setMaximum(100);
        lambdaS.setMinimum(0);
        lambdaS.setMajorTickSpacing(25);
        lambdaS.setMinorTickSpacing(1);
        lambdaS.setPaintLabels(true);
        lambdaS.setSnapToTicks(true);
        lambdaPanel.add(lambdaS);
        lambdaL.setFont(dCst.getNormalGUIFont());
        System.out.println("set");

        // CHANGE THIS
        lambdaS.setValue((int) (lambda / lambdaMultiplier));

        setLambdaSlider();
        lambdaS.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                lambdaSStateChanged(evt);
                if (lambdaSChange) {
                    setLambdaMultiplier();
                }

            }
        });
        lambdaS.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                lambdaSChange = false;
            }

            public void mouseReleased(MouseEvent e) {
                setLambdaMultiplier();
                lambdaSChange = true;
            }

        });
        lambdaS.repaint();
        return parametersP;
    }

    protected JPanel getSplitter(int width, int height) {
        JPanel splitPane = new JPanel();
        Dimension dim = new Dimension(width, height);
        splitPane.setEnabled(false);
        splitPane.setPreferredSize(dim);
        splitPane.setMaximumSize(dim);
        splitPane.setMinimumSize(dim);
        return splitPane;
    }

    public void setLambdaSlider() {
        Dictionary<Integer, JLabel> ld = lambdaS.getLabelTable();

        for (int i = lambdaS.getMinimum(); i <= lambdaS.getMaximum(); i += lambdaS.getMajorTickSpacing()) {
            ld.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * lambdaMultiplier, 2)));
        }

//        for (int i = 0; i <= 4; i++) {
//        	ld.put(new Integer(i * 25), new JLabel("" + Formatter.formatNumber(i * 0.25, 2)));
//        }
        lambdaS.setLabelTable(ld);
        lambda = lambdaMultiplier * lambdaS.getValue();
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
    }

    /** Auto-generated event handler method */
    protected void lambdaSStateChanged(ChangeEvent evt) {
        if (lambdaS.getValue() == 0) {
            lambdaMultiplier = 0.01;
            lambdaMultiplierChange = 0;
            lambdaS.setValue(1);
        }
        lambda = lambdaMultiplier * lambdaS.getValue();
        System.out.println(lambdaMultiplier * lambdaS.getValue());
        lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
    }

    public void setLambdaMultiplier() {
        while (true) {
            if (lambdaS.getValue() > lambdaS.getMaximum() * 0.95) {
                if (lambdaMultiplierChange <= 4) {
                    if (lambdaMultiplierChange % 2 == 0) {
                        lambdaMultiplier *= 2;
                        setLambdaSlider();
                        lambdaS.setValue((lambdaS.getValue() + 1) / 2);
                    } else {
                        lambdaMultiplier *= 5;
                        setLambdaSlider();
                        lambdaS.setValue((lambdaS.getValue() + 1) / 5);
                    }
                    lambdaMultiplierChange++;
                    //System.out.println("LambdaMultiplier:" + lambdaMultiplier);
                } else {
                    break;
                }
            } else if (lambdaS.getValue() < lambdaS.getMaximum() * 0.05) {
                if (lambdaMultiplierChange > 0) {
                    if (lambdaMultiplierChange % 2 == 1) {
                        lambdaMultiplier /= 2;
                        setLambdaSlider();
                        lambdaS.setValue(lambdaS.getValue() * 2);
                    } else {
                        lambdaMultiplier /= 5;
                        setLambdaSlider();
                        lambdaS.setValue(lambdaS.getValue() * 5);
                    }
                    lambdaMultiplierChange--;
                    //System.out.println("LambdaMultiplier:" + lambdaMultiplier);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }





}
