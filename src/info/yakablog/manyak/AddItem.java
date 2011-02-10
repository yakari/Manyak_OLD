/**
 * 
 */
package info.yakablog.manyak;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import info.yakablog.manyak.tools.ManyakDatabase;
import info.yakablog.manyak.tools.TakePicture;
import info.yakablog.manyak.tools.ZXingIntentIntegrator;
import info.yakablog.manyak.tools.ZXingIntentResult;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import greendroid.app.GDActivity;
import greendroid.app.GDApplication;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

/**
 * @author yakari
 *
 */
public class AddItem extends GDActivity {
	
    //private final Handler mHandler = new Handler();

	/**
	 * Ce code sert à identifier l'activity de prise de photo dans la méthode onActivityResult.
	 */
	private final static int REQUEST_CODE_PICTURE_ACTIVITY = 42;
	
	/**
	 * Ce code sert à identifier l'activity de lecture de code barre dans la méthode onActivityResult.
	 * Il a déjà été défini par l'équipe ZXing dans leur classe, mais il est renommé ici afin de
	 * faciliter la lecture du code.
	 */
	private final static int REQUEST_CODE_ZXING_ACTIVITY = ZXingIntentIntegrator.REQUEST_CODE;
	
	/**
     * Standard projection for the interesting columns of a normal collectible.
     */
    private static final String[] PROJECTION = new String[] {
        ManyakDatabase.Records.ID, // 0
        ManyakDatabase.Records.TITLE, // 1
        ManyakDatabase.Records.COVER
    };
    
    private static final String COL_ID = ManyakDatabase.Records.ID;
    
    private static final String COL_TITLE = ManyakDatabase.Records.TITLE;
    
    private static final String COL_COVER = ManyakDatabase.Records.COVER;
    
    // Clés d'enregistrement de l'état des variables lors d'une rotation d'écran
    private static final String BACKUP_BARCODE = "bckBarcode";
    private static final String BACKUP_TITLE = "bckTitle";
    private static final String BACKUP_COVER = "bckCover";
    private static final String BACKUP_BUTTONS_STATE = "bckBtnState";
    
    private Cursor mCursor;

    private String mBarcode = null;
	private EditText mEditTitle = null;
	private String mImageFileName = null;
	
	private Button mCoverPhoto = null;
	
	//private String mAlertReturn = null;
	

    /** Appelé lorsque l'activité est créée. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCursor=managedQuery(ManyakDatabase.Records.CONTENT_URI,
				PROJECTION, null, null, null);
        
        setActionBarContentView(R.layout.additem);

        addActionBarItem(Type.Add);
        
        mEditTitle = (EditText) findViewById(R.id.titleBox);
        mEditTitle.setEnabled(false);
        mEditTitle.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// si l'événement est un appui sur la touche Entrer...
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
					(keyCode == KeyEvent.KEYCODE_ENTER)) {
					// On arrête la saisie ! Ça permet de faire disparaître
					// facilement le clavier.
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(mEditTitle.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
				}
				return false;
			}
		});
        
        mCoverPhoto = (Button) findViewById(R.id.coverPhoto);
        mCoverPhoto.setEnabled(false);
        mCoverPhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(AddItem.this, TakePicture.class);
				cameraIntent.putExtra(TakePicture.PICTURE_PATH, getTempFile().getAbsolutePath());
				startActivityForResult(cameraIntent, REQUEST_CODE_PICTURE_ACTIVITY);
				
//				// Normalement, on devrait mettre ici notre propre intent.
//				// Celui du système est très complet (trop ?)
//				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
//				// Quand on lance l'intent avec une demande de résultat,
//				// cela lance automatiquement l'événement onActivityResult à la fin.
//				startActivityForResult(cameraIntent, REQUEST_CODE_PICTURE_ACTIVITY);
			}
		});
        
        final Button scanCode = (Button) findViewById(R.id.scanCode);
        scanCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ZXingIntentIntegrator.initiateScan(AddItem.this,
						R.string.zxing_title,
						R.string.zxing_message,
						R.string.zxing_yes,
						R.string.zxing_no);
			}
		});
        
        // Si on a effectué une rotation de l'écran, il est nécessaire de
        // restaurer certaines variables, car l'activity est recréée.
        if (savedInstanceState != null) {
            mBarcode = savedInstanceState.getString(BACKUP_BARCODE);
            mEditTitle.setText(savedInstanceState.getString(BACKUP_TITLE));
            mImageFileName = savedInstanceState.getString(BACKUP_COVER);
            mCoverPhoto.setEnabled(savedInstanceState.getBoolean(BACKUP_BUTTONS_STATE));
            mEditTitle.setEnabled(savedInstanceState.getBoolean(BACKUP_BUTTONS_STATE));
        }
    }
    
    private File getTempFile() {
    	final File path = new File(Environment.getExternalStorageDirectory(), Manyak.STORAGE_PATH);
    	if(!path.exists()) {
    		path.mkdir();
    	}
    	return new File(path, "image.tmp");
    }

    /* (non-Javadoc)
     * @see greendroid.app.GDActivity#onHandleActionBarItemClick(greendroid.widget.ActionBarItem, int)
     */
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

