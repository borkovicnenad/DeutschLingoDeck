package com.deutschlingodeck.dictionary.repository;

import com.deutschlingodeck.dictionary.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
