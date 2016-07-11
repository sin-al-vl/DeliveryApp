package com.prideapp.deliveryapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.prideapp.deliveryapp.Dialogs.UserMarkerDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MyMaps extends FragmentActivity implements OnMapReadyCallback, UserMarkerDialog.onMarkerEventListener {

    private GoogleMap map;

    private ArrayList<LatLng> markerSystemPosition, markerUserPosition;
    private ArrayList<String> markerUserInfo;
    private Marker currentUserMarker = null;

    private final String FILE_NAME_MARKER_SYSTEM = "System markers",
            FILE_NAME_MARKER_USER = "User markers";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        writeSystemMarkerInFile(markerSystemPosition);
        writeUserMarkerInFile(markerUserPosition, markerUserInfo);
    }

    private void initMarkers() {
        File fileSystem = new File(getFilesDir() + "/" + FILE_NAME_MARKER_SYSTEM);
        File fileUser = new File(getFilesDir() + "/" + FILE_NAME_MARKER_USER);

        markerUserPosition = new ArrayList<>();
        markerUserInfo = new ArrayList<>();

        if (fileUser.exists())
            readUserMarkerFromFile(markerUserPosition, markerUserInfo);

        if (fileSystem.exists())
            markerSystemPosition = readSystemMarkerFromFile();
        else {

            markerSystemPosition = new ArrayList<>();
            markerSystemPosition.add(0, new LatLng(0, 0));
            markerSystemPosition.add(1, new LatLng(50.005724, 36.232893));
            markerSystemPosition.add(2, new LatLng(50.026378, 36.220295));
            markerSystemPosition.add(3, new LatLng(49.980287, 36.253636));
        }
    }

    private void addMarkersOnMap(GoogleMap map) {
        for (int i = 1; i < markerSystemPosition.size(); i++)
            map.addMarker(new MarkerOptions()
                    .position(markerSystemPosition.get(i))
                    .title(getString(R.string.maps_activity_default_marker_title) + i)
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    ));

        if (markerUserPosition.size() > 0)
            for (int i = 0; i < markerUserPosition.size(); i++)
                map.addMarker(new MarkerOptions()
                        .position(markerUserPosition.get(i))
                        .title(markerUserInfo.get(i))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker()
                        ));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        initMarkers();
        addMarkersOnMap(map);

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {                //show DB or change info
            @Override
            public void onInfoWindowClick(Marker marker) {
                boolean isStockMarker = false;

                for (int i = 0; i < markerSystemPosition.size(); i++)
                    if (marker.getPosition().equals(markerSystemPosition.get(i))) {

                        Intent intent = new Intent(getBaseContext(), MapStockView.class);
                        intent.putExtra(MapStockView.STOCK_NUMBER, i);
                        startActivities(new Intent[]{intent});

                        isStockMarker = true;
                        break;
                    }

                if (!isStockMarker) {
                    askMarkerInfo(marker);
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {                      //put users marker
            @Override
            public void onMapLongClick(LatLng latLng) {

                markerUserPosition.add(latLng);

                Marker marker = map.addMarker(new MarkerOptions().position(latLng).draggable(true).
                        icon(BitmapDescriptorFactory.defaultMarker()
                        ));

                askMarkerInfo(marker);
            }
        });


        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            int pos;

            @Override
            public void onMarkerDragStart(Marker marker) {
                for (int i = 0; i < markerUserPosition.size(); i++) {
                    if (marker.getPosition().equals(markerUserPosition.get(i))) {
                        pos = i;
                        break;
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                markerUserPosition.set(pos, marker.getPosition());
            }
        });


        //Code of moving camera when activity started instead of .animateCamera()
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(getKharkovLocation(), 0));
                map.setOnCameraChangeListener(null);
            }
        });

    }

    private void askMarkerInfo(Marker marker) {

        currentUserMarker = marker;

        UserMarkerDialog userMarkerDialog = new UserMarkerDialog();
        userMarkerDialog.show(getFragmentManager(), "InfoWindowDialog");
    }

    private LatLngBounds getKharkovLocation() {
        return new LatLngBounds(new LatLng(49.911284, 36.100117), new LatLng(50.059628, 36.385761));
    }

    public void writeSystemMarkerInFile(ArrayList<LatLng> pos) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILE_NAME_MARKER_SYSTEM, MODE_PRIVATE)));

            String str = "";

            for (int i = 0; i < pos.size(); i++)
                str += pos.get(i).latitude + " " + pos.get(i).longitude + "\n";

            bw.write(str);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUserMarkerInFile(ArrayList<LatLng> pos, ArrayList<String> info) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILE_NAME_MARKER_USER, MODE_PRIVATE)));

            String str = "";
            for (int i = 0; i < pos.size(); i++)
                str += pos.get(i).latitude + " " + pos.get(i).longitude + " " + info.get(i) + "\n";

            bw.write(str);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LatLng> readSystemMarkerFromFile() {

        ArrayList<LatLng> markerPos = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILE_NAME_MARKER_SYSTEM)));
            String str;

            while ((str = br.readLine()) != null) {
                markerPos.add(new LatLng(
                        Double.valueOf(str.substring(0, str.indexOf(" "))),
                        Double.valueOf(str.substring(str.indexOf(" ") + 1, str.length()))));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return markerPos;
    }

    public void readUserMarkerFromFile(ArrayList<LatLng> markerPos, ArrayList<String> info) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILE_NAME_MARKER_USER)));
            String str;

            while ((str = br.readLine()) != null) {

                Double latitude = Double.valueOf(str.substring(0, str.indexOf(" ")));
                str = str.substring(str.indexOf(" ") + 1);
                Double longitude = Double.valueOf(str.substring(0, str.indexOf(" ")));
                str = str.substring(str.indexOf(" ") + 1);

                markerPos.add(new LatLng(latitude, longitude));
                info.add(str);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private int defineCurrentMarkerPos(){
        int pos = -1;
        for (int i = 0; i < markerUserPosition.size(); i++)
            if (currentUserMarker.getPosition().equals(markerUserPosition.get(i))) {
                pos = i;
                break;
            }

        return pos;
    }

    @Override
    public void dialogLoadedEvent(EditText title){
        title.setText(currentUserMarker.getTitle());
    }

    @Override
    public void changeMarkerEvent(String title) {

        int pos = defineCurrentMarkerPos();

        if (markerUserInfo.size() > pos)
            markerUserInfo.set(pos, title);
        else
            markerUserInfo.add(title);

        currentUserMarker.setTitle(title);

        currentUserMarker = null;
    }

    @Override
    public void deleteMarkerEvent() {

        int pos = defineCurrentMarkerPos();

        markerUserPosition.remove(pos);

        if (markerUserInfo.size() > pos)
            markerUserInfo.remove(pos);

        currentUserMarker.remove();

        currentUserMarker = null;
    }

}
