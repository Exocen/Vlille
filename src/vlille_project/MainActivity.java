package vlille_project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import vlille_project.MyLocation.LocationResult;

import com.androidhive.xmlparsing.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.widget.ListAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	// All static variables
	static final String URL = "http://vlille.fr/stations/xml-stations.aspx";
	static final String URL2 = "http://vlille.fr/stations/xml-station.aspx?borne=";

	// XML node keys
	static final String KEY_ITEM = "marker"; // parent node
	static final String KEY_ID = "id";
	static final String KEY_NAME = "name";
	static final String KEY_LAT = "lat";
	static final String KEY_LNG = "lng";
	static final String KEY_DIST = "dist";

	static final String KEY_ITEM2 = "station"; // 2nd parent node
	static final String KEY_BIKES = "bikes";
	static final String KEY_ATTACHS = "attachs";

	static MyLocation myLocation = new MyLocation();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		XMLParser parser = new XMLParser();
		String xml = parser.getXmlFromUrl(URL); // getting XML
		Document doc = parser.getDomElement(xml); // getting DOM element
		if (doc == null) {
			DialogBox("Pas de Connexion");
		} else {

			MakeList(doc);

			MakeMiniList();

		}
	}

	/**
	 * TO Debug
	 */
	public void DialogBox(String s) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(s).setTitle("Debug");
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public LocationResult locationResult = new LocationResult() {

		@Override
		public void gotLocation(Location location) {
			double Longitude = location.getLongitude();
			double Latitude = location.getLatitude();

			/*
			 * Toast.makeText(getApplicationContext(), "" + Longitude,
			 * Toast.LENGTH_LONG).show();
			 */
			try {
				SharedPreferences locationpref = getApplication()
						.getSharedPreferences("location", MODE_PRIVATE);
				SharedPreferences.Editor prefsEditor = locationpref.edit();
				prefsEditor.putString("Longitude", Longitude + "");
				prefsEditor.putString("Latitude", Latitude + "");
				prefsEditor.commit();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void LocationWifi() {
		Geocoder geocoder;
		String bestProvider;
		List<Address> user = null;

		LocationManager lm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		bestProvider = lm.getBestProvider(criteria, false);
		Location location = lm.getLastKnownLocation(bestProvider);

		if (location == null) {
			Toast.makeText(this, "Location Not found", Toast.LENGTH_LONG)
					.show();
		} else {
			geocoder = new Geocoder(this);
			try {
				user = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);
				double lat = (double) user.get(0).getLatitude();
				double lng = (double) user.get(0).getLongitude();
				SharedPreferences locationpref = getApplication()
						.getSharedPreferences("location", MODE_PRIVATE);
				SharedPreferences.Editor prefsEditor = locationpref.edit();
				prefsEditor.putString("Longitude", lng + "");
				prefsEditor.putString("Latitude", lat + "");
				prefsEditor.commit();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<Double> RefreshLocal() {

		LocationWifi();
		myLocation.getLocation(getApplicationContext(), locationResult);

		SharedPreferences sharedPref = this.getSharedPreferences("location",
				Context.MODE_PRIVATE);
		String temp = "";
		String lng = sharedPref.getString("Longitude", temp);
		String lat = sharedPref.getString("Latitude", temp);
		ArrayList<Double> loc = new ArrayList<Double>();

		if (!(lng.equals("") || lat.equals(""))) {
			loc.add(Double.parseDouble(lat));
			loc.add(Double.parseDouble(lng));
		}
		return loc;
	}

	public static double distFrom(double lat1, double lng1, double lat2,
			double lng2) {
		double earthRadius = 6371; // kilometers
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}

	public void MakeList(Document doc) {
		ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
		ArrayList<Double> loc = RefreshLocal();
		XMLParser parser = new XMLParser();

		NodeList nl = doc.getElementsByTagName(KEY_ITEM);

		for (int i = 0; i < nl.getLength(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			Element e = (Element) nl.item(i);
			double Slat = Double.parseDouble(parser.getTextValue(e, KEY_LAT));
			double Slng = Double.parseDouble(parser.getTextValue(e, KEY_LNG));
			double dist = distFrom(Slat, Slng, loc.get(0), loc.get(1));

			map.put(KEY_ID, parser.getTextValue(e, KEY_ID));
			map.put(KEY_NAME, parser.getTextValue(e, KEY_NAME));
			map.put(KEY_LAT, "Latitude : " + parser.getTextValue(e, KEY_LAT));
			map.put(KEY_LNG, "Longitude : " + parser.getTextValue(e, KEY_LNG));

			map.put(KEY_DIST, dist + "");

			menuItems.add(map);

			Collections.sort(menuItems,
					new Comparator<HashMap<String, String>>() {
						public int compare(HashMap<String, String> mapping1,
								HashMap<String, String> mapping2) {
							return mapping1.get(KEY_DIST).compareTo(
									mapping2.get(KEY_DIST));
						}

					});

		}
		ListAdapter adapter = new SimpleAdapter(this, menuItems,
				R.layout.list_item, new String[] { KEY_NAME, KEY_LNG, KEY_LAT,
						KEY_ID }, new int[] { R.id.Station, R.id.Longitude,
						R.id.Latitude, R.id.idi });

		setListAdapter(adapter);
	}

	public void MakeMiniList() {
		ListView lv = getListView();

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String idi = ((TextView) view.findViewById(R.id.idi)).getText()
						.toString();

				XMLParser parser = new XMLParser();
				String xml = parser.getXmlFromUrl(URL2 + idi); // getting //
																// XML
				Document doc = parser.getDomElement(xml); // getting DOM
				if (doc == null) {
					DialogBox("Pas de Connexion");
				} else {
					NodeList nl = doc.getElementsByTagName(KEY_ITEM2);
					Element e = (Element) nl.item(0);

					// getting values from selected ListItem
					String station = ((TextView) view
							.findViewById(R.id.Station)).getText().toString();
					String longitude = ((TextView) view
							.findViewById(R.id.Longitude)).getText().toString();
					String latitude = ((TextView) view
							.findViewById(R.id.Latitude)).getText().toString();
					String bikes = parser.getValue(e, KEY_BIKES);
					String attachs = parser.getValue(e, KEY_ATTACHS);

					// Starting new intent
					Intent in = new Intent(getApplicationContext(),
							SingleMenuItemActivity.class);
					in.putExtra(KEY_NAME, station);
					in.putExtra(KEY_LAT, latitude);
					in.putExtra(KEY_LNG, longitude);
					in.putExtra(KEY_BIKES, bikes);
					in.putExtra(KEY_ATTACHS, attachs);
					startActivity(in);

				}

			}
		});

	}
}