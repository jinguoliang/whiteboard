package com.guojin.whiteboard;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.guojin.store.DataManager;
import com.guojin.store.DatabaseContract.BoardDBEntity;

public class BoardListActivity extends Activity {

	private ListView boardListView;

	private Button addBoardBtn;
	
	// Board存储对象
	private List<ContentValues> boards = new ArrayList<ContentValues>();

	DataManager dataManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boardlist);
		dataManager = new DataManager(this);
		
		boardListView = (ListView)findViewById(R.id.boardListView);
		BoardListAdapter adapter = new BoardListAdapter(this, boards);
		boardListView.setAdapter(adapter);
		// 新建Board按钮
		addBoardBtn = (Button)findViewById(R.id.add_board_btn);
		addBoardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		loadBoard();
	}

	/**
	 * 加载Board
	 */
	private void loadBoard() {
		Cursor cursor = dataManager.getAllBoardsCursor();
		
		if (cursor != null && cursor.moveToFirst()) {
			for (; !cursor.isAfterLast(); cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(BoardDBEntity._ID, cursor.getLong(cursor.getColumnIndexOrThrow(BoardDBEntity._ID)));
				values.put(BoardDBEntity.NAME, cursor.getString(cursor.getColumnIndexOrThrow(BoardDBEntity.NAME)));
				values.put(BoardDBEntity.CTIME, cursor.getLong(cursor.getColumnIndexOrThrow(BoardDBEntity.CTIME)));
				values.put(BoardDBEntity.MTIME, cursor.getLong(cursor.getColumnIndexOrThrow(BoardDBEntity.MTIME)));
				values.put(BoardDBEntity.OFF_X, cursor.getDouble(cursor.getColumnIndexOrThrow(BoardDBEntity.OFF_X)));
				values.put(BoardDBEntity.OFF_Y, cursor.getDouble(cursor.getColumnIndexOrThrow(BoardDBEntity.OFF_Y)));
				values.put(BoardDBEntity.SCALE, cursor.getDouble(cursor.getColumnIndexOrThrow(BoardDBEntity.SCALE)));
				values.put(BoardDBEntity.THUMB_SRC, cursor.getString(cursor.getColumnIndexOrThrow(BoardDBEntity.THUMB_SRC)));
				
				boards.add(values);
			}
		}
	}
	
	private void addBoard() {
		
	}
}

class BoardListAdapter extends BaseAdapter {

	private Context mContext;
	private List<ContentValues> mItems;
	private LayoutInflater mInflater;
	
	// 显示更多选项的数据位置
	private int morePos = -1;
	
	private OnBtnClickListener mListener = null;
	
	/**
	 * 点击事件监听器接口
	 * @author donie
	 *
	 */
	public interface OnBtnClickListener {
		public void onMoreBtnClick(int position);
		public void onEditBtnClick(int position);
		public void onDeleteBtnClick(int position);
	}
	
	public BoardListAdapter(Context c, List<ContentValues> items) {
		mContext = c;
		mItems = items;
		mInflater = LayoutInflater.from(mContext);
	}
	
	public void setOnBtnClickListener(OnBtnClickListener listener) {
		mListener = listener;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.board_item_layout, null);
		}
		
		TextView titleText = (TextView)convertView.findViewById(R.id.board_item_title);
		TextView descText = (TextView)convertView.findViewById(R.id.board_item_desc);
		
		titleText.setText(mItems.get(position).getAsString(BoardDBEntity.NAME));
		descText.setText(mItems.get(position).getAsLong(BoardDBEntity.CTIME) + "");
		
		final int pos = position;
		Button moreBtn = (Button)convertView.findViewById(R.id.board_item_more_btn);
		moreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (morePos == pos) {
					morePos = -1;
				} else {
					morePos = pos;
				}
				BoardListAdapter.this.notifyDataSetChanged();
			}
		});
		
		Button editBtn = (Button)convertView.findViewById(R.id.board_item_edit_btn);
		editBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onEditBtnClick(pos);
				}
			}
		});
		Button delBtn = (Button)convertView.findViewById(R.id.board_item_delete_btn);
		delBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onDeleteBtnClick(pos);
				}
			}
		});
		
		if (position == morePos) {
			LinearLayout editLayout = (LinearLayout)convertView.findViewById(R.id.board_item_edit_layout);
			editLayout.setVisibility(View.VISIBLE);
		} else {
			LinearLayout editLayout = (LinearLayout)convertView.findViewById(R.id.board_item_edit_layout);
			editLayout.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	
}
