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
    public void setDescription(String description) { this.description = description; }

    /**
     * Parses the FIRST line of an apt search result entry.
     * Real format: "pkgname/stable version arch [installed]"
     * Description is on the NEXT line (indented with spaces) — handled by caller.
     */
    public static Package parseFromAptSearch(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        try {
            String[] parts = line.split("/");
            if (parts.length < 2) return null;
            String name = parts[0].trim();
            if (name.isEmpty()) return null;
            String rest = parts[1].trim();
            String[] restParts = rest.split("\\s+");
            String version = restParts.length > 0 ? restParts[0].trim() : "";
            boolean isInstalled = line.contains("[installed]");
            return new Package(name, version, "", isInstalled);
        } catch (Exception e) {
            return null;
        }
    }

    public static Package parseFromDpkg(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        try {
            if (!line.startsWith("ii")) return null;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 3) return null;
            String name = parts[1].trim();
            if (name.contains(":")) name = name.substring(0, name.indexOf(':'));
            String version = parts[2].trim();
            String desc = "";
            if (parts.length > 4) {
                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < parts.length; i++) {
                    if (i > 4) sb.append(" ");
                    sb.append(parts[i]);
                }
                desc = sb.toString();
            }
            return new Package(name, version, desc, true);
        } catch (Exception e) {
            return null;
        }
    }
}
