package jmt.gui.jsimgraph.controller.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jmt.gui.jsimgraph.controller.Mediator;

public class DownloadDefaultTemplates extends AbstractJmodelAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DownloadDefaultTemplates(Mediator mediator) {
		super("Download Default Templates", mediator);
		this.setTooltipText("Download Default Templates");
		this.setMnemonicKey(KeyEvent.VK_D);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		mediator.downloadDefaultTemplates();
	}

}
