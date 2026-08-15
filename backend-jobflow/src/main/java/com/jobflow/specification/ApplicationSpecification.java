package com.jobflow.specification;

import com.jobflow.entity.Application;
import com.jobflow.entity.ApplicationStatus;
import com.jobflow.entity.Priority;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ApplicationSpecification {

    private ApplicationSpecification() {}

    public static Specification<Application> build(User currentUser, String search, ApplicationStatus status,
                                                     Priority priority, UUID companyId,
                                                     LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("company").get("name"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("jobOffer").get("title"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("applicationDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("applicationDate"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
