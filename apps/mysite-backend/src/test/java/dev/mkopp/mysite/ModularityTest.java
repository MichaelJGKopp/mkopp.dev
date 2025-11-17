package dev.mkopp.mysite;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {
    
    ApplicationModules modules = ApplicationModules.of(MysiteBackendApplication.class);
    
    @Test
    void verifyModularity() {
        // Verifies module boundaries are respected
        modules.verify();
    }
    
    @Test
    void generateModuleDocumentation() {
        // Generates architecture documentation
        new Documenter(modules)
            .writeDocumentation()
            .writeModulesAsPlantUml();
    }
}
