package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.Marshal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarshalRepository extends JpaRepository<Marshal, Long> {
}


