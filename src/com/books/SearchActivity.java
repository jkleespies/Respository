// choose between scanner or manual search
package com.books;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		// call scanner 
		Button scannerButton = (Button)findViewById( R.id.searchActivity_scanner );
		scannerButton.setOnClickListener( new View.OnClickListener(){
			
			@Override
        	public void onClick(View v){
				Intent i = new Intent(getApplicationContext(), ScannerActivity.class);
				startActivityForResult(i, 0);
			}
		});
		
		Button searchButton = (Button)findViewById( R.id.searchActivity_search );
		searchButton.setOnClickListener( new View.OnClickListener(){
			
			@Override
        	public void onClick(View v){
				EditText search_keyword = (EditText)findViewById(R.id.searchActivity_input_field);
				String search_phrase = search_keyword.getText().toString();
				
				Intent i = new Intent(getApplicationContext(), SearchResultActivity.class);
				i.putExtra("searchfor", search_phrase);
				startActivity(i);
			}
		});


	}
	
	// get result from scanner
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    	super.onActivityResult(requestCode, resultCode, data);
	    	
			String returnValue = data.getExtras().getString("scanresult");
			//start Activity Result
	    	Intent i = new Intent(getApplicationContext(), SearchResultActivity.class);
	    	i.putExtra("searchfor", returnValue);
	    	startActivity(i);
	    }
	 

}
