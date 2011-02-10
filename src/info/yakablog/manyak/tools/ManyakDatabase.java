/**
 * 
 */
package info.yakablog.manyak.tools;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author yakari
 *
 */
public class ManyakDatabase {

	public static final String AUTHORITY = "info.yakablog.manyak";
	
	/** On ne veut pas permettre l'instanciation de cette classe car elle
	 *  ne contient que des données statiques, alors on met un garde-fou :
	 *  le constructeur est déclaré "private", ainsi il ne peut être appelé !
	 */
	private ManyakDatabase(){}
	
	public static final class Records implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/records");
        public static final Uri CONTENT_COUNT_URI = Uri.parse("content://" + AUTHORITY + "/records/count");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yakablog.manyak.records";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yakablog.manyak.records";
		public static final String DEFAULT_SORT_ORDER = "title";
		public static final String ALTERNATE_SORT_ORDER = "title DESC";

		// Table columns
		public static final String ID = "id";
		public static final String TITLE = "title";
		public static final String LEND_NAME = "lendname";
		public static final String LEND_DATE = "lenddate";
		public static final String COVER = "cover";
	}

}
