package com.hackovation.hybo.daoImpl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hackovation.hybo.bean.dao.PortfolioDao;
import com.hackovation.hybo.entities.PortfolioEntity;

@Repository
public class PortfolioDaoImpl implements PortfolioDao{

	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional
	public void addPortfolio(PortfolioEntity portfolioEntity) {
		System.out.println(" Persisting Portfolio in Database");
		entityManager.persist(portfolioEntity);
		System.out.println(" Portfolio Persisted");
		
	}

	@Override
	@Transactional
	public Long fetchPortfolioForClientId(int clientId) {
		Query query = entityManager.createQuery("select count(P) from PortfolioEntity P where P.clientId="+clientId);
		System.out.println(query.getResultList().get(0));
		return (Long)query.getResultList().get(0);
	}

	@Override
	public List<PortfolioEntity> fetchAllPortfolio() {
		List<PortfolioEntity> list = null;
		list = entityManager.createQuery("Select P from PortfolioEntity P").getResultList();
		return list;
	}

	@Override
	public void deletePortfolio(long userId) {
		int deleted = entityManager.createQuery("delete from PortfolioEntity P where P.clientId="+userId).executeUpdate();
		System.out.println("Deleted for user "+userId + ". No of rows : "+deleted);
		
	}	
	
	

}
