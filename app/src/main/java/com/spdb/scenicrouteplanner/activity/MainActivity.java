package com.spdb.scenicrouteplanner.activity;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.PermissionsClassLib;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.OSMParser;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    // ==============================
    // Private fields
    // ==============================
    private Dictionary<Integer, String> notGrantedPermissionsDict = new Hashtable<>();

    // ==============================
    // Override ActivityCompat
    // ==============================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        int notGrantedPermissionsCount = notGrantedPermissionsDict.size();

        if (notGrantedPermissionsCount == 0) {
            Fragment map = new MapActivity();
            attachFragment(map, false, null);
        } else {
            String[] notGrantedPermissionsArray = new String[notGrantedPermissionsDict.size()];
            Collections.list(notGrantedPermissionsDict.elements()).toArray(notGrantedPermissionsArray);
            ActivityCompat.requestPermissions(this, notGrantedPermissionsArray,
                    PermissionsClassLib.SOME_PERMISSIONS_CODE);
        }

        final ImageButton findRouteButton = findViewById(R.id.find_route_button);
        findRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                Fragment routePlanner = new RoutePlannerActivity();
                attachFragment(routePlanner, true, new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            final ImageButton findRouteButton = findViewById(R.id.find_route_button);
                            findRouteButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        OSMParser parser = new OSMParser();
        Model model = parser.parseOSMFile(Environment.getExternalStorageDirectory() + "/SRP/maps/osm");
        //model.printAll();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case PermissionsClassLib.WRITE_EXTERNAL_STORAGE_CODE:
                case PermissionsClassLib.ACCESS_FINE_LOCATION_CODE: {
                    serviceRequestPermission(requestCode, grantResults[0]);
                }
                case PermissionsClassLib.SOME_PERMISSIONS_CODE: {
                    for (int i = 0; i < permissions.length; ++i) {
                        serviceRequestPermission(PermissionsClassLib.getRequestPermissionCode(permissions[i]), grantResults[i]);
                    }
                }
            }
        }

        if (notGrantedPermissionsDict.size() == 0) {
            Fragment map = new MapActivity();
            attachFragment(map, false, null);
        }
    }


    // ==============================
    // Private methods
    // ==============================
    private void attachFragment(Fragment fragment, boolean userCanNavigateBack,
                                FragmentManager.OnBackStackChangedListener backStackChangedListener) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.content_frame_layout, fragment);
        if (userCanNavigateBack) {
            fragmentTransaction.addToBackStack(null);
            fragmentManager.addOnBackStackChangedListener(backStackChangedListener);
        }

        fragmentTransaction.commit();
    }

    private void checkPermissions() {
        for (String p : PermissionsClassLib.DANGEROUS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED)
                notGrantedPermissionsDict.put(PermissionsClassLib.getRequestPermissionCode(p), p);
        }
    }

    private void serviceRequestPermission(int permCode, int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissionsDict.remove(permCode);
        } else {
            // Ładne wyjście z apki
        }
    }
}
