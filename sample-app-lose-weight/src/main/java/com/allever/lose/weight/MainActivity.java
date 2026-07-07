package com.allever.lose.weight;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.allever.lose.weight.base.MyContextWrapper;
import com.allever.lose.weight.util.Util;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import com.allever.lose.weight.data.DataSource;
import com.allever.lose.weight.data.Repository;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.bean.MenuEvent;
import com.allever.lose.weight.ui.HistoryFragment;
import com.allever.lose.weight.ui.HomeFragment;
import com.allever.lose.weight.ui.ReminderFragment;
import com.allever.lose.weight.ui.SettingsFragment;
import com.allever.lose.weight.base.BaseMainFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import app.allever.android.lib.common.FragmentActivity;
import app.allever.android.lib.core.base.AbstractActivity;
import app.allever.android.lib.core.helper.FragmentHelper;


public class MainActivity extends AbstractActivity implements NavigationView.OnNavigationItemSelectedListener, BaseMainFragment.OnFragmentOpenDrawerListener {

    private static final String TAG = "MainActivity";

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    private final DataSource mDataSource = Repository.getInstance();


    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw_activity_main);

        EventBus.getDefault().register(this);

        //findvIEWBYID
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        FragmentHelper.INSTANCE.addToContainer(getSupportFragmentManager(), HomeFragment.newInstance(), R.id.fl_container);

        setNavigationView();


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setNavigationView() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.lw_open, R.string.lw_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public void onOpenDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale newLocale;
        if (Util.isChinese()) {
            newLocale = Locale.CHINESE;
        } else {
            newLocale = Locale.ENGLISH;
        }

        super.attachBaseContext(MyContextWrapper.wrap(newBase, newLocale));
    }


    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);
        item.setCheckable(true);
        item.setChecked(true);
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                int id = item.getItemId();
                if (id == R.id.plans) {
                    startFragment(0);
                } else if (id == R.id.report) {
                    startFragment(3);
                } else if (id == R.id.reminder) {
                    MyApplication.startFragment(ReminderFragment.class, null);

                } else if (id == R.id.setting) {
                    MyApplication.startFragment(SettingsFragment.class, null);
                } else if (id == R.id.reset_schedule) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setMessage(R.string.lw_reset_schedule).setPositiveButton(R.string.lw_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDataSource.deleteAllSchedule();
                            EventBus.getDefault().post(Constant.EVENT_REFRESH_VIEW);
                        }
                    }).setNegativeButton(R.string.lw_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        }, 300);
        return true;
    }

    private void startFragment(int pageIndex) {
        EventBus.getDefault().post(new MenuEvent(Constant.EVENT_MENU_START_HOME_PAGE, pageIndex));
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionFinish(String event) {
        if (Constant.EVENT_START_HISTORY.equals(event)) {
            FragmentActivity.Companion.start("", false, true, null, HistoryFragment.class);
        }
    }

}
