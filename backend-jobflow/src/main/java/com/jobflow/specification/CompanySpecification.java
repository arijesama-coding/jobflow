package com.jobflow.specification;

import com.jobflow.entity.Company;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CompanySpecification {

    private CompanySpecification() {}

    public static Specification<Company> forUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Company> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Company> search(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(cb.coalesce(root.get("industry"), "")), like),
                cb.like(cb.lower(cb.coalesce(root.get("location"), "")), like)
        );
    }

    public static Specification<Company> industry(String industry) {
        if (industry == null || industry.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("industry")), industry.toLowerCase());
    }

    public static Specification<Company> favorite(Boolean favorite) {
        if (favorite == null) return null;
        return (root, query, cb) -> cb.equal(root.get("favorite"), favorite);
    }

    public static Specification<Company> build(User currentUser, String search, String industry, Boolean favorite) {
        List<Specification<Company>> specs = new ArrayList<>();
        specs.add(forUser(currentUser.getId()));
        specs.add(notDeleted());
        if (search(search) != null) specs.add(search(search));
        if (industry(industry) != null) specs.add(industry(industry));
        if (favorite(favorite) != null) specs.add(favorite(favorite));

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Specification<Company> spec : specs) {
                predicates.add(spec.toPredicate(root, query, cb));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
