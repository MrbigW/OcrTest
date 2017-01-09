package com.megvii.idcardproject;

import org.apache.http.Header;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.megvii.awesomedemo.idcard.R;
import com.megvii.idcard.sdk.IDCard;
import com.megvii.idcardlib.util.ConUtil;
import com.megvii.idcardlib.util.DialogUtil;
import com.megvii.idcardlib.util.Util;
import com.megvii.idcardproject.util.Screen;
import com.megvii.licencemanage.sdk.LicenseManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by binghezhouke on 15-8-12.
 */
public class LoadingActivity extends Activity implements View.OnClickListener {

	private boolean isVertical = true, isTextDectde = true, isDebug;
	private EditText clearEdit, boundEdit, idcardEdit;
	private RelativeLayout contentRel;
	private LinearLayout barLinear;
	private TextView WarrantyText, authTime;
	private ProgressBar WarrantyBar;
	private Button againWarrantyBtn;
	private float[] faculaPasss = { 0.3f, 0.5f, 0.7f };
	private float faculaPass = faculaPasss[0];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		Screen.initialize(this);
		init();
		initData();
		network();
	}

	private void init() {
		clearEdit = (EditText) findViewById(R.id.load_layout_clearEdit);
		boundEdit = (EditText) findViewById(R.id.load_layout_inBoundEdit);
		idcardEdit = (EditText) findViewById(R.id.load_layout_isIDCard);
		findViewById(R.id.load_layout_verticalBtn).setOnClickListener(this);
		findViewById(R.id.load_layout_textDetectBtn).setOnClickListener(this);
		findViewById(R.id.load_layout_debugBtn).setOnClickListener(this);
		findViewById(R.id.load_rootRel).setOnClickListener(this);
		findViewById(R.id.loading_enterBtn).setOnClickListener(this);
		findViewById(R.id.load_layout_verticalBtn).setOnClickListener(this);
		findViewById(R.id.load_layout_textDetectBtn).setOnClickListener(this);
		findViewById(R.id.load_layout_debugBtn).setOnClickListener(this);
		contentRel = (RelativeLayout) findViewById(R.id.loading_layout_contentRel);
		barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
		WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
		WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
		againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
		againWarrantyBtn.setOnClickListener(this);
		authTime = (TextView) findViewById(R.id.load_authTimeText);
	}

	private void initData() {
		if (Util.API_KEY == null || Util.API_SECRET == null) {
			if (!ConUtil.isReadKey(this)) {
				DialogUtil mDialogUtil = new DialogUtil(this);
				mDialogUtil.showDialog("请填写API_KEY和API_SECRET");
			}
		}
	}

	private void network() {
		IDCard mIdCard = new IDCard();
		mIdCard.init(this, Util.readModel(this));
		contentRel.setVisibility(View.GONE);
		barLinear.setVisibility(View.VISIBLE);
		againWarrantyBtn.setVisibility(View.GONE);
		WarrantyText.setText("正在联网授权中...");
		WarrantyBar.setVisibility(View.VISIBLE);
		final LicenseManager licenseManager = new LicenseManager(this);
		licenseManager.setAuthTime(IDCard.getApiExpication(this) * 1000);
		// licenseManager.setAgainRequestTime(againRequestTime);

		String uuid = ConUtil.getUUIDString(LoadingActivity.this);
		long[] apiName = { IDCard.getApiName() };
		String content = licenseManager.getContent(uuid, LicenseManager.DURATION_30DAYS, apiName);

		String errorStr = licenseManager.getLastError();
		Log.w("ceshi", "getContent++++errorStr===" + errorStr);

		boolean isAuthSuccess = licenseManager.authTime();
		Log.w("ceshi", "isAuthSuccess===" + isAuthSuccess);
		if (isAuthSuccess) {
			authState(true);
		} else {
			AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
			String url = "https://api.megvii.com/megviicloud/v1/sdk/auth";
			RequestParams params = new RequestParams();
			params.put("api_key", Util.API_ONLINEKEY);
			params.put("api_secret", Util.API_ONLINESECRET);
			params.put("auth_msg", content);
			mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
					String successStr = new String(responseByte);
					boolean isSuccess = licenseManager.setLicense(successStr);
					if (isSuccess)
						authState(true);
					else
						authState(false);

					String errorStr = licenseManager.getLastError();
					Log.w("ceshi", "setLicense++++errorStr===" + errorStr + ", " + isSuccess);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					authState(false);
				}
			});
		}
		mIdCard.release();
	}

	private void authState(boolean isSuccess) {
		authTime.setText(IDCard.getVersion() + " ; " + ConUtil.getFormatterDate(IDCard.getApiExpication(this) * 1000));
		if (isSuccess) {
			barLinear.setVisibility(View.GONE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.GONE);
			contentRel.setVisibility(View.VISIBLE);
		} else {
			barLinear.setVisibility(View.VISIBLE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.VISIBLE);
			contentRel.setVisibility(View.GONE);
			WarrantyText.setText("联网授权失败！请检查网络或找服务商");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.loading_layout_againWarrantyBtn) {
			network();
		} else if (id == R.id.load_rootRel) {
			ConUtil.isGoneKeyBoard(this);
		} else if (id == R.id.load_layout_verticalBtn) {
			isVertical = !isVertical;
		} else if (id == R.id.load_layout_textDetectBtn) {
			isTextDectde = !isTextDectde;
		} else if (id == R.id.load_layout_debugBtn) {
			isDebug = !isDebug;
		} else if (id == R.id.loading_enterBtn) {
			Intent intent = new Intent(this, com.megvii.idcardlib.IDCardScanActivity.class);
			float clear = Float.parseFloat(clearEdit.getText().toString());
			float bound = Float.parseFloat(boundEdit.getText().toString());
			float idcard = Float.parseFloat(idcardEdit.getText().toString());
			intent.putExtra("isvertical", isVertical);
			intent.putExtra("clear", clear);
			intent.putExtra("bound", bound);
			intent.putExtra("idcard", idcard);
			intent.putExtra("isDebug", isDebug);
			intent.putExtra("faculaPass", faculaPass);
			startActivityForResult(intent, INTO_IDCARDSCAN_PAGE);
		}
	}

	private static final int INTO_IDCARDSCAN_PAGE = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INTO_IDCARDSCAN_PAGE && resultCode == RESULT_OK) {
			Intent intent = new Intent(this, ResultActivity.class);
			intent.putExtra("points", data.getFloatArrayExtra("points"));
			intent.putExtra("path", data.getStringExtra("path"));
			intent.putExtra("isTextDectde", isTextDectde);
			intent.putExtra("in_bound", data.getFloatExtra("in_bound", 0.8f));
			intent.putExtra("is_idcard", data.getFloatExtra("is_idcard", 0.5f));
			intent.putExtra("clear", data.getFloatExtra("clear", 0.8f));

			startActivity(intent);
		}
	}
}