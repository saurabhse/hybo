package com.hackovation.hybo.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

//@Entity
//@Table(name="Test")
public class TestEntity {

	@Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
	@NotNull
	  private String name;
	public TestEntity() {
		// TODO Auto-generated constructor stub
	}
	public TestEntity(String name) {
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
