package ru.ks.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.ks.kvlib.indep.AuthAccount;
import ru.ks.kvlib.indep.HTTPClient;
import ru.ks.kvlib.indep.Parser;
import ru.ks.kvlib.indep.consts.IntentConst;
import ru.ks.kvlib.interfaces.OnLoadCompleteListener;
import ru.ks.secret.ApiConst;

@SuppressLint("SetJavaScriptEnabled")
public class MainAuthActivity extends Activity {

	public static SharedPreferences prefs;
	//какой интерфейс был включен в прошлый раз
	public static final int INTERFACE_TV = 0; //телевидение(старый)
	public static final int INTERFACE_KS = 1; //красвью(новый)

	public static int auth_type; //("pref_auth_type")Предыдущий заход был при помощи:


	private final int REQUEST_CODE_GUEST = 0;
	private final int REQUEST_CODE_SOCIAL = 1;


	String kraslan_login = "";//логин(номер счета) для красноярской сети
	private String login;//("pref_login")//сохраненный логин
	private String password;//("pref_password")//сохраненный пароль

	private boolean logout;

	//элементы разметки
	EditText edit_login, edit_password;
	Button button_enter, button_registration;
	ImageButton button_help;

	//интенты для вызова активити
	Intent ksIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HTTPClient.setContext(this);
		ksIntent = new Intent(IntentConst.ACTION_MAIN_ACTIVITY);
		setContentView(R.layout.kv_activity_auth_small);
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		logout = prefs.getBoolean("pref_now_logout", true);
		fastAuth(!logout);
		initLayout();
	}



	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		HTTPClient.setContext(this);
	}


	private void fastAuth(boolean fast) {
		if(!fast) {
			prefs.edit().putInt("pref_auth_type", AuthAccount.AUTH_TYPE_UNKNOWN).apply();
			return;
		}
		Intent local = null;
		local = ksIntent;
		startActivity(local);
		this.finish();
	}

	@Override
	public void onStart() {
		super.onStart();
		getAuthPrefs();
	}

	private void getAuthPrefs() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		auth_type = prefs.getInt("pref_auth_type", AuthAccount.AUTH_TYPE_UNKNOWN);
		logout = prefs.getBoolean("pref_now_logout", true);
		login = prefs.getString("pref_login", "");
		edit_login.setText(login);
		password = prefs.getString("pref_password", "");
		final String kraslan_addr = ApiConst.TV_AUTH;
		final String oauth_api_addr = ApiConst.KS_OAUTH;
		OnLoadCompleteListener listener = new OnLoadCompleteListener() {
			@Override
			public void loadComplete(String result) {
			}

			@Override
			public void loadComplete(String address, String result) {
				if(address.equals(kraslan_addr)) {
					checkKraslanLogin(result);
				} else if(address.equals(oauth_api_addr)) {
					checkSocialButton(result);

				}
			}
		};
		HTTPClient.getXMLAsync(kraslan_addr, "",listener);
		HTTPClient.getXMLAsync(oauth_api_addr, "",listener);
	}

	private void checkKraslanLogin(String str) {
		if(str.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
			return;
		}
		kraslan_login = str;
		if(kraslan_login.equals("")) {
			return;
		}
	}

	private void checkSocialButton(String str) {
		if(str.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
			return;
		}
		if(str.equals("")) {
			return;
		}
		//social_grid.setAdapter(new SocialButtonAdapter());
//       adjustGridView();
//		social_grid.setOnItemClickListener(new GridView.OnItemClickListener() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
//				Map<String, Object> m = (Map<String,Object>) parent.getItemAtPosition(position);
//				Intent socialIntent = new Intent(MainAuthActivity.this, SocialAuthActivity.class);
//				socialIntent.putExtra("address", (String)m.get("url"));
//				MainAuthActivity.this.startActivityForResult(socialIntent, REQUEST_CODE_SOCIAL);
//			}
//		}
		//);
		//((SocialButtonAdapter)social_grid.getAdapter()).addData(str);
	}



	private class SocialButtonAdapter extends BaseAdapter {
		ArrayList<Map<String,Object>> mData = new ArrayList<Map<String,Object>>();

		public void addData(String xml) {
			new setDataTask().execute(xml);
		}

		private class setDataTask extends AsyncTask<String, Map<String,Object>, Void> {

			@Override
			protected void onProgressUpdate(Map<String,Object>... progress) {
				mData.add(progress[0]);
				SocialButtonAdapter.this.notifyDataSetChanged();
			}

			@Override
			protected void onPostExecute(Void result) {
			}

			@SuppressWarnings("unchecked")
			@Override
			protected Void doInBackground(String... params) {
				if(android.os.Debug.isDebuggerConnected())
					android.os.Debug.waitForDebugger();
				Map<String, Object> m;
				Document mDocument;
				mDocument = Parser.XMLfromString(params[0]);
				if(mDocument == null) {
					return null;
				}
				mDocument.normalizeDocument();
				NodeList nListChannel = mDocument.getElementsByTagName("unit");
				int numOfChannel = nListChannel.getLength();

				for (int nodeIndex = 0; nodeIndex < numOfChannel; nodeIndex++) {
					Node locNode = nListChannel.item(nodeIndex);
					m = new HashMap<String, Object>();
					m.put("title", Html.fromHtml(Parser.getValue("title", locNode)));
					String image_uri = Parser.getValue("image", locNode);
					m.put("image_uri", image_uri);
					m.put("image", HTTPClient.getImage(image_uri));
					m.put("url", Parser.getValue("url", locNode));
					publishProgress(m);
				}
				return null;
			}

		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Map<String, Object> getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView v = new ImageView(MainAuthActivity.this);
			v.setImageBitmap((Bitmap)getItem(position).get("image"));
			GridView.LayoutParams p = new GridView.LayoutParams(48, 48);
			v.setLayoutParams(p);
			return v;
		}
	}

	private  void initLayout() {
		edit_login = (EditText)findViewById(R.id.login);
		edit_password = (EditText)findViewById(R.id.password);
	}

	ProgressDialog enterProgressDialog;

	@SuppressWarnings("deprecation")
	private void enter() {
		enterProgressDialog = new ProgressDialog(this);
		login = edit_login.getText().toString();
		password = edit_password.getText().toString();
		prefs.edit().putString("pref_login", login)
		.putString("pref_password", password)
		.putInt("pref_auth_type", AuthAccount.AUTH_TYPE_UNKNOWN)
		.putString("pref_hash", "1").putString("pref_hash_tv", "1").apply();

		if(login.equals("")||password.equals("")) {
			Toast toast = Toast.makeText(getApplicationContext(),
			                             "Логин и пароль не должны быть пустыми", Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		enterProgressDialog.show();

		final String auth_address_tv = ApiConst.TV_AUTH;
		final String auth_address_ks = ApiConst.KS_AUTH;

		OnLoadCompleteListener listener = new OnLoadCompleteListener() {
			boolean tv = false;
			boolean ks = false;
			boolean check_tv = false;
			boolean check_ks = false;

			@Override
			public void loadComplete(String result) {
			}

			@Override
			public void loadComplete(String get_address, String result) {
				if(get_address.equals(auth_address_tv)) {
					if(result.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
						Toast toast = Toast.makeText(getApplicationContext(),
							"Невозможно подключиться к серверу, проверьте подключение, попробуйте позже", Toast.LENGTH_SHORT);
						toast.show();
						return;
					}
					check_tv = true;
					if(result.equals("fail") || result.equals("auth failed") || result.equals("")) {
						tv = false;
					} else {
						prefs.edit().putString("pref_hash_tv", result).apply();
						tv = true;
					}
				}
				if(get_address.equals(auth_address_ks)) {
					if(result.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
						Toast toast = Toast.makeText(getApplicationContext(),
							"Невозможно подключиться к серверу, проверьте подключение, попробуйте позже", Toast.LENGTH_SHORT);
						toast.show();
						return;
					}
					check_ks = true;
					if(result.equals("error")) {
						ks = false;
					} else {
						prefs.edit().putString("pref_hash", result).apply();
						ks = true;
					}
				}
				if(check_tv && check_ks) {
					enterProgressDialog.dismiss();
				} else {
					return;
				}
				if(ks) {
					startActivity(ksIntent);
					MainAuthActivity.this.finish();
					prefs.edit().putInt("pref_auth_type", AuthAccount.AUTH_TYPE_KS).apply();
				}
				if(tv && !ks) {
					startActivity(ksIntent);
					MainAuthActivity.this.finish();
					prefs.edit().putInt("pref_auth_type", AuthAccount.AUTH_TYPE_TV)
					.apply();
				}
				if(!tv && !ks) {
					Toast toast = Toast.makeText(getApplicationContext(),
						"Ошибка авторизации, возможно, вы неправильно ввели логин или пароль ", Toast.LENGTH_SHORT);
					toast.show();
					edit_password.setText("");
					}
			}
		};
		HTTPClient.getXMLAsync(auth_address_tv, "login=" + URLEncoder.encode(login) +"&password="+URLEncoder.encode(password) ,listener);
		HTTPClient.getXMLAsync(auth_address_ks, "login=" + URLEncoder.encode(login) +"&password="+URLEncoder.encode(password) ,listener);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.kv_auth_enter_button:
			enter();
			break;
//		case R.id.kv_auth_registration_button:
//			Intent b = new Intent(Intent.ACTION_VIEW);
//			b.setData(Uri.parse(ApiConst.CREATE_ACCOUNT));
//			startActivity(b);
//			break;
//		case R.id.kv_auth_guest_button:
//			guestAuth();
//			break;
//		case R.id.kv_auth_kraslan_button:
//			prefs.edit().putString("pref_login", "")
//			.putString("pref_password", "")
//			.putInt("pref_auth_type", AuthAccount.AUTH_TYPE_TV).apply();
//			fastAuth(true);
//			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_OK) return;
		switch(requestCode) {
		case REQUEST_CODE_GUEST:
		case REQUEST_CODE_SOCIAL:
			this.finish();
			break;
		}
	}

	private void guestAuth() {
		if(!prefs.getBoolean("pref_guest_check", false)) {
			Intent c = new Intent(this, GuestAuthActivity.class);
			this.startActivityForResult(c, REQUEST_CODE_GUEST);
		} else {
			prefs.edit().putString("pref_login", "")
			.putString("pref_password", "")
			.putInt("pref_auth_type", AuthAccount.AUTH_TYPE_GUEST).apply();
			Intent a = new Intent(IntentConst.ACTION_MAIN_ACTIVITY);
			startActivity(a);
			this.finish();
		}
	}
}
