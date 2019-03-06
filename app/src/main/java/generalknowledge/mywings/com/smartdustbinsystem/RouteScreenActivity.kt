package generalknowledge.mywings.com.smartdustbinsystem

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import generalknowledge.mywings.com.smartdustbinsystem.models.Dustbin
import generalknowledge.mywings.com.smartdustbinsystem.process.GetDustbinAsync
import generalknowledge.mywings.com.smartdustbinsystem.process.OnDustbinListener
import generalknowledge.mywings.com.smartdustbinsystem.process.ProgressDialogUtil
import kotlinx.android.synthetic.main.activity_route_screen.*
import org.json.JSONArray

class RouteScreenActivity : AppCompatActivity(),
    OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, OnDustbinListener {


    private var mMap: GoogleMap? = null
    private val SHOW_ICON_IN_MAP = 49
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var latLng: LatLng = LatLng(18.515665, 73.924090)
    private var locationManager: LocationManager? = null
    private lateinit var cPosition: Marker
    private lateinit var marker: Marker
    private lateinit var circle: Circle
    private var vId: Int = 0
    private lateinit var progressDialogUtil: ProgressDialogUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_screen)
        progressDialogUtil = ProgressDialogUtil(this)
        var frame = activity_place_map as SupportMapFragment
        frame.getMapAsync(this)


    }

    private fun setupMap() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val enabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (enabled) {
            var location = LocationUtil.getBestLastKnownLocation(this)

            latLng = LatLng(location.latitude, location.longitude)
        }

        mMap!!.uiSettings.isMyLocationButtonEnabled = false

        mGoogleApiClient = GoogleApiClient.Builder(this!!)

            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()



        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((1 * 1000).toLong())
        mGoogleApiClient!!.connect()

        val strokeColor = ContextCompat.getColor(this, R.color.map_circle_stroke)
        val shadeColor = ContextCompat.getColor(this, R.color.map_circle_shade)
        val latLng = this.latLng
        circle = mMap!!.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(5.0)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(2f)
        )

        val icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)

        marker = mMap!!.addMarker(MarkerOptions().position(latLng).icon(icon))
        val cameraPos = CameraPosition.Builder().tilt(60f).target(latLng).zoom(20f).build()
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), 1000, null)

        val intent = Intent(this@RouteScreenActivity, SelectVehicleActivity::class.java)
        startActivityForResult(intent, 1001)

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(map: GoogleMap?) {
        mMap = map

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupMap()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                SHOW_ICON_IN_MAP
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return

            if (null != marker) marker.remove()
            if (null != circle) circle.remove()

            latLng = LatLng(locationResult.locations[0].latitude, locationResult.locations[0].longitude)
            val icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)
            marker = mMap!!.addMarker(MarkerOptions().position(latLng).icon(icon))
            val cameraPos = CameraPosition.Builder().tilt(60f).target(latLng).zoom(20f).build()
            mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), 1000, null)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            mLocationRequest, locationCallback,
            Looper.myLooper()
        );
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            SHOW_ICON_IN_MAP ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    setupMap()
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    private fun initGetDustbin() {
        progressDialogUtil.show()
        val getDustbinAsync = GetDustbinAsync()
        getDustbinAsync.setOnDustbinListener(this, vId)
    }

    override fun onDustbinSuccess(result: JSONArray) {
        progressDialogUtil.hide()
        if (null != result) {
            var lst = ArrayList<Dustbin>()
            for (i in 0..(result.length() - 1)) {
                var node = Dustbin()
                val jNode = result.getJSONObject(i)
                node.id = jNode.getInt("Id")
                node.name = jNode.getString("Name")
                node.local = jNode.getString("LocalArea")
                node.latitude = jNode.getString("Latitude")
                node.longitude = jNode.getString("Longitude")
                node.weight = jNode.getString("Weight")
                node.moisture = jNode.getString("Moisture")
                node.vid = jNode.getInt("VId")
                lst.add(node)
                val nLatLng = LatLng(node.latitude.toDouble(), node.longitude.toDouble())
                var marker = MarkerOptions().position(nLatLng)
                mMap!!.addMarker(marker).title = "${node.name}"

            }


        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1001) {
                vId = data!!.getIntExtra("id", 0)
                initGetDustbin()
            }

        }
    }

}
