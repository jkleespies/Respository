// display datail view from results
package com.books;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultDetailActivity extends Activity {

	// set variables
	private static final String TAG_AUTHORS = "authors";
	private static final String TAG_TITLE = "title";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_IDENTIFIER = "identifiers";
	private static final String TAG_THUMBNAIL = "thumbnail";
	private ImageView add;
	private Button nextLibrary;
	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;

	private String authors = "";
	private String title = "";
	private String description = "";
	private String identifiers = "";
	private String imageName;
	private Bitmap imageBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result_detail);
		mHelper = new DatenbankManager(this);

		// get values from SearchActivity
		Intent in = getIntent();
		authors = in.getStringExtra(TAG_AUTHORS);
		title = in.getStringExtra(TAG_TITLE);
		description = in.getStringExtra(TAG_DESCRIPTION);
		identifiers = in.getStringExtra(TAG_IDENTIFIER);
		imageBitmap = in.getParcelableExtra(TAG_THUMBNAIL);

		// initialize GUI elements
		TextView lblAuthor = (TextView) findViewById(R.id.SearchResultDetailActivity_AutorLabel);
		TextView lblTitle = (TextView) findViewById(R.id.SearchResultDetailActivity_TitelLabel);
		TextView lblDescription = (TextView) findViewById(R.id.SearchResultDetailActivity_InhaltLabel);
		lblDescription.setMovementMethod(new ScrollingMovementMethod());
		TextView lblIsbn = (TextView) findViewById(R.id.SearchResultDetailActivity_ISBNLabel);
		ImageView lblImage = (ImageView) findViewById(R.id.SearchResultDetailActivity_BildLabel);

		// set content GUI elements
		lblAuthor.setText(authors);
		lblTitle.setText(title);
		lblDescription.setText(description);
		lblIsbn.setText(identifiers);
		lblImage.setImageBitmap(imageBitmap);

		// open MapActivity
		nextLibrary = (Button) findViewById(R.id.SearchResultDetailActivity_Buecherei);
		nextLibrary.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent library = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(library);
			}
		});

		// Button for adding book item in DB
		add = (ImageView) findViewById(R.id.SearchResultDetailActivity_Hinzufuegen);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("hinzufügen", "werte werden in db eingefügt");

				// save image in File "imageDir"
				ContextWrapper cw = new ContextWrapper(getApplicationContext());
				// file name = "booksImageDir"
				File directory = cw.getDir("booksImageDir",
						Context.MODE_PRIVATE);

				// open Cursor for DB
				Cursor bookCursor = mDatenbank.query("book", new String[] {
						"_id", "title", "author", "isbn", "description",
						"image" }, null, null, null, null, null);
				// request if DB == null or not null
				// create image name
				// if book images exits create image name "image_ + _id + .jpg"
				if (bookCursor != null && bookCursor.moveToFirst()) {
					Log.d("Curor ungleich null", "Button add");
					bookCursor.moveToLast();
					int column = bookCursor.getColumnIndex("_id");
					Integer _id = bookCursor.getInt(column);
					_id++;
					imageName = "image_" + _id + ".jpg";
				} // if book images not exist imagename "image_1.jpg"
				else {
					Log.d("Cursor ist null", "Button add");
					imageName = "image_1.jpg";
				}

				// save image in directory "booksImageDir"
				File mypath = new File(directory, imageName);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(mypath);
					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Log.d("IMAGE_NAME", imageName);

				ContentValues werte = new ContentValues();
				// values for DB
				werte.put("title", title);
				werte.put("author", authors);
				werte.put("isbn", identifiers);
				werte.put("description", description);
				werte.put("image", imageName);

				// insert values in DB
				mDatenbank.insert("book", null, werte);

				Toast.makeText(getApplicationContext(), title + " gespeichert",
						Toast.LENGTH_SHORT).show();
				finish();
			}

		});
	}

	protected void onResume() {
		super.onResume();
		mDatenbank = mHelper.getWritableDatabase();
	}
	
	// close DB 
	protected void onDestroy() {
		super.onDestroy();
		mDatenbank.close();
	}
}
