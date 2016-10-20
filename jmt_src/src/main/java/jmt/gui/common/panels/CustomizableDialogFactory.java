package jmt.gui.common.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JComponent;

import jmt.framework.gui.components.JMTDialog;
import jmt.framework.gui.wizard.WizardPanel;

/**
 * This dialog factory is used to display all template relevant panels.
 * 
 * @author S Jiang
 *
 */
public class CustomizableDialogFactory {
	
	private JMTDialog dialogFrame;
	private Frame mainWindow;
	
	public CustomizableDialogFactory(Frame mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	private void createDialog(int width, int height) {
		// Creates dialog
		dialogFrame = new JMTDialog(mainWindow, true);
		dialogFrame.centerWindow(width, height);
		dialogFrame.setResizable(true);
		dialogFrame.setMinimumSize(new Dimension(width, height));
		dialogFrame.getContentPane().setLayout(new BorderLayout());
	}
	
	public void getDialog(int width, int height, final JComponent panel, String title) {
		createDialog(width, height);
		// Adds panel
		dialogFrame.getContentPane().add(panel, BorderLayout.CENTER);
		// Sets title
		if (title != null) {
			dialogFrame.setTitle(title);
		}
		// If this is a wizard panel call gotFocus() method
		if (panel instanceof WizardPanel) {
			((WizardPanel) panel).gotFocus();
		}

		// Shows dialog
		dialogFrame.setVisible(true);
	}
	
}

