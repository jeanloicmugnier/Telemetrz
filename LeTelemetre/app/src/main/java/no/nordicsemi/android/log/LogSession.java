package no.nordicsemi.android.log;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import no.nordicsemi.android.log.LogContract.Log;
import no.nordicsemi.android.log.LogContract.Session;
import no.nordicsemi.android.log.LogContract.Session.Content;

public class LogSession implements ILogSession {
    private final Context context;
    final Uri sessionUri;

    LogSession(Context context, Uri sessionUri) {
        this.context = context;
        this.sessionUri = sessionUri;
    }

    public Context getContext() {
        return this.context;
    }

    public Uri getSessionUri() {
        return this.sessionUri;
    }

    public Uri getSessionEntriesUri() {
        return this.sessionUri.buildUpon().appendEncodedPath(Log.CONTENT_DIRECTORY).build();
    }

    public Uri getSessionsUri() {
        Cursor cursor;
        try {
            cursor = this.context.getContentResolver().query(this.sessionUri, new String[]{LogContract.SessionColumns.APPLICATION_ID}, null, null, null);
            if (cursor.moveToNext()) {
                Uri createSessionsUri = Session.createSessionsUri(cursor.getLong(0));
                cursor.close();
                return createSessionsUri;
            }
            cursor.close();
            return null;
        } catch (Exception e) {
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public Uri getSessionContentUri() {
        return this.sessionUri.buildUpon().appendEncodedPath(Log.CONTENT_DIRECTORY).appendEncodedPath(Content.CONTENT).build();
    }

    public String toString() {
        return this.sessionUri.toString();
    }
}
