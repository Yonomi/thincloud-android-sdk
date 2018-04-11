package co.yonomi.thincloud.tcsdktestplatform;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by mike on 4/11/18.
 */

public class Configurator {

    private static final String TAG = "Configurator";

    private static final String[] DEFAULTS = {
            "app.properties",
            "dev.properties"
    };

    private static Configurator instance = new Configurator();

    public static Configurator initWithContext(Context context){
        instance.assetManager = context.getAssets();
        for (String file : DEFAULTS) {
            instance.tryLoad(file);
        }
        return instance;
    }

    public static Configurator getInstance(){
        return instance;
    }

    private AssetManager assetManager;
    private Properties properties = new Properties();

    private Configurator(){}

    /**
     *
     * @param fileName
     * @return
     */
    public boolean tryLoad(String fileName) {
        try(InputStream inputStream = assetManager.open(fileName)) {
            properties.load(inputStream);
            return true;
        } catch(IOException e){
            Log.e(TAG, "Failed to load file", e);
        }
        return false;
    }

    public String get(String key){
        return properties.getProperty(key);
    }

    public String get(String key, String _default){
        return properties.getProperty(key, _default);
    }
}
