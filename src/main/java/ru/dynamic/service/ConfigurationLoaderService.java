package ru.dynamic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.dynamic.utils.CommonUtils;
import ru.dynamic.dto.DataDescription;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ConfigurationLoaderService extends AbstractConfigurationLoaderService {

    private static final String defaultContentPath = "/content";
    private static final String defaultSqlPath = "/sql";
    private Map<String, DataDescription> descriptions = new HashMap<>();

    private Map<String, String> queries = new HashMap<>();

    @Autowired
    private CommonUtils commonUtils;

    @PostConstruct
    public void initialize() throws IOException, URISyntaxException {
        findConfigurationInJar();
    }

    public DataDescription get(String dataSourceId) {
        return descriptions.get(dataSourceId);
    }


    @Override
    protected void loadJsonDataConfiguration() throws IOException, URISyntaxException {
        URL uriContent = getUri(defaultContentPath);
        loadDataConfiguration((Path path) -> path.toString().endsWith(".json"),
                (String stringPath) -> {
                    DataDescription description = commonUtils.fromJSON(stringPath, queries, DataDescription.class);
                    descriptions.put(description.getId(), description);
                }, uriContent, "content");
    }

    @Override
    protected void loadSqlQueryDataConfiguration() throws IOException, URISyntaxException {
        URL uriSql = getUri(defaultSqlPath);
        loadDataConfiguration((Path path) -> path.toString().endsWith(".sql"),
                (String stringPath) -> {
                    Path path = Paths.get(stringPath);
                    String fileName = path.getFileName().toString();
                    String sqlQuery = getSqlQuery(stringPath);
                    queries.put(fileName, sqlQuery);
                }, uriSql, "sql");
    }

    private URL getUri(String path) {
        return ConfigurationLoaderService.class.getResource(path);
    }

    private String getSqlQuery(String stringPath) {
        InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(stringPath), StandardCharsets.UTF_8);
        return new BufferedReader(reader)
                .lines()
                .collect(Collectors.joining("\n"));
    }

    private void loadDataConfiguration(Predicate<Path> predicate, Consumer<String> consumer, URL uriContent, String fsPath) throws IOException, URISyntaxException {

        Path myPath = getPath(uriContent, fsPath);
        Stream<Path> walk = Files.walk(myPath, 1);
        try {
            for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                Path path = it.next();
                String stringPath = isJar(uriContent) ? path.toString().substring(1) : fsPath + "/" + path.toFile().getName();
                log.info(getName() + " loading: Path to postgres config file - " + path.toString() + ", Path to postgres query file String: " + stringPath);

                if(predicate.test(path)) {
                    consumer.accept(stringPath);
                }
            }
        } finally {
            walk.close();
        }
    }
    private String getName() {
        return ConfigurationLoaderService.class.getName();
    }
    private Path getPath(URL uri, String fsPath) throws URISyntaxException, IOException {
        Path myPath = null;

        if (isJar(uri)) {
            log.info(getName() + " loading: Scheme JAR discovered");
            FileSystem fileSystem = commonUtils.getFileSystem(uri.toString());
            myPath = fileSystem.getPath(fsPath);
            log.info(getName() + " loading: Path to resource - " + myPath.toString());
        } else {
            log.info(getName() + " loading: Scheme discovered: " + uri.toURI().getScheme());
            myPath = Paths.get(uri.toURI());
            log.info(getName() + " loading: Path to resource - " + myPath.getFileName());
        }
        return myPath;
    }
    private boolean isJar(URL uri) throws URISyntaxException {
        return uri.toURI().getScheme().toLowerCase().equals("jar");
    }
}
