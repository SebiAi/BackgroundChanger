package com.sebiai.wallpaperchanger.objects;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;

public class Wallpaper {
    private final Context context;
    private Uri wallpaperUri = null;
    private String wallpaperName = null;

    /**
     * Constructor of an Wallpaper object
     * @param context Context
     * @param wallpaperUri Uri of mime type `image/*`
     */
    public Wallpaper(@NonNull Context context, Uri wallpaperUri) {
        // Save context
        this.context = context;

        // Check if uri valid
        if (!isUriValid(wallpaperUri))
            return;

        // set uri
        this.wallpaperUri = wallpaperUri;

        // get name
        this.wallpaperName = getWallpaperName(wallpaperUri);
    }

    /**
     * Returns the file name of the wallpaper
     * @param wallpaperUri Uri of the wallpaper
     * @return String containing the file name of the wallpaper
     */
    private String getWallpaperName(Uri wallpaperUri) {
        String wallpaperName = "null";

        DocumentFile file = DocumentFile.fromSingleUri(context, wallpaperUri);
        if (file != null)
            wallpaperName = file.getName();

        return wallpaperName;
    }

    /**
     * Returns if the uri is valid with two criteria: 1) Is mime type `image/*` 2) is actually a readable uri
     * @param wallpaperUri Uri of the wallpaper
     * @return `true` if valid, else `false`
     */
    private boolean isUriValid(Uri wallpaperUri) {
        if (wallpaperUri != null) {
            // Is image type
            DocumentFile file = DocumentFile.fromSingleUri(context, wallpaperUri);
            if (file == null)
                return false;

            String fileType = file.getType();
            if (fileType == null || !fileType.matches("image/.+"))
                return false;

            // Exists
            try {
                InputStream stream = context.getContentResolver().openInputStream(wallpaperUri);
                stream.close();
                return true;
            } catch (IOException e) {
                // Do nothing
            }
        }

        return false;
    }

    /**
     * Gets the uri of the wallpaper
     * @return Uri of the wallpaper
     */
    public Uri getWallpaperUri() {
        return wallpaperUri;
    }

    /**
     * Gets the file name of the wallpaper
     * @return String of the wallpaper file name
     */
    public String getWallpaperName() {
        return wallpaperName;
    }

    /**
     * Gets an input stream from the saved Uri
     * @return InputStream
     */
    public InputStream getInputStream() {
        InputStream inputStream = null;

        if (wallpaperUri == null)
            return null;

        try {
            inputStream = context.getContentResolver().openInputStream(wallpaperUri);
        } catch (FileNotFoundException e) {
            // Do nothing
        }

        return inputStream;
    }

    /**
     * Parses an ArrayList of Uris
     * @param context Context
     * @param inputData ArrayList with Uris
     * @return Array of Wallpapers
     */
    public static Wallpaper[] parse(@NonNull Context context, ArrayList<Uri> inputData) {
        ArrayList<Wallpaper> wallpapers = new ArrayList<>();

        // Add to list
        for (Uri wallpaperUri : inputData) {
            Wallpaper wallpaper = new Wallpaper(context, wallpaperUri);
            // Check validity
            if (wallpaper.wallpaperUri != null)
                wallpapers.add(wallpaper);
        }

        // Sort
        wallpapers.sort(Comparator.comparing(o -> o.wallpaperName));

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return wallpapers.toArray(new Wallpaper[wallpapers.size()]);
    }
}
