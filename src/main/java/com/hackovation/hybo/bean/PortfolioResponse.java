package com.hackovation.hybo.bean;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PortfolioResponse {
	double total;
	List<ProfileResponse> data;
}
