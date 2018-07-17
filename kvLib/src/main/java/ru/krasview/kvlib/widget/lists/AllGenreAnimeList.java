package ru.krasview.kvlib.widget.lists;

import android.content.Context;

import java.util.Map;

import ru.krasview.secret.ApiConst;

public class AllGenreAnimeList extends AllGenreShowList {
    public  AllGenreAnimeList(Context context, Map<String, Object> map) {
        super(context);
    }

    protected String getApiAddress() {
        return ApiConst.SHOW_GENRES;
    }

    @Override
    protected String getType() {return "genre_anime";}

    @Override
    public void setConstData() {

    }

}