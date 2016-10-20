package jmt.util;

import javax.swing.Action;
import javax.swing.JButton;

import org.fest.swing.core.GenericTypeMatcher;

/**
 * A matcher for FEST to find buttons based on their short description (tooltip).
 * (By default FEST finds buttons by name)
 * 
 * @author Piotr Tokaj
 *
 */
public class ShortDescriptionButtonMatcher extends GenericTypeMatcher<JButton> {

	private String textToMatch;

	public ShortDescriptionButtonMatcher(String textToMatch) {
		super(JButton.class);
		this.textToMatch = textToMatch;
	}
	
	@Override
	protected boolean isMatching(JButton button) {
		return button.getAction() != null && 
				textToMatch.equals(button.getAction().getValue(Action.SHORT_DESCRIPTION));
	}

}
