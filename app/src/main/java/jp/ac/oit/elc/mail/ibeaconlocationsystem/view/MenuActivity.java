package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();
    private Button mButtonStartConfig;
    private Button mButtonStartLocate;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
    }

    private void initViews() {
        mButtonStartConfig = (Button)findViewById(R.id.buttonStartConfig);
        mButtonStartLocate = (Button)findViewById(R.id.buttonStartLocate);
        mButtonStartConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ConfigActivity.class);
                startActivity(intent);
            }
        });
        mButtonStartLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocateActivity.class);
                startActivity(intent);
            }
        });
    }
}
