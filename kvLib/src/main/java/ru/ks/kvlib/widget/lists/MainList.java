package ru.ks.kvlib.widget.lists;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import ru.ks.kvlib.indep.consts.TypeConsts;
import ru.ks.kvlib.widget.List;

public class MainList extends List {
	public MainList(Context context) {
		super(context, null);
	}

	@Override
	public void setConstData() {
		Map<String, Object> m;
		if(account.isKSAccount()){
			m = new HashMap<String, Object>();
			m.put("type", "my_shows_all");
			m.put("name", "Я смотрю");
			data.add(m);
		}
		if(account.isTVAccount()){
			m = new HashMap<String, Object>();
			m.put("type", TypeConsts.TV);
			m.put("name", "Телевидение");
			data.add(m);
		}

	}
}
