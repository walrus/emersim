package jmt.gui.common;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class TreeTableNode extends AbstractMutableTreeTableNode {
	
	public String type;
	
	public TreeTableNode(Object[] data) {
		super(data);
	}

	@Override
	public int getColumnCount() {
		return getData().length;
	}

	@Override
	public Object getValueAt(int columnIndex) {
		return getData()[columnIndex];
	}

	public Object[] getData() {
		return (Object[]) getUserObject();
	}
	
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}

}

