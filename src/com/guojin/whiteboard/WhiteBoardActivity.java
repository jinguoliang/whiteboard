package com.guojin.whiteboard;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.guojin.entities.BoardEntity;

public class WhiteBoardActivity extends Activity {

	private static final String TAG = "WhiteBoardActivity";
	// 选择图片的方式
	private static final int SELECT_PICTURE = 0;
	private static final int SELECT_CAMER = 1;

	private float oldDist = Float.NaN; // 两触点之间的旧距离值
	private PointF oldMidPoint; // 旧两触点中心点

	private BoardEntity boardEntity = null; // Board实体

	private BoardView boardView; // Board View

	private RelativeLayout topbarLayout;
	
	private RadioGroup modeSelectGroup; // 模式单选组
	private LinearLayout handDrawModeConfLayout; // 手绘模式配置Layout
	private ToggleButton handDrawModeEraserBtn;	// 橡皮擦切换按钮

	private LinearLayout noteModeConfLayout; // 便签模式总配置Layout
	private Button noteAddNewBtn;
	private ToggleButton noteTextSizeBtn; // 便签字体大小设置按钮
	private ToggleButton noteStyleBtn; // 便签样式设置按钮

	private RadioGroup noteStyleConfGroup; // 便签样式配置单选组

	private LinearLayout noteTextSizeConfLayout; // 便签字体大小配置Layout
	private TextView noteTextSizeTxt; // 便签字体大小显示
	private SeekBar noteTextSizeSeekbar; // 便签字体调整

	private TextView scaleTextView; // 缩放级别显示
	protected File tmpPicFile;
	
	private float firstTouchX = -1;
	private float firstTouchY = -1;
	private boolean isOnePointAction = false;
	private long firstTouchTime = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boardEntity = new BoardEntity(this);
		boardView = new BoardView(this, boardEntity);
		// 设置view可以获取焦点
		boardView.setFocusable(true);
		boardView.setFocusableInTouchMode(true);
		boardEntity.bindView(boardView);

