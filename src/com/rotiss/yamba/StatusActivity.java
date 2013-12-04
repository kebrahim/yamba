package com.rotiss.yamba;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity implements OnClickListener, TextWatcher, OnSharedPreferenceChangeListener {
	private static final String TAG = "StatusActivity";
	EditText editText;
	Button updateButton;
	Twitter twitter;
  TextView textCount;
  SharedPreferences prefs;
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// inflate java objects from status.xml
		// R --> /res
		setContentView(R.layout.status);
		
		// Find views using lookup by id's defined in xml
		editText = (EditText) findViewById(R.id.editText);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		
		// set on-click listener to this class, which has the onClick method defined
		updateButton.setOnClickListener(this);

		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.GREEN);
		editText.addTextChangedListener(this);
		
		// setup preferences
		// prefs are not shared among apps; just shared by various parts of this app
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private Twitter getTwitter() {
	  if (twitter == null) {
	    String username, password, apiRoot;
	    // retrieve username/pass/apiroot from shared preferences store, using android:key's defined
	    // in prefs.xml
	    username = prefs.getString("username", "");
	    password = prefs.getString("password", "");
	    apiRoot = prefs.getString("apiRoot", "http://yamba.marakana.com/api");

	    // Connect to twitter.com
	    twitter = new Twitter(username, password);
	    twitter.setAPIRootUrl(apiRoot);
	  }
	  return twitter;
	}
	
	// Asynchronously posts to twitter
	class PostToTwitter extends AsyncTask<String, Integer, String> {

	  // Called to initiate the background activity
	  @Override
		protected String doInBackground(String... statuses) {
	    try {
	      Twitter.Status status = getTwitter().updateStatus(statuses[0]);
	      return status.text;
	    } catch (TwitterException e) {
	      Log.e(TAG, e.toString());
	      e.printStackTrace();
	      return "Failed to post";
	    }
		}

    // Called when there's a status to be updated
    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      // Not used in this case
    }

    // Called once the background activity has completed
    @Override
		protected void onPostExecute(String result) {
      // "result" matches up to what doInBackground returns
      Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
    }
	}
	
  @Override
  public void onClick(View v) {
    String status = editText.getText().toString();
    new PostToTwitter().execute(status);
    // what we pass to execute gets passed to doInBackground method
    Log.d(TAG, "onClicked");
    // debug log
    // d = debug, e = error, w = warning, i = info, wtf = should never happen
  }

  @Override
  public void afterTextChanged(Editable statusText) {
    int count = 140 - statusText.length();
    textCount.setText(Integer.toString(count));
    textCount.setTextColor(Color.GREEN);
    if (count < 10)
      textCount.setTextColor(Color.YELLOW);
    if (count < 0)
      textCount.setTextColor(Color.RED);
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    // inflate the menu corresponding to "menu.xml" stored in /res/menu/
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.itemPrefs:
        // menu item with id "itemPrefs" was selected
        startActivity(new Intent(this, PrefsActivity.class));
        break;
    }
    return true;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
    // invalidate twitter object, because prefs may have changed.
    twitter = null;
  }
}
