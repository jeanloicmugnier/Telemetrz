package no.nordicsemi.android.log;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import no.nordicsemi.android.log.LogContract.Application;
import no.nordicsemi.android.log.LogContract.Session;

public class Logger {
    public static final int MARK_CLEAR = 0;
    public static final int MARK_FLAG_BLUE = 5;
    public static final int MARK_FLAG_RED = 6;
    public static final int MARK_FLAG_YELLOW = 4;
    public static final int MARK_STAR_BLUE = 2;
    public static final int MARK_STAR_RED = 3;
    public static final int MARK_STAR_YELLOW = 1;
    private static final int SESSION_ID = 100;
    private static final int SESSION_ID_LOG = 101;
    private static final int SESSION_KEY_NUMBER = 102;
    private static final int SESSION_KEY_NUMBER_LOG = 103;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(-1);
        UriMatcher matcher = mUriMatcher;
        matcher.addURI(LogContract.AUTHORITY, "session/#", SESSION_ID);
        matcher.addURI(LogContract.AUTHORITY, "session/#/log", SESSION_ID_LOG);
        matcher.addURI(LogContract.AUTHORITY, "session/key/*/#", SESSION_KEY_NUMBER);
        matcher.addURI(LogContract.AUTHORITY, "session/key/*/#/log", SESSION_KEY_NUMBER_LOG);
    }

    public static LogSession newSession(Context context, String key, String name) {
        return newSession(context, null, key, name);
    }

    public static LogSession newSession(Context context, String profile, String key, String name) {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        Builder builder = ContentProviderOperation.newInsert(Application.CONTENT_URI);
        String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        if (profile != null) {
            builder.withValue(Session.APPLICATION_CONTENT_DIRECTORY, new StringBuilder(String.valueOf(appName)).append(" ").append(profile).toString());
        } else {
            builder.withValue(Session.APPLICATION_CONTENT_DIRECTORY, appName);
        }
        ops.add(builder.build());
        builder = ContentProviderOperation.newInsert(Session.CONTENT_URI.buildUpon().appendEncodedPath(Session.KEY_CONTENT_DIRECTORY).appendEncodedPath(key).build());
        builder.withValueBackReference(LogContract.SessionColumns.APPLICATION_ID, MARK_CLEAR);
        builder.withValue(LogContract.SessionColumns.NAME, name);
        ops.add(builder.build());
        try {
            return new LogSession(context, context.getContentResolver().applyBatch(LogContract.AUTHORITY, ops)[MARK_STAR_YELLOW].uri);
        } catch (Exception e) {
            return null;
        }
    }

    public static ILogSession openSession(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        switch (mUriMatcher.match(uri)) {
            case SESSION_ID /*100*/:
            case SESSION_KEY_NUMBER /*102*/:
                return new LogSession(context, uri);
            case SESSION_ID_LOG /*101*/:
            case SESSION_KEY_NUMBER_LOG /*103*/:
                Uri.Builder buider = Session.CONTENT_URI.buildUpon();
                List<String> segments = uri.getPathSegments();
                for (int i = MARK_STAR_YELLOW; i < segments.size() - 1; i += MARK_STAR_YELLOW) {
                    buider.appendEncodedPath((String) segments.get(i));
                }
                return new LogSession(context, buider.build());
            default:
                return new LocalLogSession(context, uri);
        }
    }

    public static void setSessionDescription(LogSession session, String description) {
        if (session != null) {
            ContentValues values = new ContentValues();
            values.put(LogContract.SessionColumns.DESCRIPTION, description);
            try {
                session.getContext().getContentResolver().update(session.getSessionUri(), values, null, null);
            } catch (Exception e) {
            }
        }
    }

    public static void setSessionMark(LogSession session, int mark) {
        if (session != null) {
            ContentValues values = new ContentValues();
            values.put(LogContract.SessionColumns.MARK, Integer.valueOf(mark));
            try {
                session.getContext().getContentResolver().update(session.getSessionUri(), values, null, null);
            } catch (Exception e) {
            }
        }
    }

    public static void m9d(ILogSession session, String message) {
        log(session, MARK_CLEAR, message);
    }

    public static void m15v(ILogSession session, String message) {
        log(session, MARK_STAR_YELLOW, message);
    }

    public static void m13i(ILogSession session, String message) {
        log(session, MARK_FLAG_BLUE, message);
    }

    public static void m7a(ILogSession session, String message) {
        log(session, 10, message);
    }

    public static void m17w(ILogSession session, String message) {
        log(session, 15, message);
    }

    public static void m11e(ILogSession session, String message) {
        log(session, 20, message);
    }

    public static void log(ILogSession session, int level, String message) {
        if (session != null) {
            ContentValues values = new ContentValues();
            values.put(LogContract.LogColumns.LEVEL, Integer.valueOf(level));
            values.put(LogContract.LogColumns.DATA, message);
            try {
                session.getContext().getContentResolver().insert(session.getSessionEntriesUri(), values);
            } catch (Exception e) {
            }
        }
    }

    public static void m8d(ILogSession session, int messageResId, Object... params) {
        log(session, MARK_CLEAR, messageResId, params);
    }

    public static void m14v(ILogSession session, int messageResId, Object... params) {
        log(session, MARK_STAR_YELLOW, messageResId, params);
    }

    public static void m12i(ILogSession session, int messageResId, Object... params) {
        log(session, MARK_FLAG_BLUE, messageResId, params);
    }

    public static void m6a(ILogSession session, int messageResId, Object... params) {
        log(session, 10, messageResId, params);
    }

    public static void m16w(ILogSession session, int messageResId, Object... params) {
        log(session, 15, messageResId, params);
    }

    public static void m10e(ILogSession session, int messageResId, Object... params) {
        log(session, 20, messageResId, params);
    }

    public static void log(ILogSession session, int level, int messageResId, Object... params) {
        if (session != null) {
            ContentValues values = new ContentValues();
            values.put(LogContract.LogColumns.LEVEL, Integer.valueOf(level));
            values.put(LogContract.LogColumns.DATA, session.getContext().getString(messageResId, params));
            try {
                session.getContext().getContentResolver().insert(session.getSessionEntriesUri(), values);
            } catch (Exception e) {
            }
        }
    }
}
