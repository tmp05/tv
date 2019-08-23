package ru.ks.tv;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ru.ks.tv.R;

public class PrFragment extends PreferenceFragment {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.kv_settings);
	}
}
