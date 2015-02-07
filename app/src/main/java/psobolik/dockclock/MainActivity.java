package psobolik.dockclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends ActionBarActivity implements OnSetUiVisibilityListener, android.support.v7.app.ActionBar.OnMenuVisibilityListener {
    private BroadcastReceiver mBroadcastReceiver;
    private DockClockView mDockClockView;
    private boolean mIsMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mDockClockView = (DockClockView)this.findViewById(R.id.view);

        this.mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                    ((MainActivity)context).setIsPowerConnected(true);
                } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    ((MainActivity)context).setIsPowerConnected(false);
                }
            }
        };

        Intent intent = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        boolean plugged = intent != null && intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        this.setIsPowerConnected(plugged);
    }

    private void setIsPowerConnected(boolean isPowerConnected) {
        mDockClockView.setIsPowerConnected(isPowerConnected);
        if (isPowerConnected) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.getSupportActionBar().addOnMenuVisibilityListener(this);
    }

    @Override
    public void onStop() {
        this.getSupportActionBar().removeOnMenuVisibilityListener(this);
        this.unregisterReceiver(this.mBroadcastReceiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        boolean result;
        switch (item.getItemId()) {
            case R.id.about_menu_item:
                showAboutDialog();
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void showAboutDialog() {
        AboutDialogFragment aboutDialogFragment = AboutDialogFragment.newInstance();
        aboutDialogFragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, 0);
        aboutDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onSetUiVisibility(boolean visible) {
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            if (visible) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
        }
    }

    @Override
    public boolean canSetUiVisibility() {
        //Log.d("canSetUIVisibility", Boolean.toString(!this.mIsMenuOpen).toString());
        return !this.mIsMenuOpen;
    }

    @Override
    public void onMenuVisibilityChanged(boolean isVisible) {
        this.mIsMenuOpen = isVisible;
        this.mDockClockView.setNavVisibility(true);
    }
}
