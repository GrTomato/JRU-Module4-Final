package org.javarush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.javarush.dao.CityDAO;
import org.javarush.dao.CountryDao;
import org.javarush.domain.City;
import org.javarush.domain.Country;
import org.javarush.domain.CountryLanguage;
import org.javarush.redis.CityCountry;
import org.javarush.redis.Language;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;

    private final ObjectMapper mapper;

    private final CityDAO cityDAO;
    private final CountryDao countryDao;

    public Main(){
        sessionFactory = prepareRelationalDb();
        cityDAO = new CityDAO(sessionFactory);
        countryDao = new CountryDao(sessionFactory);

        redisClient = prepareRedisClient();
        mapper = new ObjectMapper();
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            System.out.println("Connected");
        }
        return redisClient;
    }

    public static void main(String[] args) {
        Main main = new Main();
        List<City> cities = main.fetchData(main);
        List<CityCountry> preparedData = main.transformData(cities);
        main.pushToRedis(preparedData);

        main.sessionFactory.getCurrentSession().close();

        List<Integer> idsToSelect = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        main.testDataRedis(idsToSelect);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.getMySqlData(idsToSelect);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }

    private void getMySqlData(List<Integer> ids){
        try(Session session = sessionFactory.getCurrentSession()){
            session.beginTransaction();
            for (Integer id: ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }

    private void testDataRedis(List<Integer> idList){
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisCommands<String, String> sync = connection.sync();
            for (Integer id: idList) {
                String record = sync.get(String.valueOf(id));
                try{
                    mapper.readValue(record, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void pushToRedis(List<CityCountry> data) {
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()){
            RedisCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry: data) {
                try{
                    sync.set(
                            String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry)
                    );
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry cityCountry = new CityCountry();
            cityCountry.setId(city.getId());
            cityCountry.setName(city.getName());
            cityCountry.setPopulation(city.getPopulation());
            cityCountry.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            cityCountry.setAlternativeCode(country.getAlternativeCode());
            cityCountry.setContinent(country.getContinent());
            cityCountry.setCountryCode(country.getCode());
            cityCountry.setCountryName(country.getName());
            cityCountry.setCountryPopulation(country.getPopulation());
            cityCountry.setCountryRegion(country.getRegion());
            cityCountry.setCountrySurfaceArea(country.getSurfaceArea());

            Set<CountryLanguage> languages = country.getLanguages();
            Set<Language> collectedLanguages = languages.stream().map(l -> {
                Language language = new Language();
                language.setLanguage(l.getLanguage());
                language.setIsOfficial(l.getIsOfficial());
                language.setPercentage(l.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            cityCountry.setLanguages(collectedLanguages);

            return cityCountry;
        }).collect(Collectors.toList());
    }

    private SessionFactory prepareRelationalDb() {
        final SessionFactory sessionFactory;
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/world");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.STATEMENT_BATCH_SIZE, "100");

        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
        return sessionFactory;
    }

    private void shutdown() {
        if (Objects.nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (Objects.nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<City> fetchData(Main main){
        try(Session session = main.sessionFactory.getCurrentSession()){
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();

            List<Country> countryList = main.countryDao.getAll();

            int totalCount = main.cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i+=step) {
                allCities.addAll(main.cityDAO.getItems(i, step));
            }

            session.getTransaction().commit();
            return allCities;
        }
    }
}