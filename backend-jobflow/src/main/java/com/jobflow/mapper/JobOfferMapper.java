package com.jobflow.mapper;

import com.jobflow.dto.response.JobOfferResponse;
import com.jobflow.entity.JobOffer;
import com.jobflow.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface JobOfferMapper {

    @Mapping(target = "companyId", expression = "java(offer.getCompany() != null ? offer.getCompany().getId() : null)")
    @Mapping(target = "companyName", expression = "java(offer.getCompany() != null ? offer.getCompany().getName() : null)")
    @Mapping(target = "skills", expression = "java(mapSkillNames(offer.getSkills()))")
    @Mapping(target = "deadlinePassed", expression = "java(offer.isDeadlinePassed())")
    JobOfferResponse toResponse(JobOffer offer);

    default Set<String> mapSkillNames(Set<Skill> skills) {
        return skills.stream().map(Skill::getName).collect(Collectors.toSet());
    }
}
