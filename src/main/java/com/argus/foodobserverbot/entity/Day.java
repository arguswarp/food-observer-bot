package com.argus.foodobserverbot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "day")
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "bloody_rating")
    private Integer bloodyRating;

    @Column(name = "pimple_face_rating")
    private Integer pimpleFaceRating;

    @Column(name = "pimple_booty_rating")
    private Integer pimpleBootyRating;
    @OneToMany(mappedBy = "creationDay")
    private List<FoodRecord> foodRecords;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private BotUser creator;


}
