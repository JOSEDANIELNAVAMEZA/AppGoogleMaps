package mx.ipn.cic.geo.googlemapsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import mx.ipn.cic.geo.googlemapsapp.databinding.ActivityMapsBinding

import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.isPermissionGranted
import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.requestPermission
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.getInstance
import java.util.Currency.getInstance


class MapsActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var marcador: Int = 1
    private var marcadorgen: String = "Marcador generico"

    private var permissionDenied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Ocultar el app bar.
        supportActionBar?.hide()


        //Conexion a FireBase
        //val database : mFirebaseConfig = FirebaseConfig.getInstance();
        //val referenciaBD : DatabaseReference = database.getReference("app_maps_firebase/coordenada_guardadas")
        // Asignar el código para cada uno de los eventos.

        binding.btnGUARDAR.setOnClickListener {
            Toast.makeText(this, "Se guardo $marcadorgen con localización en (${this.longitud} ${this.latitud})", Toast.LENGTH_LONG).show()
            val tmp = " (${this.longitud} ${this.latitud})"

        }
        binding.btnBORRAR.setOnClickListener {
            Toast.makeText(this, "Se borraron todos los registros", Toast.LENGTH_LONG).show()
            val referenciaBD = null
            //referenciaBD.child("coordenadas").removeValue()
        }
    }

    class FirebaseConfig {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap2: GoogleMap) {
        this.googleMap = googleMap2

        // Add a marker in Mexico City and move the camera
        val ciudadMexico = LatLng(19.432608, -99.133209)
        this.googleMap.addMarker(MarkerOptions().position(ciudadMexico).title("Ciudad de México").draggable(true))
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(ciudadMexico))
        this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(14F))


        // Modificar propiedades en tiempo de ejecución.
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        this.googleMap.isTrafficEnabled = true
        this.googleMap.isIndoorEnabled = true
        this.googleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                val location = LatLng(latlng.latitude,latlng.longitude)
                googleMap.addMarker(MarkerOptions().position(location).title("Marker $marcador").draggable(true))
                marcadorgen = "Marker $marcador"
                marcador++
                latitud = latlng.latitude
                longitud = latlng.longitude
            }
        })

        this.googleMap.setOnMarkerDragListener(object :GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {
            }

            override fun onMarkerDragEnd(p0: Marker) {
                latitud = p0.position.latitude
                longitud = p0.position.longitude
                marcadorgen = p0.title.toString()
            }

            override fun onMarkerDragStart(marker: Marker) {

            }
        });

        this.googleMap.setOnMyLocationButtonClickListener(this)
        this.googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    // https://developers.google.com/maps/documentation/android-sdk/location
    // https://github.com/googlemaps/android-samples/blob/master/ApiDemos/kotlin/app/src/gms/java/com/example/kotlindemos/MyLocationDemoActivity.kt

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Comprobar que la referencia al mapa googleMap es válida.
        if (!::googleMap.isInitialized) return
        // [START maps_check_location_permission]

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Ha presionado el botón de Posición Actual", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        // Obtener las coordenadas de latitud y longitud.
        val latitud = location.latitude
        val longitud = location.longitude

        Toast.makeText(this, "Posición Actual:\n($latitud, $longitud)", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

