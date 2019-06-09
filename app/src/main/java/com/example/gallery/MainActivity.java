package com.example.gallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gallery.Adapters.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    public boolean networkConnected;
    private int currentFragment;
    private Toolbar mainToolbar;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ViewPager viewPager;
    MenuItem prevMenuItem;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
//                    fragmentTransaction.replace(R.id.frame,searchFragment);
//                    fragmentTransaction.commit();
                    viewPager.setCurrentItem(1);
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mainToolbar=(Toolbar)findViewById(R.id.homeToolbar);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Gallery");
        homeFragment=new HomeFragment();
        searchFragment=new SearchFragment();
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.frame,homeFragment);
        //agmentTransaction.commit();
        setupViewPager();
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (prevMenuItem != null)

                    prevMenuItem.setChecked(false);

                else

                    navView.getMenu().getItem(0).setChecked(false);



                navView.getMenu().getItem(i).setChecked(true);

                prevMenuItem = navView.getMenu().getItem(i);

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void setupViewPager() {

        ViewPagerAdapter pagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(HomeFragment.getInstance());
        pagerAdapter.addFragment(SearchFragment.getInstance());
        viewPager.setAdapter(pagerAdapter);
    }

}
