package com.example.how_old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	private static final int PICK_CODE = 999;
	private static final int TAKEPHOTO_CODE = 666;
	private Button btn_get;
	private Button btn_detect;
	private Button btn_takephoto;
	private Button btn_more;
	private TextView tv_tip;
	private ImageView mphoto;
	private View mWaitingbar;
	private String mCurrentPhotoStr;
	private Bitmap mPhotoImg;
	private Paint mpaint;
	private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(!isNetworkAvailable(getBaseContext())){
        	Toast toast = Toast.makeText(getBaseContext(), "请打开移动网络或wifi", Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER, 0, 0);
        	toast.show();
        }
        initViews();
        initEvent();
        mpaint = new Paint();
        mpaint.setTypeface(Typeface.SANS_SERIF);
    }
   
    	public static boolean isNetworkAvailable(Context context) {
    		ConnectivityManager connectivity = (ConnectivityManager) context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);
    		if (connectivity == null) {
    			return false;
    		} else {
    			NetworkInfo[] info = connectivity.getAllNetworkInfo();
    			if (info != null) {
    				for (int i = 0; i < info.length; i++) {
    					if (info[i].isConnected()) {
    						return true;
    					}
    				}
    			}
    		}
    		return false;
    	}


	private void initEvent() {
    	btn_get.setOnClickListener(this);
    	btn_detect.setOnClickListener(this);
    	btn_takephoto.setOnClickListener(this);
    	btn_more.setOnClickListener(this);
	}


	private void initViews() {
		
		btn_get = (Button)findViewById(R.id.btn_get);
		btn_detect = (Button)findViewById(R.id.btn_detect);
		btn_takephoto = (Button) findViewById(R.id.btn_takephoto);
		btn_more = (Button) findViewById(R.id.btn_more);
		tv_tip = (TextView)findViewById(R.id.tv_tip);
		mphoto = (ImageView)findViewById(R.id.photo);
		mWaitingbar = findViewById(R.id.waiting);
	}

    private static final int msg_success = 0x111;
    private static final int msg_error = 0x112;
    private Handler mhandler = new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		switch (msg.what) {
			case msg_success:
				mWaitingbar.setVisibility(View.GONE);
				JSONObject rs = (JSONObject) msg.obj;
				PrepareRsBitmap(rs);
				mphoto.setImageBitmap(mPhotoImg);
				break;
			case msg_error:
				mWaitingbar.setVisibility(View.GONE);
				String errormsg = (String) msg.obj;
				if(TextUtils.isEmpty(errormsg)){
					tv_tip.setText("臣妾识别不出来啊");
				}
				else{
					tv_tip.setText("臣妾大脑突然凌乱了...");
				}
				break;
			default:
				break;
			}
    	}		
    };

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_more:
			Intent moreintent = new Intent(MainActivity.this,AboutActivity.class);
			startActivity(moreintent);
			overridePendingTransition(R.anim.main_to_left, R.anim.welcome_to_left);
			break;
			
		case R.id.btn_get: 
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_CODE);
			break;
		////////////////////////////////	
		case R.id.btn_takephoto:			
				// TODO Auto-generated method stub
				Intent tpintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(tpintent, TAKEPHOTO_CODE);
				break;
			
		/////////////////////////////////	
		case R.id.btn_detect:
			mWaitingbar.setVisibility(View.VISIBLE);
			
			if(mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals("")){
				if(mCurrentPhotoStr.equals("takingphoto")) {					
				}else {
					resizePhoto();
				}
			}else{
				mPhotoImg = BitmapFactory.decodeResource(getResources(), R.drawable.test);
			}
			FaceppDetect.detect(mPhotoImg, new FaceppDetect.Callback() {
				
				@Override
				public void success(JSONObject result) {
					Message msg = Message.obtain();
					msg.what = msg_success;
					msg.obj = result;
					mhandler.sendMessage(msg);
				}
				
				@Override
				public void error(FaceppParseException exception) {
					// TODO Auto-generated method stub
					Message msg = Message.obtain();
					msg.what = msg_error;
					msg.obj = exception.getErrorMessage();
					mhandler.sendMessage(msg);
				}
			});
			break;
		}		
	}


	protected void PrepareRsBitmap(JSONObject rs) {
				
		Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(),mPhotoImg.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(mPhotoImg,0,0, null);
		try {
			JSONArray faces = rs.getJSONArray("face");
			
			int countofFaces = faces.length();
			if(countofFaces == 0){
				tv_tip.setText("主人，我没有找到脸，求虐...");
			}else{
				tv_tip.setText("主人，我找到了"+countofFaces+"张脸");
			}
			for(int i=0;i <= countofFaces;i++){
				JSONObject face = faces.getJSONObject(i);

				JSONObject posobj = face.getJSONObject("position");
				float x = (float) posobj.getJSONObject("center").getDouble("x");
				float y = (float) posobj.getJSONObject("center").getDouble("y");
				float w = (float) posobj.getDouble("width");
				float h = (float) posobj.getDouble("height");
				
				x = x /100 * bitmap.getWidth();
				y = y /100 * bitmap.getHeight();
				w = w /100 * bitmap.getWidth();
				h = h /100 * bitmap.getHeight();

				mpaint.setColor(Color.WHITE);
				mpaint.setStrokeWidth(3);
			
				canvas.drawLine(x-w/2, y-h/2, x-w/2, y+h/2, mpaint);
				canvas.drawLine(x-w/2, y-h/2, x+w/2, y-h/2, mpaint);
				canvas.drawLine(x+w/2, y-h/2, x+w/2, y+h/2, mpaint);
				canvas.drawLine(x-w/2, y+h/2, x+w/2, y+h/2, mpaint);
				int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
				int range = face.getJSONObject("attribute").getJSONObject("age").getInt("range");
				float smilingvalue = (float) face.getJSONObject("attribute").getJSONObject("smiling").getDouble("value");
				Log.e("TAG",smilingvalue+"");
				range = range / 2;
				age += range;
				String gander = face.getJSONObject("attribute").getJSONObject("gender").getString("value");
				Bitmap ageBitmap = buildAgeBitmap(age,smilingvalue,countofFaces,"Male".equals(gander));
				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();
				
				if(bitmap.getWidth() < mphoto.getWidth() &&bitmap.getHeight()< mphoto.getHeight()){
					float ratio = Math.max(bitmap.getWidth()*1.0f / mphoto.getWidth(),bitmap.getHeight()*1.0f / mphoto.getHeight());;
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap,(int) (ageWidth*ratio),
							(int)(ageHeight * ratio), false);
				}
				canvas.drawBitmap(ageBitmap, x-ageBitmap.getWidth()/2,y-h/2 - ageBitmap.getHeight(),null ); 
				mPhotoImg = bitmap;	
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private Bitmap buildAgeBitmap(int age,float smilingvalue,int countofFaces, boolean isMale) {
		TextView tv = (TextView) mWaitingbar.findViewById(R.id.id_age_and_gender);
		String strage = age+"";
		String male = "♂ ";
		String female = "♀ ";
		String smiling = " ^_^";
		if(isMale){
			//tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male), null, null, null);
			
				if(smilingvalue > 50){
				tv.setText(male+strage+ smiling);
				if(countofFaces == 1 && age <= 21){tv_tip.setText("I find a smiling boy");}
				}else{tv.setText(male+strage);}
		}else{
			//tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female), null, null, null);			
				if(smilingvalue > 50){
					tv.setText(female+strage+ smiling);
					tv.invalidate();
					if(countofFaces == 1 && age <= 21){
						tv_tip.setText("I find a smiling girl");
						tv.invalidate();
					}
				}else{tv.setText(female+strage);
					tv.invalidate();
				}	
				
		}
		tv.invalidate();
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();
		return bitmap;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == PICK_CODE){
			if(intent != null){
				Uri uri = intent.getData();
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				
				int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				mCurrentPhotoStr = cursor.getString(idx);
				cursor.close();				
				resizePhoto();
				mphoto.setImageBitmap(mPhotoImg);
				
			}
		}
		//拍照
		if(requestCode == TAKEPHOTO_CODE){
			if(resultCode == RESULT_OK){ //判断返回结果，在相机界面按返回键程序会崩溃
				Bitmap bmPhoto = (Bitmap) intent.getExtras().get("data");  
				mCurrentPhotoStr = "takingphoto";
				mPhotoImg = bmPhoto;
				mphoto.setImageBitmap(mPhotoImg);
				tv_tip.setText("");
			}else{
				
			}
			
		}
	}		

	private void resizePhoto() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoStr, options);
		double ratio = Math.max(options.outWidth * 1.0d/1024f, options.outHeight * 1.0d/1024f);
		options.inSampleSize = (int)Math.ceil(ratio);
		options.inJustDecodeBounds = false;
		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr,options);
	}
}
