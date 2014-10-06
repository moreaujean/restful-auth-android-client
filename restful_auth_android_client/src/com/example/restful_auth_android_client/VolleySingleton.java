package com.example.restful_auth_android_client;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
	
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

	private VolleySingleton(Context context) {
	    mCtx = context;
	    mRequestQueue = getRequestQueue();
	    mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(LruBitmapCache.getCacheSize(mCtx)));
	}
	
	public static synchronized VolleySingleton getInstance(Context context) {
	    if (mInstance == null) {
	        mInstance = new VolleySingleton(context);
	    }
	    return mInstance;
	}
	
	public RequestQueue getRequestQueue() {
	    if (mRequestQueue == null) {
	        mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
	    }
	    return mRequestQueue;
	}
	
	public <T> void addToRequestQueue(Request<T> req) {
	    getRequestQueue().add(req);
	}
	
	public ImageLoader getImageLoader() {
	    return mImageLoader;
	}
}