package com.jobflow.specification;

import com.jobflow.entity.ContractType;
import com.jobflow.entity.JobOffer;
import com.jobflow.entity.RemoteType;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JobOfferSpecification {

    private JobOfferSpecification() {}

    public static Specification<JobOffer> build(User currentUser, String search, UUID companyId,
                                                  RemoteType remoteType, ContractType contractType,
                                                  Boolean favorite, Boolean archived) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("location"), "")), like)
                ));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }
            if (remoteType != null) {
                predicates.add(cb.equal(root.get("remoteType"), remoteType));
            }
            if (contractType != null) {
                predicates.add(cb.equal(root.get("contractType"), contractType));
            }
            if (favorite != null) {
                predicates.add(cb.equal(root.get("favorite"), favorite));
            }
            // Default to hiding archived offers unless explicitly requested.
            predicates.add(cb.equal(root.get("archived"), archived != null && archived));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
