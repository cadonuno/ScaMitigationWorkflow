package com.veracode.scamitigation.selenium;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ExecutionParameters {
    private static final String CONFIG_FILE_LOCATION = System.getProperty("java.class.path").contains("idea_rt.jar")
            ? "C:\\Veracode\\Projects\\ScaMitigationWorkflow\\config"
            : "./config";
    private static ExecutionParameters instance;
    private final String username;
    private final String password;
    private final String webDriverName;
    private final String webDriverLocation;
    private final String profileToUse;

    public ExecutionParameters(String username, String password, String webDriverName,
                               String webDriverLocation, String profileToUse) {
        this.username = username;
        this.password = password;
        this.webDriverName = webDriverName;
        this.webDriverLocation = webDriverLocation;
        this.profileToUse = profileToUse == null ? "default" : profileToUse;
    }

    public static ExecutionParameters getInstance() {
        if (instance == null) {
            instance = readConfigFile();
        }
        return instance;
    }

    private static ExecutionParameters readConfigFile() {
        try (FileReader fileReader = new FileReader(CONFIG_FILE_LOCATION);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            return new ExecutionParameters(bufferedReader.readLine(),
                    bufferedReader.readLine(),
                    bufferedReader.readLine(),
                    bufferedReader.readLine(),
                    bufferedReader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getWebDriverLocation() {
        return this.webDriverLocation;
    }

    public String getWebDriverName() {
        return this.webDriverName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getProfileToUse() {
        return profileToUse;
    }
}

