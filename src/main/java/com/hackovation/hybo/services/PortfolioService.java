package com.hackovation.hybo.services;

import java.util.Date;
import java.util.Map;

import com.hack17.hybo.domain.InvestorProfile;
import com.hack17.hybo.domain.Portfolio;
import com.hackovation.hybo.bean.ProfileRequest;

public interface PortfolioService {

	public Map<String,Portfolio> buildPortfolio(InvestorProfile profile , String userId,boolean dummy,Date date,int investment);
	public void deleteAllPortfolio();
	public InvestorProfile createProfile(ProfileRequest profileRequest);
}
