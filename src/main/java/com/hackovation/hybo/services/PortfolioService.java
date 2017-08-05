package com.hackovation.hybo.services;

import java.util.Date;
import java.util.Map;

import com.hack17.hybo.domain.Portfolio;

public interface PortfolioService {

	public Map<String,Portfolio> buildPortfolio(String clientId,boolean dummy,Date date);
	public void deleteAllPortfolio();
}
