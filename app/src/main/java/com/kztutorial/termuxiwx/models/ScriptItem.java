package com.kztutorial.termuxiwx.models;

public class ScriptItem {
    public String name;
    public String description;
    public String installCmd;
    public String category;
    public String testCmd;

    public ScriptItem(String name, String description, String installCmd, String category, String testCmd) {
        this.name = name;
        this.description = description;
        this.installCmd = installCmd;
        this.category = category;
        this.testCmd = testCmd;
    }
}
