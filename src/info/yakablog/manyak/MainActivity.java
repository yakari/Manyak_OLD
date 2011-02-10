/**
 * 
 */
package info.yakablog.manyak;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import greendroid.app.GDActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.item.TextItem;

/**
 * @author yakari
 *
 */
public class MainActivity extends GDActivity {

    // Menu item ids
    public static final int MENU_ITEM_PREFS = Menu.FIRST;
    
	private ListView mListView;
    private Class<?>[] mDemoClasses = {
            AddItem.class, LendItem.class
    };
    
    public MainActivity() {
    	super(Type.Dashboard);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Manyak");
        
        try {
			createStorage();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
		}
        
        setActionBarContentView(R.layout.home);
        
        ItemAdapter adapter = new ItemAdapter(this);
        adapter.add(new TextItem(getString(R.string.main_additem)));
        adapter.add(new TextItem(getString(R.string.main_lend)));
        adapter.add(new TextItem(getString(R.string.main_retrieve)));
        adapter.add(new TextItem(getString(R.string.main_list_loaned)));
        adapter.add(new TextItem(getString(R.string.main_list_all)));
        //adapter.add(new TextItem("Load collection"));
        //adapter.add(new TextItem("Create new collection"));
        
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(mItemClickHandler);
    }
    
    private void createStorage() throws IOException {
    	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new IOException("Cannot access external storage: not mounted");
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            throw new IOException("Cannot access external storage: mounted read only");
        }
        
        File dir = new File(Environment.getExternalStorageDirectory(), Manyak.STORAGE_PATH);
        dir.mkdir();
        File nomedia = new File(dir, ".nomedia");
        nomedia.createNewFile();
    }

    private OnItemClickListener mItemClickHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0 && position < mDemoClasses.length) {
                Intent intent = new Intent(MainActivity.this, mDemoClasses[position]);
                
                switch (position) {
                    case 0:
                        intent.putExtra(greendroid.app.ActionBarActivity.GD_ACTION_BAR_TITLE, "Add an item");
                        break;
                }
                
                startActivity(intent);
            }
        }
    };
    

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ITEM_PREFS, Menu.NONE, getString(R.string.main_menu_preferences))
    		.setIcon(android.R.drawable.ic_menu_preferences);;
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_ITEM_PREFS:
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
}
