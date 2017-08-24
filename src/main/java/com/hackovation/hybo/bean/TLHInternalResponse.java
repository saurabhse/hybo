package com.hackovation.hybo.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TLHInternalResponse {
	@JsonProperty(value="Ticker")
	String ticker;
	@JsonProperty(value="Price")
	String price;
	@JsonProperty(value="Value")
	String value;
	@JsonProperty(value="Quantity")
	String quantity;
	@JsonProperty(value="Date")
	String date;
	@JsonProperty(value="Price after TLH")
	String tlhPrice;
	@JsonProperty(value="Value after TLH")
	String tlhValue;
	@JsonProperty(value="Quantity after TLH")
	String tlhQuantity;
	
}
