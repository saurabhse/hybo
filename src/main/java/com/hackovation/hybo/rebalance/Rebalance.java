package com.hackovation.hybo.rebalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hackovation.hybo.entities.PortfolioEntity;

public interface Rebalance {

	public void rebalance();
	default Map<Long, List<PortfolioEntity>> groupByUserId(List<PortfolioEntity> list){
		Map<Long,List<PortfolioEntity>> map = new HashMap<>();
		for(PortfolioEntity portfolio:list){
			long userId = portfolio.getClientId();
			List<PortfolioEntity> listUserIdWise = null;
			if(map.containsKey(userId)){
				listUserIdWise = map.get(userId); 
			}
			else{
				listUserIdWise = new ArrayList<>();
			}
			listUserIdWise.add(portfolio);
			map.put(userId, listUserIdWise);
		}
	return map;
}

}
