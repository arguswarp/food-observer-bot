package com.argus.foodobserverbot.repository;

import com.argus.foodobserverbot.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotUserRepository extends JpaRepository<BotUser, Long> {
    BotUser findBotUserByTelegramId(Long telegramId);
}
