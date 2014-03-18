package course.labs.locationlab;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceViewActivity extends ListActivity implements LocationListener {
	private static final long FIVE_MINS = 5 * 60 * 1000;

	private static String TAG = "Lab-Location";

	// The last valid location reading
	private Location mLastLocationReading;
	
	// The ListView's adapter
	private PlaceViewAdapter mAdapter;

	// default minimum time between new location readings
	private long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private float mMinDistance = 1000.0f;

	// Reference to the LocationManager 
	private LocationManager mLocationManager;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // TO DO - Set up the app's user interface
		// This class is a ListActivity, so it has its own ListView 
		// ListView's adapter should be a PlaceViewAdapter
        mAdapter  = new PlaceViewAdapter(getApplicationContext());

		// TO DO - add a footerView to the ListView
        LayoutInflater inflater = this.getLayoutInflater();
        final TextView footerView = (TextView) inflater.inflate(R.layout.footer_view, null);

        getListView().addFooterView(footerView);

        footerView.setClickable(true);
        setListAdapter(mAdapter);

        		// You can use footer_view.xml to define the footer
		// footerView must respond to user clicks.
		// Must handle 3 cases:
		// 1) The current location is new - download new Place Badge
		// 2) The current location has been seen before - issue Toast message
		// 3) There is no current location - response is up to you. The best
		// solution is to disable the footerView until you have a location.

        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the footerView's onClick() method is called, it must issue the follow log call
                log("Entered footerView.OnClickListener.onClick()");

                if (mLastLocationReading==null){
                    log("Location data is not available");
                    Toast.makeText(PlaceViewActivity.this, "Sorry, Location data not available. Try again later.", Toast.LENGTH_LONG).show();

                } else if(mAdapter.intersects (mLastLocationReading)){
                    log("You already have this location badge");
                    Toast.makeText(PlaceViewActivity.this,
                            "You already have this location badge",
                            Toast.LENGTH_LONG).show();
                } else {
                    log("Starting Place Download");
                    new PlaceDownloaderTask(PlaceViewActivity.this).execute(mLastLocationReading);
                }

            }
        });


	}

	@Override
	protected void onResume() {
		super.onResume();

		mMockLocationProvider = new MockLocationProvider(
				LocationManager.NETWORK_PROVIDER, this);

		//TO DO - Check NETWORK_PROVIDER for an existing location reading.
		// Only keep this last reading if it is fresh - less than 5 minutes old.
        mLastLocationReading =  mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		// TO DO - register to receive location updates from NETWORK_PROVIDER
         mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,mMinTime,mMinDistance ,this);



        }

	@Override
	protected void onPause() {

		mMockLocationProvider.shutdown();

		// TO DO - unregister for location updates
        mLocationManager.removeUpdates(this);

		
		super.onPause();
	}

	public void addNewPlace(PlaceRecord place) {

		log("Entered addNewPlace()");
		mAdapter.add(place);

	}

	@Override
	public void onLocationChanged(Location currentLocation) {

		// TO DO - Handle location updates
		// Cases to consider
		// 1) If there is no last location, keep the current location.
		// 2) If the current location is older than the last location, ignore
		// the current location
		// 3) If the current location is newer than the last locations, keep the
		// current location.
        if (mLastLocationReading == null || age(currentLocation) < age(mLastLocationReading))
            mLastLocationReading = currentLocation;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.print_badges:
			ArrayList<PlaceRecord> currData = mAdapter.getList();
			for (int i = 0; i < currData.size(); i++) {
				log(currData.get(i).toString());
			}
			return true;
		case R.id.delete_badges:
			mAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_invalid:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void log(String msg) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}

}
