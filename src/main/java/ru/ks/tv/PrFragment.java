package ru.ks.tv;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

public class PrFragment extends PreferenceFragment {
	public CheckBoxPreference checkBoxPreference;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.kv_settings);

	}

}
