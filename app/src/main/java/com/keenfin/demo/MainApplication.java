package com.keenfin.demo;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;

import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplib.util.NGWUtil;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import org.json.JSONException;

import java.io.IOException;

public class MainApplication extends GISApplication {
    @Override
    protected void onFirstRun() {
        String layerName = getString(R.string.osm);
        String layerURL = SettingsConstantsUI.OSM_URL;
        RemoteTMSLayerUI osmRasterLayer = new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage(layerName));
        osmRasterLayer.setName(layerName);
        osmRasterLayer.setURL(layerURL);
        osmRasterLayer.setTMSType(GeoConstants.TMSTYPE_OSM);
        osmRasterLayer.setVisible(true);
        osmRasterLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        osmRasterLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
        mMap.addLayer(osmRasterLayer);

        ProblemsLoader loader = new ProblemsLoader();
        loader.execute();

        mMap.save();
    }

    @Override
    public String getAuthority() {
        return Constants.AUTHORITY;
    }

    @Override
    public void showSettings() {

    }

    protected class ProblemsLoader extends AsyncTask<Void, Void, Void> {
        String mLayerName;
        NGWVectorLayerUI mNGWVectorLayer;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLayerName = Constants.PROBLEMS_LAYER_NAME;
            mNGWVectorLayer = new NGWVectorLayerUI(getApplicationContext(), mMap.createLayerStorage(mLayerName));
        }

        @Override
        protected Void doInBackground(Void[] params) {
            try {
                String token = NGWUtil.getConnectionCookie(Constants.SERVER_URL, null, null);
                boolean isAccountAdded = addAccount(Constants.ACCOUNT_NAME, Constants.SERVER_URL, Constants.ANONYMOUS, Constants.ANONYMOUS, token);
                if (isAccountAdded) {
                    Account account = getAccount(Constants.ACCOUNT_NAME);
                    ContentResolver.setSyncAutomatically(account, getAuthority(), true);
                    ContentResolver.addPeriodicSync(account, getAuthority(), Bundle.EMPTY, 600);
                }

                mNGWVectorLayer.setName(mLayerName);
                mNGWVectorLayer.setRemoteId(Constants.PROBLEMS_LAYER_ID);
                mNGWVectorLayer.setAccountName(getAccount(Constants.ACCOUNT_NAME).name);
                mNGWVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_ALL);
                mNGWVectorLayer.setVisible(true);
                mNGWVectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
                mNGWVectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
                mNGWVectorLayer.createFromNGW(null);
            } catch (IOException | JSONException | NGException e) {
                e.printStackTrace();
                mNGWVectorLayer.delete();
                mNGWVectorLayer = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mNGWVectorLayer != null) {
                mMap.addLayer(mNGWVectorLayer);
                mMap.save();
            }
        }
    };
}
