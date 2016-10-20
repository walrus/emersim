package jmt.util;

import javax.swing.JLabel;

import org.fest.swing.core.GenericTypeMatcher;
/**
 * A matcher for FEST to find labels based on the text they contain.
 * (By default FEST finds labels by name)
 * 
 * @author Piotr Tokaj
 *
 */

public class TextLabelMatcher extends GenericTypeMatcher<JLabel> {
	
	private String textToMatch;

	public TextLabelMatcher(String textToMatch) {
		super(JLabel.class);
		this.textToMatch = textToMatch;
	}
	
	@Override
	protected boolean isMatching(JLabel label) {
		return textToMatch.equals(label.getText());
	}

}
