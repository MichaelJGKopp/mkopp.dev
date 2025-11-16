package dev.mkopp.mysite.shared.api.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity<ID extends Serializable> implements Serializable {
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * Returns the identifier of the entity.
     * Must be implemented by subclasses.
     */
    public abstract ID getId();
    
    /**
     * JPA entity equality based on ID.
     * Two entities are equal if they are of the same type and have the same non-null ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }
    
    /**
     * JPA entity hashCode based on ID.
     * Returns a constant value if ID is null (transient entity),
     * otherwise returns the hashCode of the ID.
     */
    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : 0;
    }
    
    /**
     * Checks if this entity is persisted (has a non-null ID).
     */
    public boolean isPersisted() {
        return getId() != null;
    }
}
