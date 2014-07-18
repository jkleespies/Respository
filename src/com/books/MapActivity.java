// create map add marker with current position and show nearby places
package com.books;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements LocationListener {

	// instance variables
	private int userIcon, shopIcon;
	private GoogleMap theMap;
	private LocationManager locMan;
	private Marker userMarker;
	private Marker[] placeMarkers;
	private final int MAX_PLACES = 20;// number of places returned by API
	private MarkerOptions[] places;
	protected MapFragment mapFragment;
	private String placesSearchStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// get icons
		userIcon = R.drawable.location;
		shopIcon = R.drawable.location_place;

		mapFragment = MapFragment.newInstance();
		getFragmentManager().beginTransaction()
				.add(R.id.the_map, mapFragment).commit();
	}

	protected void onStart() {
		super.onStart();
		// check if map allready exists
		
		theMap = mapFragment.getMap();
		// check in case map/ Google Play services not available
		if (this.theMap != null) {
			// ok - proceed
			theMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			// create marker array
			placeMarkers = new Marker[MAX_PLACES];
			// update location
			updatePlaces();
		}
		// }
	}

	@Override
	public void onLocationChanged(Location location) {
		updatePlaces();
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	// update the markers
	private void updatePlaces() {
		// get location manager
		locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// get last location
		Location lastLoc = locMan
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		double lat = lastLoc.getLatitude();
		double lng = lastLoc.getLongitude();
		// create LatLng
		LatLng lastLatLng = new LatLng(lat, lng);

		// remove existing marker
		if (userMarker != null)
			userMarker.remove();
		// create and set the marker properties
		userMarker = this.theMap.addMarker(new MarkerOptions()
				.position(lastLatLng).title("You are here")
				.icon(BitmapDescriptorFactory.fromResource(userIcon)));

		// set camera position
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(lastLatLng).zoom(15).tilt(45).build();
		this.theMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

		// define search parameters for places -> type
		String types = "book_store|library";
		try {
			types = URLEncoder.encode(types, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// create API search string
		placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
				+ "json?location="
				+ lat
				+ ","
				+ lng
				+ "&radius=1000&sensor=true"
				+ "&types="
				+ types
				+ "&key=AIzaSyBt4UwId11OPjLA5jjnNuTAvJ-d-1FbAFM";

		// run query
		new GetPlaces().execute();

		// set Update settings (choosen Provider, min time interval between
		// updates, min meters before update, the listener)
		locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000,
				100, this);
	}

	// get the places from Google API as JSON and parse
	private class GetPlaces extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... args0) {

			String url = placesSearchStr.replaceAll("[ ]", "%20");
			Adapter ad = new Adapter();

			// URL request and response from Adapter
			String jsonStr = ad.makeServiceCall(url, Adapter.GET);

			return jsonStr;

		}

		// process data retrieved from doInBackground
		protected void onPostExecute(String result) {
			// parse place data returned from Google Places API
			// remove existing markers
			if (placeMarkers != null) {
				for (int pm = 0; pm < placeMarkers.length; pm++) {
					if (placeMarkers[pm] != null)
						placeMarkers[pm].remove();
				}
			}
			// parse JSON
			try {

				// create JSONObject, pass sting returned from doInBackground
				JSONObject resultObject = new JSONObject(result);
				// get "results" array
				JSONArray placesArray = resultObject.getJSONArray("results");
				// marker options for each place returned
				places = new MarkerOptions[placesArray.length()];
				// loop through places
				for (int p = 0; p < placesArray.length(); p++) {
					// parse each place
					// if any values are missing we won't show the marker
					boolean missingValue = false;
					LatLng placeLL = null;
					String placeName = "";
					String vicinity = "";
					int currIcon = shopIcon;
					try {
						// attempt to retrieve place data values
						missingValue = false;
						// get place at this index
						JSONObject placeObject = placesArray.getJSONObject(p);
						// get location section
						JSONObject loc = placeObject.getJSONObject("geometry")
								.getJSONObject("location");
						// read lat lng
						placeLL = new LatLng(Double.valueOf(loc
								.getString("lat")), Double.valueOf(loc
								.getString("lng")));
						// get types
						JSONArray types = placeObject.getJSONArray("types");
						// loop through types
						for (int t = 0; t < types.length(); t++) {
							// what type is it
							String thisType = types.get(t).toString();
							// check for particular types - set icons
							if (thisType.contains("book_store")) {
								currIcon = shopIcon;
								break;
							} else if (thisType.contains("library")) {
								currIcon = shopIcon;
								break;
							}

						}
						// vicinity
						vicinity = placeObject.getString("vicinity");
						// name
						placeName = placeObject.getString("name");
					} catch (JSONException jse) {
						Log.v("PLACES", "missing value");
						missingValue = true;
						jse.printStackTrace();
					}
					// if values missing we don't display
					if (missingValue)
						places[p] = null;
					else
						places[p] = new MarkerOptions()
								.position(placeLL)
								.title(placeName)
								.icon(BitmapDescriptorFactory
										.fromResource(currIcon))
								.snippet(vicinity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (places != null && placeMarkers != null) {
				for (int p = 0; p < places.length && p < placeMarkers.length; p++) {
					// will be null if a value was missing
					if (places[p] != null)
						placeMarkers[p] = theMap.addMarker(places[p]);
				}
			}

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.theMap != null) {
			locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					30000, 100, this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (this.theMap != null) {
			locMan.removeUpdates(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// initialize Intent
		Intent i;
		// Switch case between buttons, start activities
		switch (item.getItemId()) {
		case R.id.search:
			i = new Intent(getApplicationContext(), SearchActivity.class);
			startActivity(i);
			break;
		case R.id.map:
			i = new Intent(getApplicationContext(), MapActivity.class);
			startActivity(i);
			break;
		case R.id.favorite:
			i = new Intent(getApplicationContext(), FavoriteActivity.class);
			startActivity(i);
			break;
		default:
			Log.d("onOptionsItemSelected", "es wurde nichts aufgew�hlt");
		}
		return super.onOptionsItemSelected(item);

	}
}
