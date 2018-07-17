package ru.krasview.kvlib.widget.lists;

import android.content.Context;
import java.util.Map;
import ru.krasview.secret.ApiConst;

 public class AllGenreMovieList extends AllGenreShowList {
        public  AllGenreMovieList(Context context, Map<String, Object> map) {
            super(context);
        }

        protected String getApiAddress() {
            return ApiConst.SHOW_GENRES;
        }

        @Override
        protected String getType() {return "genre_movie";}

        @Override
        public void setConstData() {

        }

    }
