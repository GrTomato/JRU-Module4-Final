package org.javarush.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.javarush.domain.Country;

import java.util.List;

public class CountryDao {

    private final SessionFactory sessionFactory;

    public CountryDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Country> getAll(){
        Query<Country> countryQuery = sessionFactory.getCurrentSession()
                .createQuery("select c from Country c join fetch c.languages", Country.class);
        return countryQuery.list();
    }
}
