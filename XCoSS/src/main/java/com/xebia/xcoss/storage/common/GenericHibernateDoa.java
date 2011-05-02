package com.xebia.xcoss.storage.common;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GenericHibernateDoa<T, K extends Serializable> implements GenericDao<T, K> {

    private static Logger logger = LoggerFactory.getLogger(GenericHibernateDoa.class);
    
    protected Class<T> entityClass;

    @PersistenceUnit
    protected EntityManagerFactory entityManagerFactory;
    
    @PersistenceContext(type=PersistenceContextType.EXTENDED)
    protected EntityManager em;

    @SuppressWarnings("unchecked")
	public GenericHibernateDoa() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<T>) type.getActualTypeArguments()[0];
    }

    @Override
    public void insert(T entity) {
        em.persist(entity);
        em.flush();
    }

    @Override
    public void update(T entity) {
        em.merge(entity);
        em.flush();
    }

    @Override
    public void delete(T entity) {
        entity = em.merge(entity);
        em.remove(entity);
        em.flush();
    }

    @Override
    public T findById(K id) {
        T entity = em.find(entityClass, id);
        return entity;
    }

}
