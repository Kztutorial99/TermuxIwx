package com.kztutorial.termuxiwx.models;

public class ScriptItem {
    private final String name;
    private final String description;
    private final String installCmd;
    private final String category;
    private final String testCmd;

    public ScriptItem(String name, String description, String installCmd, String category, String testCmd) {
        this.name = name;
        this.description = description;
        this.installCmd = installCmd;
        this.category = category;
        this.testCmd = testCmd;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getInstallCmd() { return installCmd; }
    public String getCategory() { return category; }
    public String getTestCmd() { return testCmd; }
}
