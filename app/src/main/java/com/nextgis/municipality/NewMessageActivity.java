package com.nextgis.municipality;

import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.util.GeoConstants;

public class NewMessageActivity extends AppCompatActivity {
    private Spinner mTypes;
    private EditText mDesc, mName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTypes = (Spinner) findViewById(R.id.problem_type);
        mDesc = (EditText) findViewById(R.id.problem_desc);
        mName = (EditText) findViewById(R.id.problem_cred);
        findViewById(R.id.problem_submit).setOnClickListener(new View.OnClickListener() {
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

    private boolean sendMessage() {
        try {
            final MainApplication app = (MainApplication) getApplication();
            Location location = app.getGpsEventSource().getLastKnownLocation();

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

            Uri uri = Uri.parse("content://" + app.getAuthority() + "/" + Constants.PROBLEMS_LAYER_NAME);
            Uri result = app.getContentResolver().insert(uri, values);

            return result != null;
        } catch (Exception e) {
            return false;
        }
    }
}
