package ru.krasview.tv;

import java.util.regex.Pattern;

import ru.krasview.kvlib.indep.AuthAccount;
import ru.krasview.kvlib.indep.HTTPClient;
import ru.krasview.kvlib.indep.consts.IntentConst;
import ru.krasview.kvlib.interfaces.OnLoadCompleteListener;
import ru.krasview.secret.ApiConst;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;

public class SocialAuthActivity extends Activity {
	WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.social_auth_activity);

		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36 Kadu");
		wv.setWebViewClient(new CustomWebViewClient());
		if(getIntent().hasExtra("address")) {
			wv.loadUrl(getIntent().getExtras().getString("address"));
		} else {
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		HTTPClient.setContext(this);
	}

	private class CustomWebViewClient extends WebViewClient {
		@SuppressWarnings("deprecation")
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			final Uri uri = Uri.parse(url);
			return handleUri(view, uri);
		}

		@RequiresApi(Build.VERSION_CODES.N)
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			final Uri uri = request.getUrl();
			return handleUri(view, uri);
		}

		private boolean handleUri(WebView view, final Uri uri) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				CookieSyncManager.createInstance(getApplication());
			}
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
			String cookie = cookieManager.getCookie("hlamer.ru");
			Log.i("Debug", "cookie " +  cookie + "; address: " + uri.getSchemeSpecificPart());
			if(cookie!=null) {
				String[] x = Pattern.compile(";").split(cookie);
				String hash = x[0].replaceFirst("user=", "");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				prefs.edit().putString("pref_hash", hash).commit();

				final String get_tv_hash = ApiConst.TV_HASH;
				HTTPClient.getXMLAsync(get_tv_hash, "hash="+hash ,new OnLoadCompleteListener() {
					@Override
					public void loadComplete(String result) {
					}

					@Override
					public void loadComplete(String address, String result) {
						if(address.equals(get_tv_hash)) {
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
							prefs.edit().putString("pref_hash_tv", result).commit();
							Log.i("Debug", "получен тв хеш с красвью " + result);
							AuthAccount.getInstance().setTvHash(result);
						}
					}
				});

				prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				prefs.edit().putString("pref_login", "")
					.putString("pref_password", "")
					.putInt("pref_auth_type", AuthAccount.AUTH_TYPE_KRASVIEW_SOCIAL)
					.commit();
				Intent a = new Intent(IntentConst.ACTION_MAIN_ACTIVITY);
				startActivity(a);
				SocialAuthActivity.this.setResult(SocialAuthActivity.RESULT_OK);
				SocialAuthActivity.this.finish();
			} else if(uri.getQueryParameter("error") != null) {
				SocialAuthActivity.this.finish();
			} else {
				view.loadUrl(uri.getScheme() + ":" + uri.getSchemeSpecificPart());
			}

			return true;
		}
	};
}
