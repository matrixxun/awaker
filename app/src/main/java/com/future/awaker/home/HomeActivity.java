package com.future.awaker.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.future.awaker.R;
import com.future.awaker.base.ViewModelHolder;
import com.future.awaker.base.listener.DebouncingOnClickListener;
import com.future.awaker.data.Video;
import com.future.awaker.data.source.NewRepository;
import com.future.awaker.databinding.ActivityHomeBinding;
import com.future.awaker.news.NewViewModel;
import com.future.awaker.util.AnimatorUtils;
import com.future.awaker.util.ResUtils;
import com.future.awaker.video.VideoViewModel;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright ©2017 by Teambition
 */

public class HomeActivity extends AppCompatActivity {

    public static final String MAIN_VIEW_MODEL_TAG = "MAIN_VIEW_MODEL_TAG";
    public static final String VIDEO_VIEW_MODEL_TAG = "VIDEO_VIEW_MODEL_TAG";

    private ActivityHomeBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private MaterialSheetFab materialSheetFab;
    private int statusBarColor;

    private HomeClickListener homeClickListener = new HomeClickListener();
    private HomeAdapter homeAdapter;

    public static void launch(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        setTitle(R.string.app_name);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.app_name,
                R.string.app_name);
        binding.drawerLayout.setDrawerListener(drawerToggle);

        setupFab();
        setupTabs();
    }

    @Override
    protected void onDestroy() {
        NewRepository.destroyInstance();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleDrawer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleDrawer() {
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setupTabs() {
        List<String> titles = Arrays.asList(ResUtils.getString(R.string.news),
                ResUtils.getString(R.string.video));

        homeAdapter = new HomeAdapter(getSupportFragmentManager(), titles);
        homeAdapter.setNewViewModel(findOrCreateNewViewModel());
        homeAdapter.setVideoViewModel(findOrCreateVideoViewModel());
        binding.viewpager.setAdapter(homeAdapter);

        updateFab(binding.viewpager.getCurrentItem());
        setupColor(binding.tabs.getSelectedTabPosition());

        binding.tabs.setupWithViewPager(binding.viewpager);
        binding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupColor(int position) {
        binding.toolbar.post(() -> {
            int centerX = 0;
            int centerY = binding.appbar.getMeasuredHeight();
            switch (position) {
                case 0:
                    centerX = binding.toolbar.getMeasuredWidth() / 4;
                    binding.coordinator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    binding.appbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case 1:
                    centerX = binding.toolbar.getMeasuredWidth() / 4 * 3;
                    binding.coordinator.setBackgroundColor(getResources().getColor(R.color.themePrimary));
                    binding.appbar.setBackgroundColor(getResources().getColor(R.color.themePrimary));
                    break;
            }

            Animator animator = AnimatorUtils.createRevealAnimator(binding.appbar, centerX, centerY,
                    false, new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            //updateThemeColor(position);
                        }
                    });
            animator.start();
        });
    }

    public void updateThemeColor(int position) {
        switch (position) {
            case 0:
                setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                binding.tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                break;
            case 1:
                setStatusBarColor(getResources().getColor(R.color.themePrimaryDark));
                binding.toolbar.setBackgroundColor(getResources().getColor(R.color.themePrimary));
                binding.tabs.setBackgroundColor(getResources().getColor(R.color.themePrimary));

                break;
        }
    }

    private void updateFab(int position) {
        switch (position) {
            case 0:
                materialSheetFab.hideSheetThenFab();
                break;
            case 1:
                materialSheetFab.showFab();
                break;
        }
    }

    private void setupFab() {
        int sheetColor = getResources().getColor(R.color.white);
        int fabColor = getResources().getColor(R.color.colorAccent);

        materialSheetFab = new MaterialSheetFab<>(binding.fab, binding.fabSheet, binding.overlay,
                sheetColor, fabColor);
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                super.onShowSheet();
                statusBarColor = getStatusBarColor();
                setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark2));
            }

            @Override
            public void onHideSheet() {
                super.onHideSheet();
                setStatusBarColor(statusBarColor);
            }
        });

        binding.fabSheetItemUfo.setOnClickListener(homeClickListener);
        binding.fabSheetItemTheory.setOnClickListener(homeClickListener);
        binding.fabSheetItemSpirit.setOnClickListener(homeClickListener);
        binding.fabSheetItemFree.setOnClickListener(homeClickListener);
        binding.fabSheetItemNormal.setOnClickListener(homeClickListener);
    }

    private int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getWindow().getStatusBarColor();
        }
        return 0;
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    private NewViewModel findOrCreateNewViewModel() {
        @SuppressWarnings("unchecked")
        ViewModelHolder<NewViewModel> retainedViewModel = (ViewModelHolder<NewViewModel>) getSupportFragmentManager()
                .findFragmentByTag(MAIN_VIEW_MODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewModel() != null) {
            return retainedViewModel.getViewModel();
        } else {
            NewViewModel newViewModel = new NewViewModel(NewRepository.get());
            ViewModelHolder viewModelHolder = ViewModelHolder.createContainer(newViewModel);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(viewModelHolder, MAIN_VIEW_MODEL_TAG);
            transaction.commit();
            return newViewModel;
        }
    }

    private VideoViewModel findOrCreateVideoViewModel() {
        @SuppressWarnings("unchecked")
        ViewModelHolder<VideoViewModel> retainedViewModel = (ViewModelHolder<VideoViewModel>) getSupportFragmentManager()
                .findFragmentByTag(VIDEO_VIEW_MODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewModel() != null) {
            return retainedViewModel.getViewModel();
        } else {
            VideoViewModel viewModel = new VideoViewModel(NewRepository.get());
            ViewModelHolder viewModelHolder = ViewModelHolder.createContainer(viewModel);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(viewModelHolder, VIDEO_VIEW_MODEL_TAG);
            transaction.commit();
            return viewModel;
        }
    }


    private class HomeClickListener extends DebouncingOnClickListener {

        @Override
        public void doClick(View v) {
            switch (v.getId()) {
                case R.id.fab_sheet_item_ufo:
                    homeAdapter.setCat(Video.UFO);
                    materialSheetFab.hideSheet();
                    break;
                case R.id.fab_sheet_item_theory:
                    homeAdapter.setCat(Video.THEORY);
                    materialSheetFab.hideSheet();
                    break;
                case R.id.fab_sheet_item_spirit:
                    homeAdapter.setCat(Video.SPIRIT);
                    materialSheetFab.hideSheet();
                    break;
                case R.id.fab_sheet_item_free:
                    homeAdapter.setCat(Video.FREE);
                    materialSheetFab.hideSheet();
                    break;
                case R.id.fab_sheet_item_normal:
                    homeAdapter.setCat(Video.NORMAL);
                    materialSheetFab.hideSheet();
                    break;
            }
        }
    }
}
