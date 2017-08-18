package com.hackovation.hybo;

public enum CreatedBy {
	PORT("PORT"),REBAL("REBAL"),TLH("TLH");
	private CreatedBy(String type) {
		this.type = type;
	}

	String type;
}
