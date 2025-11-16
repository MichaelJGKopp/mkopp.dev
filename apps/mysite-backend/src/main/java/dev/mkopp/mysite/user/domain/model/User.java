package dev.mkopp.mysite.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.UUID;

@AggregateRoot
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Identity
    private UUID id;
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    
    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
