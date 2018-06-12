package com.example.park.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.park.R;
import com.example.park.activities.MainActivity;

public class ParkFragment extends Fragment {
	View view;
	private Button personButton, publicButton;
	private PublicPark fragment1;
	private PersonPark fragment2;

	public static final int PUBLIC_PARK = 1;
	public static final int PERSON_PARK = 2;
	public int currentFragmentType = -1;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_park, container, false);
		initData();
		return view;
	}

	private void initData() {
		personButton = (Button) view.findViewById(R.id.personButton);
		publicButton = (Button) view.findViewById(R.id.publicButton);
		personButton.setOnClickListener(onClicker);
		publicButton.setOnClickListener(onClicker);
		loadFragment(PUBLIC_PARK);
	}


	private void switchFragment(int type) {
		switch (type) {
			case PUBLIC_PARK:
				loadFragment(PUBLIC_PARK);
				break;
			case PERSON_PARK:
				loadFragment(PERSON_PARK);
				break;
		}


	}


	private void loadFragment(int type) {
		//Fragment嵌套Fragment必须用getChildFragmentManager，不然tab切换界面显示不出来，不信你试试
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (type == PUBLIC_PARK) {
			if (fragment1 == null) {
				fragment1 = new PublicPark();
				transaction.add(R.id.parkLayout, fragment1, "公共停车位");

			} else {
				transaction.show(fragment1);
			}
			if (fragment2 != null) {
				transaction.hide(fragment2);
			}
			currentFragmentType = PUBLIC_PARK;
			MainActivity.toolbar.setTitle("公共车位");
		} else {
			if (fragment2 == null) {
				fragment2 = new PersonPark();
				transaction.add(R.id.parkLayout, fragment2, "私人停车位");
			} else {
				transaction.show(fragment2);
			}
			if (fragment1 != null) {
				transaction.hide(fragment1);
			}
			currentFragmentType = PERSON_PARK;
			MainActivity.toolbar.setTitle("私人车位");
		}
		transaction.commitAllowingStateLoss();
	}




	private View.OnClickListener onClicker = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.publicButton:
					personButton.setBackground(getResources().getDrawable(R.drawable.left_tab));
					personButton.setTextColor(getResources().getColor(R.color.tab));
					publicButton.setBackground(getResources().getDrawable(R.drawable.right_tab));
					publicButton.setTextColor(getResources().getColor(R.color.white));
					switchFragment(PUBLIC_PARK);
					break;
				case R.id.personButton:
					publicButton.setBackground(getResources().getDrawable(R.drawable.left_tab));
					publicButton.setTextColor(getResources().getColor(R.color.tab));
					personButton.setBackground(getResources().getDrawable(R.drawable.right_tab));
					personButton.setTextColor(getResources().getColor(R.color.white));
					switchFragment(PERSON_PARK);
					break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}