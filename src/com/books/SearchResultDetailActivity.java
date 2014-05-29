package com.books;

//Importieren der Bibliotheken
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.google.android.gms.plus.model.people.Person.Image;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultDetailActivity extends Activity {

	//	Variablen deklarieren
	private static final String TAG_AUTHORS = "authors";
	private static final String TAG_TITLE = "title";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_IDENTIFIER = "identifiers";
	private Button add;
	private Button nextLibrary;
	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;

	private String authors="";
	private String title="";
	private String description="";
    private String identifiers="";
	private String imageName;
	Bitmap bm;
	private byte[] img = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_detail);
        mHelper = new DatenbankManager(this);
        
        //	Zum verbinden der verschiedenen Activitys
        Intent in = getIntent();

        /* ISBN 10 und 13 per if abfrage */
        
        //	JSON Werte aus dem vorherigen Intent ziehen
        authors = in.getStringExtra(TAG_AUTHORS);
        title = in.getStringExtra(TAG_TITLE);
        description = in.getStringExtra(TAG_DESCRIPTION);
        identifiers = in.getStringExtra(TAG_IDENTIFIER);
        
		// TESTWEISE EIN BILD ALS BITMAP ERHALTEN UND SPEICHERN
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		.permitAll().build();
		StrictMode.setThreadPolicy(policy);
        String url = "http://bks4.books.google.de/books?id=JrsJX4dzXB0C&printsec=frontcover&img=1&zoom=1&source=gbs_api";
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		bm = loadBitmap(url, bmOptions);
        ImageView lblImage = (ImageView) findViewById(R.id.SearchResultDetailActivity_BildLabel);
        lblImage.setImageBitmap(bm);
        
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
		img = bos.toByteArray();

         //	Ausgabe der Werte
        TextView lblAuthor = (TextView) findViewById(R.id.SearchResultDetailActivity_AutorLabel);
        TextView lblTitle = (TextView) findViewById(R.id.SearchResultDetailActivity_TitelLabel);
        TextView lblDescription = (TextView) findViewById(R.id.SearchResultDetailActivity_InhaltLabel);
        lblDescription.setMovementMethod(new ScrollingMovementMethod());
        TextView lblIsbn = (TextView) findViewById(R.id.SearchResultDetailActivity_ISBNLabel); 

       
        
        lblAuthor.setText(authors);
        lblTitle.setText(title);
        lblDescription.setText(description);
        lblIsbn.setText(identifiers);
        
    
        
        
		add = (Button) findViewById(R.id.SearchResultDetailActivity_Hinzufuegen);
		add.setOnClickListener(new View.OnClickListener() { // Button
																	// zum
																	// Hinzufüengen
																	// in die
																	// SQL
																	// Datenbank
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("hinzufügen", "werte werden in db eingefügt");
				
				// Speichern des Bildes in File "imageDir"
				ContextWrapper cw = new ContextWrapper (getApplicationContext());
				File directory = cw.getDir("booksImageDir", Context.MODE_PRIVATE);
				
				Cursor bookCursor = mDatenbank.query("book",
						new String[] {"_id", "title", "author", "isbn", "description", "image"},  
						null, null, null, null, null);				
				// Abfrage, ob DB schon Inhalte aufweist und je nachdem Bildnamen festlegen
				if (bookCursor != null && bookCursor.moveToFirst()){
					Log.d("Curor ungleich null", "Button add");
					bookCursor.moveToLast();
					int column = bookCursor.getColumnIndex("_id");
					Integer _id = bookCursor.getInt(column);
					_id++;
					imageName = "image_" + _id + ".jpg";
				} else {
					Log.d("Cursor ist null", "Button add");
					imageName = "image_1.jpg";
				}
				File mypath = new File(directory, imageName);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(mypath);
					bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (Exception e){
					e.printStackTrace();
				}
				
				ContentValues werte = new ContentValues();
				// Werte die in die Datenbank geschrieben werden sollen			
				werte.put("title", title);
				werte.put("author", authors);
				werte.put("isbn", identifiers);
				werte.put("description", description);
				werte.put("image", imageName);
				
				
				//Befüllen der DB
				mDatenbank.insert("book",null,werte);
				
				Toast.makeText(
						getApplicationContext(),
						title + "in Favoriten gespeichert",
						Toast.LENGTH_SHORT).show();
				
				Intent goToStart = new Intent (getApplicationContext(), StartActivity.class);
				startActivity(goToStart);
				
			}

		});
        
		nextLibrary = (Button) findViewById(R.id.SearchResultDetailActivity_Buecherei); 
		nextLibrary.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent library = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(library);

			}
		});
    }
	
	protected void onResume(){
		super.onResume();
		mDatenbank = mHelper.getReadableDatabase();
		Toast.makeText(this, "Datenbank geöffnet", Toast.LENGTH_SHORT).show();
	}
	
	//*********** TESTWEISE HINZUGEFÜGTE KLASSEN!!!!
	public static Bitmap loadBitmap(String URL, BitmapFactory.Options options) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = OpenHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in, null, options); // methodenaufruf
																	// OpenHttpConnection
			in.close();
		} catch (IOException e1) {
		}
		return bitmap;
	}

	private static InputStream OpenHttpConnection(String strURL)
			throws IOException {
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();
			}
		} catch (Exception ex) {
		}
		return inputStream;
	}
	
	
}
