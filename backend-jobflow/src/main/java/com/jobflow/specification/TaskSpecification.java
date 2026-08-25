package com.jobflow.specification;

import com.jobflow.entity.Priority;
import com.jobflow.entity.Task;
import com.jobflow.entity.TaskStatus;
import com.jobflow.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TaskSpecification {

    private TaskSpecification() {}

    public static Specification<Task> build(User currentUser, UUID applicationId, TaskStatus status,
                                              Priority priority, LocalDate dueFrom, LocalDate dueTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));

            if (applicationId != null) predicates.add(cb.equal(root.get("application").get("id"), applicationId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (dueFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueFrom));
            if (dueTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
