package com.argus.foodobserverbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "day")
//TODO: add notes field
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

    @Column(name = "notes")
    private String notes;
    @OneToMany(mappedBy = "creationDay")
    private List<FoodRecord> foodRecords;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private BotUser creator;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Day day = (Day) o;
        return getId() != null && Objects.equals(getId(), day.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
