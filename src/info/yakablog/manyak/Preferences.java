/**
 * 
 */
package info.yakablog.manyak;

import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

/**
 * @author yakari
 *
 */
public class Preferences extends PreferenceActivity {
	
	private static final String TAG = Preferences.class.getSimpleName();
	
	public static final String KEY_EXTERNAL_STORAGE = "use_external_storage";
	
	public static final String KEY_CONTACT_DEVELOPER = "contact_developer";

    public static final String KEY_REPORT_BUG = "report_bug";

    public static final String KEY_CHANGE_LOG = "change_log";

    public static final String KEY_ABOUT_APPLICATION = "about_application";
    
    private CheckBoxPreference mUseExternalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mUseExternalStorage = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_EXTERNAL_STORAGE);
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	mUseExternalStorage.setEnabled(false);
            mUseExternalStorage.setChecked(false);
        }
         
    }

}
