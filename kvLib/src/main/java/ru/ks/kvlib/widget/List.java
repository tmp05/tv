package ru.ks.kvlib.widget;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.ks.kvlib.adapter.CombineSimpleAdapter;
import ru.ks.kvlib.adapter.LoadDataToGUITask;
import ru.ks.kvlib.indep.AuthAccount;
import ru.ks.kvlib.indep.consts.AuthRequestConst;
import ru.ks.kvlib.indep.consts.TagConsts;
import ru.ks.kvlib.indep.consts.TypeConsts;
import ru.ks.kvlib.interfaces.Factory;
import ru.ks.kvlib.interfaces.ViewProposeListener;
import ru.ks.kvlib.interfaces.ViewPropotionerInterface;

public abstract  class List extends ListView implements ViewPropotionerInterface {
	private Factory mFactory;
	private ViewProposeListener mViewProposeListener;
	private Map<String, Object> mMap;

	protected ArrayList<Map<String, Object>> data;

	protected AuthAccount account;

	protected void setConstData() {}

	protected String getApiAddress() {
		return null;
	};

	protected int getAuthRequest() {
		return AuthRequestConst.AUTH_NONE;
	}

	public void parseData(String doc, LoadDataToGUITask task) {};

	public List(Context context, Map<String, Object> map) {
		super(context);
		data = new ArrayList<Map<String, Object>>();
		mMap = map;
		account = AuthAccount.getInstance();
		((Activity)getContext()).registerForContextMenu(this);
	}

	final public void setFactory(Factory factory) {
		mFactory = factory;
	}

	final protected Factory getFactory() {
		return mFactory;
	}

	final protected Map<String, Object> getMap() {
		return mMap;
	}

//--------------------------------------------------------------------
// Работа с биллингом
//--------------------------------------------------------------------

	protected boolean showBilling() {
		return false;
	}

	public void addBillingHeader() {
		if(showBilling()){
			Map<String, Object> m;
			m = new HashMap<String, Object>();
			m.put(TagConsts.TYPE, TypeConsts.BILLING);
			m.put(TagConsts.NAME, "");
			data.add(0, m);
		}
	}

//--------------------------------------------------------------------
// Работа с адаптером
//--------------------------------------------------------------------

	protected CombineSimpleAdapter createAdapter() {
		return new CombineSimpleAdapter(this, data, getApiAddress(), getAuthRequest());
	}

	@Override
	public CombineSimpleAdapter getAdapter() {
		return (CombineSimpleAdapter)super.getAdapter() ;
	}

//--------------------------------------------------------------------
// Implementation of View method
//--------------------------------------------------------------------

	@Override
	public boolean dispatchKeyEvent (KeyEvent event) {
		boolean handled = super.dispatchKeyEvent(event);
		if(!handled) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				KeyEvent e = new KeyEvent(event.getAction(), KeyEvent.KEYCODE_DPAD_CENTER);
				handled = super.dispatchKeyEvent(e);
			}
		}
		return handled;
	}

//--------------------------------------------------------------------
// Implementation of ViewPropotionerInterface
//--------------------------------------------------------------------

	@Override
	final public void setViewProposeListener(ViewProposeListener listener) {
		mViewProposeListener = listener;
	}

	@Override
	final public ViewProposeListener getViewProposeListener() {
		return mViewProposeListener;
	}

	@Override
	public void init() {
		addBillingHeader();
		setConstData();
		this.setAdapter(createAdapter());
		// this.setOnItemClickListener(mOnItemClickListener);
	}

	@Override
	public void refresh() {
		editConstData();
		getAdapter().refresh();
	}

	@Override
	public void enter() {
		editConstData();
	}

	private void editConstData() {
		getAdapter().editConstData();
		setConstData();
		addBillingHeader();
		getAdapter().notifyDataSetChanged();
	}

	@Override
	public void exit() {
	}

}
