package jp.ac.oit.elc.mail.ibeaconlocationsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();
    private Button mButtonStartMeasure;
    private Button mButtonStartLocate;
    private Button mButtonStartEvaluate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
    }

    private void initViews() {
        mButtonStartMeasure = (Button) findViewById(R.id.buttonStartMeasure);
        mButtonStartLocate = (Button) findViewById(R.id.buttonStartLocate);
        mButtonStartEvaluate = (Button) findViewById(R.id.buttonStartEvaluate);
        mButtonStartMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MeasurementActivity.class);
                startActivity(intent);
            }
        });
        mButtonStartLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationActivity.class);
                startActivity(intent);
            }
        });
        mButtonStartEvaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EvaluationActivity.class);
                startActivity(intent);
            }
        });
    }
}
