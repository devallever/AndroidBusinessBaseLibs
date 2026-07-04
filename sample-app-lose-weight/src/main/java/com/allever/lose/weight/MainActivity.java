package com.allever.lose.weight;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.allever.lose.weight.base.MyContextWrapper;
import com.allever.lose.weight.util.Util;
import com.google.android.material.navigation.NavigationView;
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

import app.allever.android.lib.core.ext.ToastKt;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;


public class MainActivity extends SupportActivity implements NavigationView.OnNavigationItemSelectedListener, BaseMainFragment.OnFragmentOpenDrawerListener {

    private static final String TAG = "MainActivity";

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    private DataSource mDataSource = Repository.getInstance();


    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw_activity_main);

        EventBus.getDefault().register(this);

        //findvIEWBYID
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());  //load root Fragment
        }
        setNavigationView();

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
                final ISupportFragment topFragment = getTopFragment();
                if (id == R.id.plans) {
                    startFragment(0);
                } else if (id == R.id.report) {
                    startFragment(3);
                } else if (id == R.id.reminder) {
                    ReminderFragment reminderFragment = findFragment(ReminderFragment.class);
                    if (reminderFragment == null) {
                        popTo(HomeFragment.class, false, new Runnable() {
                            @Override
                            public void run() {
                                start(ReminderFragment.newInstance());
                            }
                        }, getFragmentAnimator().getPopExit());
                    } else {
                        popTo(ReminderFragment.class, false);
                    }
                } else if (id == R.id.setting) {
                    SettingsFragment settingsFragment = findFragment(SettingsFragment.class);
                    if (settingsFragment == null) {
                        popTo(HomeFragment.class, false, new Runnable() {
                            @Override
                            public void run() {
                                start(SettingsFragment.newInstance());
                            }
                        }, getFragmentAnimator().getPopExit());
                    } else {
                        popTo(SettingsFragment.class, false);
                    }
                } else if (id == R.id.reset_schedule) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.lw_reset_schedule)
                            .setPositiveButton(R.string.lw_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mDataSource.deleteAllSchedule();
                                    EventBus.getDefault().post(Constant.EVENT_REFRESH_VIEW);
                                }
                            })
                            .setNegativeButton(R.string.lw_cancel, new DialogInterface.OnClickListener() {
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
        Log.d(TAG, "run: ");
        HomeFragment fragment = findFragment(HomeFragment.class);
        Bundle newBundle = new Bundle();
        newBundle.putInt(Constant.EXTRA_MAIN_PAGE_INDEX, pageIndex);
        fragment.putNewBundle(newBundle);
        start(fragment, SupportFragment.SINGLETASK);
    }

    @Override
    public void onBackPressedSupport() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            ISupportFragment topFragment = getTopFragment();

            // 主页的Fragment
            if (topFragment instanceof BaseMainFragment) {
                navigationView.setCheckedItem(R.id.plans);
            }

            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                pop();
            } else {
                checkExit(null);
            }
        }
    }


    private long firstPressedBackTime = 0L;
    protected void checkExit(Runnable runnable) {
        if (System.currentTimeMillis() - firstPressedBackTime < 2000) {
            finish();
        } else {
            ToastKt.toast("common_click_again_to_exit");
            firstPressedBackTime = System.currentTimeMillis();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionFinish(String event){
        if (Constant.EVENT_START_HISTORY.equals(event)){
            start(new HistoryFragment());
        }
    }

}
