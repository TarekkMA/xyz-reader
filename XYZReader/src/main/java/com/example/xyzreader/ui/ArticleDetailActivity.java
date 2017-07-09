package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContentResolver;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.sqlbrite2.SqlBrite;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity {

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  private Cursor mCursor;
  private long mStartId;

  private long mSelectedItemId;

  private ViewPager mPager;
  private MyPagerAdapter mPagerAdapter;
  private CoordinatorLayout rootLayout;
  private Toolbar mToolbar;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_article_detail);

    mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
    mPager = (ViewPager) findViewById(R.id.pager);
    rootLayout = (CoordinatorLayout) findViewById(R.id.root);
    mPager.setAdapter(mPagerAdapter);

    mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
      }

      @Override public void onPageSelected(int position) {
        if (mCursor != null) {
          mCursor.moveToPosition(position);
          setTitle(mCursor.getString(ItemsContentResolver.Query.TITLE));
          mSelectedItemId = mCursor.getLong(ItemsContentResolver.Query._ID);
        }
      }
    });

    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      if (getIntent() != null && getIntent().getData() != null) {
        mStartId = ItemsContract.Items.getItemId(getIntent().getData());
        mSelectedItemId = mStartId;
      }
    }
    loadData();
  }

  @Override protected void onPause() {
    super.onPause();
    compositeDisposable.clear();
    mCursor.close();
  }

  private void loadData() {
    compositeDisposable.add(ItemsContentResolver.getAll(getContentResolver())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<SqlBrite.Query>() {
          @Override public void onNext(@NonNull SqlBrite.Query query) {
            mCursor = query.run();
            mPager.setAdapter(mPagerAdapter);

            if (mStartId > 0) {
              while (mCursor.moveToNext()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                  final int position = mCursor.getPosition();
                  mPager.postDelayed(new Runnable() {
                    @Override public void run() {
                      mPager.setCurrentItem(position,false);
                    }
                  }, 100);
                  break;
                }
              }
              mStartId = 0;
            }
          }

          @Override public void onError(@NonNull Throwable e) {
            final Snackbar s = Snackbar.make(rootLayout, "Error loading data",
                BaseTransientBottomBar.LENGTH_INDEFINITE);
            s.setAction(R.string.retry_error, new View.OnClickListener() {
              @Override public void onClick(View v) {
                loadData();
                s.dismiss();
              }
            });
            s.setActionTextColor(Color.RED);
            s.show();
          }

          @Override public void onComplete() {

          }
        }));
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int position) {
      mCursor.moveToPosition(position);
      return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
    }

    @Override public int getCount() {
      return (mCursor != null) ? mCursor.getCount() : 0;
    }
  }
}
