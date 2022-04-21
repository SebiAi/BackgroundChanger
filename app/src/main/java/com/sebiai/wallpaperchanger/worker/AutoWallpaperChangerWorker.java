package com.sebiai.wallpaperchanger.worker;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sebiai.wallpaperchanger.MyFileHandler;
import com.sebiai.wallpaperchanger.R;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AutoWallpaperChangerWorker extends Worker {
    private Context context;
    private WorkerParameters workerParams;

    private SharedPreferences sharedPreferences;

    private Date lastExecutionDate = null;

    public AutoWallpaperChangerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParams = workerParams;

        // Get shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Uri wallpaperUri = MyFileHandler.getWallpaperDirUri(context);

        // If no directory is set then return
        if (wallpaperUri == null)
            return Result.success();

        // Set file as wallpaper
        DocumentFile file = MyFileHandler.setRandomFileAsWallpaper(context, wallpaperUri);
        if (file != null) {
            String fileName = file.getName();

            // Logging
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN);
            SimpleDateFormat sdfsmall = new SimpleDateFormat("HH:mm:ss", Locale.GERMAN);
            Date currentDate = Calendar.getInstance(Locale.GERMAN).getTime();
            String logMessage = "Worker run at " + sdf.format(currentDate);
            if (lastExecutionDate != null) // TODO: Remove or make it work
                logMessage += " | Delta: " + sdfsmall.format(currentDate.getTime() - lastExecutionDate.getTime());
            lastExecutionDate = currentDate;

            logMessage += " | File: " + fileName;

            Log.d("AutoWallpaperChangerWorker", logMessage);
            appendLog(wallpaperUri, logMessage + "\r\n");

            // Save preferences
            int amountChangesAutomatic = sharedPreferences.getInt(context.getString(R.string.key_amount_changes_automatic), 0);
            sharedPreferences.edit().
                    putString(context.getString(R.string.key_current_picture), file.getUri().toString()).
                    putInt(context.getString(R.string.key_amount_changes_automatic), ++amountChangesAutomatic).
                    apply();
            // Set cache if possible
            if (getMyApplication(context) != null) {
                getMyApplication(context).wallpaperFileName = fileName;
                getMyApplication(context).wallpaperDrawableCache = MyFileHandler.getDrawableFromWallpaperUri(context, file.getUri());
            }
            return Result.success();
        }

        return Result.failure();
    }

    private void appendLog(Uri wallpaperUri, String text)
    {
        DocumentFile wallpaperDir = DocumentFile.fromTreeUri(context, wallpaperUri);
        if (wallpaperDir == null)
            return;

        DocumentFile logFile = wallpaperDir.findFile("WallpaperChangerLogs.txt");
        if (logFile == null) {
            // Create new File
            logFile = wallpaperDir.createFile("text/plain", "WallpaperChangerLogs");
            if (logFile == null)
                return;
        }

        try {
            OutputStream openedLogFile = context.getContentResolver().openOutputStream(logFile.getUri(), "wa");
            openedLogFile.write(text.getBytes());
            openedLogFile.flush();
            openedLogFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
