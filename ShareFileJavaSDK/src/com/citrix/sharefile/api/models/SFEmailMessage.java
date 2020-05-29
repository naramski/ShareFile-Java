
// ------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//  
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
//     
//	   Copyright (c) 2017 Citrix ShareFile. All rights reserved.
// </auto-generated>
// ------------------------------------------------------------------------------

package com.citrix.sharefile.api.models;

import java.io.InputStream;
import java.util.ArrayList;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;
import com.citrix.sharefile.api.*;
import com.citrix.sharefile.api.enumerations.*;
import com.citrix.sharefile.api.models.*;

public class SFEmailMessage extends SFODataObject {

	@SerializedName("To")
	private ArrayList<String> To;
	@SerializedName("CC")
	private ArrayList<String> CC;
	@SerializedName("BCC")
	private ArrayList<String> BCC;
	@SerializedName("FromName")
	private String FromName;
	@SerializedName("FromEmail")
	private String FromEmail;
	@SerializedName("ReplyTo")
	private String ReplyTo;
	@SerializedName("Subject")
	private String Subject;
	@SerializedName("Html")
	private String Html;
	@SerializedName("PlainText")
	private String PlainText;
	@SerializedName("CustomHeader")
	private String CustomHeader;
	@SerializedName("CustomFooter")
	private String CustomFooter;

	public ArrayList<String> getTo() {
		return this.To;
	}

	public void setTo(ArrayList<String> to) {
		this.To = to;
	}
	public ArrayList<String> getCC() {
		return this.CC;
	}

	public void setCC(ArrayList<String> cc) {
		this.CC = cc;
	}
	public ArrayList<String> getBCC() {
		return this.BCC;
	}

	public void setBCC(ArrayList<String> bcc) {
		this.BCC = bcc;
	}
	public String getFromName() {
		return this.FromName;
	}

	public void setFromName(String fromname) {
		this.FromName = fromname;
	}
	public String getFromEmail() {
		return this.FromEmail;
	}

	public void setFromEmail(String fromemail) {
		this.FromEmail = fromemail;
	}
	public String getReplyTo() {
		return this.ReplyTo;
	}

	public void setReplyTo(String replyto) {
		this.ReplyTo = replyto;
	}
	public String getSubject() {
		return this.Subject;
	}

	public void setSubject(String subject) {
		this.Subject = subject;
	}
	public String getHtml() {
		return this.Html;
	}

	public void setHtml(String html) {
		this.Html = html;
	}
	public String getPlainText() {
		return this.PlainText;
	}

	public void setPlainText(String plaintext) {
		this.PlainText = plaintext;
	}
	public String getCustomHeader() {
		return this.CustomHeader;
	}

	public void setCustomHeader(String customheader) {
		this.CustomHeader = customheader;
	}
	public String getCustomFooter() {
		return this.CustomFooter;
	}

	public void setCustomFooter(String customfooter) {
		this.CustomFooter = customfooter;
	}

}