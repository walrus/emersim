package jmt.jmarkov.SpatialQueue.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class RenameEntityFrame extends JFrame {

    private Entity entity;

    public RenameEntityFrame(Entity entity) {
        this.entity = entity;
        init();
        show();
    }

    private void init() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 0));

        JPanel deletePanel = new JPanel();
        mainPanel.add(deletePanel);
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        JButton delete = new JButton("Delete");
        delete.setPreferredSize(new Dimension(90, 30));
        delete.setAlignmentX(Component.CENTER_ALIGNMENT);
        deletePanel.add(Box.createVerticalGlue());
        deletePanel.add(delete);
        deletePanel.add(Box.createVerticalGlue());
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entity.remove();
                dispose();
            }
        });

        JPanel renamePanel = new JPanel();
        renamePanel.setLayout(new BoxLayout(renamePanel, BoxLayout.Y_AXIS));
        mainPanel.add(renamePanel);
        JButton rename = new JButton("Rename");
        rename.setAlignmentX(Component.CENTER_ALIGNMENT);
        rename.setPreferredSize(new Dimension(90, 30));
        final JTextField newName = new JTextField("Enter new name...");
        newName.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                newName.setText("");
            }

            public void focusLost(FocusEvent e) {
            }
        });
        renamePanel.add(newName);
        renamePanel.add(rename);
        renamePanel.add(Box.createVerticalGlue());
        rename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entity.rename(newName.getText());
                dispose();
            }
        });

        add(mainPanel);
        setTitle("Options");

        Dimension d = new Dimension(400, 100);
        setPreferredSize(d);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
