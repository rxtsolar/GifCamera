package com.zimiao.gifcamera.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity {

    private View mButton;

    private OnClickListener mOnclickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            Intent intent = new Intent(mButton.getContext(), CameraActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.record_button);
        mButton.setOnClickListener(mOnclickListener);
    }
}
