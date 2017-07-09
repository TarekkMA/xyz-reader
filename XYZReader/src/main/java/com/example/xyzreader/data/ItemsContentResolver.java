package com.example.xyzreader.data;

import android.content.ContentResolver;
import android.content.Context;
import com.squareup.sqlbrite2.BriteContentResolver;
import com.squareup.sqlbrite2.QueryObservable;
import com.squareup.sqlbrite2.SqlBrite;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by TarekLMA on 7/9/17.
 * tarekkma@gmail.com
 */

public class ItemsContentResolver {

  private static final SqlBrite sqlBrite = new SqlBrite.Builder().build();

  public static QueryObservable getAll(ContentResolver resolver) {
    return sqlBrite.wrapContentProvider(resolver, Schedulers.io())
        .createQuery(ItemsContract.Items.buildDirUri(), Query.PROJECTION, null, null,
            ItemsContract.Items.DEFAULT_SORT, false);
  }

  public static QueryObservable getById(ContentResolver resolver, long id) {

    return sqlBrite.wrapContentProvider(resolver, Schedulers.io())
        .createQuery(ItemsContract.Items.buildItemUri(id), Query.PROJECTION, null, null,
            ItemsContract.Items.DEFAULT_SORT, false);
  }

  public interface Query {
    String[] PROJECTION = {
        ItemsContract.Items._ID, ItemsContract.Items.TITLE, ItemsContract.Items.PUBLISHED_DATE,
        ItemsContract.Items.AUTHOR, ItemsContract.Items.THUMB_URL, ItemsContract.Items.PHOTO_URL,
        ItemsContract.Items.ASPECT_RATIO, ItemsContract.Items.BODY,
    };

    int _ID = 0;
    int TITLE = 1;
    int PUBLISHED_DATE = 2;
    int AUTHOR = 3;
    int THUMB_URL = 4;
    int PHOTO_URL = 5;
    int ASPECT_RATIO = 6;
    int BODY = 7;
  }
}
