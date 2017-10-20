package ru.krasview.kvlib.widget.lists;

import java.util.HashMap;
import java.util.Map;

import ru.krasview.kvlib.indep.ListAccount;
import ru.krasview.kvlib.indep.consts.AuthRequestConst;
import ru.krasview.secret.ApiConst;

import android.content.Context;

public class FavesList extends AllShowList {
	String section;

	public FavesList(Context context, Map<String, Object> map) {
		super(context, map);
		section = (String)map.get("section");
	}

	@Override
	protected int getAuthRequest() {
		return AuthRequestConst.AUTH_KRASVIEW;
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.BASE + section + "/faves";
	}

	@Override
	protected String getType() {return section;}


	@Override
	public void setConstData() {
	}
}
