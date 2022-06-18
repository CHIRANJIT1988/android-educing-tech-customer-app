package educing.tech.customer.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

import educing.tech.customer.model.User;


public class SessionManager 
{
	
	SharedPreferences pref; // Shared Preferences
	
	Editor editor; // Editor for Shared preferences
	
	Context _context; // Context
	
	int PRIVATE_MODE = 0; // Shared pref mode
	
	
	private static final String PREF_NAME = "EducingTechPref"; // Sharedpref file name
	
	private static final String IS_LOGIN = "IsLoggedIn"; // All Shared Preferences Keys
	
	public static final String KEY_USER_ID = "user_id"; // User id (make variable public to access from outside)
	public static final String KEY_USER_NAME = "user_name"; // User id (make variable public to access from outside)
	public static final String KEY_PHONE = "phone"; // Password (make variable public to access from outside)
	
	
	@SuppressLint("CommitPrefEdits") 
	public SessionManager(Context context) // Constructor
	{
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	
	/**
	 * Create login session
	 * */
	
	public void createLoginSession(User user)
	{
		
		editor.putBoolean(IS_LOGIN, true); // Storing login value as TRUE

		editor.putInt(KEY_USER_ID, user.user_id); // Storing user id in pref
		editor.putString(KEY_USER_NAME, user.name); // Storing user id in pref
		editor.putString(KEY_PHONE, user.phoneNo); // Storing password in pref
		
		editor.commit(); // commit changes
	}	
	
	
	/**
	 * Check login method wil check user login status
	 * If false it will redirect user to login page
	 * Else won't do anything
	 * */
	
	public boolean checkLogin()
	{
		
		if(!this.isLoggedIn()) // Check login status
		{
			
			/*Intent i = new Intent(_context, MainActivity.class); // user is not logged in redirect him to Login Activity
			
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Closing all the Activities
			
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add new Flag to start new Activity
			
			_context.startActivity(i); // Staring Login Activity*/
			
			return false;
			
		}
		
		return true;
	}
	
	
	
	/**
	 * Get stored session data
	 * */
	
	public HashMap<String, String> getUserDetails()
	{
		
		HashMap<String, String> user = new HashMap<>();

		user.put(KEY_USER_ID, String.valueOf(pref.getInt(KEY_USER_ID, 0))); // user id
		user.put(KEY_USER_NAME, pref.getString(KEY_USER_NAME, null)); // user name
		user.put(KEY_PHONE, pref.getString(KEY_PHONE, null)); // phone no
		
		return user; // return user
	}


	public void editProfile(String user_name) // Get Login State
	{
		editor.putString(KEY_USER_NAME, user_name);
		editor.commit(); // commit changes
	}


	public int getUserId() // Get Login State
	{
		return pref.getInt(KEY_USER_ID, 0);
	}


	/**
	 * Clear session details
	 * */
	
	public void logoutUser()
	{
		
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
		
		
		// After logout redirect user to Loing Activity
		/*Intent i = new Intent(_context, MainActivity.class);

		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// Staring Login Activity
		_context.startActivity(i);*/
	
	}
	
	
	/**
	 * Quick check for login
	 * ***/
	
	public boolean isLoggedIn() // Get Login State
	{
		return pref.getBoolean(IS_LOGIN, false);
	}
}