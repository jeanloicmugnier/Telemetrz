package no.nordicsemi.android.log.localprovider;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LogTransaction {
    private final boolean mBatch;
    private final Map<String, SQLiteDatabase> mDatabaseTagMap;
    private final List<SQLiteDatabase> mDatabasesForTransaction;
    private boolean mIsDirty;
    private boolean mYieldFailed;

    public LogTransaction(boolean batch) {
        this.mBatch = batch;
        this.mDatabasesForTransaction = new ArrayList();
        this.mDatabaseTagMap = new HashMap();
        this.mIsDirty = false;
    }

    public boolean isBatch() {
        return this.mBatch;
    }

    public boolean isDirty() {
        return this.mIsDirty;
    }

    public void markDirty() {
        this.mIsDirty = true;
    }

    public void markYieldFailed() {
        this.mYieldFailed = true;
    }

    public void startTransactionForDb(SQLiteDatabase db, String tag) {
        if (!hasDbInTransaction(tag)) {
            this.mDatabasesForTransaction.add(0, db);
            this.mDatabaseTagMap.put(tag, db);
            db.beginTransaction();
        }
    }

    public boolean hasDbInTransaction(String tag) {
        return this.mDatabaseTagMap.containsKey(tag);
    }

    public SQLiteDatabase getDbForTag(String tag) {
        return (SQLiteDatabase) this.mDatabaseTagMap.get(tag);
    }

    public SQLiteDatabase removeDbForTag(String tag) {
        SQLiteDatabase db = (SQLiteDatabase) this.mDatabaseTagMap.get(tag);
        this.mDatabaseTagMap.remove(tag);
        this.mDatabasesForTransaction.remove(db);
        return db;
    }

    public void markSuccessful(boolean callerIsBatch) {
        if (!this.mBatch || callerIsBatch) {
            for (SQLiteDatabase db : this.mDatabasesForTransaction) {
                db.setTransactionSuccessful();
            }
        }
    }

    public void finish(boolean callerIsBatch) {
        if (!this.mBatch || callerIsBatch) {
            for (SQLiteDatabase db : this.mDatabasesForTransaction) {
                if (!this.mYieldFailed || db.isDbLockedByCurrentThread()) {
                    db.endTransaction();
                }
            }
            this.mDatabasesForTransaction.clear();
            this.mDatabaseTagMap.clear();
            this.mIsDirty = false;
        }
    }
}
