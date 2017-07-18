package com.hackovation.hybo.rebalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hack17.hybo.domain.Allocation;
import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;


public interface Rebalance {

	public void rebalance();
	default Map<Portfolio, List<Allocation>> groupByUserId(List<Portfolio> list){
		Map<Portfolio,List<Allocation>> map = new HashMap<>();
		for(Portfolio portfolio:list){
			InvestorProfile investorProfile = portfolio.getInvestorProfile();
			if(portfolio.getAllocations().size()>0)
				map.put(portfolio, portfolio.getAllocations());
		}
	return map;
}

}
