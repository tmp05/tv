package ru.krasview.kvlib.widget;

import java.util.HashMap;

import ru.krasview.kvlib.indep.Parser;
import ru.krasview.kvlib.interfaces.OnLoadCompleteListener;
import ru.krasview.kvlib.interfaces.ViewProposeListener;
import ru.krasview.kvlib.indep.HTTPClient;
import ru.krasview.secret.ApiConst;

import com.example.kvlib.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SerialDescription extends RelativeLayout {
	protected ImageView image;
	protected TextView text;
	protected Button button;
	protected TextView name;
	protected TextView params;

	public SerialDescription(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init1();
	}

	public SerialDescription(Context context, AttributeSet attrs) {
		super(context, attrs);
		init1();
	}

	public SerialDescription(Context context) {
		super(context);
		init1();
	}

	private void init1(){
		LayoutInflater ltInflater = ((Activity)getContext()).getLayoutInflater();
		ltInflater.inflate(R.layout.kv_serial_description, this, true);
		image = (ImageView)findViewById(R.id.image);
		text = (TextView)findViewById(R.id.text);
		name = (TextView)findViewById(R.id.name);
		button = (Button)findViewById(R.id.button);
		params = (TextView)findViewById(R.id.params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setTag(Object tag) {
		super.setTag(tag);
		HashMap<String,Object> hm = (HashMap<String,Object>)tag;
		new ImageAsyncTask().execute((String)hm.get("img_uri"));
		text.setText((String)hm.get("description"));
		name.setText((CharSequence)hm.get("show_name"));

		final String get_description = ApiConst.GET_DESCRIPTION;
		HTTPClient.getXMLAsync(get_description, "id="+hm.get("id") ,new OnLoadCompleteListener() {
			@Override
			public void loadComplete(String result) {
			}

			@SuppressWarnings("deprecation")
			@Override
			public void loadComplete(String address, String result) {
				Log.d("Description", result);
				if(address.equals(get_description)) {
					Document mDocument = Parser.XMLfromString(result);
					if(mDocument == null) { return; }
					mDocument.normalizeDocument();
					Node node = mDocument.getElementsByTagName("description").item(0);
					Element desc = (Element)node;
					String val = "";
					String text = "";
					text = desc.getElementsByTagName("year").item(0).getTextContent();
					if(text != "") val += "<b>Год</b>: " + text + "<br>";
					text = desc.getElementsByTagName("genres").item(0).getTextContent();
					if(text != "") val += "<b>Жанр</b>: " + text + "<br>";
					text = desc.getElementsByTagName("country").item(0).getTextContent();
					if(text != "") val += "<b>Производство</b>: " + text + "<br>";
					text = desc.getElementsByTagName("production_company").item(0).getTextContent();
					if(text != "") val += "<b>Компания</b>: " + text + "<br>";
					text = desc.getElementsByTagName("director").item(0).getTextContent();
					if(text != "") val += "<b>Режиссёр</b>: " + text + "<br>";
					text = desc.getElementsByTagName("actors").item(0).getTextContent();
					if(text != "") val += "<b>Актеры</b>: " + text + "<br>";
					text = desc.getElementsByTagName("writers").item(0).getTextContent();
					if(text != "") val += "<b>Сценаристы</b>: " + text + "<br>";
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						params.setText(Html.fromHtml(val, Html.FROM_HTML_MODE_COMPACT));
					} else {
						params.setText(Html.fromHtml(val));
					}
				}
			}
		});
	}

	class ImageAsyncTask extends AsyncTask<String,Void,Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			return HTTPClient.getImage(params[0]);
		}

		@Override 
		protected void onPostExecute(Bitmap bmp){
			image.setImageBitmap(bmp);
		}
	}

	protected ViewProposeListener mViewProposeListener;

	public void setViewProposeListener(ViewProposeListener listener) {
		mViewProposeListener = listener;
	}

	@Override public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
			KeyEvent e = new KeyEvent(event.getAction(), KeyEvent.KEYCODE_DPAD_CENTER);
			return super.dispatchKeyEvent(e);
		}
		return super.dispatchKeyEvent(event);
	}
}
