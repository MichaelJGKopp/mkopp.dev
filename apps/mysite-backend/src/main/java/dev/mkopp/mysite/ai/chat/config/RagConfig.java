package dev.mkopp.mysite.ai.chat.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RagConfig {

    @Value("vectorstore.json")
    private String vectorStoreName;

    @Value("classpath:/data/README.md")
    private Resource models;

    // @Bean
    SimpleVectorStore simpleVectorStore(@Qualifier("googleGenAiTextEmbedding") EmbeddingModel embeddingModel) throws IOException {
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        var vectorStoreFile = getVectorStoreFile();
        if (vectorStoreFile.exists()) {
            log.info("Vector Store File Exists,");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector Store File Does Not Exist, loading documents");
            TextReader textReader = new TextReader(models);
            textReader.getCustomMetadata().put("filename", "README.md");
            List<Document> documents = textReader.get();
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
        Path dataDir = Paths.get("data"); // relative to backend working dir
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir); // create folder if missing
            } catch (IOException e) {
                throw new RuntimeException("Failed to create data directory", e);
            }
        }
        return dataDir.resolve("vectorstore.json").toFile();
    }
}
