package dev.mkopp.mysite.user.application.service;

import dev.mkopp.mysite.user.api.UserApi;
import dev.mkopp.mysite.user.api.dto.UserDto;
import dev.mkopp.mysite.user.application.mapper.UserDtoMapper;
import dev.mkopp.mysite.user.application.mapper.UserEventMapper;
import dev.mkopp.mysite.user.application.port.in.FindOrCreateUserUseCase;
import dev.mkopp.mysite.user.application.port.in.GetUserUseCase;
import dev.mkopp.mysite.user.application.port.out.UserRepository;
import dev.mkopp.mysite.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
class UserService implements FindOrCreateUserUseCase, GetUserUseCase, UserApi {
    
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDtoMapper userDtoMapper;
    private final UserEventMapper userEventMapper;
    
    @Override
    public User execute(UUID keycloakId, String username, String email, String firstName, String lastName) {
        return userRepository.findById(keycloakId)
            .orElseGet(() -> {
                log.info("Creating new user from Keycloak JWT: {}", username);
                
                User newUser = User.builder()
                    .id(keycloakId)
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();
                
                User savedUser = userRepository.save(newUser);
                
                eventPublisher.publishEvent(userEventMapper.toCreatedEvent(savedUser));
                
                return savedUser;
            });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(UUID userId) {
        return userRepository.findById(userId).map(userDtoMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userDtoMapper::toDto);
    }
    
    @Override
    public UserDto findOrCreateUser(UUID keycloakId, String username, String email, String firstName, String lastName) {
        User user = execute(keycloakId, username, email, firstName, lastName);
        return userDtoMapper.toDto(user);
    }
}
