package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import android.graphics.drawable.Drawable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.text.Spanned;
import android.widget.ScrollView;
import com.example.xyzreader.data.ItemsContentResolver;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.sqlbrite2.SqlBrite;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {
  private static final String TAG = "ArticleDetailFragment";

  public static final String ARG_ITEM_ID = "item_id";

  private Cursor mCursor;
  private long mItemId;
  private View mRootView;
  TextView bodyView;

  private int mMutedColor = 0xFF333333;
  CompositeDisposable compositeDisposable = new CompositeDisposable();

  //private View mPhotoContainerView;
  private ImageView mPhotoView;
  private boolean mIsCard = false;

  private ContentLoadingProgressBar loadingProgressBar;

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
  // Use default locale format
  private SimpleDateFormat outputFormat = new SimpleDateFormat();
  // Most time functions can only handle 1902 - 2037
  private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ArticleDetailFragment() {
  }

  public static ArticleDetailFragment newInstance(long itemId) {
    Bundle arguments = new Bundle();
    arguments.putLong(ARG_ITEM_ID, itemId);
    ArticleDetailFragment fragment = new ArticleDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItemId = getArguments().getLong(ARG_ITEM_ID);
    }

    mIsCard = getResources().getBoolean(R.bool.detail_is_card);

    setHasOptionsMenu(true);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    loadData();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

    loadingProgressBar = (ContentLoadingProgressBar) mRootView.findViewById(R.id.loading);

    mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
    bodyView = (TextView) mRootView.findViewById(R.id.article_body);

    mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
            .setType("text/plain")
            .setText("Some sample text")
            .getIntent(), getString(R.string.action_share)));
      }
    });

    bindViews();
    return mRootView;
  }

  private Date parsePublishedDate() {
    try {
      String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
      return dateFormat.parse(date);
    } catch (ParseException ex) {
      Log.e(TAG, ex.getMessage());
      Log.i(TAG, "passing today's date");
      return new Date();
    }
  }

  private void bindViews() {
    if (mRootView == null) {
      return;
    }

    TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
    TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
    bylineView.setMovementMethod(new LinkMovementMethod());

    //bodyView.setTypeface(
    //    Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

    if (mCursor != null) {
      mRootView.setAlpha(0);
      mRootView.setVisibility(View.VISIBLE);
      mRootView.animate().alpha(1);
      String title = mCursor.getString(ArticleLoader.Query.TITLE);
      titleView.setText(title);
      Date publishedDate = parsePublishedDate();
      if (!publishedDate.before(START_OF_EPOCH.getTime())) {
        bylineView.setText(Html.fromHtml(
            DateUtils.getRelativeTimeSpanString(publishedDate.getTime(), System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString()
                + " by <font color='#ffffff'>"
                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                + "</font>"));
      } else {
        // If date is before 1902, just show the string
        bylineView.setText(fromHtml(
            outputFormat.format(publishedDate) + " by <font color='#ffffff'>" + mCursor.getString(
                ArticleLoader.Query.AUTHOR) + "</font>"));
      }
      bodyView.setText(
          fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

      ImageLoaderHelper.getInstance(getActivity())
          .getImageLoader()
          .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
            @Override public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
              Bitmap bitmap = imageContainer.getBitmap();
              if (bitmap != null) {
                Palette p = Palette.generate(bitmap, 12);
                mMutedColor = p.getDarkMutedColor(0xFF333333);
                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                mRootView.findViewById(R.id.meta_bar).setBackgroundColor(mMutedColor);
              }
            }

            @Override public void onErrorResponse(VolleyError volleyError) {

            }
          });
    } else {
      mRootView.setVisibility(View.GONE);
      titleView.setText("N/A");
      bylineView.setText("N/A");
      bodyView.setText("N/A");
    }
  }

  @Override public void onPause() {
    super.onPause();
    bodyView.setText("");
    mCursor.close();
    compositeDisposable.clear();
  }

  public static Spanned fromHtml(String html) {
    Spanned result;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
    } else {
      result = Html.fromHtml(html);
    }
    return result;
  }

  private void loadData() {
    compositeDisposable.add(ItemsContentResolver.getById(getContext().getContentResolver(), mItemId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<SqlBrite.Query>() {
          @Override public void onNext(@NonNull SqlBrite.Query query) {
            mCursor = query.run();
            if (mCursor != null) {
              mCursor.moveToFirst();
              bindViews();
            }
          }

          @Override public void onError(@NonNull Throwable e) {
            Log.e(TAG, "onError: ", e);
            final Snackbar s = Snackbar.make(mRootView, "Error loading data",
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
}
