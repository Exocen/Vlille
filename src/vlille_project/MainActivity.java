package vlille_project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.androidhive.xmlparsing.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.widget.ListAdapter;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

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

	static final String KEY_ITEM2 = "station"; // 2nd parent node
	static final String KEY_BIKES = "bikes";
	static final String KEY_ATTACHS = "attachs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();

		XMLParser parser = new XMLParser();
		String xml = parser.getXmlFromUrl(URL); // getting XML
		Document doc = parser.getDomElement(xml); // getting DOM element
		if (doc == null) {
			DialogBox("Pas de Connexion");
		} else {

			NodeList nl = doc.getElementsByTagName(KEY_ITEM);

			// looping through all item nodes <item>
			for (int i = 0; i < nl.getLength(); i++) {
				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);
				// adding each child node to HashMap key => value
				map.put(KEY_ID, parser.getTextValue(e, KEY_ID));
				map.put(KEY_NAME, parser.getTextValue(e, KEY_NAME));
				map.put(KEY_LAT,
						"Latitude : " + parser.getTextValue(e, KEY_LAT));
				map.put(KEY_LNG,
						"Longitude : " + parser.getTextValue(e, KEY_LNG));

				// adding HashList to ArrayList
				menuItems.add(map);

				// tri alphabetique
				Collections.sort(menuItems,
						new Comparator<HashMap<String, String>>() {
							public int compare(
									HashMap<String, String> mapping1,
									HashMap<String, String> mapping2) {
								return mapping1.get(KEY_NAME).compareTo(
										mapping2.get(KEY_NAME));
							}

						});

			}

			// Adding menuItems to ListView
			ListAdapter adapter = new SimpleAdapter(this, menuItems,
					R.layout.list_item, new String[] { KEY_NAME, KEY_LNG,
							KEY_LAT, KEY_ID }, new int[] { R.id.Station,
							R.id.Longitude, R.id.Latitude, R.id.idi });

			setListAdapter(adapter);

			// selecting single ListView item
			ListView lv = getListView();

			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					// TODO Mettre en méthode + ameliorer systeme d'erreur

					String idi = ((TextView) view.findViewById(R.id.idi))
							.getText().toString();

					XMLParser parser = new XMLParser();
					String xml = parser.getXmlFromUrl(URL2 + idi); // getting
																	// XML
					Document doc = parser.getDomElement(xml); // getting DOM
					if (doc == null) {
						DialogBox("Pas de Connexion");
					} else {
						NodeList nl = doc.getElementsByTagName(KEY_ITEM2);
						Element e = (Element) nl.item(0);

						// getting values from selected ListItem
						String station = ((TextView) view
								.findViewById(R.id.Station)).getText()
								.toString();
						String longitude = ((TextView) view
								.findViewById(R.id.Longitude)).getText()
								.toString();
						String latitude = ((TextView) view
								.findViewById(R.id.Latitude)).getText()
								.toString();
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

	/**
	 * TO Debug
	 */
	public void DialogBox(String s) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(s).setTitle("Debug");
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}