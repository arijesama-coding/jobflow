package com.jobflow.specification;

import com.jobflow.entity.Interview;
import com.jobflow.entity.InterviewResult;
import com.jobflow.entity.InterviewType;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InterviewSpecification {

    private InterviewSpecification() {}

    public static Specification<Interview> build(User currentUser, UUID applicationId, InterviewType type,
                                                   InterviewResult result, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("application").get("user").get("id"), currentUser.getId()));

            if (applicationId != null) {
                predicates.add(cb.equal(root.get("application").get("id"), applicationId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
