package com.hackovation.hybo.services;

import java.util.Map;

import com.hackovation.hybo.entities.PortfolioEntity;

public interface PortfolioService {

	public Map<String,PortfolioEntity> buildPortfolio(String clientId);
}
