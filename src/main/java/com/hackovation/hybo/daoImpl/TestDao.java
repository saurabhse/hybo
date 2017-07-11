package com.hackovation.hybo.daoImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hackovation.hybo.bean.dao.TestDaoInterface;
import com.hackovation.hybo.entities.TestEntity;

@Transactional
@Repository
public class TestDao implements TestDaoInterface {

	@PersistenceContext	
	private EntityManager entityManager;	
	
	@Override
	@Transactional
	public void insert() {
		entityManager.persist(new TestEntity("Aman"));
		
	}

}
