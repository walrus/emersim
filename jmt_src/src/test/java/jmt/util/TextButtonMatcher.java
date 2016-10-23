package jmt.util;

import javax.swing.JButton;

import org.fest.swing.core.GenericTypeMatcher;

/**
 * A matcher for FEST to find buttons based on the text they contain.
 * (By default FEST finds buttons by name)
 * 
 * @author Piotr Tokaj
 *
 */
public class TextButtonMatcher extends GenericTypeMatcher<JButton> {

	private String textToMatch;

	public TextButtonMatcher(String textToMatch) {
		super(JButton.class);
		this.textToMatch = textToMatch;
	}
	
	@Override
	protected boolean isMatching(JButton button) {
		return textToMatch.equals(button.getText());
	}
}
