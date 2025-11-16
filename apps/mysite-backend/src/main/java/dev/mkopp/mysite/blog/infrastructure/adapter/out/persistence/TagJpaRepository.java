package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence;

import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface TagJpaRepository extends JpaRepository<TagEntity, UUID> {
    Optional<TagEntity> findByName(String name);
}
