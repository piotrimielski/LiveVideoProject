package com;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import com.givevision.lifevideo.MainActivity;
import com.givevision.lifevideo.R;

public class ConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // set def rtmp url
        ((EditText) findViewById(R.id.streamUrl)).setText("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8");

        findViewById(R.id.startNetwork).setOnClickListener(view -> {
            CheckBox showControls = findViewById(R.id.showControls);
            String streamUrl = ((EditText) findViewById(R.id.streamUrl)).getText().toString();
            MainActivity.launch(this, showControls.isChecked(), streamUrl, Source.NETWORK_STREAM);
        });

        findViewById(R.id.startCamera).setOnClickListener(view -> {
            CheckBox showControls = findViewById(R.id.showControls);
            MainActivity.launch(this, showControls.isChecked(), "", Source.CAMERA);
        });
    }
}
