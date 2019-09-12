package ru.ks.tv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import ru.ks.kvlib.adapter.CombineSimpleAdapter;
import ru.ks.kvlib.indep.AuthAccount;
import ru.ks.kvlib.indep.HTTPClient;
import ru.ks.kvlib.indep.consts.AuthRequestConst;
import ru.ks.kvlib.indep.consts.TagConsts;
import ru.ks.kvlib.indep.consts.TypeConsts;
import ru.ks.kvlib.interfaces.FatalErrorExitListener;
import ru.ks.kvlib.widget.List;
import ru.ks.secret.ApiConst;

public abstract class KVSearchAndMenuActivity extends AppCompatActivity
									 implements  FatalErrorExitListener{

	AuthAccount account = AuthAccount.getInstance();
	View searchHost;

	protected abstract void exit();
	protected abstract void refresh();

	@Override
	public void onError() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


		@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.kv_activity_animator, menu);
		MenuItem loginItem = menu.findItem(R.id.kv_login_item);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String login = prefs.getString("pref_login", "");
    	loginItem.setTitle("лицевой счет №"+login+" ");

    	//requestFocus();
		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.new_game) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= 11) {
                    Intent settingsActivity = new Intent(getBaseContext(), PrMainActivity.class);
                    startActivity(settingsActivity);
                    return true;
                } else {
                    Intent settingsActivity = new Intent(getBaseContext(), OldPreferenceActivity.class);
                    startActivity(settingsActivity);
                    return true;
                }
        } else if (id == R.id.kv_login_item) {
            return true;
        } else if (id == R.id.exit) {
           // exit();
			return true;
		} else if (id == R.id.exitlogin) {
			exit();
			return true;
        } else if (id == R.id.kv_refresh_item) {
			refresh();
			return true;
  //      } else if (id == R.id.update) {
			//first init
//			Thread updateThread = new Thread() {
//				@Override
//				public void run() {
//					AppUpdateUtil.checkForUpdate(KVSearchAndMenuActivity.this);
//				}
//			};
//			updateThread.start();
//			ProgressBar progressBar = findViewById(R.id.ProgressBar);
//			progressBar.setProgress(50);
//		return true;
        }

        return super.onOptionsItemSelected(item);
	}

	Map<String, Object> contextMenuMap;
	CombineSimpleAdapter contextMenuAdapter;


	@SuppressWarnings("unchecked")
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		contextMenuAdapter = ((List)v).getAdapter();
		contextMenuMap = (Map<String, Object>) ((List)v).getAdapter().getItem(info.position);
		menu.setHeaderTitle((CharSequence)contextMenuMap.get("name"));

		if(contextMenuMap.get(TagConsts.TYPE) != null
		        && contextMenuMap.get(TagConsts.TYPE).equals(TypeConsts.CHANNEL)) {
			if(!account.isTVAccount()) {
				return;
			}
			if(contextMenuMap.get("star").equals("0")) {
				menu.add(Menu.NONE, 0, 0, "добавить в избранное");
			} else {
				menu.add(Menu.NONE, 1, 0, "удалить из избранного");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		int menuItemIndex = item.getItemId();
		String[] menuItems = {"add", "remove"};
		String menuItemName = menuItems[menuItemIndex];
		if(menuItemName.equals("add")) {
			if(contextMenuMap != null) {
				AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
					@Override
					protected String doInBackground(String... arg0) {
						String address = ApiConst.STAR;
						String params = "channel_id=" + arg0[0];
						return HTTPClient.getXML(address, params, AuthRequestConst.AUTH_TV);
					}

					@Override
					protected void onPostExecute(String result) {
						String str;
						if(result.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
							str = "Невозможно подключиться к серверу";
						} else {
							contextMenuMap.put("star", "1");
							contextMenuAdapter.notifyDataSetChanged();
							str = "Канал добавлен в избранное: " + contextMenuMap.get("name");
						}

						Toast toast = Toast.makeText(getApplicationContext(),
						                             str, Toast.LENGTH_SHORT);
						toast.show();
						return;
					}
				};
				task.execute((String)contextMenuMap.get("id"));
			}
		} else if(menuItemName.equals("remove")) {
			if(contextMenuMap != null) {
				AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
					@Override
					protected String doInBackground(String... arg0) {
						String address = ApiConst.UNSTAR;
						String params = "channel_id=" + arg0[0];
						return HTTPClient.getXML(address, params, AuthRequestConst.AUTH_TV);
					}

					@Override
					protected void onPostExecute(String result) {
						String str;
						if(result.equals("<results status=\"error\"><msg>Can't connect to server</msg></results>")) {
							str = "Невозможно подключиться к серверу";
						} else {
							contextMenuAdapter.getData().remove(contextMenuMap);
							contextMenuAdapter.notifyDataSetChanged();
							str = "Канал удален из избранного: " + contextMenuMap.get("name");
						}

						Toast toast = Toast.makeText(getApplicationContext(),
						                             str, Toast.LENGTH_SHORT);
						toast.show();
						return;
					}
				};
				task.execute((String)contextMenuMap.get("id"));
			}
		}
		return true;
	}


}
