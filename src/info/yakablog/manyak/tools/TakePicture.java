/**
 * 
 */
package info.yakablog.manyak.tools;

import info.yakablog.manyak.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author yakari
 *
 */
public class TakePicture extends Activity implements SurfaceHolder.Callback {
	/**
	 * Le TAG facilite le débuggage en permettant de filtrer la sortie de
	 * LOGCAT en fonction du mot clé.
	 */
	private static final String TAG = "ManiakPhoto";
	/**
	 * Indique la clé permettant de récupérer le chemin absolu (nom compris)
	 * où l'image doit être écrite, afin que l'activity appelante puisse décider
	 * de ce chemin.
	 */
	public static final String PICTURE_PATH = "picturePath";
	/**
	 * Variable désignant le chemin absolu (nom compris) où l'image doit être
	 * écrite.
	 */
	private String destFile = null;
	
	/** Le bouton permettant de prendre la photo. */
	private Button snap = null;
	
	/** La référence à l'objet Camera du système. */
	private Camera camera;
	
	/** La surface permettant d'afficher la preview et des boutons en overlay. */
	private SurfaceView surfaceView;
	
	/**
	 * Sert à gérer le type et éventuellement la taille de la SurfaceView,
	 * ainsi qu'à surveiller ses changements.
	 */
	private SurfaceHolder surfaceHolder;
	
	/** Indique quand la prévisualisation de l'appareil photo est active. */
	private boolean previewing = false;
	
	/**
	 * Utilisé pour inclure un layout XML dans une vue (permet ici la
	 * superposition des boutons de contrôle par-dessus la SurfaceView).
	 */
	private LayoutInflater controlInflater = null;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// On applique le layout takephoto_main.xml à cette Activity
		setContentView(R.layout.takephoto_main);

		// SETUP :
        // 1. On récupère l'Intent qui a appelé cette Activity
		final Intent intent = getIntent();
        // 2. On récupère les paramètres passés à cette Activity
        //    (ici, destFile qui contient le chemin où sera stockée la photo)
		Bundle extras = intent.getExtras();
		destFile = extras.getString(PICTURE_PATH);
		// 3. On force l'orientation en paysage
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		controlInflater = LayoutInflater.from(getBaseContext());
		View viewControl = controlInflater.inflate(R.layout.takephoto_control, null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);

		snap = (Button) findViewById(R.id.takepicture);
		snap.setOnClickListener(snapAction);

		LinearLayout layoutBackground = (LinearLayout) findViewById(R.id.background);
		layoutBackground.setOnClickListener(LayoutClickListener);
		
		Log.d(TAG, "onCreate'd");
	}
	
	
	Button.OnClickListener snapAction = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			camera.takePicture(shutterCallback, rawCallback,
					jpegCallback);
		}
	};
	
	
	LinearLayout.OnClickListener LayoutClickListener = new LinearLayout.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			snap.setEnabled(false);
			camera.autoFocus(myAutoFocusCallback);
		}
	};
	
	
	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			snap.setEnabled(true);
		}
	};
	
	
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};
	
	
	/** Gestion des données brutes */
	PictureCallback rawCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};
	
	
	/** Gestion des données pour enregistrement jpeg */
	PictureCallback jpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// écriture dans la sandbox locale:
				// outStream = TakePicture.this.openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), 0);
				// ou écriture sur la SD Card
				outStream = new FileOutputStream(destFile);
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				Toast.makeText(TakePicture.this, "onPictureTaken - wrote bytes: " + data.length, Toast.LENGTH_SHORT);

	            // The new entry was created, so assume all will end well and
	            // set the result to be returned.
	            setResult(RESULT_OK);
	            // On termine l'Activity courante en propageant l'ActivityResult
	            // à l'Activity qui a appelé celle-ci.
	            TakePicture.this.finish();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};
	
	

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Maintenant que la taille est connue, on lance la preview.
		if(previewing){
			camera.stopPreview();
			previewing = false;
		}
		
		if (camera != null){
			try {
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
				previewing = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	
	
	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
		previewing = false;
	}
}
