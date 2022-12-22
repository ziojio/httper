package com.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.demo.databinding.ActivityHttpBinding;

import httper.HttpCallback;
import httper.HttpResponse;
import httper.Httper;

public class HttpActivity extends BaseActivity {
    private ActivityHttpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHttpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.input.setText("http://www.baidu.com");
        binding.request.setOnClickListener(v -> {
            String in = binding.input.getText().toString().trim();
            if (TextUtils.isEmpty(in)) {
                showToast("input is empty");
            } else {
                request(in);
            }
        });
    }

    private void request(String url) {
        Httper httper = new Httper.Builder().setDebug(true).build();
        httper.get().url(url).request(new HttpCallback<String>() {
            @Override
            public void onResult(HttpResponse<String> httpResponse) {
                Log.d(TAG, "onResult: " + httpResponse);
                runOnUiThread(() -> {
                    if (httpResponse.isSuccess()) {
                        binding.text.setText(httpResponse.data);
                    } else {
                        binding.text.setText(httpResponse.error.toString());
                    }
                });
            }
        });
    }

}
