package com.books;

//Importieren der Bibliotheken
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultDetailActivity extends Activity {

	//	Variablen deklarieren
	private static final String TAG_AUTHORS = "authors";
	private static final String TAG_TITLE = "title";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_IDENTIFIER = "identifier";
	private Button hinzufuegen;
	private SQLiteDatabase mDatenbank;
	private DatenbankManager mHelper;
	private static final String KLASSEN_SELECT_RAW = "SELECT _id, titel, autor FROM klassen";
	private String authors="";
	private String title="";
	private String description="";
//	private String isbn;
	
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
//        isbn = in.getStringExtra(TAG_IDENTIFIER);

        //	Ausgabe der Werte
        TextView lblAuthor = (TextView) findViewById(R.id.SearchResultDetailActivity_AutorLabel);
        TextView lblTitle = (TextView) findViewById(R.id.SearchResultDetailActivity_TitelLabel);
        TextView lblDescription = (TextView) findViewById(R.id.SearchResultDetailActivity_InhaltLabel);
//        TextView lblIsbn = (TextView) findViewById(R.id.SearchResultDetailActivity_ISBNLabel); 

        lblAuthor.setText(authors);
        lblTitle.setText(title);
        lblDescription.setText(description);
//        lblIsbn.setText(isbn);
        
        
		hinzufuegen = (Button) findViewById(R.id.SearchResultDetailActivity_Hinzufuegen);
		hinzufuegen.setOnClickListener(new View.OnClickListener() { // Button
																	// zum
																	// Hinzufüengen
																	// in die
																	// SQL
																	// Datenbank
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				ContentValues werte = new ContentValues();
								
				werte.put("titel", title);
				werte.put("autor", authors);
//				werte_isbn.put("isbn", isbn);
				mDatenbank.insert("klassen",null,werte);
				
				Toast.makeText(
						getApplicationContext(),
						title + "in Favoriten gespeichert",
						Toast.LENGTH_SHORT).show();
				
				Intent goToStart = new Intent (getApplicationContext(), StartActivity.class);
				startActivity(goToStart);
				
			}

		});
        

    }
	
	protected void onResume(){
		super.onResume();
		mDatenbank = mHelper.getReadableDatabase();
		Toast.makeText(this, "Datenbank geöffnet", Toast.LENGTH_SHORT).show();
	}
	
	
}