package jmt.common;

import java.net.URL;
import java.net.URLConnection;

import jmt.gui.jsimgraph.template.TemplateConstants;

public class ConnectionCheck {
	public static boolean netCheck(String[] testURLs) {
		for (String testURL : testURLs) {
			try {
        	   URL url = new URL(testURL);
        	   URLConnection conn = (URLConnection)url.openConnection();
        	   conn.setConnectTimeout(TemplateConstants.CONN_SHORT_TIMEOUT);
        	   conn.setReadTimeout(TemplateConstants.READ_SHORT_TIMEOUT);
        	   conn.getContent();
        	   return true;
			} catch (Exception e) {              
				System.out.println("failed to connect to " + testURL);
			}
		}
		
        return false;
	}
}
