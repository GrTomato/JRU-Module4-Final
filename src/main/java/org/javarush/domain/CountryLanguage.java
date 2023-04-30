package org.javarush.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

import static org.hibernate.annotations.CascadeType.DELETE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@Data
@Entity
@Table(
        schema = "world",
        name = "country_language",
        indexes = @Index(columnList = "country_id", name = "country_language_ibfk_1_idx")
)
public class CountryLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    @Cascade({SAVE_UPDATE, DELETE})
    private Country country;

    @Column(name = "language", length = 30, nullable = false)
    @ColumnDefault(value = "")
    private String language;

    @Column(name = "is_official", columnDefinition = "BIT", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    @ColumnDefault(value = "False")
    private Boolean isOfficial;

    @Column(name = "percentage", nullable = false)
    @ColumnDefault(value = "0.0")
    private BigDecimal percentage;



}
