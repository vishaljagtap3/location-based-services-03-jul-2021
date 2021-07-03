package `in`.bitcode.locationbasedservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    lateinit var myLocationListener: MyLocationListener

    inner class MyLocationUpdateBR : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if(intent?.action.equals("in.bitcode.my.LOCATION") ) {
                var loc = intent?.getParcelableExtra<Location>(LocationManager.KEY_LOCATION_CHANGED)
                mt("Single location update: ${loc?.latitude} ${loc?.longitude}")
            }

            if(intent?.action.equals("in.bitcode.OFFICE") ) {
                var entering = intent?.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)
                if(entering == true) {
                    mt("You are at Office...")
                }
                else {
                    mt("You have left the office...")
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        for (provider in locationManager.allProviders) {
            mt("---> $provider");
            var locationProvider = locationManager.getProvider(provider)
            mt("Power: ${locationProvider?.powerRequirement}")
            mt("Sat Cell N/W ${locationProvider?.requiresSatellite()} ${locationProvider?.requiresCell()} ${locationProvider?.requiresNetwork()}")
            mt("Cost: ${locationProvider?.hasMonetaryCost()}");
            mt("Alt Support: ${locationProvider?.supportsAltitude()}")
            mt("Accuracy: ${locationProvider?.accuracy}");

            var lastKnownLocation: Location? = locationManager.getLastKnownLocation(provider)
            if (lastKnownLocation != null) {
                mt("Last known loc: ${lastKnownLocation.latitude} ${lastKnownLocation.longitude}")
            }
        }

        var criteria = Criteria()
        criteria.isCostAllowed = true
        criteria.isAltitudeRequired = true
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.accuracy = Criteria.ACCURACY_FINE

        var bestProvider: String? = locationManager.getBestProvider(criteria, true)
        mt("Best Provider $bestProvider");


        myLocationListener = MyLocationListener()

        if (bestProvider != null) {
            locationManager.requestLocationUpdates(
                bestProvider,
                //LocationManager.NETWORK_PROVIDER,
                3000,
                500f,
                //MyLocationListener()
                myLocationListener
            )
        }

        //locationManager.removeUpdates(myLocationListener)

        var locationUpdateBR = MyLocationUpdateBR()
        var intentFilter = IntentFilter("in.bitcode.my.LOCATION")
        intentFilter.addAction("in.bitcode.OFFICE")

        registerReceiver(
            locationUpdateBR,
            intentFilter
        )

        locationManager.requestSingleUpdate(
            LocationManager.NETWORK_PROVIDER,
            PendingIntent.getBroadcast(
                this,
                1,
                Intent("in.bitcode.my.LOCATION"),
                0
            )
        )

        if (bestProvider != null) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000,
                200F,
                PendingIntent.getBroadcast(
                    this,
                    1,
                    Intent("in.bitcode.my.LOCATION"),
                    0
                )
            )
        }

       /* locationManager.removeUpdates(
            PendingIntent.getBroadcast(
                this,
                1,
                Intent("in.bitcode.my.LOCATION"),
                0
            )
        )*/


        //proximity alerts
        locationManager.addProximityAlert(
            18.52271483, 73.76778349,
            2000F,
            //System.currentTimeMillis() + 2 * 60 * 60 * 1000,
            -1,
            PendingIntent.getBroadcast(
                this,
                2,
                Intent("in.bitcode.OFFICE"),
                0
            )
        )

       /* locationManager.removeProximityAlert(
            PendingIntent.getBroadcast(
                this,
                2,
                Intent("in.bitcode.OFFICE"),
                0
            )
        )*/
    }

    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            mt("location: ${location.latitude} ${location.longitude}")
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
        }

        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }
    }

    private fun mt(text: String) {
        Log.e("tag", text)
    }
}