package info.yakablog.manyak;

import java.io.File;

import greendroid.app.GDApplication;

public class Manyak extends GDApplication {
	public static final String APPTAG = "Manyak";
	public static final String STORAGE_PATH = "Manyak";
	public static final String STORAGE_IMAGES = "Manyak" + File.separatorChar + "images";

    @Override
    public Class<?> getHomeActivityClass() {
        return MainActivity.class;
    }

}