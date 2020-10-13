package com.example.testapp


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import khttp.get
import khttp.post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {


    private lateinit var mMap: GoogleMap

    private var mSearchText: EditText? = null

    private val TAG = "MapActivity"

    private var descriptionShowingFlag = false

    private var lastMarketLatLng: LatLng? = null
    private var markerList: ArrayList<Marker> = arrayListOf()

    private var clickedMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        mSearchText = findViewById(R.id.input_search);
        ic_magnify.setOnClickListener {
            geoLocate()
        }
        cancel.setOnClickListener {
            descriptionPad.visibility = View.GONE
            markerList.last().remove()
            markerList.dropLast(1)
        }

        add.setOnClickListener {
            CoroutineScope(IO).launch {
                val lat = lastMarketLatLng?.latitude
                val lng = lastMarketLatLng?.longitude
                if (lat != null && lng != null) {
                    fakeSetPoint(lat, lng, name.text.toString(), description.text.toString(), 0, 0 )
                }
            }
            descriptionPad.visibility = View.GONE
            name.text.clear()
            description.text.clear()
            hideKeyboard()
        }

        delete.setOnClickListener {

            val lat = clickedMarker?.position?.latitude
            val lng = clickedMarker?.position?.longitude
            CoroutineScope(IO).launch {
                if (lat != null && lng != null) {
                    fakeDelPoint(lat, lng)
                }
            }
            descriptionInfoPad.visibility = View.GONE
            clickedMarker?.remove()

        }

        init()

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val spb = LatLng(59.937500, 30.308611)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spb, 10f))
        mMap.setOnMarkerClickListener { marker ->
            clickedMarker = marker
            val lat = marker.position.latitude
            val lng = marker.position.longitude
            if (descriptionShowingFlag) {
                descriptionInfoPad.visibility = View.GONE
                descriptionShowingFlag = false
            }
            else {
                CoroutineScope(IO).launch {
                    fakeGetPoint(lat, lng)
                }
                descriptionShowingFlag = true
            }
            true
        }

        CoroutineScope(IO).launch {
            fakeGetPoints()
        }
        init()
    }

    override fun onMapClick(point: LatLng) {


        if (descriptionShowingFlag) {
            descriptionInfoPad.visibility = View.GONE
            descriptionShowingFlag = false
        } else {
            markerList.add(mMap.addMarker(MarkerOptions().position(point).title("Marker in $point")))
            descriptionPad.visibility = View.VISIBLE
            lastMarketLatLng = point
        }

        Log.d("click", "yes")
    }

    private suspend fun getPoint(lat: Double, lng:Double): String {
        delay(1000)
        val params = mapOf("lat" to lat.toString(), "lng" to lng.toString())
        val r = get("http://643610d6a860.ngrok.io/point", params=params)
        return r.text
    }

    private suspend fun delPoint(lat: Double, lng:Double): String {
        delay(1000)
        val params = mapOf("lat" to lat.toString(), "lng" to lng.toString())
        val r = get("http://643610d6a860.ngrok.io/deletePoint", params=params)
        return r.text
    }

    private suspend fun setPoint(lat: Double, lng:Double, name: String, description : String, like: Int, dislike : Int): String {
        delay(1000)
        val info = mapOf("name" to name.toString(), "description" to description.toString(), "like" to like.toString(), "dislike" to dislike.toString())
        val data = mapOf("lat" to lat.toString(), "lng" to lng.toString(), "info" to JSONObject(info))
        val r = post("http://643610d6a860.ngrok.io/point", data=data)
        return r.text
    }

    private suspend fun getPoints(): String {
        delay(1000)
        val r = get("http://643610d6a860.ngrok.io/points")
        return r.text
    }


    private fun setInfo(input: String){
        Log.d("JSON", input)
        val json = JSONObject("$input")
        Log.d("JSON", json.toString())
//        val newText = name.text.toString() + "\n$input"

        nameInfo.text = json.getString("name")
        descriptionInfo.text = json.getString("description")
        descriptionInfoPad.visibility = View.VISIBLE
    }

    private fun setPoints(input: String){
        val json = JSONObject("$input")
        val jsonKeys = json.keys()
        for(latlng in jsonKeys) {
            val latlngList = latlng.split("|")
            Log.d("PLACE", latlngList.toString())
            val lat = latlng[0]
            val lng = latlng[1]
            val place = LatLng(latlngList[0].toDouble(), latlngList[1].toDouble())
            Log.d("PLACE", lat.toString())
            Log.d("PLACE", place.toString())
            mMap.addMarker(MarkerOptions().position(place).title("$place"))
        }
    }

    private suspend fun setInfoOnMainThread(input: String) {
        withContext (Main) {
            setInfo(input)
        }
    }

    private suspend fun setPointsOnMainThread(input: String) {
        withContext (Main) {
            setPoints(input)
        }
    }

    private suspend fun fakeGetPoint(lat : Double, lng : Double) {
        val result = getPoint(lat, lng) // wait until job is done
        setInfoOnMainThread(result)
    }

    private suspend fun fakeGetPoints() {
        val result = getPoints() // wait until job is done
        setPointsOnMainThread(result)
    }

    private suspend fun fakeSetPoint(lat : Double, lng : Double, name: String, description : String, like: Int, dislike : Int) {
        val result = setPoint(lat, lng, name, description, like, dislike) // wait until job is done
    }

    private suspend fun fakeDelPoint(lat : Double, lng : Double) {
        val result = delPoint(lat, lng) // wait until job is done
    }





    private fun init() {
        Log.d(this.TAG, "init: initializing")
        mSearchText?.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action === KeyEvent.KEYCODE_ENTER
            ) {

                //execute our method for searching
                geoLocate()
            }
            false
        })
    }
    public fun find() {
        geoLocate()
    }
    private fun geoLocate() {
        Log.d(this.TAG, "geoLocate: geolocating")
        val searchString: String = mSearchText?.text.toString()
        val geocoder = Geocoder(this@MapsActivity)
        var list: List<Address> = ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            Log.e(this.TAG, "geoLocate: IOException: " + e.message)
        }
        if (list.size > 0) {
            val address: Address = list[0]
            Log.d(
                this.TAG,
                "geoLocate: found a location: " + address.toString()
            )
            val lat = address.latitude
            val lng = address.longitude
            val place = LatLng(lat, lng)
//            mMap.addMarker(MarkerOptions().position(place).title(address.featureName))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 10f))
            hideKeyboard()
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}




