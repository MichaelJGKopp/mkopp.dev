package dev.mkopp.mysite.user.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.user.application.port.out.UserRepository;
import dev.mkopp.mysite.user.domain.model.User;
import dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;
    
    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
