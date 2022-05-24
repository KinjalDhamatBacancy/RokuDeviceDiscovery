package com.roku.rokudevicediscovery;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AppInfoActivity extends Activity {
    protected TextView appInfoTextView;
    protected Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        appInfoTextView= (TextView)findViewById(R.id.appInfo);
        backButton = (Button)findViewById(R.id.buttonBack);
        backButton.setOnClickListener(backListener);
        Intent intent = getIntent();
        appInfoTextView.setText(intent.getStringExtra("appInfo"));
    }

    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            AppInfoActivity.this.finish();
        }
    };


    public void setAppInfoText(String appInfo) {
        appInfoTextView.setText(appInfo);
    }
}