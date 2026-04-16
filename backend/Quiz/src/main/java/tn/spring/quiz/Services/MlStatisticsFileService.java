package tn.spring.quiz.Services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Resolves {@code ml model/output/...} and lists or reads PNG statistics for the admin UI.
 */
@Service
@Slf4j
public class MlStatisticsFileService {

    private static final Map<String, Path> ML_STATISTICS_DIRS = Map.of(
            "dataset", Paths.get("student_engagement_dataset", "statistics"),
            "facial", Paths.get("student_engagement_with_facial_data", "statistics")
    );

    @Value("${app.ml-output-base:ml model/output}")
    private String mlOutputBaseProperty;

    private Path mlOutputBase;

    @PostConstruct
    void initMlOutputBase() {
        this.mlOutputBase = resolveMlOutputBase(mlOutputBaseProperty);
    }

    public Map<String, Object> listPngDescriptors(String dataset) {
        Optional<Path> statisticsDir = resolveStatisticsDirIfPresent(dataset);
        Path directoryForResponse = statisticsDir.orElseGet(() -> expectedStatisticsDir(dataset));

        List<Map<String, Object>> files = new ArrayList<>();
        if (statisticsDir.isPresent()) {
            Path dir = statisticsDir.get();
            try (Stream<Path> stream = Files.list(dir)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(path -> isAllowedPngFile(path.getFileName().toString()))
                        .sorted(Comparator.comparing(this::safeLastModified).reversed())
                        .forEach(path -> files.add(toFileDescriptor(dir, path)));
            } catch (IOException exception) {
                log.warn("Unable to list ML statistics directory {}: {}", dir, exception.getMessage());
                Map<String, Object> response = new HashMap<>();
                response.put("dataset", dataset);
                response.put("directory", directoryForResponse.toString());
                response.put("files", List.of());
                response.put("warning", "Could not read the statistics folder. Check permissions or disk.");
                return response;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("dataset", dataset);
        response.put("directory", directoryForResponse.toString());
        response.put("files", files);
        return response;
    }

    public Optional<byte[]> readPngFile(String dataset, String file) {
        if (file == null || file.isBlank()) {
            return Optional.empty();
        }
        if (file.contains("..") || file.contains("/") || file.contains("\\")) {
            return Optional.empty();
        }
        if (!isAllowedPngFile(file)) {
            return Optional.empty();
        }
        Optional<Path> statisticsDirOpt = resolveStatisticsDirIfPresent(dataset);
        if (statisticsDirOpt.isEmpty()) {
            return Optional.empty();
        }
        Path statisticsDir = statisticsDirOpt.get();
        Path requestedFile = statisticsDir.resolve(file).normalize();
        if (!isSubPath(statisticsDir, requestedFile)) {
            return Optional.empty();
        }
        if (!Files.exists(requestedFile) || !Files.isRegularFile(requestedFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(requestedFile));
        } catch (IOException exception) {
            log.warn("Unable to read ML chart {}: {}", requestedFile, exception.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Path> resolveStatisticsDirIfPresent(String dataset) {
        Path relative = ML_STATISTICS_DIRS.getOrDefault(
                String.valueOf(dataset).trim().toLowerCase(),
                ML_STATISTICS_DIRS.get("dataset")
        );
        Path base = mlOutputBase != null ? mlOutputBase : resolveMlOutputBase(mlOutputBaseProperty);
        Path baseAbs = base.toAbsolutePath().normalize();
        Path resolved = baseAbs.resolve(relative).normalize();
        if (!isSubPath(baseAbs, resolved)) {
            return Optional.empty();
        }
        if (Files.isDirectory(resolved)) {
            return Optional.of(resolved);
        }
        return Optional.empty();
    }

    private Path expectedStatisticsDir(String dataset) {
        Path relative = ML_STATISTICS_DIRS.getOrDefault(
                String.valueOf(dataset).trim().toLowerCase(),
                ML_STATISTICS_DIRS.get("dataset")
        );
        Path base = mlOutputBase != null ? mlOutputBase : resolveMlOutputBase(mlOutputBaseProperty);
        return base.toAbsolutePath().normalize().resolve(relative).normalize();
    }

    private static boolean isSubPath(Path parent, Path child) {
        Path p = parent.toAbsolutePath().normalize();
        Path c = child.toAbsolutePath().normalize();
        return c.startsWith(p);
    }

    private static Path resolveMlOutputBase(String configured) {
        String trimmed = String.valueOf(configured).trim();
        if (trimmed.isEmpty()) {
            trimmed = "ml model/output";
        }
        Path relative = Paths.get(trimmed);
        if (relative.isAbsolute()) {
            return relative.normalize();
        }
        Path fromWalk = walkParentsForMlRoot(relative);
        if (Files.isDirectory(fromWalk)) {
            return fromWalk;
        }
        Path fromCodeSource = mlRootNearCodeSource(relative);
        if (fromCodeSource != null && Files.isDirectory(fromCodeSource)) {
            return fromCodeSource;
        }
        return fromWalk;
    }

    private static Path walkParentsForMlRoot(Path relative) {
        Path start = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path dir = start;
        for (int depth = 0; depth < 18 && dir != null; depth++) {
            Path candidate = dir.resolve(relative).normalize();
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return start.resolve(relative).normalize();
    }

    private static Path mlRootNearCodeSource(Path relative) {
        try {
            var source = MlStatisticsFileService.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return null;
            }
            Path codePath = Paths.get(source.getLocation().toURI());
            Path moduleRoot;
            if (Files.isDirectory(codePath)) {
                Path targetDir = codePath.getParent();
                moduleRoot = targetDir != null ? targetDir.getParent() : null;
            } else {
                Path targetDir = codePath.getParent();
                moduleRoot = targetDir != null ? targetDir.getParent() : null;
            }
            if (moduleRoot == null) {
                return null;
            }
            return moduleRoot.resolve(relative).normalize();
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isAllowedPngFile(String fileName) {
        String lower = String.valueOf(fileName).trim().toLowerCase();
        return lower.endsWith(".png");
    }

    private FileTime safeLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException exception) {
            return FileTime.fromMillis(0);
        }
    }

    private Map<String, Object> toFileDescriptor(Path baseDir, Path filePath) {
        Map<String, Object> descriptor = new HashMap<>();
        descriptor.put("name", filePath.getFileName().toString());
        descriptor.put("size", safeSize(filePath));
        descriptor.put("lastModified", safeLastModified(filePath).toString());
        descriptor.put("relativePath", baseDir.relativize(filePath).toString());
        return descriptor;
    }

    private long safeSize(Path filePath) {
        try {
            return Files.size(filePath);
        } catch (IOException exception) {
            return 0;
        }
    }
}
