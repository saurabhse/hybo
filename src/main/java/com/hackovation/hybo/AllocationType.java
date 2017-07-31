package com.hackovation.hybo;

public enum AllocationType {
	EQ("EQ"),BOND("BOND");
	private AllocationType(String type) {
		this.type = type;
	}

	String type;
	
}