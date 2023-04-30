package org.javarush.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.javarush.domain.City;

import java.util.List;

public class CityDAO {

    private final SessionFactory sessionFactory;

    public CityDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<City> getItems(int offset, int limit){
        Query<City> cityQuery = sessionFactory.getCurrentSession()
                .createQuery("select c from City c", City.class);
        cityQuery.setFirstResult(offset);
        cityQuery.setMaxResults(limit);
        return cityQuery.list();
    }

    public int getTotalCount(){
        Query<Long> countQuery = sessionFactory.getCurrentSession()
                .createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(countQuery.uniqueResult());
    }

    public City getById(Integer id){
        Query<City> cityQuery = sessionFactory.getCurrentSession()
                                            .createQuery("select c from City c join fetch c.country where c.id = :ID", City.class);
        cityQuery.setParameter("ID", id);
        return cityQuery.getSingleResult();
    }
}
