package ru.ks.tv;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PrMainActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new PrFragment()).commit();
//		Context context = getApplicationContext();
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("autoplay")){
			setUpAdmin(sharedPreferences.getBoolean("autoplay", false));
		}
	}
}
