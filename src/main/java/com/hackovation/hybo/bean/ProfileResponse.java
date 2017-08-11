package com.hackovation.hybo.bean;

import java.util.List;

public class ProfileResponse {
	public ProfileResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ProfileResponse(String label, String value, int clientId) {
		super();
		this.label = label;
		this.value = value;
		this.clientId = clientId;
	}
	String label;
	String value;
	int clientId;
	
	
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	
}

