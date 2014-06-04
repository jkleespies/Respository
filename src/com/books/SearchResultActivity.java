package com.books;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultActivity extends ListActivity {

	private ProgressDialog pDialog;

	// URL für die JSON Anfrage und die Sucheingabe
	public String url;
	public String search_phrase;
	
	// Deklaration von mehrmals benötigten Variablen
	private static final String TAG_ITEMS = "items";
	private static final String TAG_VOLUMEINFO = "volumeInfo";
	private static final String TAG_TITLE = "title";
	private static final String TAG_AUTHORS = "authors";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_INDUSTRYIDENTIFIER = "industryIdentifiers";
	private static final String TAG_IDENTIFIER = "identifiers";
	private static final String TAG_IMAGELINKS = "imageLinks";
	private static final String TAG_THUMBNAIL = "thumbnail";
	public Bitmap bm; 
	
	// JSON Array initialisieren
	JSONArray items = null;
	
	// Hashmap für die ListView
	ArrayList<HashMap<String, Object>> bookList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);

		Intent i = getIntent();
		String searchPhrase = i.getExtras().getString("searchfor");
		
		if(searchPhrase.equals("")) {
			TextView tv = (TextView)findViewById(R.id.SearchResultActivity_Suche);
			tv.setText(getString(R.string.SearchResultActivity_emptyValue));
		}
		else {
			TextView tv = (TextView)findViewById(R.id.SearchResultActivity_Suche);
//			tv.setText(searchPhrase);	
//			 unterstreicht die Suchphrase
			SpannableString content = new SpannableString(searchPhrase);	
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			tv.setText(content);
			
			
			bookList = new ArrayList<HashMap<String, Object>>();	         
		
			ListView lv = getListView();	
		
			// Suchstring an die URL anketten und die Leerzeichen durch %20 ersetzen
			String searchurl = "https://www.googleapis.com/books/v1/volumes?q="+ searchPhrase;
			url = searchurl.replaceAll("[ ]", "%20");
		
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Werte des ausgewählten ListView Items
					String authors = ((TextView) view.findViewById(R.id.SearchResultActivity_Autor))
							.getText().toString();
					String title = ((TextView) view.findViewById(R.id.SearchResultActivity_Titel))
							.getText().toString();
					String description = ((TextView) view.findViewById(R.id.SearchResultActivity_Inhalt))
							.getText().toString();	
					String identifier = ((TextView) view.findViewById(R.id.SearchResultActivity_ISBN))
							.getText().toString();
					ImageView image = (ImageView)findViewById(R.id.SearchResultActivity_Bild);
					image.buildDrawingCache();
					Bitmap imageBitmap = image.getDrawingCache();
					
					// Starten der Detailansicht
					Intent in = new Intent(getApplicationContext(), SearchResultDetailActivity.class);
					in.putExtra(TAG_AUTHORS, authors);
					in.putExtra(TAG_TITLE, title);
					in.putExtra(TAG_DESCRIPTION, description);
					in.putExtra(TAG_IDENTIFIER, identifier);
					in.putExtra(TAG_THUMBNAIL, imageBitmap);
					startActivity(in);
				}
			});
			
			// Async task aufrufen um den JSON zu bekommen
			new GetBooks().execute();
		}
	}	
	
	/**
	 * HTTP Call für den JSON Aufruf
	 **/
	private class GetBooks extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Ladebildschirm
			pDialog = new ProgressDialog(SearchResultActivity.this);
			pDialog.setMessage("Bitte warten...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Adapter ad = new Adapter();

			// URL request und response
			String jsonStr = ad.makeServiceCall(url, Adapter.GET);			
			Log.d("Response: ", "> " + jsonStr);

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);
					
					// JSON Array auslesen
					items = jsonObj.getJSONArray(TAG_ITEMS);
					
					// Schleifendurchlauf für die Bücher            						            	
					for (int i = 0; i < 10; i++) {	
						JSONObject it = items.getJSONObject(i);										
						
						JSONObject volumeInfo = it.getJSONObject(TAG_VOLUMEINFO);
						String author = volumeInfo.getString("authors").replaceAll("[^a-zA-Z, ]", "");
						String authors = author.replace("," , ", ");			
						String title = volumeInfo.getString(TAG_TITLE);	
						
						String description = "";
						if (volumeInfo.has(TAG_DESCRIPTION) == true) {
							description = volumeInfo.getString(TAG_DESCRIPTION);
						}
						else {
							description = getString(R.string.no_description_available);
						}
						
						String identifier = "";
						JSONArray industryIdentifiers = volumeInfo.getJSONArray(TAG_INDUSTRYIDENTIFIER);
						
						for (int j = 0; j < industryIdentifiers.length(); j++) {			
							JSONObject industry = industryIdentifiers.getJSONObject(j);							
							
							if (industry.get("type").equals("ISBN_13")) {
								identifier = industry.getString("identifier");
							}	
							else if (industry.get("type").equals("OTHER")) {
								identifier = industry.getString("identifier");
							}
						}
								
						JSONObject imageLinks = volumeInfo.getJSONObject(TAG_IMAGELINKS);
						String thumbnail = imageLinks.getString(TAG_THUMBNAIL);
						
						BitmapFactory.Options bmOptions;
						bmOptions = new BitmapFactory.Options();
						bmOptions.inSampleSize = 1;
						bm = loadBitmap(thumbnail, bmOptions);
						
						// Tmp Hashmap für die einzelnen Bücherdaten
						HashMap<String, Object> item = new HashMap<String, Object>();

						// Einzelne Bücher der Hashmap hinzufügen
						item.put(TAG_AUTHORS, authors);
						item.put(TAG_TITLE, title);
						item.put(TAG_DESCRIPTION, description);	
						item.put(TAG_IDENTIFIER, identifier);
						item.put(TAG_THUMBNAIL, bm);

						// Buch der Buchliste hinzufügen
						bookList.add(item);
					}
				}
				catch(JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				Log.e("ServiceHandler", "Es konnten keine Bücher abgerufen werden");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (pDialog.isShowing())
				pDialog.dismiss();
			
			/*** Updaten der JSON Daten in die ListView */
			

			ListAdapter adapter = new ExtendedSimpleAdapter(SearchResultActivity.this, bookList, R.layout.activity_search_result_listitem,
					new String[] {TAG_AUTHORS, TAG_TITLE, TAG_DESCRIPTION, TAG_IDENTIFIER, TAG_THUMBNAIL}, 
					new int[] {R.id.SearchResultActivity_Autor, R.id.SearchResultActivity_Titel, R.id.SearchResultActivity_Inhalt, R.id.SearchResultActivity_ISBN, R.id.SearchResultActivity_Bild});
			setListAdapter(adapter);
//			ListAdapter adapter = new SimpleAdapter (
//					SearchResultActivity.this, bookList,
//					R.layout.activity_search_result_listitem, new String[] { TAG_AUTHORS, TAG_TITLE, TAG_DESCRIPTION, TAG_IDENTIFIER }, 
//					new int[] { R.id.SearchResultActivity_Autor, R.id.SearchResultActivity_Titel, R.id.SearchResultActivity_Inhalt, R.id.SearchResultActivity_ISBN }
//			);
//
//			setListAdapter(adapter);
		}
		
		public Bitmap loadBitmap(String URL, BitmapFactory.Options options) {
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
		
		private InputStream OpenHttpConnection(String strURL)
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
}
