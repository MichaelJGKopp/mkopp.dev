package dev.mkopp.mysite.blog.application.port.in;

import java.util.UUID;

public interface DeleteBlogPostUseCase {
    void execute(UUID id);
}
