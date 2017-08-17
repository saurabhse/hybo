package com.hackovation.hybo.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class ProfileRequest {
	public ProfileRequest() {
	}
	int age;
	int amount;
	int time;
	String risk;
	int income;
	String date;
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getRisk() {
		return risk;
	}
	public void setRisk(String risk) {
		this.risk = risk;
	}
	public int getIncome() {
		return income;
	}
	public void setIncome(int income) {
		this.income = income;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	

}
