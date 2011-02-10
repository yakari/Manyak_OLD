/**
 * 
 */
package info.yakablog.manyak.tools;

import info.yakablog.manyak.Manyak;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author yakari
 *
 */
public class DbManager extends SQLiteOpenHelper{

    private static final String TAG = DbManager.class.getSimpleName();
    
	private static final int DB_VERSION = 1;
	
	private static final String DB_PATH = "Manyak";
	
	private static final String DB_NAME = "manyak.db";
	
	private static final String TABLE_NAME = "manyak";

    private SQLiteDatabase mDatabase;

    private boolean mIsInitializing = false;

    private boolean mUseExternalStorage = false;
	
	
	public DbManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mUseExternalStorage = sp.getBoolean("use_external_storage", false);
	}
	
    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (!mUseExternalStorage) {
            return super.getWritableDatabase();
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new SQLiteDiskIOException("Cannot access external storage: not mounted");
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            throw new SQLiteDiskIOException("Cannot access external storage: mounted read only");
        }

        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return mDatabase;
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        boolean success = false;
        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            File dir = new File(Environment.getExternalStorageDirectory(), Manyak.STORAGE_PATH);
            dir.mkdir();
            File file = new File(dir, DB_NAME);
            db = SQLiteDatabase.openOrCreateDatabase(file, null);
            int version = db.getVersion();
            if (version != DB_VERSION) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, DB_VERSION);
                    }
                    db.setVersion(DB_VERSION);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            onOpen(db);
            success = true;
            return db;
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                    }
                }
                mDatabase = db;
            } else {
                if (db != null)
                    db.close();
            }
        }
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (!mUseExternalStorage) {
            return super.getReadableDatabase();
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new SQLiteDiskIOException("Cannot access external storage: not mounted");
        }

        if (mDatabase != null && mDatabase.isOpen()) {
            return mDatabase;
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Couldn't open " + DB_NAME + " for writing (will try read-only):",
                    e);
        }

        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            File dir = new File(Environment.getExternalStorageDirectory(), Manyak.STORAGE_PATH);
            dir.mkdir();
            File file = new File(dir.getAbsolutePath(), DB_NAME);
            db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READONLY);
            if (db.getVersion() != DB_VERSION) {
                throw new SQLiteException("Can't upgrade read-only database from version "
                        + db.getVersion() + " to " + DB_VERSION + ": "
                        + file.getAbsolutePath());
            }
            onOpen(db);
            Log.w(TAG, "Opened " + DB_VERSION + " in read-only mode");
            mDatabase = db;
            return mDatabase;
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase)
                db.close();
        }
    }

    @Override
    public synchronized void close() {
        super.close();
        if (mUseExternalStorage && mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY, "
				+ "title TEXT NOT NULL, lendname TEXT, lenddate INTEGER, "
				+ "cover TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO La plupart des devs disent que quand on crée une nouvelle
		// version de la BDD il vaut mieux la supprimer et la recréer...
		// À voir si une meilleure méthode n'existe pas !
		//db.execSQL("DROP TABLE " + tableName + ";");
		//onCreate(db);
		
		
		// MEILLEURE VERSION !
		Log.d(TAG, "Upgrading database from version " + oldVersion + " to version "
                + newVersion);
        if (oldVersion < 1) {
            db.beginTransaction();
            try {
                /*
                 * Upgrading tweets table: - Add column TWEET_TYPE
                 */
                db.execSQL("CREATE TEMPORARY TABLE " + TABLE_NAME + "_backup (id INTEGER PRIMARY KEY, "
        				+ "title TEXT NOT NULL, lendname TEXT, lenddate INTEGER, "
        				+ "cover TEXT);");
                db.execSQL("INSERT INTO " + TABLE_NAME + "_backup SELECT id, title, lendname, lenddate, cover FROM " + TABLE_NAME + ";");
                db.execSQL("DROP TABLE " + TABLE_NAME + ";");
                db.execSQL("CREATE TEMPORARY TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY, "
        				+ "title TEXT NOT NULL, lendname TEXT, lenddate INTEGER, "
        				+ "cover TEXT);");
                db.execSQL("INSERT INTO " + TABLE_NAME + " SELECT id, title, lendname, lenddate, cover FROM " + TABLE_NAME + "_backup;");
                db.execSQL("DROP TABLE " + TABLE_NAME + "_backup;");
            } catch (SQLException e) {
                Log.e(TAG, "Could not upgrade database from version " + oldVersion
                        + " to version " + newVersion, e);
            } finally {
                db.endTransaction();
            }
        }
		
	}

}
