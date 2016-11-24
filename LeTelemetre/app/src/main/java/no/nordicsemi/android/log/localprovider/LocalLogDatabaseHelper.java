package no.nordicsemi.android.log.localprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

class LocalLogDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_LOG = "CREATE TABLE log(_id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER NOT NULL, time INTEGER NOT NULL, level INTEGER NOT NULL, data TEXT NOT NULL, FOREIGN KEY(session_id) REFERENCES sessions(_id));";
    private static final String CREATE_LOG_SESSIONS = "CREATE TABLE sessions(_id INTEGER PRIMARY KEY AUTOINCREMENT, key TEXT NOT NULL, name TEXT, created_at INTEGER NOT NULL);";
    private static final String DATABASE_NAME = "local_log.db";
    private static final int DATABASE_VERSION = 1;
    private static LocalLogDatabaseHelper sInstance;

    public interface LogColumns {
        public static final String DATA = "log.data";
        public static final String ID = "log._id";
        public static final String LEVEL = "log.level";
        public static final String SESSION_ID = "log.session_id";
        public static final String TIME = "log.time";
    }

    public interface Projections {
        public static final String[] ID = new String[LocalLogDatabaseHelper.DATABASE_VERSION];
        public static final String[] MAX_NUMBER = new String[LocalLogDatabaseHelper.DATABASE_VERSION];

        //static {
        //    String[] strArr = new String[LocalLogDatabaseHelper.DATABASE_VERSION];
        //  strArr[0] = "_id";
        //  ID = strArr;
        //  strArr = new String[LocalLogDatabaseHelper.DATABASE_VERSION];
        //  strArr[0] = "MAX(number)";
        //  MAX_NUMBER = strArr;
        //}
    }

    public interface SessionColumns {
        public static final String APPLICATION_ID = "sessions.application_id";
        public static final String CREATED_AT = "sessions.created_at";
        public static final String DESCRIPTION = "sessions.description";
        public static final String ID = "sessions._id";
        public static final String KEY = "sessions.key";
        public static final String MARK = "sessions.mark";
        public static final String NAME = "sessions.name";
        public static final String NUMBER = "sessions.number";
    }

    public interface Tables {
        public static final String LOG = "log";
        public static final String LOG_SESSIONS = "sessions";
    }

    static {
        sInstance = null;
    }

    public static synchronized LocalLogDatabaseHelper getInstance(Context context) {
        LocalLogDatabaseHelper localLogDatabaseHelper;
        synchronized (LocalLogDatabaseHelper.class) {
            if (sInstance == null) {
                sInstance = new LocalLogDatabaseHelper(context, DATABASE_NAME, DATABASE_VERSION);
            }
            localLogDatabaseHelper = sInstance;
        }
        return localLogDatabaseHelper;
    }

    protected LocalLogDatabaseHelper(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
    }

    public void onCreate(SQLiteDatabase db) {
        List<String> ddls = new ArrayList();
        ddls.add(CREATE_LOG_SESSIONS);
        ddls.add(CREATE_LOG);
        for (String ddl : ddls) {
            db.execSQL(ddl);
        }
        initializeAutoIncrementSequences(db);
    }

    protected void initializeAutoIncrementSequences(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
