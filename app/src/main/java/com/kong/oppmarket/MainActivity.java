package com.kong.oppmarket;

import com.kong.oppmarket.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.kong.oppmarket.service.MarketAccessibilityService;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_stop).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_market).setOnClickListener(mOnClickListener);
        mContext = this;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_start:
                    Intent in = new Intent(MarketAccessibilityService.EVENT_CHANGE_ACTION);
                    in.putExtra(MarketAccessibilityService.EXTRA_TYPE, MarketAccessibilityService.TYPE_INTENT_DOWN);
                    in.putExtra(MarketAccessibilityService.EXTRA_APPNAME, "微信");
                    in.putExtra(MarketAccessibilityService.EXTRA_MARKET, "com.oppo.market");
                    in.setPackage(getPackageName());
                    mContext.sendBroadcast(in);
                    Log.d("kong", "MainActivity sendBroadcast LocalBroadcastManager TYPE_INTENT_DOWN");
                    break;
                case R.id.btn_stop:
                    Intent in1 = new Intent(MarketAccessibilityService.EVENT_CHANGE_ACTION);
                    in1.putExtra(MarketAccessibilityService.EXTRA_TYPE, MarketAccessibilityService.TYPE_INVALID);
                    mContext.sendBroadcast(in1);
                    Log.d("kong", "MainActivity sendBroadcast LocalBroadcastManager TYPE_INVALID");
                    break;
                case R.id.btn_market:
                    launchAppDetail("com.tencent.mm", null);
                    break;
                default:
                    break;
            }
        }
    };

    public void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}