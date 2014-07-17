// choose between scanner or manual search
package com.books;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_search);

	Button scannerButton = (Button) findViewById(R.id.searchActivity_scanner);
	scannerButton.setOnClickListener(new View.OnClickListener() {

	@Override
	public void onClick(View v) {
		
	Intent intent = new Intent(getApplicationContext(),	ZBarScannerActivity.class);
	startActivityForResult(intent, 0);
	}
	});

	Button searchButton = (Button) findViewById(R.id.searchActivity_search);
	searchButton.setOnClickListener(new View.OnClickListener() {

	@Override
	public void onClick(View v) {
	EditText search_keyword = (EditText) findViewById(R.id.searchActivity_input_field);
	String search_phrase = search_keyword.getText().toString();
	if (search_phrase.compareTo("") == 0) {
	Toast toast = Toast.makeText(getApplicationContext(),
	"wronge input", Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
	toast.show();
	} else {
	Intent i = new Intent(getApplicationContext(),
	SearchResultActivity.class);
	i.putExtra("searchfor", search_phrase);
	startActivity(i);
	}

	}
	});

	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	if (resultCode == RESULT_OK) {
	// Scan result is available by making a call to
	// data.getStringExtra(ZBarConstants.SCAN_RESULT)
	// Type of the scan result is available by making a call to
	// data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
	Toast.makeText(	this,"Scan Result = "+ data.getStringExtra(ZBarConstants.SCAN_RESULT),Toast.LENGTH_SHORT).show();
	String returnValue = data.getStringExtra(ZBarConstants.SCAN_RESULT);

	Intent i = new Intent(getApplicationContext(),
	SearchResultActivity.class);
	i.putExtra("searchfor", returnValue);

	startActivity(i);

	// The value of type indicates one of the symbols listed in Advanced
	// Options below.
	} else if (resultCode == RESULT_CANCELED) {
	Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT)
	.show();
	}

	}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {

			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// initialize Intent 
			Intent i;
			// Switch case between buttons, start activities 
			switch (item.getItemId()){
			case R.id.search:
				i = new Intent(getApplicationContext(), SearchActivity.class);
				startActivity(i);
				break;
			case R.id.map:
				i = new Intent(getApplicationContext(), MapActivity.class);
				startActivity(i);
				break;
			case R.id.favorite:
				i = new Intent(getApplicationContext(), FavoriteActivity.class);
				startActivity(i);
				break;
			default:
				Log.d("onOptionsItemSelected", "es wurde nichts aufgewählt");
			}
				return super.onOptionsItemSelected(item);
		}
}
