package com.hackovation.hybo.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Portfolio")
public class PortfolioEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ID")
	private int id;
	
	@Column(name="CLIENT_ID")
	private long clientId;
	
	@Column(name="INVESTMENT")
	private double investment;
	
	@Column(name="ETF")
	private String ETF;
	
	@Column(name="NUMBER_OF_ETF")
	private int number;
	
	@Column(name="COST")
	private double cost;
	
	@Column(name="WEIGHT")
	private int weight;
	
	
	public PortfolioEntity() {
		super();
		// TODO Auto-generated constructor stub
	}


	public long getClientId() {
		return clientId;
	}


	public void setClientId(long clientId) {
		this.clientId = clientId;
	}


	public double getInvestment() {
		return investment;
	}


	public void setInvestment(double investment) {
		this.investment = investment;
	}


	public String getETF() {
		return ETF;
	}


	public void setETF(String eTF) {
		ETF = eTF;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	public double getCost() {
		return cost;
	}


	public void setCost(double cost) {
		this.cost = cost;
	}


	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}

	
	
}
