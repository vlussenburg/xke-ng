package com.xebia.xcoss.axcv;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Session;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class CVSearchAuthor extends BaseActivity {

	private Session session;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_author);
		
		session = getSession(getConference());
		
        String[] allPersons = ConferenceServer.getInstance().getAllAuthors();
        
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, allPersons);
        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.ssa_authortext);
        textView.setAdapter(adapter);
        textView.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER ) {
					Log.e("XCS", "Reads = " + textView.getText().toString());
				}
				session.addAuthor("Test");
				return false;
			}
		});
        
        // set up the list
    }

}
