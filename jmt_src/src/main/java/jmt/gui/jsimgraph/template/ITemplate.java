package jmt.gui.jsimgraph.template;

import jmt.gui.common.definitions.CommonModel;
import jmt.gui.jsimgraph.mainGui.MainWindow;

public interface ITemplate{
	
	public abstract String getName();
	
	public abstract CommonModel createModel();
	
	public abstract void showDialog(MainWindow mainWindow);

}
