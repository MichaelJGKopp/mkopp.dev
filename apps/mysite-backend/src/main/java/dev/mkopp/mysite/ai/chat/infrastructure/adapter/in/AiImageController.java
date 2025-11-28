package dev.mkopp.mysite.ai.chat.infrastructure.adapter.in;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/ai/image")
@RequiredArgsConstructor
public class AiImageController {
    // private final AiImageDetectionService aiImageDetectionService;

    private final Map<String, ChatClient> chatClients;
    // private final OpenAiImageModel imageModel;

    @Value("classpath:images/linkedin-banner.png")
    private Resource imageResource;

    @GetMapping("/to-text")
    public ResponseEntity<String> convertImageToText(
            @RequestParam(defaultValue = "Can you please describe what you see in the following image?") String prompt,
            @RequestParam(defaultValue = "geminiChatClient") String clientBean) throws IOException {

        ChatClient chatClient = getChatClient(clientBean);
        Path imagePath = imageResource.getFile().toPath();

        String result = chatClient.prompt()
                .user(u -> {
                    u.text(prompt);
                    u.media(detectMimeType(imagePath), imageResource);
                })
                .call()
                .content();
        return ResponseEntity.ok(result);
    }

    // @GetMapping("/from-text")
    // public ResponseEntity<Map<String, String>> convertTextToImage(
    //         @RequestParam(defaultValue = "A beautiful sunset over mountains") String prompt,
    //         @RequestParam(defaultValue = "geminiChatClient") String clientBean) {

    //     ImageOptions imageOptions = OpenAiImageOptions.builder()
    //             .model("dall-e-3")
    //             .width(1024)
    //             .height(1024)
    //             .quality("hd")
    //             .style("vivid") // or natural
    //             .build();

    //     ImagePrompt imagePrompt = new ImagePrompt(prompt, imageOptions);

    //     ImageResponse imageResponse = imageModel.call(imagePrompt);

    //     String url = imageResponse.getResult().getOutput().getUrl();

    //     return ResponseEntity.ok(Map.of("prompt", prompt,
    //             "imageUrl", url));
    // }

    private ChatClient getChatClient(String clientBean) {
        ChatClient chatClient = chatClients.get(clientBean);
        if (chatClient == null) {
            throw new IllegalArgumentException("No ChatClient bean found with name: " + clientBean);
        }
        return chatClient;
    }

    private MimeType detectMimeType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".png"))
            return MimeTypeUtils.IMAGE_PNG;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            return MimeTypeUtils.IMAGE_JPEG;
        if (filename.endsWith(".gif"))
            return MimeTypeUtils.IMAGE_GIF;
        throw new IllegalArgumentException("Unsupported image format: " + filename);
    }

}