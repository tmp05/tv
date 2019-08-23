package ru.ks.tv;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.ks.tv.R;

public class OldPreferenceActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.kv_settings);
	}
}