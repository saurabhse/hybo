package com.hackovation.hybo.bean.dao;

import java.util.List;

import com.hackovation.hybo.entities.PortfolioEntity;

public interface PortfolioDao {

	public void addPortfolio(PortfolioEntity portfolioEntity);
	public Long fetchPortfolioForClientId(int clientId);
	public List<PortfolioEntity> fetchAllPortfolio();
	public void deletePortfolio(long userId);
}
