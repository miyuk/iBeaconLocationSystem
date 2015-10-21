package jp.ac.oit.elc.mail.ibeaconlocationsystem.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import jp.ac.oit.elc.mail.ibeaconlocationsystem.R;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();
    private Button mButtonStartConfig;
    private Button mButtonStartLocate;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
        weka.core.Environment.getSystemWide().addVariable("WEKA_HOME", android.os.Environment.getExternalStorageDirectory() + "/local/wekafiles");
        try {
            FileReader reader = new FileReader(android.os.Environment.getExternalStorageDirectory() + "/local/iris-train.arff");
            Instances instances = new Instances(reader);
            instances.setClassIndex(instances.numAttributes() - 1);
            MultilayerPerceptron mlp = new MultilayerPerceptron();
            mlp.setLearningRate(0.1);
            mlp.setMomentum(0.2);
            mlp.setTrainingTime(2000);
            mlp.setHiddenLayers("3");
            mlp.buildClassifier(instances);
            Log.d(TAG, mlp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


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
