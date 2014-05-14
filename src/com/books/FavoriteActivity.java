package com.books;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.os.Build;

public class FavoriteActivity extends ListActivity {

	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;
	
	private ArrayList<HashMap<String, Object>> FavoriteList;
	private static final String TAG_ID = "_id";
	private static final String TAG_TITLE = "title";
	private static final String TAG_AUTHOR = "author";
	private static final String TAG_ISBN = "isbn";
	private static final String TAG_DESCRIPTION = "description";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new DatenbankManager(this);
		setContentView(R.layout.activity_favorite);	

		Button Suche = (Button) findViewById(R.id.StartActivity_Suche); // Button
																			// für
																			// Suche,
																			// um
																			// auf
																			// die
																			// Suchmaske
																			// (SearchActivity)
																			// zu
																			// kommen
			Suche.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent suche = new Intent(getApplicationContext(),
							SearchActivity.class);
					startActivity(suche);
				}
			});

			Button Buecherei = (Button) findViewById(R.id.StartActivity_Buecherei); // Button
																					// für
																					// nächste
																					// Bücherei,
																					// um
																					// auf
																					// LibraryMapActivity
																					// zu
																					// kommen
			Buecherei.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent buecherei = new Intent(getApplicationContext(),
							MapActivity.class);
					startActivity(buecherei);

				}
			});


		// if (savedInstanceState == null) {
		// getSupportFragmentManager().beginTransaction()
		// }
	}

	public void getBooks() {
		FavoriteList = new ArrayList<HashMap<String, Object>>();
		Cursor bookCursor = mDatenbank.query("book",
				new String[] {"_id", "title", "author", "isbn", "description"},  
				null, null, null, null, null);
		HashMap<String, Object> item = new HashMap<String, Object>();
		bookCursor.moveToFirst();
		
		for (int i = 0; i < bookCursor.getCount(); i++){
			item = new HashMap<String, Object>();
			String title = bookCursor.getString(1);
			String author = bookCursor.getString(2);
			String isbn = bookCursor.getString(3);
			String description = bookCursor.getString(4);
			
			item.put(TAG_TITLE, title);
			item.put(TAG_AUTHOR, author);
			item.put(TAG_ISBN, isbn);
			item.put(TAG_DESCRIPTION, description);
			FavoriteList.add(item);
			
			bookCursor.moveToNext();
		}
		ListAdapter adapter = new SimpleAdapter (FavoriteActivity.this, FavoriteList, R.layout.activity_favorite_listview, 
				new String[] {TAG_TITLE, TAG_AUTHOR, TAG_ISBN}, 
				new int[] {R.id.FavoriteActivity_Titel, R.id.FavoriteActivity_Autor, R.id.FavoriteActivity_ISBN});
		
		setListAdapter(adapter);
	
	}
	
	protected void onResume() { // hier wird die Datenbank geöffnet
		super.onResume();
		mDatenbank = mHelper.getReadableDatabase();
//		Toast.makeText(this, "Datenbank geöffnet", Toast.LENGTH_SHORT).show();
		getBooks();
	}

	protected void onDestroy() { // hier wird beim kompletten schließen der
									// FavoriteActivity der Datenbankzugriff
									// gelöscht
		super.onDestroy();
		mDatenbank.close();
//		Toast.makeText(this, "Datenbank geschlossen", Toast.LENGTH_SHORT)
//				.show();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {	// Aktion, wenn auf Listenitem geklickt wird
		super.onListItemClick(l, v, position, id);

		Cursor cursor = mDatenbank.query("book", new String[] { "_id",
				"title", "author", "isbn", "description" }, null, null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToPosition(position)) {
				int column = cursor.getColumnIndex("title"); // um titel aus
				String titel = cursor.getString(column);	// liste auszulesen
															
				column = cursor.getColumnIndex("author");	 // um autor aus liste
				String autor = cursor.getString(column);	// auszulesen
								
				column = cursor.getColumnIndex("isbn"); 	// um isbn aus liste
				String isbn = cursor.getString(column);			// auszulesen
								
				column = cursor.getColumnIndex("_id");		//um _id der Spalte zu bekommen
				Integer _id = cursor.getInt(column);
				
				column = cursor.getColumnIndex("description");
				String description= cursor.getString(column);
				
				Toast.makeText(getApplicationContext(),
						titel + " wurde ausgewählt",
						Toast.LENGTH_SHORT).show();

				// hier werden dem Intent die Werte für die
				// FavoriteDetailActivity übergeben
				Intent startFavoriteDetail = new Intent(getApplicationContext(),
						FavoriteDetailActivity.class);
				startFavoriteDetail.putExtra(TAG_TITLE, titel);
				startFavoriteDetail.putExtra(TAG_AUTHOR, autor);
				startFavoriteDetail.putExtra(TAG_ISBN, isbn);
				startFavoriteDetail.putExtra(TAG_ID, _id);
				startFavoriteDetail.putExtra(TAG_DESCRIPTION, description);
				startActivity(startFavoriteDetail);
				Log.d("onListItemClick aufgerufen", "Item wurde ausgwählt, es sollte FavoriteDetailActivity starten");

			} // endif moveToPostition
		} // endif cursor != null

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.favorite, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
//	public static class PlaceholderFragment extends Fragment {
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_favorite,
//					container, false);
//			return rootView;
//		}
//	}

}
