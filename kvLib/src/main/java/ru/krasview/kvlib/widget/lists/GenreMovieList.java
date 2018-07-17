package ru.krasview.kvlib.widget.lists;

import android.content.Context;

import java.util.Map;

import ru.krasview.secret.ApiConst;

public class GenreMovieList extends GenreShowList{
    @Override
    public void setConstData(){
    }

    public GenreMovieList(Context context, Map<String, Object> map) {
        super(context, map);
    }

    @Override
    protected String getApiAddress() {
        return ApiConst.SHOW_GENRES_MOVIE+"?id="+ getMap().get("id");
    }
}