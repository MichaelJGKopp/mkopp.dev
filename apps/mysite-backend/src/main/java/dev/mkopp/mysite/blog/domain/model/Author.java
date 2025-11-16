package dev.mkopp.mysite.blog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.UUID;

@ValueObject
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Author {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
