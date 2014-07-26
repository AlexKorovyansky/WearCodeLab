package org.gdgomsk.codelabs.wear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends Activity {

    private Runnable counterRunnable = new  Runnable() {
        @Override
        public void run() {
            if (!Thread.currentThread().isInterrupted()) {
                updateCounter();
                handler.postDelayed(counterRunnable, 950 /*almost one second*/);
            }
        }
    };

    private TextView mainText;
    private Handler handler;
    private BroadcastReceiver timeUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mainText = (TextView) stub.findViewById(R.id.main_text);
                updateCounter();
                runCounterUpdates(true);
            }
        });
        handler = new Handler(Looper.myLooper());
        initReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runCounterUpdates(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        runCounterUpdates(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Traditionally this is done in the onPause.
        // But since we are going to still get time updates
        // when paused, we can unregister the receiver
        this.unregisterReceiver(timeUpdateReceiver);
    }

    private void initReceivers() {
        // create the intent filter for the time tick action
        IntentFilter timeTickFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        IntentFilter timeChangedFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        IntentFilter timeZoneChangedFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);

        // create the receiver
        timeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    updateCounter();
                }
            }
        };
        this.registerReceiver(timeUpdateReceiver, timeTickFilter);
        this.registerReceiver(timeUpdateReceiver, timeChangedFilter);
        this.registerReceiver(timeUpdateReceiver, timeZoneChangedFilter);
    }

    private void runCounterUpdates(boolean run) {
        // make sure that there is not a task running
        // before starting a new task
        handler.removeCallbacksAndMessages(null);
        if (run) {
            handler.post(counterRunnable);
        }
    }

    private void updateCounter() {
        if (mainText != null) {
            mainText.setText(String.format("%d s", calculateSecondBeforeMidnight()));
        }
    }

    private long calculateSecondBeforeMidnight() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (c.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }
}
