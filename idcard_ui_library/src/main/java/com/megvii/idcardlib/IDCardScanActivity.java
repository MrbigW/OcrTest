package com.megvii.idcardlib;

import com.megvii.idcard.sdk.IDCard;
import com.megvii.idcard.sdk.IDCard.IDCardConfig;
import com.megvii.idcard.sdk.IDCard.IDCardDetect;
import com.megvii.idcard.sdk.IDCard.IDCardQuality;
import com.megvii.idcardlib.util.ConUtil;
import com.megvii.idcardlib.util.DialogUtil;
import com.megvii.idcardlib.util.ICamera;
import com.megvii.idcardlib.util.IDCardIndicator;
import com.megvii.idcardlib.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IDCardScanActivity extends Activity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

	private TextureView textureView;
	private DialogUtil mDialogUtil;
	private ICamera mICamera;// 照相机工具类
	private IDCardIndicator mIndicatorView;
	private boolean mIsVertical = false, mIsDebug = false;
	private TextView fps;
	private TextView errorType, verticalType, warmText, verticalWarmText;
	private IDCard mIdCard;
	private HandlerThread mHandlerThread = new HandlerThread("hhh");
	private Handler mHandler;
	private long handle;
	private ImageView image;
	private float setClear = 0.8f, setIdcard = 0.5f, setBound = 0.8f;
	private RelativeLayout barRel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idcardscan_layout);
		init();
	}

	private void init() {
		mIdCard = new IDCard();
		mIdCard.init(this, Util.readModel(this));
		setClear = getIntent().getFloatExtra("clear", 0.8f);
		setIdcard = getIntent().getFloatExtra("idcard", 0.5f);
		setBound = getIntent().getFloatExtra("bound", 0.8f);
		Log.w("ceshi", "setClear==="  + setClear + ", " + setIdcard + ", " + setBound);
		mIsVertical = getIntent().getBooleanExtra("isvertical", false);
		mIsDebug = getIntent().getBooleanExtra("isDebug", false);
		image = (ImageView) findViewById(R.id.image);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		mICamera = new ICamera(mIsVertical);
		mDialogUtil = new DialogUtil(this);
		textureView = (TextureView) findViewById(R.id.idcardscan_layout_surface);
		textureView.setSurfaceTextureListener(this);
		textureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mICamera.autoFocus();
			}
		});
		barRel = (RelativeLayout) findViewById(R.id.idcard_layout_barRel);
		fps = (TextView) findViewById(R.id.idcardscan_layout_fps);
		fps.setVisibility(View.VISIBLE);
		errorType = (TextView) findViewById(R.id.idcardscan_layout_error_type);
		verticalType = (TextView) findViewById(R.id.idcardscan_layout_verticalerror_type);
		mIndicatorView = (IDCardIndicator) findViewById(R.id.idcardscan_layout_indicator);
		warmText = (TextView) findViewById(R.id.idcardscan_layout_warmText);
		verticalWarmText = (TextView) findViewById(R.id.idcardscan_layout_verticalWarmText);

		if (mIsVertical) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			verticalType.setVisibility(View.VISIBLE);
			verticalWarmText.setVisibility(View.VISIBLE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			errorType.setVisibility(View.VISIBLE);
			warmText.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Camera mCamera = mICamera.openCamera(this);
		if (mCamera != null) {
			RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam(this);
			textureView.setLayoutParams(layout_params);
			mIndicatorView.setLayoutParams(layout_params);

		} else {
			mDialogUtil.showDialog("打开摄像头失败");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mICamera.closeCamera();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDialogUtil.onDestory();
		if (handle != 0) {
			mIdCard.release();
		}
	}

	int width;
	int height;
	int orientation = 0;

	private void doPreview() {
		if (!mHasSurface)
			return;

		mICamera.startPreview(textureView.getSurfaceTexture());

		IDCardConfig idCardConfig = mIdCard.getFaceppConfig();

		RectF rectF = mIndicatorView.getPosition();
		width = mICamera.cameraWidth;
		height = mICamera.cameraHeight;

		int left = (int) (width * rectF.left);
		int top = (int) (height * rectF.top);
		int right = (int) (width * rectF.right);
		int bottom = (int) (height * rectF.bottom);
		if (mIsVertical) {
			left = (int) (width * rectF.top);
			top = (int) (height * rectF.left);
			right = (int) (width * rectF.bottom);
			bottom = (int) (height * rectF.right);
			orientation = 180 - mICamera.orientation;
		}

		idCardConfig.orientation = orientation;
		idCardConfig.roi_left = left;
		idCardConfig.roi_top = top;
		idCardConfig.roi_right = right;
		idCardConfig.roi_bottom = bottom;

		mIdCard.setFaceppConfig(idCardConfig);
	}

	private boolean mHasSurface = false;

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mHasSurface = true;
		doPreview();

		mICamera.actionDetect(this);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mHasSurface = false;
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	boolean isSuccess = false;

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		if (isSuccess)
			return;
		isSuccess = true;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				long actionDetectTme = System.currentTimeMillis();
				IDCardDetect iCardDetect = mIdCard.detect(data, width, height, IDCard.IMAGEMODE_NV21);
				final long DetectTme = System.currentTimeMillis() - actionDetectTme;
				final float in_bound = iCardDetect.inBound;
				final float is_idcard = iCardDetect.isIdcard;
				final float clear = iCardDetect.clear;
				String errorStr = "";

				if (clear < setClear)
					errorStr = "请点击屏幕对焦";
				else if (in_bound < setBound)
					errorStr = "请将身份证对准引导框";

				if (in_bound >= setBound && is_idcard >= setIdcard && clear >= setClear) {
						Bitmap bitmap = ConUtil.cutImage(mIndicatorView.getPosition(), data, mICamera.mCamera,
								mIsVertical);
						String path = ConUtil.saveBitmap(IDCardScanActivity.this, bitmap);
						enterToResult(path, clear, is_idcard, in_bound);
				} else {
					isSuccess = false;
				}

				String fpsStr = "in_bound: " + in_bound + "\nis_idcard: " + is_idcard + "\nclear: " + clear
						+ "\nDetectTme: " + DetectTme;
				print(errorStr, fpsStr);
			}
		});
	}

	private void print(final String errorStr, final String fpsStr) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				errorType.setText(errorStr);
				verticalType.setText(errorStr);
				if (mIsDebug) {
					fps.setText(fpsStr);
				}
			}
		});
	}

	private void enterToResult(String path, float clear, float is_idcard, float in_bound) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				barRel.setVisibility(View.VISIBLE);
				errorType.setText("");
				verticalType.setText("");
			}
		});
		Intent intent = new Intent();
		intent.putExtra("path", path);
		intent.putExtra("clear", clear);
		intent.putExtra("is_idcard", is_idcard);
		intent.putExtra("in_bound", in_bound);
		setResult(RESULT_OK, intent);
		finish();
	}

	public boolean isEven01(int num) {
		if (num % 2 == 0) {
			return true;
		} else {
			return false;
		}
	}
}