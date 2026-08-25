package com.jobflow.specification;

import com.jobflow.entity.FollowUp;
import com.jobflow.entity.FollowUpStatus;
import com.jobflow.entity.FollowUpType;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FollowUpSpecification {

    private FollowUpSpecification() {}

    public static Specification<FollowUp> build(User currentUser, UUID applicationId, FollowUpType type,
                                                  FollowUpStatus status, LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("application").get("user").get("id"), currentUser.getId()));

            if (applicationId != null) predicates.add(cb.equal(root.get("application").get("id"), applicationId));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("followUpDate"), dateFrom));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("followUpDate"), dateTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
