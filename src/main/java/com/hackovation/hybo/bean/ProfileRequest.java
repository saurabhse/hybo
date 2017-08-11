package com.hackovation.hybo.bean;

public class ProfileRequest {
	public ProfileRequest() {
	}
	public ProfileRequest(int age, int amount, int time, String risk, int income) {
		this.age = age;
		this.amount = amount;
		this.time = time;
		this.risk = risk;
		this.income = income;
	}
	int age;
	int amount;
	int time;
	String risk;
	int income;
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
	

}
