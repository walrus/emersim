package jmt.util;

import javax.swing.JMenuItem;

import org.fest.swing.core.GenericTypeMatcher;

/**
 * A matcher for FEST to find menu items based on the text they contain.
 * (By default FEST finds menu items by name)
 * 
 * @author Piotr Tokaj
 *
 */
public class TextMenuItemMatcher extends GenericTypeMatcher<JMenuItem> {
	
	private String textToMatch;

	public TextMenuItemMatcher(String textToMatch) {
		super(JMenuItem.class);
		this.textToMatch = textToMatch;
	}
	
	@Override
	protected boolean isMatching(JMenuItem item) {
		return textToMatch.equals(item.getText());
	}

}
