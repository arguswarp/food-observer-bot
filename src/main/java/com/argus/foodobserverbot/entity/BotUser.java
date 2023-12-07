package com.argus.foodobserverbot.entity;

import com.argus.foodobserverbot.entity.enums.UserState;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bot_user")
public class BotUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "user_state")
    @Enumerated(EnumType.STRING)
    private UserState userState;
    @Column(name = "telegram_id")
    private Long telegramId;
    @OneToMany(mappedBy = "creator")
    private List<Day> days;
    @Column(name = "today_mode")
    private Boolean todayMode = true;
}
