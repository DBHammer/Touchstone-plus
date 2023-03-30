package org.example.utils;

import org.example.dbconnector.DatabaseConnectorConfig;

public class TaskConfigurator {
    private DatabaseConnectorConfig databaseConnectorConfig;
    private String inputDirectory;
    private String outputDirectory;
    private String newsqlDirectory;
    private String dataDirectory;

    public DatabaseConnectorConfig getDatabaseConnectorConfig() {
        return databaseConnectorConfig;
    }

    public void setDatabaseConnectorConfig(DatabaseConnectorConfig databaseConnectorConfig) {
        this.databaseConnectorConfig = databaseConnectorConfig;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getNewsqlDirectory() {
        return newsqlDirectory;
    }

    public void setNewsqlDirectory(String newsqlDirectory) {
        this.newsqlDirectory = newsqlDirectory;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
}
