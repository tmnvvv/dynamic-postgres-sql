package ru.dynamic.service;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractConfigurationLoaderService {
    protected abstract void loadSqlQueryDataConfiguration() throws IOException, URISyntaxException;
    protected abstract void loadJsonDataConfiguration() throws IOException, URISyntaxException;
    protected void findConfigurationInJar() throws IOException, URISyntaxException {
        loadSqlQueryDataConfiguration();
        loadJsonDataConfiguration();
    }
}