        switch (position) {

            case 0:
            	if(mBarcode == null) {
            		Toast.makeText(this, getString(R.string.additem_toast_nobarcode), Toast.LENGTH_SHORT).show();
            		return false;
            	} else {
            		if(mEditTitle.getText() == null || mEditTitle.getText().toString().equals("")) {
            			mEditTitle.setText(android.R.string.untitled);
            		}
            		Toast.makeText(this,
            				getString(R.string.additem_toast_item_registered) +
            						  " " + mEditTitle.getText() + " " +
            						  getString(R.string.additem_toast_item_registered_part2),
            				Toast.LENGTH_SHORT).show();
            		if(itemExists(mBarcode)) {
            			processUpdate();
            		} else {
            			processAdd();
            		}
            	}
                break;

            default:
                return super.onHandleActionBarItemClick(item, position);
        }

        return true;
    }
    
    /**
     * Retourne à la page de démarrage de l'appli.
     */
//    private void returnHome() {
//		final GDApplication app = getGDApplication();
//		final Class<?> klass = app.getHomeActivityClass();
//		if (klass != null && !klass.equals(AddItem.this.getClass())) {
//			Intent homeIntent = new Intent(AddItem.this, klass);
//			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(homeIntent);
//		}
//    }
	
	private void processAdd() {
		final ContentValues values=new ContentValues(3);

		values.put(ManyakDatabase.Records.ID, mBarcode);
		values.put(ManyakDatabase.Records.TITLE, mEditTitle.getText().toString());
		values.put(ManyakDatabase.Records.COVER, mImageFileName);
		
		getContentResolver().insert(ManyakDatabase.Records.CONTENT_URI, values);
		mCursor.requery();
		finish();
	}
	
	private void processUpdate() {
		Uri barCodeUri = ContentUris.withAppendedId(ManyakDatabase.Records.CONTENT_URI, Long.parseLong(mBarcode));
		ContentValues values=new ContentValues(3);

		values.put(ManyakDatabase.Records.ID, mBarcode);
		values.put(ManyakDatabase.Records.TITLE, mEditTitle.getText().toString());
		values.put(ManyakDatabase.Records.COVER, mImageFileName);
		
		getContentResolver().update(barCodeUri, values, null, null);
		mCursor.requery();
		finish();
	}
	
	private boolean itemExists(String barCode) {
		Cursor existCursor = managedQuery(ManyakDatabase.Records.CONTENT_URI,
				PROJECTION, COL_ID + "=?", new String[] {barCode}, null);
		if(existCursor.getCount() != 0) return true;
		return false;
	}
    
    
    
    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
			case REQUEST_CODE_ZXING_ACTIVITY:
				if(resultCode == RESULT_OK) {
					ZXingIntentResult scan = ZXingIntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
					if(scan != null) {
						mBarcode = scan.getContents();
						if(itemExists(mBarcode)) {
							Cursor fillCur = managedQuery(ManyakDatabase.Records.CONTENT_URI,
									PROJECTION, COL_ID + "=?", new String[] {mBarcode}, null);
							if(fillCur.moveToFirst()) {
								int titleCol = fillCur.getColumnIndex(COL_TITLE);
								int coverCol = fillCur.getColumnIndex(COL_COVER);
								mEditTitle.setText(fillCur.getString(titleCol));
								mImageFileName = fillCur.getString(coverCol);
							}
						}
						mEditTitle.setEnabled(true);
						mCoverPhoto.setEnabled(true);
					}
				}
				break;
			case REQUEST_CODE_PICTURE_ACTIVITY:
				if(resultCode == RESULT_OK) {
					final File tempFile = getTempFile();
					try{
						Bitmap photo = Media.getBitmap(getContentResolver(), Uri.fromFile(tempFile));
						//TODO Rendre paramétrable la taille des photos (avec avertissement sur l'espace occupé) !
						photo = Bitmap.createScaledBitmap(photo, 320, 240, false);
						File destDir = new File(Environment.getExternalStorageDirectory(), Manyak.STORAGE_IMAGES);//+chemin rep images
						if(!destDir.exists()) {
							destDir.mkdir();
						}
						OutputStream fOut = null;
						mImageFileName = "cover_"+System.currentTimeMillis()+".jpg";
						File file = new File(destDir, mImageFileName);
						fOut = new FileOutputStream(file);
						
						//TODO Rendre paramétrable la compression ?
						photo.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
						fOut.flush();
						fOut.close();
						photo.recycle();
						photo = null;
						getTempFile().delete();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			default:
				break;
		}
	}

//	private AlertDialog createAlertDialog(String title, String msg, String buttonText1, String buttonText2){
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//		AlertDialog msgDialog = dialogBuilder.create();
//		msgDialog.setTitle(title);
//		msgDialog.setMessage(msg);
//		msgDialog.setCancelable(true);
//		msgDialog.setButton(buttonText1, new DialogInterface.OnClickListener(){
//			@Override
//			public void onClick(DialogInterface dialog, int idx){
//				mAlertReturn = "a";
//				//return; // Rien à faire pour l'instant...
//			}
//		});
//		msgDialog.setButton2(buttonText2, new DialogInterface.OnClickListener(){
//			@Override
//			public void onClick(DialogInterface dialog, int idx){
//				mAlertReturn = "b";
//				//return; // Rien à faire pour l'instant...
//			}
//		});
//		msgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//			
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				mAlertReturn = "c";
//			}
//		});
//		
//		msgDialog.setOwnerActivity(this);
//
//		return msgDialog;
//	}

    /* (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BACKUP_BARCODE, mBarcode);
        outState.putString(BACKUP_TITLE, mEditTitle.getText().toString());
        outState.putString(BACKUP_COVER, mImageFileName);
        outState.putBoolean(BACKUP_BUTTONS_STATE, mCoverPhoto.isEnabled());
    }
	
}
