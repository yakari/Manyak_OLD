/**
 * 
 */
package info.yakablog.manyak.tools;

import java.util.HashMap;

import info.yakablog.manyak.tools.ManyakDatabase.Records;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author yakari
 *
 */
public class ManyakProvider extends ContentProvider {

    //private static final String TAG = ManyakProvider.class.getSimpleName();
    
	//private static final int DB_VERSION = 1;
	
	//private static final String DB_PATH = "Manyak";
	
	//private static final String DB_NAME = "manyak.db";
	
	private static final String TABLE_NAME = "manyak";

    private static final UriMatcher sUriMatcher;

    private DbManager mOpenHelper;

    private static HashMap<String, String> sRecordsProjectionMap;
    
    private static final int RECORDS = 1;
    
    private static final int RECORD_ID = 2;
    
    private static final int RECORDS_COUNT = 3;
    

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
        case RECORDS:
        case RECORDS_COUNT:
            return Records.CONTENT_TYPE;
        case RECORD_ID:
            return Records.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
        mOpenHelper = new DbManager(getContext());
        return (mOpenHelper == null) ? false : true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String sql = "";
        qb.setTables(TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case RECORDS:
            qb.setProjectionMap(sRecordsProjectionMap);
            break;

        case RECORD_ID:
            qb.setProjectionMap(sRecordsProjectionMap);
            qb.appendWhere(Records.ID + "=" + uri.getPathSegments().get(1));
            break;

        case RECORDS_COUNT:
            sql = "SELECT count(*) FROM " + TABLE_NAME;
            if (selection != null && selection.length() > 0) {
                sql += " WHERE " + selection;
            }
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Records.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
        ContentValues newValues;
        long rowId;
        // 2010-07-21 yvolk: "now" is calculated exactly like it is in other
        // parts of the code
        Long now = System.currentTimeMillis();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table;
        String nullColumnHack;
        Uri contentUri;

        if (values != null) {
        	newValues = new ContentValues(values);
        } else {
        	newValues = new ContentValues();
        }

        switch (sUriMatcher.match(uri)) {
            case RECORDS:
                table = TABLE_NAME;
                nullColumnHack = Records.TITLE;
                contentUri = Records.CONTENT_URI;
                if (newValues.containsKey(Records.TITLE) == false) {
                	Resources r = Resources.getSystem();
                	values.put(Records.TITLE, r.getString(android.R.string.untitled));
                }
                if (newValues.containsKey(Records.LEND_NAME) == false)
                	newValues.put(Records.LEND_NAME, "");
                if (newValues.containsKey(Records.LEND_DATE) == false)
                	newValues.put(Records.LEND_DATE, now);
                if (newValues.containsKey(Records.COVER) == false)
                	newValues.put(Records.COVER, "");

                break;
                
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        rowId = db.insert(table, nullColumnHack, values);
        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(contentUri, rowId);
            return newUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case RECORDS:
            count = db.update(TABLE_NAME, values, where, whereArgs);
            break;

        case RECORD_ID:
            String recordId = uri.getPathSegments().get(1);
            count = db.update(TABLE_NAME, values, Records.ID + "=" + recordId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case RECORDS:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case RECORD_ID:
                String recId = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, Records.ID + "=" + recId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	


    // Static Definitions for UriMatcher and Projection Maps
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(ManyakDatabase.AUTHORITY, "records", RECORDS);
        sUriMatcher.addURI(ManyakDatabase.AUTHORITY, "records/#", RECORD_ID);
        sUriMatcher.addURI(ManyakDatabase.AUTHORITY, "records/count", RECORDS_COUNT);

        sRecordsProjectionMap = new HashMap<String, String>();
        sRecordsProjectionMap.put(Records.ID, Records.ID);
        sRecordsProjectionMap.put(Records.TITLE, Records.TITLE);
        sRecordsProjectionMap.put(Records.LEND_NAME, Records.LEND_NAME);
        sRecordsProjectionMap.put(Records.LEND_DATE, Records.LEND_DATE);
        sRecordsProjectionMap.put(Records.COVER, Records.COVER);
    }
}
