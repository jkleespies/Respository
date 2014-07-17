package com.books;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FavoriteActivity extends ListActivity {

	// initialize variables
	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;

	private ArrayList<HashMap<String, Object>> FavoriteList;
	private static final String TAG_ID = "_id";
	private static final String TAG_TITLE = "title";
	private static final String TAG_AUTHOR = "author";
	private static final String TAG_ISBN = "isbn";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_IMAGE_NAME = "image_name";
	// private String imageName;
	private File path;
	private File file;
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new DatenbankManager(this);
		setContentView(R.layout.activity_favorite);

		// if list:empty
		// button for start SearchActivity
		Button Suche = (Button) findViewById(R.id.StartActivity_Suche);
		Suche.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent suche = new Intent(getApplicationContext(),
						SearchActivity.class);
				startActivity(suche);
			}
		});

		// button for start MapActivity
		Button Buecherei = (Button) findViewById(R.id.StartActivity_Buecherei);
		Buecherei.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent buecherei = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(buecherei);

			}
		});

	}

	// put content to FavoriteList
	// show ListView with content
	public void getBooks() {
		// initialize FavoriteList
		FavoriteList = new ArrayList<HashMap<String, Object>>();
		// cursor for handling DB
		Cursor bookCursor = mDatenbank.query("book", new String[] { "_id",
				"title", "author", "isbn", "description", "image" }, null,
				null, null, null, null);
		// initialize Hashmap for book item s
		HashMap<String, Object> item = new HashMap<String, Object>();
		bookCursor.moveToFirst();

		for (int i = 0; i < bookCursor.getCount(); i++) {
			item = new HashMap<String, Object>();
			String title = bookCursor.getString(1);
			String author = bookCursor.getString(2);
			String isbn = bookCursor.getString(3);
			// String description = bookCursor.getString(4);
			String imageName = bookCursor.getString(5);

			// open file "booksImageDir"
			ContextWrapper cw = new ContextWrapper(getApplicationContext());
			path = cw.getDir("booksImageDir", Context.MODE_PRIVATE);
			file = new File(path, imageName);

			// get bitmap image from file
			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(file));

				item.put(TAG_IMAGE, bitmap);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			item.put(TAG_TITLE, title);
			item.put(TAG_AUTHOR, author);
			item.put(TAG_ISBN, isbn);
			// item.put(TAG_DESCRIPTION, description);

			FavoriteList.add(item);

			bookCursor.moveToNext();
		}

		ListAdapter adapter = new ExtendedSimpleAdapter(
				FavoriteActivity.this,
				FavoriteList,
				R.layout.activity_favorite_listview,
				new String[] { TAG_TITLE, TAG_AUTHOR, TAG_ISBN, TAG_IMAGE },
				new int[] { R.id.FavoriteActivity_Titel,
						R.id.FavoriteActivity_Autor,
						R.id.FavoriteActivity_ISBN, R.id.FavoriteActivity_Bild });

		setListAdapter(adapter);

	}

	// open DB
	protected void onResume() {
		super.onResume();
		mDatenbank = mHelper.getReadableDatabase();
		getBooks();
	}

	// close DB
	protected void onDestroy() {
		super.onDestroy();
		mDatenbank.close();
	}

	// handle ListView click
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get values from ListView
		String titel = ((TextView) v.findViewById(R.id.FavoriteActivity_Titel))
				.getText().toString();
		String autor = ((TextView) v.findViewById(R.id.FavoriteActivity_Autor))
				.getText().toString();
		String isbn = ((TextView) v.findViewById(R.id.FavoriteActivity_ISBN))
				.getText().toString();
		ImageView image = ((ImageView) v.findViewById(R.id.FavoriteActivity_Bild));

		// save image
		image.buildDrawingCache();
		// initialize Bitmap
		Bitmap imageBitmap = image.getDrawingCache();
				
		// Cursor for loading information from Database
		Cursor cursor = mDatenbank.query("book", new String[] { "_id", "title",
				"author", "isbn", "description", "image" }, null, null, null,
				null, null);
		if (cursor != null) {
			if (cursor.moveToPosition(position)) {

				// get id from DB
				int column = cursor.getColumnIndex("_id");
				Integer _id = cursor.getInt(column);

				// get description from DB
				column = cursor.getColumnIndex("description");
				String description = cursor.getString(column);

				// get image-Name from DB
				column = cursor.getColumnIndex("image");
				String imageName = cursor.getString(column);

				Intent startFavoriteDetail = new Intent(
						getApplicationContext(), FavoriteDetailActivity.class);
				// insert values into Intent
				startFavoriteDetail.putExtra(TAG_TITLE, titel);
				startFavoriteDetail.putExtra(TAG_AUTHOR, autor);
				startFavoriteDetail.putExtra(TAG_ISBN, isbn);
				startFavoriteDetail.putExtra(TAG_ID, _id);
				startFavoriteDetail.putExtra(TAG_DESCRIPTION, description);
				startFavoriteDetail.putExtra(TAG_IMAGE, imageBitmap);
				startFavoriteDetail.putExtra(TAG_IMAGE_NAME, imageName);
				// start ActivtyFavoriteDetail
				startActivity(startFavoriteDetail);

				Log.d("onListItemClick aufgerufen",
						"Item wurde ausgwählt, es sollte FavoriteDetailActivity starten");

			}
		}

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

}
