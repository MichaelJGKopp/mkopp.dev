package dev.mkopp.mysite.blog.infrastructure.primary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blog")
public class BlogController {

    @GetMapping
    public ResponseEntity<String> getBlogPosts() {
        return ResponseEntity.ok("Blog posts");
    }
}
