package ru.krasview.kvlib.widget.lists;

import android.content.Context;

import java.util.Map;

import ru.krasview.secret.ApiConst;

public class GenreShowList extends AllShowList {
    String section;

    public void setConstData(){
    }

    public GenreShowList(Context context, Map<String, Object> map) {
        super(context, map);
        section = (String)map.get("type");
    }

    @Override
    protected String getApiAddress() {
        return ApiConst.BASE + section+"?id="+getMap().get("id");
      }
}
