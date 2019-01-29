package com.kong.oppmarket.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class MarketAccessibilityService extends AccessibilityService {
    private String TAG = this.getClass().getSimpleName();
    public static final String EVENT_CHANGE_ACTION = "com.zt.accessibility.action.EVENT_CHANGE";
    public static final String EXTRA_TYPE = "even_type";
    public static final String EXTRA_APPNAME = "even_appname";
    public static final String EXTRA_MARKET = "even_market";
    public static final int TYPE_INVALID = Integer.MIN_VALUE;
    public static final int TYPE_INTENT_DOWN = 1001;
    public static final int TYPE_INTENT_SEARCH_DOWN = 1002;
    public static final int TYPE_INTENT_INSTALL = 2001;
    private static final String INSTALL_PACKAGENAME = "com.android.packageinstaller";
    public static final String OPPOMARKET_PACKAGENAME = "com.oppo.market";
    private Context mContext;
    private volatile int mEventType = TYPE_INVALID;
    private String mInstallAppName;
    private String mMarketPackageName;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mContext = getApplicationContext();
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(EVENT_CHANGE_ACTION));
        mEventType = TYPE_INVALID;
        mMarketPackageName = null;
        mInstallAppName = null;
        Log.d(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (TYPE_INVALID != mEventType) {
            if (null == event)
                return;
            Log.d(TAG, event.toString());
            switch (mEventType) {
                case TYPE_INTENT_DOWN:
                    if (TextUtils.equals(event.getPackageName(), OPPOMARKET_PACKAGENAME)) {
                        doOppoMarketIntentDown(event);
                    }
                    break;
                case TYPE_INTENT_SEARCH_DOWN:
                    //KeyEvent.KEYCODE_BACK
                    break;
                case TYPE_INTENT_INSTALL:
                    if (TextUtils.equals(event.getPackageName(), INSTALL_PACKAGENAME)) {
                        doInstall(event);
                    }
                    break;
            }
        }
    }

    //com.oppo.market:id/header_app_name
    //com.oppo.market:id/button_download
    private void doOppoMarketIntentDown(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (null == rootNode) return;
            List<AccessibilityNodeInfo> appnames = rootNode.findAccessibilityNodeInfosByViewId("com.oppo.market:id/header_app_name");
            if (null != appnames && !appnames.isEmpty()) {
                AccessibilityNodeInfo appnameNode = appnames.get(0);
                if (TextUtils.equals(appnameNode.getClassName(), "android.widget.TextView")
                        && TextUtils.equals(mInstallAppName, appnameNode.getText())) {
                    List<AccessibilityNodeInfo> downloadNodes = rootNode.findAccessibilityNodeInfosByViewId("com.oppo.market:id/button_download");
                    if (null != downloadNodes && !downloadNodes.isEmpty()) {
                        AccessibilityNodeInfo downloadNode = downloadNodes.get(0);
                        if (TextUtils.equals(downloadNode.getContentDescription(), "打开")) {
                            Log.d(TAG, "已经安装，无需要再安装");
                            Toast.makeText(this.getApplicationContext(), "已经安装，无需要再安装", Toast.LENGTH_SHORT).show();
                            mEventType = TYPE_INVALID;
                            mMarketPackageName = null;
                            mInstallAppName = null;
                        } else {
                            downloadNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.d(TAG, "开始 " + downloadNode.getContentDescription());
                            Toast.makeText(this.getApplicationContext(), "开始" + downloadNode.getContentDescription() + mInstallAppName, Toast.LENGTH_SHORT).show();
                            mEventType = TYPE_INTENT_INSTALL;
                        }
                    }
                }
            }
        }
    }

    private void doInstall(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (null == rootNode) return;
            List<AccessibilityNodeInfo> appnames = rootNode.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/app_name");
            if (null != appnames && !appnames.isEmpty()) {
                AccessibilityNodeInfo appnameNode = appnames.get(0);
                if (TextUtils.equals(appnameNode.getClassName(), "android.widget.TextView")
                        && TextUtils.equals(mInstallAppName, appnameNode.getText())) {
                    List<AccessibilityNodeInfo> okNodes = rootNode.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/ok_button");
                    if (null != okNodes && !okNodes.isEmpty()) {
                        AccessibilityNodeInfo okNode = okNodes.get(0);
                        okNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d(TAG, "ACTION_CLICK " + okNode.getText());
                    }
                    List<AccessibilityNodeInfo> doneNodes = rootNode.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/done_button");
                    if (null != doneNodes && !doneNodes.isEmpty()) {
                        AccessibilityNodeInfo doneNode = doneNodes.get(0);
                        doneNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d(TAG, "安装 " + doneNode.getText());
                        Toast.makeText(this.getApplicationContext(), "安装 " + doneNode.getText(), Toast.LENGTH_SHORT).show();
                        mEventType = TYPE_INVALID;
                        mMarketPackageName = null;
                        mInstallAppName = null;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, EVENT_CHANGE_ACTION)) {
                if (mEventType == TYPE_INVALID) {
                    mEventType = intent.getIntExtra(EXTRA_TYPE, TYPE_INVALID);
                    mMarketPackageName = intent.getStringExtra(EXTRA_MARKET);
                    mInstallAppName = intent.getStringExtra(EXTRA_APPNAME);
                }
            }
        }
    };
}