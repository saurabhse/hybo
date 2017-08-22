package com.hackovation.hybo.bean;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DataVO {

	String etf;
	int quantity;
	double perc;
	double perEtfPrice;
	double value;
	Date transactionDate;
}
