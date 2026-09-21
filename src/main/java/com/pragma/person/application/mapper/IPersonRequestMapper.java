package com.pragma.person.application.mapper;

import com.pragma.person.application.dto.request.PersonRequestDto;
import com.pragma.person.domain.model.PersonModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IPersonRequestMapper {
    PersonModel toPerson(PersonRequestDto personRequestDto);
}
