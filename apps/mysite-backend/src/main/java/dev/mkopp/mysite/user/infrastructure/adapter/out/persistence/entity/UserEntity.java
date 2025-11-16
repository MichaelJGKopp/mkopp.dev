package dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.shared.api.persistence.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users", schema = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserEntity extends AbstractAuditingEntity<UUID> {
    
    @Id
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Override
    public UUID getId() {
        return id;
    }
}