		// 向布局中添加boardview
		FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(
				R.layout.activity_whiteboard, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		boardView.setLayoutParams(lp);
		layout.addView(boardView, 0);

		setContentView(layout);

		// 初始化控件
		topbarLayout = (RelativeLayout) findViewById(R.id.topbar_layout);
		topbarLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		// 模式选择
		modeSelectGroup = (RadioGroup) findViewById(R.id.modesel_group);
		handDrawModeConfLayout = (LinearLayout) findViewById(R.id.handdraw_conf_layout);
		// 橡皮切换按钮
		handDrawModeEraserBtn = (ToggleButton)findViewById(R.id.handraw_conf_eraser_btn);
		handDrawModeEraserBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					
				} else {
					
				}
			}
		});
		
		
		// 便签模式设置
		noteModeConfLayout = (LinearLayout) findViewById(R.id.note_conf_layout);
		noteModeConfLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		noteTextSizeBtn = (ToggleButton)findViewById(R.id.note_conf_textsize_btn);
		noteStyleBtn = (ToggleButton)findViewById(R.id.note_conf_style_btn);
		// 添加新便签按钮
		noteAddNewBtn = (Button)findViewById(R.id.note_add_new);
		noteAddNewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boardEntity.addEntity();
			}
		});
		noteStyleConfGroup = (RadioGroup)findViewById(R.id.note_style_group);
		noteStyleConfGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		noteTextSizeConfLayout = (LinearLayout) findViewById(R.id.note_textsize_layout);
		noteTextSizeConfLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		noteTextSizeTxt = (TextView) findViewById(R.id.note_textsize_txt);
		noteTextSizeSeekbar = (SeekBar) findViewById(R.id.note_textsize_sbar);

		// 缩放比例显示
		scaleTextView = (TextView) findViewById(R.id.scale_ratio_txt);
		scaleTextView.setText((int) (boardEntity.getTotalScale() * 100) + "%");

		// 模式选择监听器
		modeSelectGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (group.getCheckedRadioButtonId()) {
				case R.id.mode_handdraw_btn:
					// 手绘模式
					handDrawModeConfLayout.setVisibility(View.VISIBLE);
					noteModeConfLayout.setVisibility(View.GONE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					
					// 修改模式
					boardEntity.changeMode(BoardEntity.MODE_HANDDRAW);
					break;
				case R.id.mode_picdraw_btn:
					// 图片模式
					handDrawModeConfLayout.setVisibility(View.GONE);
					noteModeConfLayout.setVisibility(View.GONE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					
					// 修改模式
					boardEntity.changeMode(BoardEntity.MODE_PIC);
					break;
				case R.id.mode_notedraw_btn:
					// 便签模式
					handDrawModeConfLayout.setVisibility(View.GONE);
					noteModeConfLayout.setVisibility(View.VISIBLE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					noteStyleBtn.setChecked(false);
					noteTextSizeBtn.setChecked(false);
					
					// 修改模式
					boardEntity.changeMode(BoardEntity.MODE_NOTE);
					break;
				}
			}
		});
		((RadioButton)modeSelectGroup.findViewById(R.id.mode_handdraw_btn)).setChecked(true);
		
		// 便签字体设置按钮监听
		noteTextSizeBtn
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							noteStyleBtn.setChecked(false);
							noteTextSizeConfLayout.setVisibility(View.VISIBLE);
						} else {
							noteTextSizeConfLayout.setVisibility(View.GONE);
						}
					}
				});

		// 便签样式设置按钮监听
		noteStyleBtn
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							noteTextSizeBtn.setChecked(false);
							noteStyleConfGroup.setVisibility(View.VISIBLE);
						} else {
							noteStyleConfGroup.setVisibility(View.GONE);
						}
					}
				});

		// 便签样式选择监听器
		noteStyleConfGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.note_style_blue:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_blue));
							break;
						case R.id.note_style_green:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_green));
							break;
						case R.id.note_style_orange:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_orange));
							break;
						case R.id.note_style_purple:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_purple));
							break;
						case R.id.note_style_red:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_red));
							break;
						case R.id.note_style_gray:
							boardEntity.setNoteStyleColor(getResources()
									.getColor(R.color.note_style_gray));
							break;
						}
					}
				});
	}

	/**
	 * 获取两触点之间的距离
	 * 
	 * @param event
	 * @return
	 */
	private float getPointsDist(MotionEvent event) {
		float dx = event.getX(0) - event.getX(1);
		float dy = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 获取两触点之间的中心点
	 * 
	 * @param event
	 * @return
	 */
	private PointF getMidPoint(MotionEvent event) {
		float x1 = event.getX(0);
		float y1 = event.getY(0);
		float x2 = event.getX(1);
		float y2 = event.getY(1);

		int[] loc = new int[2];
		boardView.getLocationOnScreen(loc);

		return new PointF((x1 + x2) / 2, (y1 + y2) / 2 - loc[1]);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// 触点数目
		int pointerCount = event.getPointerCount();
		
		// 缩放比例
		float scale = 1;
		// 偏移距离
		float dx = 0f;
		float dy = 0f;

		// Log.d("DevLog", String.format("touch position: %f,%f", event.getX(),
		// event.getY()));
		
		if (pointerCount == 1) {
			
			if (boardEntity.mode == BoardEntity.MODE_HANDDRAW) {
				if (!isOnePointAction) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						firstTouchX = event.getX();
						firstTouchY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						float deltaX = event.getX() - firstTouchX;
						float deltaY = event.getY() - firstTouchY;
						int distSqua = (int)(deltaX * deltaX) + (int)(deltaY * deltaY);
						if (distSqua > 40) {
							isOnePointAction = true;
							event.setAction(MotionEvent.ACTION_DOWN);
							boardEntity.onEntityTouchEvent(event);
						}
						break;
					}
					return false;
				}
			} else if (boardEntity.mode == BoardEntity.MODE_PIC) {
				if (!isOnePointAction) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						firstTouchTime = new Date().getTime();
						break;
					case MotionEvent.ACTION_MOVE:
						long curTouchTime = new Date().getTime();
						if (curTouchTime - firstTouchTime > 100) {
							isOnePointAction = true;
							event.setAction(MotionEvent.ACTION_DOWN);
							boardEntity.onEntityTouchEvent(event);
						}
						break;
					}
					return false;
				}
			}
			
			
			boardEntity.onEntityTouchEvent(event);
			if (event.getAction() == MotionEvent.ACTION_UP) {
				isOnePointAction = false;
			}
		}
		
		if (!isOnePointAction && pointerCount == 2) {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:

				// 计算缩放比例scale
				if (Float.isNaN(oldDist)) {
					oldDist = getPointsDist(event);
				} else {
					float newDist = getPointsDist(event);

					if (Math.abs(newDist - oldDist) > 1f) {
						scale = newDist / oldDist;
						oldDist = newDist;
					}
				}

				// 计算偏移距离
				if (oldMidPoint == null) {
					oldMidPoint = getMidPoint(event);
				} else {
					PointF newMidPoint = getMidPoint(event);

					float tdx = newMidPoint.x - oldMidPoint.x;
					float tdy = newMidPoint.y - oldMidPoint.y;

					if (Math.abs(tdx) > 1f || Math.abs(tdy) > 1f) {
						dx = tdx;
						dy = tdy;
						oldMidPoint = newMidPoint;
					}
				}

				PointF currMidPoint = getMidPoint(event);
				boardEntity.calculate(currMidPoint.x, currMidPoint.y, scale,
						dx, dy, boardView.getWidth(), boardView.getHeight());
				// 显示缩放比例
				scaleTextView.setText((int) (boardEntity.getTotalScale() * 100)
						+ "%");
				boardView.postInvalidate();

				// Log.d("DevLog", String.format("Scale: %f\nDist: %f , %f",
				// scale, dx, dy));
				break;
			case MotionEvent.ACTION_POINTER_UP:

				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = getPointsDist(event);
				oldMidPoint = getMidPoint(event);
				break;
			}
		} 

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
//			if (requestCode == SELECT_PICTURE) {
//
//				if (data != null) {
//					Uri uri = data.getData();
//					Log.e(TAG, uri.toString());
//					Bitmap mBitmap = BitmapFactory.decodeFile(tmpPicFile
//							.getAbsolutePath());
//					boardEntity.receivePicture(mBitmap);
//				} else {
//					Log.e(TAG, "data == null");
//				}
//			}else{
				Bitmap mBitmap = BitmapFactory.decodeFile(tmpPicFile
						.getAbsolutePath());
				boardEntity.receivePicture(mBitmap);
