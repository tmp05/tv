package ru.krasview.kvlib.widget.lists;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.krasview.kvlib.adapter.LoadDataToGUITask;
import ru.krasview.kvlib.indep.Parser;
import ru.krasview.secret.ApiConst;
import ru.krasview.kvlib.widget.List;

import android.content.Context;
import android.text.Html;

public class MovieList extends List {
	public MovieList(Context context, Map<String, Object> map) {
		super(context, map);
	}

	@Override
	public void setConstData() {
		Map<String, Object> m;
		m = new HashMap<String, Object>();
		m.put("type", "series_light");
		m.put("name", "Описание");
		m.put("id", getMap().get("id"));
		m.put("img_uri", (String)getMap().get("img_uri"));
		m.put("description", (String)getMap().get("description"));
		m.put("show_name", getMap().get("name"));
		data.add(m);
	}
	@Override
	protected String getApiAddress() {
		return ApiConst.SHOW + "?" + "id=" + getMap().get("id") + "&movie";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void parseData(String doc, LoadDataToGUITask task) {
		Document mDocument;
		mDocument = Parser.XMLfromString(doc);
		if(mDocument == null){
			return;
		}
		mDocument.normalizeDocument();

		NodeList nListChannel = mDocument.getElementsByTagName("unit");
		int numOfChannel = nListChannel.getLength();

		Map<String, Object> m;
		for (int nodeIndex = 0; nodeIndex < numOfChannel; nodeIndex++) {
			Node locNode = nListChannel.item(nodeIndex);
			m = new HashMap<String, Object>();
			m.put("id", Parser.getValue("id", locNode));
			m.put("name",  Html.fromHtml(Parser.getValue("title", locNode)));
			m.put("uri", Parser.getValue("file", locNode));
			m.put("type", "video" );
			if(task.isCancelled()) {
				return;
			}
			task.onStep(m);
		}
	}
}
