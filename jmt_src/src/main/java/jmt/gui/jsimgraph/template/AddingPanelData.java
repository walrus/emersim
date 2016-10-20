package jmt.gui.jsimgraph.template;

import java.util.ArrayList;
import java.util.List;

import jmt.gui.common.RootNode;
import jmt.gui.common.TreeTableNode;


public class AddingPanelData {
	
	private List<String> MD5s;
	
	//use TemplateData to store all template-relevant data
	private List<TemplateData> templateDatas;

	// info of all sites stored in sites.xml
	private List<String> siteNames;
	private List<String> siteURLs;
	private List<String> siteInfos;
	
	private String siteName;
	private String siteURL;
	private String siteInfo;
	
	private TreeTableNode root;
	
	public AddingPanelData() {
		MD5s = new ArrayList<String>();
		templateDatas = new ArrayList<TemplateData>();
		siteNames = new ArrayList<String>();
		siteURLs = new ArrayList<String>();
		siteInfos = new ArrayList<String>();
		root = new RootNode("root");
	}
	
	public List<String> getMD5s() {
		return MD5s;
	}
	
	public void setMD5s(List<String> mD5s) {
		MD5s = mD5s;
	}
	
	public List<String> getSiteNames() {
		return siteNames;
	}
	public void setSiteNames(List<String> siteNames) {
		this.siteNames = siteNames;
	}
	
	public List<String> getSiteURLs() {
		return siteURLs;
	}
	public void setSiteURLs(List<String> siteURLs) {
		this.siteURLs = siteURLs;
	}
	
	public List<String> getSiteInfos() {
		return siteInfos;
	}
	public void setSiteInfos(List<String> siteInfos) {
		this.siteInfos = siteInfos;
	}
	
	public String getSiteURL() {
		return siteURL;
	}
	public void setSiteURL(String siteURL) {
		this.siteURL = siteURL;
	}
	
	public List<TemplateData> getTemplateDatas() {
		return templateDatas;
	}
	public void setTemplateDatas(List<TemplateData> templateDatas) {
		this.templateDatas = templateDatas;
	}

	public TreeTableNode getRoot() {
		return root;
	}

	public void setRoot(TreeTableNode root) {
		this.root = root;
	}
	
	public void clearRoot() {
		root = new RootNode("root");
	}
	
	public void clearTemplateDatas() {
		templateDatas = new ArrayList<TemplateData>(); 
	}
	
	public void clearSitesData() {
		siteNames = new ArrayList<String>();
		siteURLs = new ArrayList<String>();
		siteInfos = new ArrayList<String>();
	}
	
	//add
	public void addTemplateData(TemplateData data) {
		this.templateDatas.add(data);
	}
	
	public void addSite(String name, String URL) {
		this.siteURLs.add(URL);
		this.siteNames.add(name);
		this.siteInfos.add(name +"-"+ URL);
	}
	
	public void addNode(TreeTableNode node) {
		this.root.add(node);
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteInfo() {
		return siteInfo;
	}

	public void setSiteInfo(String name, String URL) {
		this.siteName = name;
		this.siteURL = URL;
		this.siteInfo = name + " - " + URL;
	}
	
}
