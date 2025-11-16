package dev.mkopp.mysite.blog.infrastructure.adapter.out.user;

import dev.mkopp.mysite.blog.application.port.out.UserRepository;
import dev.mkopp.mysite.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class UserModuleAdapter implements UserRepository {
    
    private final UserApi userApi;
    private final UserInfoMapper mapper;
    
    @Override
    public Optional<UserInfo> findById(UUID userId) {
        return userApi.getUserById(userId)
            .map(mapper::toUserInfo);
    }
}
