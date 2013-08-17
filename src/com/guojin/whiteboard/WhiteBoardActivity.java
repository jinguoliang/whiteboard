package com.guojin.whiteboard;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.guojin.entities.BoardEntity;

public class WhiteBoardActivity extends Activity {
	
	
	private BoardEntity boardEntity = null;	// Board实体
	
	private BoardView boardView;	// Board View
	
	private RelativeLayout topbarLayout;
	private RadioGroup modeSelectGroup;		// 模式单选组
	private RadioGroup handDrawModeConfGroup;		// 手绘模式配置单选组
	
	private LinearLayout noteModeConfLayout;		// 便签模式总配置Layout
	private ToggleButton noteTextSizeBtn;	// 便签字体大小设置按钮
	private ToggleButton noteStyleBtn;		// 便签样式设置按钮
	
	private RadioGroup noteStyleConfGroup;		// 便签样式配置单选组
	
	private LinearLayout noteTextSizeConfLayout;	// 便签字体大小配置Layout
	private TextView noteTextSizeTxt;		// 便签字体大小显示
	private SeekBar noteTextSizeSeekbar;	// 便签字体调整
	
	private TextView scaleTextView;		// 缩放级别显示
	
	
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
		FrameLayout layout = (FrameLayout)getLayoutInflater().inflate(R.layout.activity_whiteboard, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		boardView.setLayoutParams(lp);
		layout.addView(boardView, 0);
		
		setContentView(layout);
		
		// 初始化控件
		topbarLayout = (RelativeLayout)findViewById(R.id.topbar_layout);
		
		// 模式选择
		modeSelectGroup = (RadioGroup)findViewById(R.id.modesel_group);
		handDrawModeConfGroup = (RadioGroup)findViewById(R.id.handdraw_conf_bar);
		
		// 便签模式设置
		noteModeConfLayout = (LinearLayout)findViewById(R.id.note_conf_layout);
		noteTextSizeBtn = (ToggleButton)findViewById(R.id.note_conf_textsize_btn);
		noteStyleBtn = (ToggleButton)findViewById(R.id.note_conf_style_btn);
		
		noteStyleConfGroup = (RadioGroup)findViewById(R.id.note_style_group);
		noteTextSizeConfLayout = (LinearLayout)findViewById(R.id.note_textsize_layout);
		noteTextSizeTxt = (TextView)findViewById(R.id.note_textsize_txt);
		noteTextSizeSeekbar = (SeekBar)findViewById(R.id.note_textsize_sbar);
		
		// 缩放比例显示
		scaleTextView = (TextView)findViewById(R.id.scale_ratio_txt);
		scaleTextView.setText((int)(boardEntity.getTotalScale() * 100) + "%");
		
		// 模式选择监听器
		modeSelectGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (group.getCheckedRadioButtonId()) {
				case R.id.handdraw_btn:
					// 手绘模式
					handDrawModeConfGroup.setVisibility(View.VISIBLE);
					noteModeConfLayout.setVisibility(View.GONE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					
					break;
				case R.id.picdraw_btn:
					// 图片模式
					handDrawModeConfGroup.setVisibility(View.GONE);
					noteModeConfLayout.setVisibility(View.GONE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					
					break;
				case R.id.notedraw_btn:
					// 便签模式
					handDrawModeConfGroup.setVisibility(View.GONE);
					noteModeConfLayout.setVisibility(View.VISIBLE);
					noteStyleConfGroup.setVisibility(View.GONE);
					noteTextSizeConfLayout.setVisibility(View.GONE);
					noteStyleBtn.setChecked(false);
					noteTextSizeBtn.setChecked(false);
					
					break;
				}
			}
		});
		
		// 便签字体设置按钮监听
		noteTextSizeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					noteStyleBtn.setChecked(false);
					noteTextSizeConfLayout.setVisibility(View.VISIBLE);
				} else {
					noteTextSizeConfLayout.setVisibility(View.GONE);
				}
			}
		});
		
		// 便签样式设置按钮监听
		noteStyleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					noteTextSizeBtn.setChecked(false);
					noteStyleConfGroup.setVisibility(View.VISIBLE);
				} else {
					noteStyleConfGroup.setVisibility(View.GONE);
				}
			}
		});
		
		// 便签样式选择监听器
		noteStyleConfGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.note_style_blue:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_blue));
					break;
				case R.id.note_style_green:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_green));
					break;
				case R.id.note_style_orange:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_orange));
					break;
				case R.id.note_style_purple:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_purple));
					break;
				case R.id.note_style_red:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_red));
					break;
				case R.id.note_style_gray:
					boardEntity.setNoteStyleColor(getResources().getColor(R.color.note_style_gray));
					break;
				}
			}
		});
	}
	
	public void setScaleText(String text) {
		scaleTextView.setText(text);
	}
}
