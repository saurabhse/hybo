package com.hackovation.hybo.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@lombok.Data
public class EtfParent {

	String name;
	List<Data> data = new ArrayList<>();
	List<Value> value = new ArrayList<>();
	List<Price> price = new ArrayList<>();
	List<Allocation> allocation = new ArrayList<>();
	
}
