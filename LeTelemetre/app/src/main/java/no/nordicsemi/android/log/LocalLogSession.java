package no.nordicsemi.android.log;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import no.nordicsemi.android.log.LogContract.Session;
import no.nordicsemi.android.log.LogContract.Session.Content;

public class LocalLogSession implements ILogSession {
    private final Context context;
    private final Uri sessionUri;

    public static LocalLogSession newSession(Context context, Uri authority, String key, String name) {
        Uri uri = authority.buildUpon().appendEncodedPath(Session.SESSION_CONTENT_DIRECTORY).appendEncodedPath(Session.KEY_CONTENT_DIRECTORY).appendEncodedPath(key).build();
        ContentValues values = new ContentValues();
        values.put(LogContract.SessionColumns.NAME, name);
        try {
            return new LocalLogSession(context, context.getContentResolver().insert(uri, values));
        } catch (Exception e) {
            Log.e("LocalLogSession", "Error while creating a local log session.", e);
            return null;
        }
    }

    LocalLogSession(Context context, Uri sessionUri) {
        this.context = context;
        this.sessionUri = sessionUri;
    }

    public void delete() {
        try {
            this.context.getContentResolver().delete(this.sessionUri, null, null);
        } catch (Exception e) {
            Log.e("LocalLogSession", "Error while deleting local log session.", e);
        }
    }

    public Context getContext() {
        return this.context;
    }

    public Uri getSessionUri() {
        return this.sessionUri;
    }

    public Uri getSessionEntriesUri() {
        return this.sessionUri.buildUpon().appendEncodedPath(LogContract.Log.CONTENT_DIRECTORY).build();
    }

    public Uri getSessionContentUri() {
        return this.sessionUri.buildUpon().appendEncodedPath(LogContract.Log.CONTENT_DIRECTORY).appendEncodedPath(Content.CONTENT).build();
    }
}
