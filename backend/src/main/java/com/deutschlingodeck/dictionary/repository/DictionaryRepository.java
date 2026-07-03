package com.deutschlingodeck.dictionary.repository;

import com.deutschlingodeck.dictionary.entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {
}
