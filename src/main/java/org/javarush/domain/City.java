package org.javarush.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;

import static org.hibernate.annotations.CascadeType.DELETE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@Data
@Entity
@Table(
        schema = "world",
        name = "city",
        indexes = @Index(columnList = "country_id", name = "city_ibfk_1_idx")
)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 35, nullable = false)
    @ColumnDefault(value = "")
    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    @Cascade({SAVE_UPDATE, DELETE})
    private Country country;

    @Column(name = "district", length = 20, nullable = false)
    @ColumnDefault(value = "")
    private String district;

    @Column(name = "population", nullable = false)
    @ColumnDefault(value = "0")
    private Integer population;

}
