package dev.mkopp.mysite.blog.application.port.out;

import java.util.Set;

public interface TagRepository {
    Set<String> findOrCreateTags(Set<String> tagNames);
}
