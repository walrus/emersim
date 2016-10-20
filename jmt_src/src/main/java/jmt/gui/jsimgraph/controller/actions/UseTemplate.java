package jmt.gui.jsimgraph.controller.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jmt.gui.jsimgraph.controller.Mediator;

/**
 * @author S Jiang
 *
 */
public class UseTemplate extends AbstractJmodelAction{
private static final long serialVersionUID = 1L;
	
	public UseTemplate(Mediator mediator) {
		
		super("Use Template", "Template", mediator);
		putValue(SHORT_DESCRIPTION, "add/use templates");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
		setEnabled(false);

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		mediator.editTemplate();
	}

}

