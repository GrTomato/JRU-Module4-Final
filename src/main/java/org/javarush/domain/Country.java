package org.javarush.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Entity
@Table(
        schema = "world",
        name = "country",
        indexes = @Index(columnList = "capital", name = "country_ibfk_1_idx")
)
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, length = 3)
    @ColumnDefault(value = "")
    private String code;

    @Column(name = "code_2", nullable = false, length = 2)
    @ColumnDefault(value = "")
    private String code2;

    @Column(name = "name", nullable = false, length = 52)
    @ColumnDefault(value = "")
    private String name;

    @Column(name = "continent", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @ColumnDefault(value = "0")
    private Continent continent;

    @Column(name = "region", nullable = false, length = 26)
    @ColumnDefault(value = "")
    private String region;

    @Column(name = "surface_area", nullable = false)
    @ColumnDefault(value = "0.0")
    private BigDecimal surfaceArea;

    @Column(name = "indep_year")
    private Short independenceYear;

    @Column(name = "population", nullable = false)
    @ColumnDefault(value = "0")
    private Integer population;

    @Column(name = "life_expectancy")
    private BigDecimal lifeExpectancy;

    @Column(name = "gnp")
    private BigDecimal GNP;

    @Column(name = "gnpo_id")
    private BigDecimal GNPOId;

    @Column(name = "local_name", length = 45, nullable = false)
    @ColumnDefault(value = "")
    private String localName;

    @Column(name = "government_form", length = 45, nullable = false)
    @ColumnDefault(value = "")
    private String governmentForm;

    @Column(name = "head_of_state", length = 60)
    private String headOfState;

    @OneToOne
    @JoinColumn(name = "capital")
    private City city;

    @OneToMany
    @JoinColumn(name = "country_id")
    private Set<CountryLanguage> languages;









}
