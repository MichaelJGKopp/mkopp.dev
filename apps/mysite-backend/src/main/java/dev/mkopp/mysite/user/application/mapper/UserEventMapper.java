package dev.mkopp.mysite.user.application.mapper;

import dev.mkopp.mysite.user.api.event.UserCreatedEvent;
import dev.mkopp.mysite.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserEventMapper {
    
    UserCreatedEvent toCreatedEvent(User user);
}
