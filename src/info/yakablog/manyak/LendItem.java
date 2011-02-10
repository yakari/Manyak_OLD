/**
 * 
 */
package info.yakablog.manyak;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import info.yakablog.manyak.tools.ManyakDatabase;
import greendroid.app.GDActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.TextItem;

/**
 * @author yakari
 *
 */
public class LendItem extends GDActivity {

	/**
	 * Ce code sert à identifier l'activity de prise de photo dans la méthode onActivityResult.
	 */
	private final static int REQUEST_CODE_CONTACT_ACTIVITY = 1337;

	/**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] PROJECTION = new String[] {
        ManyakDatabase.Records.ID, // 0
        ManyakDatabase.Records.TITLE, // 1
        ManyakDatabase.Records.LEND_NAME,
        ManyakDatabase.Records.LEND_DATE,
        ManyakDatabase.Records.COVER
    };
    
    private static final String COL_ID = ManyakDatabase.Records.ID;
    
    private static final String COL_TITLE = ManyakDatabase.Records.TITLE;
    
    private static final String COL_COVER = ManyakDatabase.Records.COVER;
    
    private static final String COL_LENDNAME = ManyakDatabase.Records.LEND_NAME;
    
    private static final String COL_LENDDATE = ManyakDatabase.Records.LEND_DATE;
    
    private Cursor mCursor;
    
	private ListView mListView;
	
    private ListView mContactList;
    

    /** Appelé lorsque l'activité est créée. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCursor=managedQuery(ManyakDatabase.Records.CONTENT_URI,
				PROJECTION, null, null, null);
        
        setActionBarContentView(R.layout.lenditem);

        ItemAdapter adapter = new ItemAdapter(this);
        Cursor fillCur = managedQuery(ManyakDatabase.Records.CONTENT_URI,
				PROJECTION, COL_LENDNAME + " IS NULL", null, COL_TITLE + " ASC");
		if(fillCur.moveToFirst()) {
			int titleColIndex = fillCur.getColumnIndex(COL_TITLE);
			int idColIndex = fillCur.getColumnIndex(COL_ID);
			//int coverCol = fillCur.getColumnIndex(COL_COVER);
			do {
				TextItem ti = new TextItem(fillCur.getString(titleColIndex));
				//TODO Obviously, setTag ne marche pas, quand je récupère le tag plus loin, j'ai un Null...
				ti.setTag(fillCur.getInt(idColIndex));
				adapter.add(ti);
			} while(fillCur.moveToNext());
		}
		
        mListView = (ListView) findViewById(R.id.listLend);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(mItemClickHandler);
        
        
        
    }
    


    private OnItemClickListener mItemClickHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	int index = (Integer) ((TextItem) mListView.getAdapter().getItem(position)).getTag();
        	
        	Dialog dialog = new Dialog(LendItem.this);
        	dialog.setContentView(R.layout.contactslist);
        	
        	ArrayAdapter<TextView> contactsAdapter = new ArrayAdapter<TextView>(LendItem.this, android.R.string.untitled);
        	Cursor contactsCursor = getContacts();
//            String[] fields = new String[] {
//                    ContactsContract.Data.DISPLAY_NAME
//            };
//            SimpleCursorAdapter contactsAdapter = new SimpleCursorAdapter(LendItem.this, R.layout.contactslist, cursor,
//                    fields, new int[] {R.id.contactslist});
//            mContactList.setAdapter(contactsAdapter);
        	
        	if(contactsCursor.moveToFirst()) {
    			int contactIndex = contactsCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
    			do {
    				//TextView ti = new TextView(contactsCursor.getString(contactIndex));
    				TextView ti = new TextView(LendItem.this);
    				ti.setText(contactsCursor.getString(contactIndex));
    				contactsAdapter.add(ti);
    			} while (contactsCursor.moveToNext());
            	mContactList = (ListView) dialog.findViewById(R.id.contactslist);
    			mContactList.setAdapter(contactsAdapter);
        	}
        	
        	
//        	while (contactsCursor.moveToNext()) {
//    			String displayName = contactsCursor.getString(contactsCursor
//    					.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
//    			mContactList.addView(child);
//        	}
            
        	dialog.setContentView(mContactList);
        	dialog.show();
        }
    };

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getContacts()
    {
    	//TODO à mettre en var globale
    	boolean mShowInvisible = false;
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
                (mShowInvisible ? "0" : "1") + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    }

}