//			}
		} else {
			Log.e(TAG, "result wrong");
		}
	}

	public void loadPicture() {
		CharSequence[] items = { "相册", "相机" };
		new AlertDialog.Builder(this).setTitle("选择图片来源")
				.setItems(items, new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						tmpPicFile = new File(Environment
								.getExternalStorageDirectory().getPath()
								+ "/tmppic"
								+ SystemClock.currentThreadTimeMillis()
								+ ".jpg");

						if (which == SELECT_PICTURE) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							intent.putExtra("output", Uri.fromFile(tmpPicFile));
							 intent.putExtra("crop", "true");
							 intent.putExtra("aspectX", 1);// 裁剪框比例
							 intent.putExtra("aspectY", 1);
							 intent.putExtra("outputX", 400);// 输出图片大小
							 intent.putExtra("outputY", 400);
							startActivityForResult(
									Intent.createChooser(intent, "选择图片"),
									SELECT_PICTURE);
						} else {
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra("output", Uri.fromFile(tmpPicFile));
//							 intent.putExtra("crop", "true");
//							 intent.putExtra("aspectX", 1);// 裁剪框比例
//							 intent.putExtra("aspectY", 1);
//							 intent.putExtra("outputX", 180);// 输出图片大小
//							 intent.putExtra("outputY", 180);
							startActivityForResult(intent, SELECT_CAMER);
						}
					}
				}).create().show();
	}

}
