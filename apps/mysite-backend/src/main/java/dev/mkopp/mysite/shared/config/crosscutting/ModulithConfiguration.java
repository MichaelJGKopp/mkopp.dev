package dev.mkopp.mysite.shared.config.crosscutting;

import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.Modulithic;

@Configuration
@Modulithic(sharedModules = "shared")
public class ModulithConfiguration {
}