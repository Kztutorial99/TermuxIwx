package com.kztutorial.termuxiwx.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CommandResultReceiver extends BroadcastReceiver {

    public static final String ACTION_RESULT = "com.kztutorial.termuxiwx.COMMAND_RESULT";
    public static final String EXTRA_STDOUT = "stdout";
    public static final String EXTRA_STDERR = "stderr";
    public static final String EXTRA_EXIT_CODE = "exit_code";
    public static final String EXTRA_COMMAND_ID = "command_id";

    public interface ResultCallback {
        void onResult(String stdout, String stderr, int exitCode, String commandId);
    }

    private static ResultCallback sCallback;

    public static void setCallback(ResultCallback callback) {
        sCallback = callback;
    }

    public static void clearCallback() {
        sCallback = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        String stdout = "";
        String stderr = "";
        int exitCode = -1;
        String commandId = "";

        Bundle resultBundle = bundle.getBundle("result");
        if (resultBundle != null) {
            stdout = resultBundle.getString("stdout", "");
            stderr = resultBundle.getString("stderr", "");
            exitCode = resultBundle.getInt("exitCode", -1);
        }

        commandId = bundle.getString(EXTRA_COMMAND_ID, "");

        if (sCallback != null) {
            sCallback.onResult(stdout, stderr, exitCode, commandId);
        }
    }
}
