package ru.krasview.kvlib.widget.lists;

import android.content.Context;

import java.util.Map;

import ru.krasview.secret.ApiConst;

public class GenreAnimeList extends GenreShowList{
    //не убирать
    @Override
    public void setConstData(){
    }

    public GenreAnimeList(Context context, Map<String, Object> map) {
        super(context, map);
    }

    @Override
    protected String getApiAddress() {
        return ApiConst.SHOW_GENRES_ANIME+"?id="+ getMap().get("id");
    }
}