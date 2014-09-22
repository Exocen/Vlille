package vlille_project;

import com.androidhive.xmlparsing.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class SingleMenuItemActivity extends Activity {

	// XML node keys
	static final String KEY_NAME = "name";
	static final String KEY_BIKES = "bikes";
	static final String KEY_ATTACH = "attachs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_list_item);

		// getting intent data
		Intent in = getIntent();

		// Get XML values from previous intent
		String name = in.getStringExtra(KEY_NAME);

		this.setTitle(name);

		int nb_Bikes = Integer.parseInt(in.getStringExtra(KEY_BIKES));
		int nb_attachs = Integer.parseInt(in.getStringExtra(KEY_ATTACH));

		String bikes = "Pas de Vélos disponibles";
		String attachs = "Pas d'emplacements disponibles";

		// Displaying all values on the screen
		TextView lblBik = (TextView) findViewById(R.id.bikes);
		TextView lblAtt = (TextView) findViewById(R.id.attachs);

		if (nb_attachs > 0) {
			lblAtt.setTextColor(Color.GREEN);
			attachs = "Nombre d'emplacements disponibles : " + nb_attachs;
		}

		if (nb_Bikes > 0) {
			lblBik.setTextColor(Color.GREEN);
			bikes = "Nombre de Vélos disponibles : " + nb_Bikes;
		}

		lblBik.setText(bikes);
		lblAtt.setText(attachs);

	}
}
