package ru.dynamic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommonUtils {

    // Cache
    private Map<String, FileSystem> fileSystems = new HashMap<>();

    public <T> T fromJSON(String fileName, Map<String, String> sqlQuery, Class<T> clazz) {
        InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);
        String value = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
        for (Map.Entry map : sqlQuery.entrySet()) {
            value = value.replace("${" + map.getKey() + "}", map.getValue().toString());
        }
        return clazz.cast(new Gson().fromJson(value, clazz));
    }

    public Map<String, Object> jsonToMap(String s) throws JsonParseException, IOException {
        return s.isEmpty() ? new HashMap() : (Map) (new ObjectMapper()).readValue(s, HashMap.class);
    }


    public FileSystem getFileSystem(String path) throws IOException, URISyntaxException {
        log.debug("Getting File System by path " + path);
        val key = path.replace("///", "/").split("!")[0];
        log.debug("Key for filesystem is " + path);
        if (fileSystems.get(key) == null) {
            fileSystems.put(key, FileSystems.newFileSystem(new URI(path), Collections.<String, Object>emptyMap()));
        }
        return fileSystems.get(key);
    }

    public int getPostgresTypeByStringType(String resultValue) throws Exception {
        switch (resultValue) {
            case DataTypes.LONG:
                return Types.INTEGER;
            case DataTypes.STRING:
                return Types.VARCHAR;
            case DataTypes.BOOLEAN:
                return Types.BOOLEAN;
            case DataTypes.BIGDECIMAL:
                return Types.NUMERIC;
            case DataTypes.INTEGER:
                return Types.INTEGER;
            case DataTypes.DOUBLE:
                return Types.DOUBLE;
            case DataTypes.DATE:
                return Types.DATE;
        }
        throw new Exception("Unknown type");
    }
}
