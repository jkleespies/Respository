// display detail favorite items
package com.books;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteDetailActivity extends Activity {

	// initialize varialbe
	private TextView title;
	private TextView author;
	private TextView isbn;
	private TextView description;
	private ImageView image;

	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;

	private String readTitle;
	private String readAuthor;
	private String readISBN;
	private Integer readID;
	private String _id;
	private String readDescription;
	private Bitmap readImageBitmap;
	private String readImageName;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_detail);
		mHelper = new DatenbankManager(this);

		// get values from FavoriteActivity
		Intent startFavoriteDetail = getIntent();
		readTitle = startFavoriteDetail.getExtras().getString("title");
		readAuthor = startFavoriteDetail.getExtras().getString("author");
		readISBN = startFavoriteDetail.getExtras().getString("isbn");
		// id is need if data will be delete
		readID = startFavoriteDetail.getExtras().getInt("_id");
		readDescription = startFavoriteDetail.getExtras().getString(
				"description");
		readImageBitmap = startFavoriteDetail.getParcelableExtra("image");
		readImageName = startFavoriteDetail.getExtras().getString("image_name");

		// initialize GUI elements
		title = (TextView) findViewById(R.id.FavoriteDetailActivity_Titel);
		author = (TextView) findViewById(R.id.FavoriteDetailActivity_Autor);
		isbn = (TextView) findViewById(R.id.FavoriteDetailActivity_ISBN);
		description = (TextView) findViewById(R.id.FavoriteDetailActivity_Inhalt);
		image = (ImageView) findViewById(R.id.FavoriteDetailActivity_Bild);

		// set GUI elements
		title.setText(readTitle);
		author.setText(readAuthor);
		isbn.setText(readISBN);
		description.setText(readDescription);
		// description element should be scrollable
		description.setMovementMethod(new ScrollingMovementMethod());

		image.setImageBitmap(readImageBitmap);

		// get Filesystem "booksImageDir"
		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		File path = cw.getDir("booksImageDir", Context.MODE_PRIVATE);
		file = new File(path, readImageName);

		// delete button for delete book item form DB
		ImageView delete = (ImageView) findViewById(R.id.FavoriteDetailActivity_Loeschen);
		delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// delete book item
				_id = (Integer.toString(readID));
				String where = "_id = ?";
				String[] whereArgs = { _id };

				mDatenbank.delete("book", where, whereArgs);
				Toast.makeText(getApplicationContext(),
						"Eintrag aus Favoriten gelöscht", Toast.LENGTH_SHORT)
						.show();
				// delete image from file "booksImgDir"
				file.delete();
				// close activity
				finish();

			}
		});

		// start Activity Map
		Button buecherei = (Button) findViewById(R.id.FavoriteDetailActivity_Buecherei);
		buecherei.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent maps = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(maps);

			}
		});
	}

	public void onResume() {
		super.onResume();
		mDatenbank = mHelper.getReadableDatabase();
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
