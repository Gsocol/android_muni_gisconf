package com.nextgis.municipality;

import android.content.SyncResult;
import android.support.v7.app.AppCompatActivity;

import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWVectorLayer;

public class BaseActivity extends AppCompatActivity {

    protected void sync() {
        IGISApplication application = (IGISApplication) getApplication();
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
                ngwVectorLayer.sync(application.getAuthority(), new SyncResult());
            }
        }
    }
}
