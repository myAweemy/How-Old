package com.example.how_old;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class TitleLayout extends LinearLayout {

	public TitleLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.title, this);
		Button titleBack = (Button) findViewById(R.id.title_back);
		Button titleabout = (Button) findViewById(R.id.title_about);
		titleBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Activity) getContext()).finish();
			}
		});
		titleabout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(context,AboutActivity.class));
				//overridePendingTransition(R.anim.login_tobuttom, R.anim.welcome_tobuttom);
			}
		});
	}

}

/*

<include
android:id="@+id/title"
layout="@layout/title" 
android:layout_width="match_parent"
android:layout_height="45dp"
android:layout_alignParentLeft="true"
android:layout_alignParentRight="true"
android:layout_alignParentTop="true"
/>

<com.example.how_old.TitleLayout 
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:layout_alignParentTop="true"></com.example.how_old.TitleLayout>

*/