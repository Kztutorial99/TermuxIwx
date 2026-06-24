package com.kztutorial.termuxiwx.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private static final String OWNER = "Kztutorial99";
    private static final String REPO  = "TermuxIwx";
    private static final String RELEASES_API =
            "https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases/latest";

    public interface UpdateCallback {
        void onUpdateAvailable(String tagName, String downloadUrl, String releaseNotes, String releasePageUrl);
        void onUpToDate();
        void onError(String message);
    }

    public static void check(Context context, String currentVersion, UpdateCallback callback) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(RELEASES_API).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "TermuxIwx-App");
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    post(callback::onUpToDate);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                String tagName = json.optString("tag_name", "");
                String releaseNotes = json.optString("body", "");
                String releasePageUrl = json.optString("html_url", "https://github.com/" + OWNER + "/" + REPO + "/releases");

                String downloadUrl = null;
                JSONArray assets = json.optJSONArray("assets");
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url");
                            break;
                        }
                    }
                }

                String latest  = stripV(tagName);
                String current = stripV(currentVersion);

                if (!latest.isEmpty() && isNewer(latest, current)) {
                    String finalDownloadUrl    = downloadUrl;
                    String finalTagName        = tagName;
                    String finalNotes          = releaseNotes;
                    String finalReleasePageUrl = releasePageUrl;
                    post(() -> callback.onUpdateAvailable(
                            finalTagName, finalDownloadUrl, finalNotes, finalReleasePageUrl));
                } else {
                    post(callback::onUpToDate);
                }

            } catch (Exception e) {
                post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    private static String stripV(String version) {
        if (version == null) return "";
        return version.startsWith("v") ? version.substring(1) : version;
    }

    private static boolean isNewer(String latest, String current) {
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");
            int len = Math.max(l.length, c.length);
            for (int i = 0; i < len; i++) {
                int lv = i < l.length ? Integer.parseInt(l[i].replaceAll("[^0-9]", "")) : 0;
                int cv = i < c.length ? Integer.parseInt(c[i].replaceAll("[^0-9]", "")) : 0;
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static void post(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
