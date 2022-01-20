package de.willemer.gpsosm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements LocationListener {
    private static final String TAG = "GpsOsm";
    private final int MEINE_REQUEST_NUMMER = 123; // irgendwas
    private MapView map = null;
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sichere die OSM-Umgebung
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx,
                PreferenceManager
                        .getDefaultSharedPreferences(ctx));
        // Entfalte das Layout
        setContentView(R.layout.activity_main);
        // Initialisiere die MapView
        map = (MapView) findViewById(R.id.map);
        map.setMultiTouchControls(true); // Zoom-Tasten
        map.getController().setZoom(16.0); // Vergrößere die Karte
        frageUserNachBerechtigungen(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
        // Bereite GPS vor
        locationManager = (LocationManager)
            getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Die Activity wird sichtbar ...
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Darf ich GPS?
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "onResume: requestLoc");
            // Aktiviere die GPS-Kommunikation.
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, 10,
                    this);
        }
        map.onResume(); // aktualisiere Karte
    }

    /**
     * Die Activity verschwindet vom Display
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Darf ich GPS?
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "onPause: requestLoc");
            // Stoppe die GPS-Kommunikation.
            locationManager.removeUpdates(this);
        }
        map.onPause(); // Stoppe die Karte
    }

    /**
     * Lese die Ergebnisse des Berechtigungsdialogs aus.
     * @param requestCode Wer hat gerufen: Die 123 von oben
     * @param permissions Die verbleibenden Berechtigungen
     * @param grantResults Die Antworten des Benutzers
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
               String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        // Sammle die verbleibenden Berechtigungen
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            // Da fehlt noch etwas.
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    MEINE_REQUEST_NUMMER);
        }
    }

    /**
     * Der Benutzer muss bei dangerous permissions
     * per Dialog explizit gefragt werden.
     * @param permissions
     */
    private void frageUserNachBerechtigungen(
            String[] permissions) {
        ArrayList<String> permissionsToRequest
                = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    permission)
                != PackageManager.PERMISSION_GRANTED)
            {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(
                            new String[0]),
                    MEINE_REQUEST_NUMMER);
        }
    }

    /**
     * GPS hat eine Standortänderung festgestellt.
     * @param loc Die neue Position
     */
    @Override
    public void onLocationChanged(@NonNull Location loc) {
        Log.d(TAG,"onLocationChanged");
        GeoPoint geo = new GeoPoint(loc.getLatitude(),
                loc.getLongitude());
        map.getController().setCenter(geo);
        map.invalidate();
    }
}
