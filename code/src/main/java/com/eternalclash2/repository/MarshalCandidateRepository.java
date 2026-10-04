package com.eternalclash2.repository;

import com.eternalclash2.domain.entity.MarshalCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarshalCandidateRepository extends JpaRepository<MarshalCandidate, Long> {
}


