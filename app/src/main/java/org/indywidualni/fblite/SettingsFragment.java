package org.indywidualni.fblite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class SettingsFragment extends PreferenceFragment {

    private static Context context;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // set context
        context = MyApplication.getContextOfApplication();

        // get shared preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // listener for changing preferences (works after the value change)
        myPrefListner = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // service intent (start, stop)
                final Intent intent = new Intent(context, NotificationsService.class);

                switch (key) {
                    case "notifications_activated":
                        if (prefs.getBoolean("notifications_activated", false))
                            context.startService(intent);
                        else
                            context.stopService(intent);
                        break;
                    case "interval_pref":
                        // restart service after time interval change
                        if (prefs.getBoolean("notifications_activated", false)) {
                            context.stopService(intent);
                            context.startService(intent);
                        }
                        break;
                }

            }
        };

        // register the listener above
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);

        // listener for clearing cache preference
        findPreference("clear_cache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v("SettingsFragment", "clearing cache...");
                // clear cache dirs
                deleteCache(getActivity().getApplicationContext());
                // restart the app (really ugly way of doing it but...)
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        });

        // listener for changing transparent_nav preference
        findPreference("transparent_nav").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v("SettingsFragment", "transparent_nav changed");
                relaunch();
                return true;
            }
        });

        // listener for changing hardware_acceleration preference
        findPreference("hardware_acceleration").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v("SettingsFragment", "hardware_acceleration changed");
                relaunch();
                return true;
            }
        });

        // listener for changing drawer_pos preference
        findPreference("drawer_pos").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.v("SettingsFragment", "drawer_pos changed");
                relaunch();
                return true;
            }
        });

        // listener for get_feed preference
        findPreference("get_feed").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v("SettingsFragment", "get_feed clicked");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.get_feed_link))));
                return true;
            }
        });

        // listener for notifications_settings preference
        findPreference("notifications_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v("SettingsFragment", "notifications_settings");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, 0)
                        .addToBackStack(null).replace(android.R.id.content,
                        new NotificationsSettingsFragment()).commit();
                return true;
            }
        });
    }

    // relaunch the app
    private void relaunch() {
        // notify user about relaunching the app
        Toast.makeText(getActivity(), getString(R.string.applying_changes), Toast.LENGTH_SHORT).show();
        // sending intent to onNewIntent() of MainActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("core_settings_changed", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // methods for clearing cache
    private static void deleteCache(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib") && !s.equals("shared_prefs")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}