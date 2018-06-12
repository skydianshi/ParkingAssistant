package com.example.park.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.park.R;


public class ConnectDialog extends Dialog {

	private TextView mTextViewTips;
	private Context mContext;

	public ConnectDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		mContext = context;
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		setCancelable(false);
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.connect_view, null);
		mTextViewTips = (TextView) view.findViewById(R.id.textViewCnnTips);
		setContentView(view);
	}

	public void setTips(int nStrID) {
		if (mTextViewTips != null) {
			mTextViewTips.setText(nStrID);
		}
	}
}
