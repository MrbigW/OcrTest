package com.slbxtz.ocrtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.slbxtz.ocrtest.activity.CameraActivity;
import com.slbxtz.ocrtest.bean.ResultBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileInputStream;

import okhttp3.Call;

public class MainActivity extends Activity {

    private static final String URL = "https://api-cn.faceplusplus.com/cardpp/v1/ocridcard";


    private Button mCatch, mRecognize;
    private EditText mPath, mName, mType;
    private ImageView mPhoto;
    private String mPhotoPath;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initClickListener();
    }

    private void initClickListener() {
        mCatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                String pathStr = mPath.getText().toString();
                String nameStr = mName.getText().toString();
                String typeStr = mType.getText().toString();
                if (!TextUtils.isEmpty(pathStr)) {
                    intent.putExtra("path", pathStr);
                }
                if (!TextUtils.isEmpty(nameStr)) {
                    intent.putExtra("name", nameStr);
                }
                if (!TextUtils.isEmpty(typeStr)) {
                    intent.putExtra("type", typeStr);
                }
                startActivityForResult(intent, 100);
            }
        });

        mRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAndRecognize();
            }
        });
    }

    private void uploadAndRecognize() {
        if (!TextUtils.isEmpty(mPhotoPath)) {
            File file = new File(mPhotoPath);


            OkHttpUtils
                    .post()
                    .url(URL)
                    .addParams("api_key", "vNVEtiKTRz8a6PKnDf5oDWrxD1oV5cae")
                    .addParams("api_secret", "GhmTrji4OJchlwQg0Z7hcqVsI_oOj7sP")
                    .addFile("image_file", "idcardFront_user.jpg", file)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Log.e("TAG", e.getMessage());
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            processData(response);
                        }
                    });

        }
    }

    private void processData(String response) {
        ResultBean resultBean = JSON.parseObject(response, ResultBean.class);

        ResultBean.CardsBean cardsBean = resultBean.getCards().get(0);
        StringBuilder result = new StringBuilder("");

        result.append("name:" + cardsBean.getName())
                .append("\n")
                .append("cardno:" + cardsBean.getId_card_number())
                .append("\n")
                .append("sex:" + cardsBean.getGender())
                .append("\n")
                .append("folk:" + cardsBean.getRace())
                .append("\n")
                .append("birthday:" + cardsBean.getBirthday())
                .append("\n")
                .append("address:" + cardsBean.getAddress())
                .append("\n");


        mTextView.setText(result);

    }

    private void initView() {
        mPath = (EditText) findViewById(R.id.path);
        mName = (EditText) findViewById(R.id.name);
        mType = (EditText) findViewById(R.id.type);
        mCatch = (Button) findViewById(R.id.btn);
        mTextView = (TextView) findViewById(R.id.tv);
        mRecognize = (Button) findViewById(R.id.recognize);
        mPhoto = (ImageView) findViewById(R.id.photo);
        //File file=getFilesDir();
        File externalFile = getExternalFilesDir("/idcard/");
        mPath.setText(externalFile.getAbsolutePath());
        Log.e("TAG", externalFile.getAbsolutePath() + "\n" + externalFile.getAbsolutePath());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("TAG", "onActivityResult");
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String path = extras.getString("path");
                String type = extras.getString("type");
                Toast.makeText(getApplicationContext(), "path:" + path + " type:" + type, Toast.LENGTH_LONG).show();
                mPhotoPath = path;
                File file = new File(path);
                FileInputStream inStream = null;
                try {
                    inStream = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(inStream);
                    mPhoto.setImageBitmap(bitmap);
                    inStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
