package ru.krasview.kvlib.widget.lists;

import java.util.HashMap;
import java.util.Map;

import ru.krasview.kvlib.indep.ListAccount;
import ru.krasview.secret.ApiConst;

import android.content.Context;

public class AllMovieList extends AllShowList {
	public  AllMovieList(Context context, Map<String, Object> map) {
		super(context, map);
	}

	protected String getApiAddress() {
		return ApiConst.MOVIE;
	}

	@Override
	protected String getType() {return "movie";}

	@Override
	public void setConstData() {
		Map<String, Object> m;
		if(account.isKrasviewAccount()) {
			m = new HashMap<String, Object>();
			m.put("type", "my_view");
			m.put("section", "movie");
			m.put("name", "Подписки");
			data.add(m);

			m = new HashMap<String, Object>();
			m.put("type", "faves");
			m.put("section", "movie");
			m.put("name", "Избранное");
			data.add(m);
		}
		m = new HashMap<String, Object>();
		m.put("type", "alfabet_movie");
		m.put("name", "По алфавиту");
		data.add(m);
	}

}
