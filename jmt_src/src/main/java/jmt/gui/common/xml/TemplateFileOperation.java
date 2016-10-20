package jmt.gui.common.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmt.common.MD5Checksum;
import jmt.gui.common.ChildNode;
import jmt.gui.common.TreeTableNode;
import jmt.gui.jsimgraph.panels.TemplateAddingPanel;
import jmt.gui.jsimgraph.template.AddingPanelData;
import jmt.gui.jsimgraph.template.TemplateConstants;
import static jmt.gui.jsimgraph.template.TemplateConstants.SITE_FILE;
import jmt.gui.jsimgraph.template.TemplateData;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TemplateFileOperation {

	// download via URL
	// myURL is the target file URL
	// fileName is the 'path + name' of the file stored in local device
	public static File download(String myURL, String fileName, int ctime,
			int rtime) throws IOException {
		File outFile = null;

		URL link = new URL(myURL);
		URLConnection conn = link.openConnection();
		conn.setConnectTimeout(ctime);
		Map< String, List< String >> header = conn.getHeaderFields();
		while ( isRedirected( header )) {
			String link2 = header.get( "Location" ).get( 0 );
			link    = new URL( link2 );
			conn = (URLConnection) link.openConnection();
			conn.setConnectTimeout(ctime);
			header = conn.getHeaderFields();
		}
		conn.setReadTimeout(rtime);
		// InputStream in = new BufferedInputStream(link.openStream());
		InputStream in = new BufferedInputStream(conn.getInputStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();

		outFile = new File(fileName);
		FileOutputStream fos = new FileOutputStream(outFile);
		fos.write(response);
		fos.close();

		return outFile;
	}

	private static boolean isRedirected(Map<String, List<String>> header) {
		if (header.get(null) != null) {
			for (String hv : header.get(null)) {
				if (hv.contains(" 301 ") || hv.contains(" 302 ")) {
					return true;
				}
			}
		}
		return false;
	}

	public static void parseIndexXML(String filename, AddingPanelData data)
			throws SAXException, IOException {

		DOMParser parser = new DOMParser();
		InputSource in_source = new InputSource(filename);
		//TODO: the parser must be created first
		parser.parse(in_source);
		Document doc = parser.getDocument();
		NodeList sites = doc.getElementsByTagName("site");

		// extract the site info

		Element site = (Element)sites.item(0);

		String siteName = site.getElementsByTagName("name").item(0).getTextContent();
		String siteURL = site.getElementsByTagName("URI").item(0).getTextContent();

		NodeList trustedsites = doc.getElementsByTagName("trustedsite");

		String tsiteName = null;
		String tsiteURL = null;

		for (int i = 0; i < trustedsites.getLength(); i++) {
			Element trustedsite = (Element)trustedsites.item(i);

			tsiteName = trustedsite.getElementsByTagName("name").item(0).getTextContent();
			tsiteURL = trustedsite.getElementsByTagName("URI").item(0).getTextContent();

			data.addSite(tsiteName, tsiteURL);
		}

		List<String> MD5s = data.getMD5s();
		List<TemplateData> templateDatas = data.getTemplateDatas();

		NodeList cates = doc.getElementsByTagName("category");
		TreeTableNode root = data.getRoot();

		TreeTableNode siteNode = new ChildNode(new Object[]{siteName});
		siteNode.setType("site");
		root.add(siteNode);

		for (int j = 0; j < cates.getLength(); j++) {

			Element cate = (Element) cates.item(j);
			String category = cate.getAttribute("name");
			TreeTableNode cateNode = new ChildNode(new Object[] { category });
			cateNode.setType("category");
			NodeList temps = cate.getElementsByTagName("template");

			// extract the template info
			for (int i = 0; i < temps.getLength(); i++) {

				Element temp = (Element) temps.item(i);
				TemplateData tData = new TemplateData();

				String name = temp
						.getElementsByTagName(TemplateConstants.MF_NAME)
						.item(0).getTextContent();
				String fileName = temp
						.getElementsByTagName(TemplateConstants.MF_FILE_NAME)
						.item(0).getTextContent();
				String author = temp
						.getElementsByTagName(TemplateConstants.MF_AUTHOR)
						.item(0).getTextContent();
				String version = temp
						.getElementsByTagName(TemplateConstants.MF_VERSION)
						.item(0).getTextContent();
				String date = temp
						.getElementsByTagName(TemplateConstants.MF_DATE)
						.item(0).getTextContent();
				String shortDescription = temp
						.getElementsByTagName(
								TemplateConstants.MF_SHORT_DESCRIPTION).item(0)
						.getTextContent();
				String updateAddress = temp
						.getElementsByTagName(
								TemplateConstants.MF_UPDATE_ADDRESS).item(0)
						.getTextContent();
				String toolTip = temp
						.getElementsByTagName(TemplateConstants.MF_TOOLTIP)
						.item(0).getTextContent();
				String descriptionAddress = temp
						.getElementsByTagName(
								TemplateConstants.MF_DESCRIPTION_ADDRESS)
						.item(0).getTextContent();
				String md5 = temp
						.getElementsByTagName(TemplateConstants.MF_MD5).item(0)
						.getTextContent();

				tData.setName(name);
				tData.setFileName(fileName);
				tData.setAuthor(author);
				tData.setVersion(version);
				tData.setDate(date);
				tData.setShortDescription(shortDescription);
				tData.setToolTip(toolTip);
				tData.setUpdateAddress(updateAddress);
				tData.setDescriptionAddress(descriptionAddress);
				tData.setMd5(md5);

				MD5s.add(md5);

				templateDatas.add(tData);

				TreeTableNode templateNode = new ChildNode(
						new TemplateData[] { tData });
				templateNode.setType("template");
				cateNode.add(templateNode);
			}

			siteNode.add(cateNode);
		}

		data.setSiteInfo(siteName, siteURL);
	}

	public static List<TemplateData> getDefaultTemplatesFromIndex(
			String filename) throws SAXException, IOException {
		List<TemplateData> defaultList = new ArrayList<TemplateData>();

		DOMParser parser = new DOMParser();
		InputSource in_source = new InputSource(filename);
		//TODO: the parser must be created first
		parser.parse(in_source);
		Document doc = parser.getDocument();

		NodeList temps = doc.getElementsByTagName("template");

		for (int i = 0; i < temps.getLength(); i++) {

			Element temp = (Element) temps.item(i);
			if ("true".equals(temp
					.getElementsByTagName(TemplateConstants.MF_DEFAULT).item(0)
					.getTextContent())) {
				TemplateData tData = new TemplateData();

				String name = temp
						.getElementsByTagName(TemplateConstants.MF_NAME)
						.item(0).getTextContent();
				String fileName = temp
						.getElementsByTagName(TemplateConstants.MF_FILE_NAME)
						.item(0).getTextContent();
				String author = temp
						.getElementsByTagName(TemplateConstants.MF_AUTHOR)
						.item(0).getTextContent();
				String version = temp
						.getElementsByTagName(TemplateConstants.MF_VERSION)
						.item(0).getTextContent();
				String date = temp
						.getElementsByTagName(TemplateConstants.MF_DATE)
						.item(0).getTextContent();
				String shortDescription = temp
						.getElementsByTagName(
								TemplateConstants.MF_SHORT_DESCRIPTION).item(0)
						.getTextContent();
				String updateAddress = temp
						.getElementsByTagName(
								TemplateConstants.MF_UPDATE_ADDRESS).item(0)
						.getTextContent();
				String toolTip = temp
						.getElementsByTagName(TemplateConstants.MF_TOOLTIP)
						.item(0).getTextContent();
				String descriptionAddress = temp
						.getElementsByTagName(
								TemplateConstants.MF_DESCRIPTION_ADDRESS)
						.item(0).getTextContent();
				String md5 = temp
						.getElementsByTagName(TemplateConstants.MF_MD5).item(0)
						.getTextContent();

				tData.setName(name);
				tData.setFileName(fileName);
				tData.setAuthor(author);
				tData.setVersion(version);
				tData.setDate(date);
				tData.setShortDescription(shortDescription);
				tData.setToolTip(toolTip);
				tData.setUpdateAddress(updateAddress);
				tData.setDescriptionAddress(descriptionAddress);
				tData.setMd5(md5);

				defaultList.add(tData);
			}
		}

		return defaultList;
	}

	public static List<TemplateData> getTemplatesFromIndex(String filename)
			throws SAXException, IOException {
		List<TemplateData> defaultList = new ArrayList<TemplateData>();

		DOMParser parser = new DOMParser();
		InputSource in_source = new InputSource(filename);
		//TODO: the parser must be created first
		parser.parse(in_source);
		Document doc = parser.getDocument();

		NodeList temps = doc.getElementsByTagName("template");

		for (int i = 0; i < temps.getLength(); i++) {

			Element temp = (Element) temps.item(i);
			TemplateData tData = new TemplateData();

			String name = temp.getElementsByTagName(TemplateConstants.MF_NAME)
					.item(0).getTextContent();
			String fileName = temp
					.getElementsByTagName(TemplateConstants.MF_FILE_NAME)
					.item(0).getTextContent();
			String author = temp
					.getElementsByTagName(TemplateConstants.MF_AUTHOR).item(0)
					.getTextContent();
			String version = temp
					.getElementsByTagName(TemplateConstants.MF_VERSION).item(0)
					.getTextContent();
			String date = temp.getElementsByTagName(TemplateConstants.MF_DATE)
					.item(0).getTextContent();
			String shortDescription = temp
					.getElementsByTagName(
							TemplateConstants.MF_SHORT_DESCRIPTION).item(0)
					.getTextContent();
			String updateAddress = temp
					.getElementsByTagName(TemplateConstants.MF_UPDATE_ADDRESS)
					.item(0).getTextContent();
			String toolTip = temp
					.getElementsByTagName(TemplateConstants.MF_TOOLTIP).item(0)
					.getTextContent();
			String descriptionAddress = temp
					.getElementsByTagName(
							TemplateConstants.MF_DESCRIPTION_ADDRESS).item(0)
					.getTextContent();
			String md5 = temp.getElementsByTagName(TemplateConstants.MF_MD5)
					.item(0).getTextContent();

			tData.setName(name);
			tData.setFileName(fileName);
			tData.setAuthor(author);
			tData.setVersion(version);
			tData.setDate(date);
			tData.setShortDescription(shortDescription);
			tData.setToolTip(toolTip);
			tData.setUpdateAddress(updateAddress);
			tData.setDescriptionAddress(descriptionAddress);
			tData.setMd5(md5);

			defaultList.add(tData);
		}

		return defaultList;
	}

	public static void parseSiteXML(AddingPanelData data)
			throws SAXException, IOException {

		File XMLFile = new File(SITE_FILE);
		if (XMLFile.exists()) {
			DOMParser parser = new DOMParser();
			InputSource in_source = new InputSource(SITE_FILE);
			//TODO: the parser must be created first
			parser.parse(in_source);
			Document doc = parser.getDocument();
			NodeList sites = doc.getElementsByTagName("site");
			List<String> siteNames = data.getSiteNames();
			List<String> siteURLs = data.getSiteURLs();
			List<String> siteInfos = data.getSiteInfos();
			// extract the repository info
			for (int i = 0; i < sites.getLength(); i++) {
				Element temp = (Element) sites.item(i);

				String siteName = temp.getElementsByTagName("name").item(0)
						.getTextContent();
				String siteURL = temp.getElementsByTagName("URL").item(0)
						.getTextContent();
				// the info displayed in the bar
				String siteInfo = siteName + " - " + siteURL;

				siteNames.add(siteName);
				siteURLs.add(siteURL);
				siteInfos.add(siteInfo);
			}
		}
	}

	// write site info into sites.xml when adding a new repository
	public static void writeSiteXML(String siteName, String siteAddress)
			throws ParserConfigurationException, FileNotFoundException, SAXException,
			IOException, TransformerFactoryConfigurationError, TransformerException {

		File XMLFile = new File(SITE_FILE);
		if (!XMLFile.exists()) {
			Files.copy(TemplateAddingPanel.class.getResourceAsStream(
					TemplateConstants.F_NAME_SITES), XMLFile.toPath());
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		FileInputStream fis = new FileInputStream(XMLFile);
		Document doc = db.parse(new InputSource(fis));
		fis.close();
		Element element = doc.getDocumentElement();
		Node node = doc.createElement("site");
		Node name = doc.createElement("name");
		name.setTextContent(siteName);
		Node url = doc.createElement("URL");
		url.setTextContent(siteAddress);
		node.appendChild(name);
		node.appendChild(url);
		element.appendChild(node);

		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult sr = new StreamResult(XMLFile);
		tf.transform(new DOMSource(doc), sr);
	}

	// find all available local templates
	public static File[] findTemplateFiles(String dir) {
		File folder = new File(dir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}

		});
		return listOfFiles;
	}

	public static String readManifest(File file, String attr) throws IOException {
		String value = null;
		JarFile templateJarFile = new JarFile(file);
		Manifest m = templateJarFile.getManifest();
		value = m.getMainAttributes().getValue(attr).toString();
		templateJarFile.close();
		return value;
	}

	public static TemplateData[] readTemplates(String dir) {
		File[] files = findTemplateFiles(dir);
		int tempNum = files.length;
		List<TemplateData> found = new ArrayList<>();

		for (int i = 0; i < tempNum; i++) {
			TemplateData temp = new TemplateData();
			try {
				File tempFile = files[i];
				temp.setFile(tempFile);

				temp.setName(readManifest(tempFile, TemplateConstants.MF_NAME));
				temp.setFileName(tempFile.getName());
				temp.setAuthor(readManifest(tempFile, TemplateConstants.MF_AUTHOR));
				temp.setVersion(readManifest(tempFile, TemplateConstants.MF_VERSION));
				temp.setDate(readManifest(tempFile, TemplateConstants.MF_DATE));
				temp.setShortDescription(readManifest(tempFile,
						TemplateConstants.MF_SHORT_DESCRIPTION));
				temp.setToolTip(readManifest(tempFile, TemplateConstants.MF_TOOLTIP));
				temp.setUpdateAddress(readManifest(tempFile,
						TemplateConstants.MF_UPDATE_ADDRESS));
				temp.setDescriptionAddress(readManifest(tempFile,
						TemplateConstants.MF_DESCRIPTION_ADDRESS));
				temp.setMd5(MD5Checksum.getMD5Checksum(tempFile
						.getAbsolutePath()));
			} catch (Exception e) {
				continue;
			}
			found.add(temp);
		}

		return found.toArray(new TemplateData[found.size()]);
	}

}
