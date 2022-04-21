package com.sebiai.wallpaperchanger;

import static com.sebiai.wallpaperchanger.MyApplicationHelper.getMyApplication;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MyFileHandler {

    public static ArrayList<Uri> getFiles(Context context, Uri treePath) {
        final ArrayList<Uri> uris = new ArrayList<>();

        if (treePath == null)
            return uris;

        // Get all files as Cursor
        DocumentFile dir = DocumentFile.fromTreeUri(context, treePath);
        Uri dirUri = Objects.requireNonNull(dir).getUri();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(dirUri,
                DocumentsContract.getDocumentId(dirUri));
        Cursor c = null;

        try {
            c = context.getContentResolver().query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
            }, null, null, null);
        } catch (SecurityException ignored) {

        }

        if (c == null)
            return uris;

        // Get files
        while (c.moveToNext()) {
            // Only images
            if (c.getString(2).matches("image/.*")) {
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(dirUri,
                        c.getString(0));
                uris.add(documentUri);
            }
        }
        c.close();

        return uris;
    }

    public static DocumentFile getRandomFile(Context context, ArrayList<Uri> uris) {
        if (uris.size() != 0) {
            Random random = new Random();
            Uri randomUri = uris.get(random.nextInt(uris.size()));
            return DocumentFile.fromSingleUri(context, randomUri);
        }
        return null;
    }

    public static DocumentFile setRandomFileAsWallpaper(Context context, Uri wallpaperUri) {
        // Save picture amount
        ArrayList<Uri> uris = getFiles(context, wallpaperUri);
        PreferenceManager.getDefaultSharedPreferences(context).edit().
                putInt(context.getString(R.string.key_amount_pictures), uris.size()).
                apply();
        // Get random
        DocumentFile file = getRandomFile(context, uris);

        if (file != null) {
            // Set as Wallpaper
            try {
                MyFileHandler.setFileAsWallpaper(context, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void setFileAsWallpaper(Context context, DocumentFile file) throws IOException {
        InputStream stream = context.getContentResolver().openInputStream(file.getUri());
        WallpaperManager.getInstance(context).setStream(stream);
        stream.close();
    }

    public static boolean isWallpaperDirValid(Context context) {
        if (MyFileHandler.getWallpaperDirUri(context) == null)
            return false;
        ArrayList<Uri> uris = getFiles(context, MyFileHandler.getWallpaperDirUri(context));
        return uris.size() != 0;
    }

    public static Drawable getDrawableFromWallpaperUri(Context context, Uri uri) {
        Drawable drawable = null;
        if (uri == null)
            return null;

        DocumentFile file = DocumentFile.fromSingleUri(context, uri);
        if (file != null) {
            InputStream stream;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                drawable = Drawable.createFromStream(stream, file.getUri().getPath());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException ignored) {

            }
        }

        return drawable;
    }

    public static String getNameFromWallpaperUri(Context context, Uri uri) {
        String fileName = "-";

        if (uri != null) {
            // Get file
            DocumentFile file = DocumentFile.fromSingleUri(context, uri);
            // Get file name
            if (file != null)
                fileName = file.getName();
        }

        return fileName;
    }

    public static Uri getWallpaperDirUri(Context context) {
        return getUriFromPreference(context, context.getString(R.string.key_wallpaper_dir));
    }

    public static Uri getCurrentWallpaperUri(Context context) {
        return getUriFromPreference(context, context.getString(R.string.key_current_picture));
    }

    private static Uri getUriFromPreference(Context context, String preferenceKey) {
        Uri uri = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = sharedPreferences.getString(preferenceKey, null);

        if (uriString != null)
            uri = Uri.parse(uriString);

        return uri;
    }
}
