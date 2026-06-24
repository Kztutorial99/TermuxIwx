package com.kztutorial.termuxiwx.models;

public class Package {
    private String name;
    private String version;
    private String description;
    private boolean installed;

    public Package(String name, String version, String description, boolean installed) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.installed = installed;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public static Package parseFromAptSearch(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        try {
            String[] parts = line.split("/");
            if (parts.length < 2) return null;
            String name = parts[0].trim();
            String rest = parts[1];
            String[] restParts = rest.split(" ");
            String version = restParts.length > 0 ? restParts[0].trim() : "";
            boolean installed = line.contains("[installed]");
            return new Package(name, version, "", installed);
        } catch (Exception e) {
            return null;
        }
    }
}
