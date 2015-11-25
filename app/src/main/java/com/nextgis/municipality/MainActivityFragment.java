package com.nextgis.municipality;

import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.mapui.MapViewOverlays;
import com.nextgis.maplibui.overlay.CurrentLocationOverlay;
import com.nextgis.maplibui.util.ConstantsUI;

import java.util.List;

public class MainActivityFragment extends Fragment implements MapViewEventListener {
    private MapViewOverlays mMap;
    private CurrentLocationOverlay mCurrentLocationOverlay;
    private float mTolerancePX;
    private VectorLayer mLayer;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        MainApplication app = (MainApplication) getActivity().getApplication();
        mMap = new MapViewOverlays(getActivity(), (MapDrawable) app.getMap());
        mCurrentLocationOverlay = new CurrentLocationOverlay(getActivity(), mMap);
        mCurrentLocationOverlay.setStandingMarker(R.drawable.ic_location_standing);
        mCurrentLocationOverlay.setMovingMarker(R.drawable.ic_location_moving);
        mMap.addOverlay(mCurrentLocationOverlay);

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.maprl);
        layout.addView(mMap, 0, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mMap.invalidate();

        mLayer = (VectorLayer) mMap.getMap().getLayerByName(Constants.PROBLEMS_LAYER_NAME);
        mTolerancePX = getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;
        mMap.addListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentLocationOverlay.startShowingCurrentLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentLocationOverlay.stopShowingCurrentLocation();
    }

    @Override
    public void onLongPress(MotionEvent event) {

    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if (mLayer != null)
            selectProblemPoint(event);
    }

    private void selectProblemPoint(MotionEvent event) {
        double dMinX = event.getX() - mTolerancePX;
        double dMaxX = event.getX() + mTolerancePX;
        double dMinY = event.getY() - mTolerancePX;
        double dMaxY = event.getY() + mTolerancePX;
        GeoEnvelope screenEnv = new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY);
        GeoEnvelope mapEnv = mMap.screenToMap(screenEnv);
        List<Long> items = mLayer.query(mapEnv);

        if (!items.isEmpty()) {
            String selection = com.nextgis.maplib.util.Constants.FIELD_ID + " = ?";
            Cursor attributes = mLayer.query(new String[]{Constants.FIELD_DESC}, selection, new String[]{items.get(items.size() - 1) + ""}, null, null);

            if (attributes.moveToFirst())
                Snackbar.make(mMap, attributes.getString(0), Snackbar.LENGTH_SHORT).show();

            attributes.close();
        }
    }

    @Override
    public void panStart(MotionEvent e) {

    }

    @Override
    public void panMoveTo(MotionEvent e) {

    }

    @Override
    public void panStop() {

    }

    @Override
    public void onLayerAdded(int id) {

    }

    @Override
    public void onLayerDeleted(int id) {

    }

    @Override
    public void onLayerChanged(int id) {

    }

    @Override
    public void onExtentChanged(float zoom, GeoPoint center) {

    }

    @Override
    public void onLayersReordered() {

    }

    @Override
    public void onLayerDrawFinished(int id, float percent) {

    }

    @Override
    public void onLayerDrawStarted() {

    }
}
