package com.uam.psychoform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ModuleStructureTest {
    private static final Path MAIN_JAVA = Path.of("src", "main", "java", "com", "uam", "psychoform");
    private static final List<String> MODULES = List.of("academic", "assessment", "audit", "instrument",
            "reporting", "scoring", "security");
    private static final List<String> MODULE_FOLDERS = List.of("model", "repository", "service", "controller", "dto");
    private static final Pattern LEGACY_PACKAGE_REFERENCE = Pattern
            .compile("com\\.uam\\.psychoform\\.[\\w.]+\\.(entity|web)(\\.|;)");

    @Test
    void modulesUseTheExpectedLayerFolders() {
        for (String module : MODULES) {
            Path modulePath = MAIN_JAVA.resolve(module);
            for (String folder : MODULE_FOLDERS) {
                assertThat(modulePath.resolve(folder)).as(module + "/" + folder).isDirectory();
            }
            assertThat(modulePath.resolve("entity")).as(module + "/entity").doesNotExist();
            assertThat(modulePath.resolve("web")).as(module + "/web").doesNotExist();
        }
    }

    @Test
    void sourcePackagesDoNotReferenceLegacyEntityOrWebPackages() throws IOException {
        try (Stream<Path> sources = Files.walk(MAIN_JAVA)) {
            assertThat(sources.filter(path -> path.toString().endsWith(".java"))
                    .map(this::read)
                    .filter(source -> LEGACY_PACKAGE_REFERENCE.matcher(source).find())
                    .toList()).isEmpty();
        }
    }

    @Test
    void controllersDoNotDeclareInlineDtoRecords() throws IOException {
        try (Stream<Path> sources = Files.walk(MAIN_JAVA)) {
            assertThat(sources.filter(path -> path.toString().contains("\\controller\\") && path.toString().endsWith(".java"))
                    .map(this::read)
                    .filter(source -> source.contains(" record "))
                    .toList()).isEmpty();
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }
}
