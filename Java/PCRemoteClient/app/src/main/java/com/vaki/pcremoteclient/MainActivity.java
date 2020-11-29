package com.vaki.pcremoteclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    TcpClient mTcpClient;
    public LinearLayout statusbar;
    public TextView statusbar_text;
    public Timer timerRef;
    private FragmentManager fm;
    public int currentFragment;
    private SharedPreferences SP;
    NavigationView navigationView;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        statusbar = (LinearLayout) findViewById(R.id.statusbar);
        statusbar_text = (TextView)findViewById(R.id.statusbar_text) ;

        currentFragment = 1;

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        fm= getSupportFragmentManager();
        if(Integer.parseInt(SP.getString("startup_activity_selected","1"))==0)
            changeFragment(SP.getInt("last_used_activity",1));
        else
            changeFragment(Integer.parseInt(SP.getString("startup_activity_selected","1")));
        AsyncTask ConnectTask = new ConnectTask(this);
        //new ConnectTask().execute("");
    }
    public void changeStatusBar(final int status)
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch(status)
                {
                    case 1:
                        statusbar.setVisibility(View.VISIBLE);
                        statusbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.connected, null));
                        statusbar_text.setText("Connected");
                        timerRef = new Timer();
                        timerRef.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Message msg = handler.obtainMessage();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        }, 3000);
                        break;
                    case 2:
                        statusbar.setVisibility(View.VISIBLE);
                        statusbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.connecting, null));
                        statusbar_text.setText("Connection Lost, Reconnecting");
                        timerRef.cancel();
                        break;
                }
            }
        });

    }
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1)
            {
                try {
                    statusbar.setVisibility(View.GONE);
                }
                catch (Exception e){}
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
   /* @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("dispatchKeyEvent", event.get()+":" +event.getUnicodeChar());

        return super.dispatchKeyEvent(event);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        JSONObject obj = new JSONObject();
        String key;
        if (event.getKeyCode() == 67)
            key = "bs";
        else if (event.getKeyCode() == 66)
            key = "en";
        else if(event.getKeyCode() == 59)
            return super.onKeyDown(keyCode, event);
        else
            key = String.valueOf(event.getUnicodeChar());
        try {
            obj.put("a", "k");
            obj.put("k", key);
            mTcpClient.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("onKeyDown", event.getKeyCode()+":" +event.getUnicodeChar());
        return super.onKeyDown(keyCode, event);
    }
    public void closeSoftKeyb()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.drawer_layout).getWindowToken(),0);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
       closeSoftKeyb();
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                changeFragment(1);
                break;
            case R.id.nav_input:
                changeFragment(2);
                break;
            case R.id.nav_volume:
                changeFragment(3);
                break;
            case R.id.nav_power:
                changeFragment(4);
                break;
            case R.id.nav_settings:
                changeFragment(5);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void changeFragment(int id)
    {
        SharedPreferences.Editor editor = SP.edit();
        switch (id) {
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                currentFragment=1;
                navigationView.setCheckedItem(R.id.nav_home);
                editor.putInt("last_used_activity",1 );
                editor.commit();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InputFragment()).commit();
                currentFragment=2;
                navigationView.setCheckedItem(R.id.nav_input);
                editor.putInt("last_used_activity",2 );
                editor.commit();
                break;
            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolumeControlFragment()).commit();
                currentFragment=3;
                navigationView.setCheckedItem(R.id.nav_volume);
                editor.putInt("last_used_activity",3 );
                editor.commit();
                break;
            case 4:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PowerFragment()).commit();
                currentFragment=4;
                navigationView.setCheckedItem(R.id.nav_power);
                editor.putInt("last_used_activity",4 );
                editor.commit();
                break;
            case 5:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                currentFragment=5;
                navigationView.setCheckedItem(R.id.nav_settings);
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                currentFragment=1;
                navigationView.setCheckedItem(R.id.nav_home);
                editor.putInt("last_used_activity",1 );
                editor.commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        MainActivity mainActivity;
        public ConnectTask(MainActivity activity)
        {
            mainActivity =activity;
            this.execute("");
        }
        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            },mainActivity);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d("TCP Client", "response " + values[0]);
            if(currentFragment==1) {
                HomeFragment fragm = (HomeFragment) fm.findFragmentById(R.id.fragment_container);
                Message msg = fragm.handler.obtainMessage();
                msg.what = 1;
                msg.obj = values[0];
                fragm.handler.sendMessage(msg);
            }
        }
    }
}

