package jmt.util;

import javax.swing.JFrame;

import org.fest.swing.core.GenericTypeMatcher;

/**
 * A matcher for FEST to find Frames based on a partial match on their title.
 * More precisely, it checks that the name of the frame starts with the given 
 * text.
 * 
 * @author Piotr Tokaj
 *
 */
public class PartialTitleFrameMatcher extends GenericTypeMatcher<JFrame> {

	private String textToMatch;

	public PartialTitleFrameMatcher(String textToMatch) {
		super(JFrame.class);
		this.textToMatch = textToMatch;
	}
	
	@Override
	public boolean isMatching(JFrame frame) {
		return frame.isVisible() && frame.getTitle().startsWith(textToMatch);
	}
}
