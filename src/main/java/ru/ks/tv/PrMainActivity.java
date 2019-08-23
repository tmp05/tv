package ru.ks.tv;

import android.app.Activity;
import android.os.Bundle;

public class PrMainActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new PrFragment()).commit();
	}
}
