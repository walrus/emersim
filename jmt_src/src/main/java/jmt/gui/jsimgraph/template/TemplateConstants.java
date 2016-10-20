package jmt.gui.jsimgraph.template;

import jmt.gui.common.Defaults;

public interface TemplateConstants {

	public final static String TEMPLATE_FOLDER = Defaults.getWorkingPath()
			.getAbsolutePath() + "/templates/";

	public final static String F_NAME_INDEX = "index.xml";
	public final static String F_NAME_SITES = "sites.xml";

	public final static String SITE_FILE = TEMPLATE_FOLDER + F_NAME_SITES;

	public final static String D_TYPE_INDEX = "index";
	public final static String D_TYPE_TEMPLATE = "template";

	public final static int CONN_TIMEOUT = 10000;
	public final static int READ_TIMEOUT = 10000;
	
	public final static int CONN_SHORT_TIMEOUT = 3000;
	public final static int READ_SHORT_TIMEOUT = 3000;

	public final static String MF_NAME = "Name";
	public final static String MF_FILE_NAME = "File-name";
	public final static String MF_AUTHOR = "Author";
	public final static String MF_VERSION = "Version";
	public final static String MF_DATE = "Date";
	public final static String MF_SHORT_DESCRIPTION = "Short-description";
	public final static String MF_TOOLTIP = "Tool-tip";
	public final static String MF_UPDATE_ADDRESS = "Update-address";
	public final static String MF_DESCRIPTION_ADDRESS = "Description-address";
	public final static String MF_MD5 = "Md5";
	public final static String MF_DEFAULT = "Default";

	public final static String DEFAULT_SITE = "http://jmt.sourceforge.net/jtemplates";
	
	public final static String[] CONN_TEST_ADDRESSES = 
		{"http://jmt.sourceforge.net", "https://www.google.co.uk"};
	
	public final static int CELL_HEIGHT = 30;

}
