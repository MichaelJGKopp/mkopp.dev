package dev.mkopp.mysite.ai.chat.infrastructure.adapter.out;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTools {

    @Tool(name = "getCurrentDateTime", description = "Get the current date and time in the user's timezone")
    // @ToolParam("parameter text description") if there are parameters
    // Ideas: 
    // - Tool for AI Comments
    // - retrieving current DB Tags for BlogPosts
    // - creating/assigning issues
    // - get weather -> running date recommendations, send by email
    // - crawler for spring ai
    // - spring documentation search tool, vector store with spring docs, add all docs I care about, context7 contains this?
    //   tool that regularly updates the vector store with new docs
    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}