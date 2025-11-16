package dev.mkopp.mysite.shared.persistence.jpa;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/* 
 * Base class for entities to provide auditing fields, hashcode and equals.
 * T is the type of the entity's identifier.
 * Identity, hashCode, and equals are based on the identifier only.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AbstractAuditingEntity<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract T getId();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AbstractAuditingEntity<?> that = (AbstractAuditingEntity<?>) o;

    return getId() != null && getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : System.identityHashCode(this);
  }
}
