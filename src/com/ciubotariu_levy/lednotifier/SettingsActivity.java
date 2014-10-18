package com.ciubotariu_levy.lednotifier;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Telephony.Sms;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
		setupNewApiPhoneSizePreferences();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && getActionBar() != null){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupNewApiPhoneSizePreferences() {
		if (!isXLargeTablet(this) && Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
			getFragmentManager().beginTransaction().replace(android.R.id.content, new AllPreferencesFragment()).commit();
		}
	}

	protected void onResume (){
		super.onResume();
		Preference smsAppPreference = findPreference(SmsAppChooserDialog.KEY_SMS_APP_PACKAGE);
		if (smsAppPreference != null){
			setupSMSAppPreference(smsAppPreference);
		}
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		addPreferencesFromResource(R.xml.pref_general);

		PreferenceCategory notifHeader = new PreferenceCategory(this);
		notifHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(notifHeader);
		addPreferencesFromResource(R.xml.pref_notifs);
		bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));

		PreferenceCategory otherHeader = new PreferenceCategory(this);
		otherHeader.setTitle(R.string.pref_header_other);
		getPreferenceScreen().addPreference(otherHeader);
		addPreferencesFromResource(R.xml.pref_other);
	}

	@TargetApi(19)
	private static void setupSMSAppPreference (Preference smsPreference){
		String summary = "Set the SMS app in use";
		PackageManager packageManager = smsPreference.getContext().getPackageManager();
		String smsAppPackageName =  Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? smsPreference.getSharedPreferences().getString(smsPreference.getKey(), null)
				: Sms.getDefaultSmsPackage(smsPreference.getContext());
		if (smsAppPackageName != null){
			try {
				CharSequence smsAppLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(smsAppPackageName, 0));
				summary = "Launching " + smsAppLabel + " if Contact Notifier notification is tapped";
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		smsPreference.setSummary(summary);
		smsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				preference.getContext().startActivity(new Intent (preference.getContext(),SMSAppChooserContainer.class));
				return false;
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			smsPreference.setEnabled(false);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this) && isXLargeTablet(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
				.setSummary(index >= 0 ? listPreference.getEntries()[index]
						: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
		.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
								""));
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = NavUtils.getParentActivityIntent(this);    
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity (i);
			return true;
		default: return super.onOptionsItemSelected(item);        
		}
	}
	
	@Override
	public boolean isValidFragment(String fragmentName){
		return GeneralPreferenceFragment.class.getName().equals(fragmentName)
				|| NotifPreferenceFragment.class.getName().equals(fragmentName)
				|| OtherPreferenceFragment.class.getName().equals(fragmentName)
				|| AllPreferencesFragment.class.getName().equals(fragmentName);
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
		}

		@Override
		public void onResume (){
			super.onResume();
			setupSMSAppPreference(findPreference(SmsAppChooserDialog.KEY_SMS_APP_PACKAGE));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotifPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notifs);
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class OtherPreferenceFragment extends	PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_other);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AllPreferencesFragment extends PreferenceFragment{
		@Override
		public void onCreate (Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			PreferenceCategory notifHeader = new PreferenceCategory(getActivity());
			notifHeader.setTitle(R.string.pref_header_notifications);
			getPreferenceScreen().addPreference(notifHeader);
			addPreferencesFromResource(R.xml.pref_notifs);
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));

			PreferenceCategory otherHeader = new PreferenceCategory(getActivity());
			otherHeader.setTitle(R.string.pref_header_other);
			getPreferenceScreen().addPreference(otherHeader);
			addPreferencesFromResource(R.xml.pref_other);
		}

		@Override
		public void onResume (){
			super.onResume();
			setupSMSAppPreference(findPreference(SmsAppChooserDialog.KEY_SMS_APP_PACKAGE));
		}
	}
}
