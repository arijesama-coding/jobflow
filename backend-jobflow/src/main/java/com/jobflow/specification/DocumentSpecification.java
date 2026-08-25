package com.jobflow.specification;

import com.jobflow.entity.Document;
import com.jobflow.entity.DocumentType;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DocumentSpecification {

    private DocumentSpecification() {}

    public static Specification<Document> build(User currentUser, UUID applicationId, DocumentType type) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            if (applicationId != null) predicates.add(cb.equal(root.get("application").get("id"), applicationId));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
