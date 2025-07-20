package com.qa.atlibs.core.manager;

import com.qa.atlibs.core.exception.CoreTestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataManager {
    private static final String TEST_DATA_DIRECTORY = EnvironmentManager.getEnvironmentVariables().testDataDir();
    private static final String FILE_DOES_NOT_EXIST_ERROR = "File %s does not exist!";
    private static Map<String, String> testDataFileNames = new HashMap<>();

    static {
        setupTestDataFileNames();
    }

    public static void setupTestDataFileNames() {
        Path testDataDirectoryClassLoader = new File(Objects.requireNonNull(
                        TestDataManager.class.getClassLoader().getResource(TEST_DATA_DIRECTORY))
                .getFile()).toPath();
        try (final Stream<Path> walk = Files.walk(testDataDirectoryClassLoader)) {
            testDataFileNames = walk.filter(Predicate.not(Files::isDirectory))
                    .map(testDataDirectoryClassLoader::relativize)
                    .collect(Collectors.toMap(path -> path.getFileName().toString(), Path::toString,
                            (file, duplicate) -> {
                                throw new CoreTestException(
                                        "\nFile \"" + file + "\" has the same name as a file \"" + duplicate + "\"");
                            }));
        } catch (IOException e) {
            throw new CoreTestException("Error during scanning path " + testDataDirectoryClassLoader, e);
        }
    }

    public static String getTestFileData(String filePath) {
        String relativePath = getRelativePathToFile(filePath);
        try (InputStream fileInputStream = Optional.ofNullable(TestDataManager.class.getClassLoader().getResourceAsStream(
                        TEST_DATA_DIRECTORY + File.separator + relativePath))
                .orElseThrow(() -> new CoreTestException(String.format(FILE_DOES_NOT_EXIST_ERROR, filePath)))) {
            return IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CoreTestException(e);
        }
    }

    private static String getRelativePathToFile(String filePath) {
        if (MapUtils.isNotEmpty(testDataFileNames)) {
            String relativePath = testDataFileNames.get(filePath);
            if (Objects.isNull(relativePath)) {
                throw new CoreTestException(String.format(FILE_DOES_NOT_EXIST_ERROR, filePath));
            }
            return relativePath;
        }
        return filePath;
    }

}
