package com.books;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		// start SearchActivity
		Button search = (Button) findViewById(R.id.StartActivity_Suche);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent suche = new Intent(getApplicationContext(),
						SearchActivity.class);
				startActivity(suche);
			}
		});
		
		// start MapActivity
		Button nearby = (Button) findViewById(R.id.StartActivity_Buecherei); 
		nearby.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent buecherei = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(buecherei);

			}
		});
		
		// start FavoriteActivity
		Button favorite = (Button) findViewById(R.id.StartActivity_Favoriten); 
		favorite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent favoriten = new Intent(getApplicationContext(),
						FavoriteActivity.class);
				startActivity(favoriten);
			}
		});

	}

}
