package ru.ks.tv;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.FrameLayout;

import com.example.kvlib.R;

import ru.krasview.kvlib.animator.NewAnimator;
import ru.krasview.kvlib.indep.AuthAccount;
import ru.krasview.kvlib.indep.HTTPClient;
import ru.krasview.kvlib.indep.HeaderAccount;
import ru.krasview.kvlib.indep.ListAccount;
import ru.krasview.kvlib.indep.consts.RequestConst;
import ru.krasview.kvlib.indep.consts.TypeConsts;
import ru.krasview.kvlib.interfaces.OnLoadCompleteListener;
import ru.krasview.kvlib.interfaces.PropotionerView;
import ru.krasview.kvlib.widget.List;
import ru.krasview.kvlib.widget.NavigationViewFactory;
import ru.krasview.secret.ApiConst;

public class MainActivity extends KVSearchAndMenuActivity {
	NewAnimator animator;
	String start = TypeConsts.MAIN;
	FrameLayout layout;
	private BroadcastReceiver mNetworkStateIntentReceiver;
	public boolean pref_autoplay;
//OnCreate

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		                     WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Intent intent = getIntent();
		super.onCreate(savedInstanceState);

		styleActionBar();

		setContentView(R.layout.activity_main_new);
		layout = (FrameLayout) findViewById(R.id.root);
		animator = new NewAnimator(this, new NavigationViewFactory());
		layout.addView(animator);

		styleBackground();
		getPacketAndStart();
	}

	ProgressDialog pd;
	private void getPacketAndStart() {
		getPrefs();

		//получение данных о подключенном пакете
		pd = new ProgressDialog(this);
		pd.setTitle("Подождите");
		pd.setCancelable(false);

		HTTPClient.getXMLAsync(ApiConst.USER_PACKET, "hash=" + AuthAccount.getInstance().getTvHash(),
		new OnLoadCompleteListener() {
			@Override
			public void loadComplete(String result) {
				if(!result.equals("Бесплатный")) {
					HeaderAccount.hh();
				}
				animator.init(start);
			}

			@Override
			public void loadComplete(String address, String result) {
			}
		});
	}

	//настройка actionbar-a
	private void styleActionBar() {
		if(getSupportActionBar() != null) {
			getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_background));
		}
	}

	//настройка фона
	private void styleBackground() {
			layout.setBackgroundColor(Color.rgb(20, 20, 20));
	}

	SharedPreferences prefs;

	private void getPrefs() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		String tv_pl = prefs.getString("video_player_tv", "std");
		if(tv_pl.equals("Стандартный плеер")) {
			prefs.edit().putString("video_player_tv", "std").apply();
		}
		tv_pl = prefs.getString("video_player_serial", "std");
		if(tv_pl.equals("Стандартный плеер")) {
			prefs.edit().putString("video_player_serial", "std").apply();
		}

		AuthAccount.getInstance().setType(
		    prefs.getInt("pref_auth_type", AuthAccount.AUTH_TYPE_UNKNOWN));
		if(account.isUnknownAccount()) {
			prefs.edit().putBoolean("pref_now_logout", true).apply();
			Intent a = new Intent(this, MainAuthActivity.class);
			startActivity(a);
			this.finish();
			return;
		}
		prefs.edit().putBoolean("pref_now_logout", false)
		.putInt("pref_last_interface", MainAuthActivity.INTERFACE_KRASVIEW).apply();
		HTTPClient.setContext(this);
		HTTPClient.setExitListener(this);
		AuthAccount.getInstance().setLogin(prefs.getString("pref_login", ""));
		AuthAccount.getInstance().setPassword(prefs.getString("pref_password", ""));
		AuthAccount.getInstance().setHash(prefs.getString("pref_hash", "1"));
		AuthAccount.getInstance().setTvHash(prefs.getString("pref_hash_tv", "1"));
		AuthAccount.getInstance().setTvChannel(prefs.getString("pref_channel", "1"));
	}

	String pref_orientation = "default";
	@Override
	public void onResume() {
		super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		pref_orientation = prefs.getString("orientation", "default");
		switch (pref_orientation) {
			case "default":
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			case "album":
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			case "book":
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
		}
	}

	@Override
	public void onBackPressed() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		pref_autoplay = prefs.getBoolean("autoplay", false);
		if (pref_autoplay) {
				super.onBackPressed();
		 }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode,data);
		if(resultCode != RESULT_OK) {
			return;
		}
		switch(requestCode) {
		case RequestConst.REQUEST_CODE_BILLING:
			((PropotionerView)animator.getCurrentView()).enter();
			break;
		case RequestConst.REQUEST_CODE_VIDEO:
			if (data == null) {
				return;
			}
			int index = data.getIntExtra("index", 0);
			ListAccount.currentList.setSelection(index + ((List)ListAccount.currentList).getAdapter().getConstDataCount());
			break;
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			boolean result;
			if(animator.getVisibility() == View.VISIBLE) {
				result = animator.dispatchKeyEvent(event);
			} else {
				result = false;
			}
			if(result) {
				return result;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onError() {
		exit();
	}

	@Override
	protected final void exit() {
		HeaderAccount.shh();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs.edit().putBoolean("pref_now_logout", true).putString("pref_hash", "").putString("pref_hash_tv", "").apply();
		CookieSyncManager.createInstance(this.getApplication());
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		Intent a = new Intent(this, MainAuthActivity.class);
		startActivity(a);
		this.finish();
	}


	@Override
	protected final void refresh() {
		if(animator.getVisibility() == View.VISIBLE) {
			animator.refresh();
		}
	}

}
