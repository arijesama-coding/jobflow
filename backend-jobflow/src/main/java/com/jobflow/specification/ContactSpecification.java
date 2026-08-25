package com.jobflow.specification;

import com.jobflow.entity.Contact;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ContactSpecification {

    private ContactSpecification() {}

    public static Specification<Contact> build(User currentUser, String search, UUID companyId, UUID applicationId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("email"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("position"), "")), like)
                ));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }
            if (applicationId != null && query != null) {
                query.distinct(true);
                predicates.add(cb.equal(root.join("applications").get("id"), applicationId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
