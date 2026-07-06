package com.deutschlingodeck.dictionary.repository;

import com.deutschlingodeck.dictionary.entity.Dictionary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

	Page<Dictionary> findByOwnerIdAndDeletedFalse(Long ownerId, Pageable pageable);
}
