package com.hackovation.hybo.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TLHInternalResponse {
	String ticker;
	String price;
	String value;
	String date;
	String TLH_Price;
	String TLH_Value;
	
}
