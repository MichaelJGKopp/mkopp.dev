package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.application.port.out.TagRepository;
import dev.mkopp.mysite.blog.infrastructure.entity.TagEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class TagRepositoryAdapter implements TagRepository {
    
    private final TagJpaRepository jpaRepository;
    
    @Override
    public Set<String> findOrCreateTags(Set<String> tagNames) {
        return tagNames.stream()
            .map(name -> jpaRepository.findByName(name)
                .orElseGet(() -> jpaRepository.save(TagEntity.builder().name(name).build())))
            .map(TagEntity::getName)
            .collect(Collectors.toSet());
    }
}
