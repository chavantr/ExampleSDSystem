package generalknowledge.mywings.com.smartdustbinsystem

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import com.mywings.messmanagementsystem.routes.Constants
import generalknowledge.mywings.com.smartdustbinsystem.models.Dustbin
import generalknowledge.mywings.com.smartdustbinsystem.models.UserInfoHolder
import generalknowledge.mywings.com.smartdustbinsystem.process.GetDustbinAsync
import generalknowledge.mywings.com.smartdustbinsystem.process.OnDustbinListener
import generalknowledge.mywings.com.smartdustbinsystem.process.ProgressDialogUtil
import generalknowledge.mywings.com.smartdustbinsystem.routes.DirectionsJSONParser
import generalknowledge.mywings.com.smartdustbinsystem.routes.JsonUtil
import kotlinx.android.synthetic.main.activity_route_screen_with_navigation.*
import kotlinx.android.synthetic.main.app_bar_route_screen_activity_with_navigation.*
import kotlinx.android.synthetic.main.content_route_screen_activity_with_navigation.*
import kotlinx.android.synthetic.main.layout_dustbin_row.view.*
import kotlinx.android.synthetic.main.nav_header_route_screen_activity_with_navigation.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RouteScreenActivityWithNavigation : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, OnDustbinListener {

    private var mMap: GoogleMap? = null
    private val SHOW_ICON_IN_MAP = 49
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var latLng: LatLng = LatLng(18.538811, 73.831981)
    private var locationManager: LocationManager? = null

    private lateinit var marker: Marker
    private lateinit var circle: Circle
    private var vId: Int = 0
    private lateinit var progressDialogUtil: ProgressDialogUtil

    private lateinit var jsonUtil: JsonUtil
    private lateinit var nsource: String
    private lateinit var ndest: String
    private var destlat: Double = 0.0
    private var destlng: Double = 0.0
    private var srctlat: Double = 0.0
    private var srclng: Double = 0.0
    private lateinit var points: ArrayList<Dustbin>

    private lateinit var latLngPoints: ArrayList<LatLng>

    //private lateinit var timer: Timer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_screen_with_navigation)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        progressDialogUtil = ProgressDialogUtil(this)

        var frame = activity_place_map as SupportMapFragment

        frame.getMapAsync(this)

        jsonUtil = JsonUtil()

        nsource = ""
        ndest = ""

        //timer = Timer()

        var view = nav_view.getHeaderView(0)

        view.lblHeaderName.text = UserInfoHolder.getInstance().user.name

        view.lblUser.text = UserInfoHolder.getInstance().user.username

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.route_screen_activity_with_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                initGetDustbin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_help -> {
                val intent = Intent(this@RouteScreenActivityWithNavigation, HelpActivity::class.java)
                startActivity(intent)
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_logout -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                val intent = Intent(this@RouteScreenActivityWithNavigation, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                return true
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun setupMap() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val enabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        try {
            if (enabled) {
                var location = LocationUtil.getBestLastKnownLocation(this)
                latLng = LatLng(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            Log.e("test", e.printStackTrace().toString())
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

        mMap!!.setInfoWindowAdapter(infoWindowAdapter)

        val intent = Intent(this@RouteScreenActivityWithNavigation, SelectVehicleActivity::class.java)
        startActivityForResult(intent, 1001)

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(map: GoogleMap?) {
        mMap = map

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupMap()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                SHOW_ICON_IN_MAP
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            if (null != marker) marker.remove()
            if (null != circle) circle.remove()
            val speed = locationResult.locations[0].speed
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

    override fun onLocationChanged(location: Location?) {
        val speed = location!!.speed
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
            latLngPoints = ArrayList<LatLng>()
            points = ArrayList<Dustbin>()
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

                val location = Location("Current location")
                location.longitude = latLng.longitude
                location.latitude = latLng.latitude
                val locationN = Location("Location")
                locationN.latitude = node.latitude.toDouble()
                locationN.longitude = node.longitude.toDouble()
                node.distance = location.distanceTo(locationN).toInt()
                lst.add(node)


            }

            Collections.sort(lst, SortDistanceWithGenetic())
            UserInfoHolder.getInstance().dustbin = lst


            for (i in lst.indices) {
                val nLatLng = LatLng(lst[i].latitude.toDouble(), lst[i].longitude.toDouble())
                val icon =
                    if (checkIcon(lst[i])) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) else BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                    )
                var marker = MarkerOptions().position(nLatLng).snippet(i.toString())
                var n = mMap!!.addMarker(marker)
                n.setIcon(icon)
                n.title = "${lst[i].name}"
            }

            latLngPoints.clear()
            latLngPoints.add(latLng)
            for (i in lst.indices) {
                if (`50withMoisture`(lst[i]) || `40withMoisture`(lst[i])) {
                    points.add(lst[i])
                    latLngPoints.add(LatLng(lst[i].latitude.toDouble(), lst[i].longitude.toDouble()))
                }
            }
            if (latLngPoints.isNotEmpty()) {
                for (i in latLngPoints.indices) {
                    if (i < latLngPoints.size - 1) {
                        val str = getDirectionsUrl(
                            latLngPoints[i],
                            latLngPoints[i + 1]
                        )
                        val downloadTask = InitializePopulationOfGenetic()
                        downloadTask.execute(str)
                    }
                }
                //timer.schedule(checkDistanceWithUpdate, 10000 * 60, 10000 * 60)
            }
        }
    }

    private fun checkIcon(lst: Dustbin): Boolean =
        `50withMoisture`(lst) || `40withMoisture`(lst)

    private fun `50withMoisture`(node: Dustbin) =
        node.weight.toInt() > 50 && node.moisture.equals("false", true)

    private fun `40withMoisture`(node: Dustbin) =
        node.weight.toInt() > 40 && node.moisture.equals("true", true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1001) {
                vId = data!!.getIntExtra("id", 0)
                initGetDustbin()
            }
        }
    }

    inner class SortDistanceWithGenetic : Comparator<Dustbin> {
        override fun compare(left: Dustbin?, right: Dustbin?): Int {
            return left!!.distance - right!!.distance
        }
    }

    private val infoWindowAdapter = object : GoogleMap.InfoWindowAdapter {
        override fun getInfoContents(marker: Marker?): View? {
            var view: View? = null
            try {
                if (marker!!.tag != 1) {
                    view = layoutInflater.inflate(R.layout.layout_dustbin_row, null)
                    val i = marker.snippet.toInt()
                    view!!.lblName.text = "Name : " + UserInfoHolder.getInstance().dustbin[i].name
                    view!!.lblWeight.text = "Weight : " + UserInfoHolder.getInstance().dustbin[i].weight
                    view!!.lblMoisture.text =
                        "Moisture : " + if (UserInfoHolder.getInstance().dustbin[i].moisture.equals(
                                "true",
                                true
                            )
                        ) "Yes" else "No"
                }
            } catch (e: Exception) {
                view!!.lblName.text = "Vehicle location"
                view!!.lblWeight.visibility = View.GONE
                view!!.lblMoisture.visibility = View.GONE
            }
            return view;
        }

        override fun getInfoWindow(marker: Marker?): View? {
            return null
        }

    }

    // IMP

    private var key = "&key=AIzaSyClCN7T0VPX7MIoOJEMA3W9JLXhV_S7yx4"

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        val strOrigin = ("origin=" + origin.latitude + ","
                + origin.longitude)
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        val sensor = "sensor=false"

        val parameters = "$strOrigin&$strDest&$sensor$key"
        val output = "json"
        return ("https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters)
    }

    private inner class InitializePopulationOfGenetic : AsyncTask<String, Void, String>() {

        // Downloading data in non-ui thread
        override fun doInBackground(vararg url: String): String {

            // For storing data from web service
            var data = ""

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())

            }

            return data
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            val parserTask = GeneticParserTask(mMap!!)

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)

        }
    }

    /** A method to download json data from url  */
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection

            // Connecting to url
            urlConnection.connect()

            iStream = urlConnection.inputStream

            data = jsonUtil.convertStreamToString(iStream)

        } catch (e: Exception) {

        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /** A class to parse the Google Places in JSON format  */
    private inner class GeneticParserTask(internal var map: GoogleMap?) :
        AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(
            vararg jsonData: String
        ): List<List<HashMap<String, String>>>? {

            val jObject: JSONObject
            var jArray: JSONArray
            var routes: List<List<HashMap<String, String>>>? = null

            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return routes
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {

            //progressDialogUtil.show()

            var points: java.util.ArrayList<LatLng>? = null

            var lineOptions: PolylineOptions? = null

            // MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (i in result.indices) {
                points = java.util.ArrayList()
                lineOptions = PolylineOptions()
                // Fetching i-th route
                val path = result[i]
                // Fetching all the points in i-th route
                for (j in path.indices) {
                    // lineOptions = new PolylineOptions();
                    val point = path[j]
                    val lat = java.lang.Double.parseDouble(point[Constants.LAT]!!)
                    val lng = java.lang.Double.parseDouble(point[Constants.LNG]!!)
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(9f)
                lineOptions.color(Color.RED)
            }

            // Drawing polyline in the Google Map for the i-th route

            //map!!.clear()

            if (null != lineOptions) {
                map!!.addPolyline(lineOptions)
                setStartPosition(srctlat, srclng)
                setDestPosition(destlat, destlng)
                if (map != null) {
                    fixZoom(lineOptions.points)
                }

                progressDialogUtil.hide()

            } else {
                Toast.makeText(
                    this@RouteScreenActivityWithNavigation,
                    "Enable to draw routes, Please try again",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    /**
     * @param lat
     * @param lng
     */
    private fun setStartPosition(lat: Double, lng: Double) {
        var startmark = mMap!!.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(nsource)
                .snippet("")
        )
        startmark.tag = 1
    }

    /**
     * @param lat
     * @param lng
     */
    private fun setDestPosition(lat: Double, lng: Double) {
        var destmark = mMap!!.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .title(ndest)
                .snippet("")
        )

        destmark.tag = 1
    }


    private fun fixZoom(points: List<LatLng>) {
        val bc = LatLngBounds.Builder()
        for (item in points) {
            bc.include(item)
        }
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 90))
    }


    private val checkDistanceWithUpdate = object : TimerTask() {
        override fun run() {
            initGetDustbin()
        }

    }
}
