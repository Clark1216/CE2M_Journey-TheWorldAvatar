package com.cmclinnovations.stack.clients.citydb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmclinnovations.stack.clients.docker.ContainerClient;
import com.cmclinnovations.stack.clients.utils.TempDir;

public class CityDBClient extends ContainerClient {

    private static final Logger logger = LoggerFactory.getLogger(CityDBClient.class);

    private static CityDBClient instance = null;

    public static CityDBClient getInstance() {
        if (null == instance) {
            instance = new CityDBClient();
        }
        return instance;
    }

    private CityDBClient() {
    }

    public void uploadCityGMLStringToPostGIS(String fileContents,
            String database, ImpExpOptions options, boolean append) {
        uploadStringToPostGIS("gml", fileContents, database, options, append);
    }

    public void uploadCityJSONStringToPostGIS(String fileContents,
            String database, ImpExpOptions options, boolean append) {
        uploadStringToPostGIS("kml", fileContents, database, options, append);
    }

    private void uploadStringToPostGIS(String fileType, String fileContents,
            String database, ImpExpOptions options, boolean append) {
        try (TempDir tmpDir = makeLocalTempDir()) {
            Path filePath = Files.createTempFile(tmpDir.getPath(), "citydb", "." + fileType);
            try {
                Files.writeString(filePath, fileContents);
                uploadToPostGIS(filePath.toString(), database, options, append);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write string for '" + fileType
                        + "' layer to a file in a temporary directory.", ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create temporary " + fileType + " file", ex);
        }
    }

    public void uploadFilesToPostGIS(String dirPath, String database, ImpExpOptions options, boolean append) {
        try (Stream<Path> paths = Files.list(Path.of(dirPath))) {
            if (paths.filter(path -> Files.isRegularFile(path)
                    && Stream.of(".gz", ".gzip", ".zip")
                            .anyMatch(extension -> path.toString().endsWith(extension)))
                    .map(path -> {
                        uploadFileToPostGIS(path.toString(), database, options, append);
                        return path;
                    })
                    .count() == 0) {
                try (TempDir tmpDir = makeLocalTempDir()) {
                    tmpDir.copyFrom(Path.of(dirPath));
                    uploadToPostGIS(tmpDir.toString(), database, options, append);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to list child paths of the directory '" + dirPath + "'.", ex);
        }
    }

    public void uploadFileToPostGIS(String filePath, String database, ImpExpOptions options, boolean append) {
        try (TempDir tmpDir = makeLocalTempDir()) {
            Path sourcePath = Path.of(filePath);
            tmpDir.copyFrom(sourcePath);

            uploadToPostGIS(tmpDir.getPath().resolve(sourcePath.getFileName()).toString(),
                    database, options, append);
        }
    }

    public void uploadURLToPostGIS(String url, String database, ImpExpOptions options, boolean append) {
        uploadToPostGIS(url, database, options, append);
    }

    private void uploadToPostGIS(String filePath, String database, ImpExpOptions options, boolean append) {

        String containerId = getContainerId("citydbimpexp");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        String execId = createComplexCommand(containerId, options.appendArgs(filePath, "--db-name", database))
                .withOutputStream(outputStream)
                .withErrorStream(errorStream)
                .withEvaluationTimeout(600)
                .exec();

        handleErrors(errorStream, execId, logger);
    }

}
