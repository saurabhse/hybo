package com.hackovation.hybo;

import java.util.*;
import java.util.function.Consumer;

//import jnr.ffi.types.gid_t;

public class Temp {

	public static void main(String...args){
		List<Employee> list = new ArrayList<>();
		list.add(new Employee(1,"Aman"));
		list.add(new Employee(2,"Goyal"));
		list.add(new Employee(3,"Aman goyal"));
		
		
		list.forEach(e->{
			System.out.println(e.getName());
		});
		
	}
	
}

class Employee{
	public Employee(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	int id;
	String name;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}


