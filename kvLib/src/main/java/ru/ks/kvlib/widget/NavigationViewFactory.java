package ru.ks.kvlib.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import ru.ks.kvlib.indep.consts.TagConsts;
import ru.ks.kvlib.indep.consts.TypeConsts;
import ru.ks.kvlib.interfaces.Factory;
import ru.ks.kvlib.widget.lists.DateList;
import ru.ks.kvlib.widget.lists.RecordList;
import ru.ks.kvlib.widget.lists.TVFavoriteList;
import ru.ks.kvlib.widget.lists.TVFavoriteRecordList;
import ru.ks.kvlib.widget.lists.TVList;
import ru.ks.kvlib.widget.lists.TVRecordList;

public class NavigationViewFactory implements Factory {
	private Context c;

	public NavigationViewFactory(){
	}

	@Override
	public View getView(Map<String, Object> map, Context context){
		c = context;
		View view = null;
		String type = (String)map.get(TagConsts.TYPE);
		// Log.d("Navigation", "type: " + type);
		if(type == null)				{view = get_null();
		}else if(type.equals(TypeConsts.MAIN))		{view = new TVList(c);
		}else if(type.equals(TypeConsts.TV))		{view = new TVList(c);
		}else if(type.equals(TypeConsts.FAVORITE_TV))	{view = new TVFavoriteList(c);
		//}else if(type.equals(TypeConsts.MY_ALL))	{view = new UserShowList(c);
		//}else if(type.equals("my_view"))		{view = new UserShowList(c,(String)map.get("section"));
		}else if(type.equals(TypeConsts.TV_RECORD))	{view = new TVRecordList(c);
		}else if(type.equals(TypeConsts.FAVORITE_TV_RECORD))
								{view = new TVFavoriteRecordList(c);
		}else if(type.equals(TypeConsts.DATE_LIST))	{view = new DateList(c, map);
		}else if(type.equals(TypeConsts.RECORD_LIST))	{view = new RecordList(c, map);
		}
		else						{view = get_unknown(type);
		}
		if(implementsInterface(view, List.class)){
			((List)view).setFactory(this);
			((List)view).setOnItemClickListener(new KVItemClickListener(((List)view)));
		}
		return view;
	}

	@SuppressWarnings("rawtypes")
	private static boolean implementsInterface(Object object, Class inter){
		if(inter.isInstance(object)){
			return true;
		}else{
			return false;
		}
	}

	private View get_unknown(String type ){
		TextView text = new TextView(c);
		text.setText("тип " + type);
		text.setTextSize(30);
		text.setGravity(Gravity.CENTER);
		return text;
		//return null;
	}

	private static View get_null(){
		return null;
	}
}