package com.nextgis.municipality;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.keenfin.easypicker.PhotoPicker;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NewMessageActivity extends BaseActivity {
    private MainApplication mApp;

    private Spinner mTypes;
    private EditText mDesc, mName;
    private PhotoPicker mGallery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mApp = (MainApplication) getApplication();

        mGallery = (PhotoPicker) findViewById(R.id.photo);
        mTypes = (Spinner) findViewById(R.id.problem_type);
        mDesc = (EditText) findViewById(R.id.problem_desc);
        mName = (EditText) findViewById(R.id.problem_cred);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int info = R.string.problem_submit_failed;
                if (sendMessage()) {
                    info = R.string.problem_submitted;
                    finish();
                }

                Toast.makeText(NewMessageActivity.this, info, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGallery.onActivityResult(requestCode, resultCode, data);
    }

    private boolean sendMessage() {
        try {
            Location location = mApp.getGpsEventSource().getLastKnownLocation();

            if (location == null)
                return false;

            ContentValues values = new ContentValues();
            values.put(Constants.FIELD_TYPE, (String) mTypes.getSelectedItem());
            values.put(Constants.FIELD_DESC, String.valueOf(mDesc.getText()));
            values.put(Constants.FIELD_AUTHOR, String.valueOf(mName.getText()));
            values.put(Constants.FIELD_DATE, System.currentTimeMillis());

            GeoPoint point = new GeoPoint(location.getLongitude(), location.getLatitude());
            point.setCRS(GeoConstants.CRS_WGS84);
            point.project(GeoConstants.CRS_WEB_MERCATOR);
            GeoMultiPoint multiPoint = new GeoMultiPoint();
            multiPoint.add(point);
            values.put(com.nextgis.maplib.util.Constants.FIELD_GEOM, multiPoint.toBlob());

            Uri uri = Uri.parse("content://" + mApp.getAuthority() + "/" + Constants.PROBLEMS_LAYER_NAME);
            Uri result = mApp.getContentResolver().insert(uri, values);

            if (result != null)
                putAttaches(result.getLastPathSegment());

            sync();

            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void putAttaches(String lastPathSegment) {
        Uri uri = Uri.parse("content://" + mApp.getAuthority() + "/" + Constants.PROBLEMS_LAYER_NAME + "/" + lastPathSegment + "/attach");

        for (String path : mGallery.getImagesPath()) {
            String[] segments = path.split("/");
            String name = segments.length > 0 ? segments[segments.length - 1] : "image.jpg";
            ContentValues values = new ContentValues();
            values.put(VectorLayer.ATTACH_DISPLAY_NAME, name);
            values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");

            Uri result = getContentResolver().insert(uri, values);
            if (result != null) {
                try {
                    OutputStream outStream = getContentResolver().openOutputStream(result);
                    if (outStream == null)
                        return;

                    InputStream inStream = new FileInputStream(path);
                    byte[] buffer = new byte[2048];
                    int counter;

                    while ((counter = inStream.read(buffer, 0, buffer.length)) > 0) {
                        outStream.write(buffer, 0, counter);
                        outStream.flush();
                    }

                    outStream.close();
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
