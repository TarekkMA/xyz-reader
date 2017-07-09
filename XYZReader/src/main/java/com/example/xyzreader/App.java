package com.example.xyzreader;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import okhttp3.OkHttpClient;

/**
 * Created by TarekLMA on 7/9/17.
 * tarekkma@gmail.com
 */

public class App extends Application {
  @Override public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }
}
