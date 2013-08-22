package com.guojin.entities;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author jinux
 * 
 */
public class PictureEntity implements Entity {

	@Override
	public long getID() { return -1; }
	
	@Override
	public void setID(long id) {  }
	
	private static String TAG = "BoardPicture";

	private BoardEntity boardEntity;
	/**
	 * 图片位置以中心位置代表，是在board上的位置
	 */
	double centerXonBoard, centerYonBoard;
	/**
	 * 相对原图大小的缩放比例 newEdge/oldEdge
	 */
	float scale;
	/**
	 * 旋转角度
	 */
	float rotate;
	/**
	 * 原图像宽高
	 */
	float height, width;
	/**
	 * 原图像经board到屏幕缩放后的宽高
	 */
	private float swidth, sheight;

	/**
	 * 对图片进行矩阵变换
	 */
	private Matrix mMatrix;
	private Matrix bMatrix;
	private Bitmap mBitmap;

	/*
	 * 标志当前图片是否获取焦点
	 */
	boolean isFocused;

	public Paint boxPaint; // 控制框的paint
	private int touchWherre;// 记录触摸的位置
	private float touchPointSize = 30;
	private float strokeWidth = 5;
	// 当前四个顶点位置 中心位置 顶部位置
	private PointF sltp;
	private PointF srtp;
	private PointF srbp;
	private PointF slbp;
	private PointF stopp;
	private PointF sbotp;
	private PointF center;

	// 触点当前位置和按下位置
	private float cx;
	private float cy;
	private float sx;
	private float sy;

	// 因缩放而使中心点产生的偏移
	private float dx = 0;
	private float dy = 0;

	// 触摸位置标记
	final static int a_left_top = 0;
	final static int a_right_top = 1;
	final static int a_left_bottom = 2;
	final static int a_right_bottom = 3;
	final static int a_center = 4;
	final static int a_rotate_handle = 5;
	final static int a_delete_handle = 6;
	final static int a_out = 7;

	public PictureEntity(BoardEntity board, Bitmap b, float x, float y) {
		this.boardEntity = board;
		this.mBitmap = b;

		// 初始化
		double tmpXY[] = board.screenToBoardCoodTrans(x, y);
		this.centerXonBoard = tmpXY[0];
		this.centerYonBoard = tmpXY[1];
		this.scale = 1;
		this.rotate = 0;
		this.isFocused = false;
		this.height = b.getHeight();
		this.width = b.getWidth();

		boxPaint = new Paint();
		boxPaint.setStyle(Paint.Style.STROKE);
		boxPaint.setStrokeWidth(strokeWidth);
		boxPaint.setColor(Color.RED);
		boxPaint.setStrokeJoin(Paint.Join.ROUND);
		boxPaint.setStrokeCap(Paint.Cap.ROUND);
		boxPaint.setAntiAlias(true);

		mMatrix = new Matrix();
	}

	/**
	 * 移动一段距离x方向dx y方向dy
	 * 
	 * @param dx
	 * @param dy
	 */
	public void translate(float dx, float dy) {
		this.centerXonBoard = this.centerXonBoard + (dx);
		this.centerYonBoard = this.centerYonBoard + (dy);
		mMatrix.postTranslate(dx, dy);
	}

	/**
	 * 绘制图片框,包括控制点
	 * 
	 * @param c
	 *            Canvas
	 */
	private void drawPictureBox(Canvas c) {

		// 定制画笔并画出控制点
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.RED);
		c.drawCircle(sltp.x, sltp.y, touchPointSize, p);
		c.drawCircle(slbp.x, slbp.y, touchPointSize, p);
		c.drawCircle(srtp.x, srtp.y, touchPointSize, p);
		c.drawCircle(srbp.x, srbp.y, touchPointSize, p);
		c.drawCircle(stopp.x, stopp.y, touchPointSize, p);
		c.drawCircle(sbotp.x, sbotp.y, touchPointSize, p);
		// 框
		drawRect(sltp, srtp, srbp, slbp, c);

	}

	/**
	 * 有四个顶点画出矩形
	 * 
	 * @param ltp
	 *            左上角
	 * @param rtp
	 *            右上角
	 * @param rbp
	 *            右下角
	 * @param lbp
	 *            左下角
	 * @param c
	 */
	private void drawRect(PointF ltp, PointF rtp, PointF rbp, PointF lbp,
			Canvas c) {
		boxPaint.setStrokeWidth(strokeWidth);
		c.drawLine(ltp.x, ltp.y, rtp.x, rtp.y, boxPaint);
		c.drawLine(rtp.x, rtp.y, rbp.x, rbp.y, boxPaint);
		c.drawLine(rbp.x, rbp.y, lbp.x, lbp.y, boxPaint);
		c.drawLine(ltp.x, ltp.y, lbp.x, lbp.y, boxPaint);
	}

	/**
	 * 将x,y点绕centerX,centerY旋转一定角度
	 * 
	 * @param centerX
	 * @param centerY
	 * @param x
	 * @param y
	 * @param rotate
	 * @return 旋转后的点
	 */
	private PointF rotateByPointF(float centerX, float centerY, float x,
			float y, float rotate) {
		double angleHude = Math.toRadians(-rotate);/* 角度变成弧度 */
		PointF tmp = new PointF();
		tmp.x = (float) ((x - centerX) * Math.cos(angleHude) + (y - centerY)
				* Math.sin(angleHude) + centerX);
		tmp.y = (float) (-(x - centerX) * Math.sin(angleHude) + (y - centerY)
				* Math.cos(angleHude) + centerY);
		return tmp;
	}

	public void draw(Canvas c) {

		// 坐标转换
		transform();
		// 图片
		drawBitmap(c);
		// 如果是当前操作的图片，显示控制框
		if (isFocused) {
			drawPictureBox(c);
		}
	}

	private void transform() {
		// 把画板坐标转换为屏幕坐标
		transformFromBoard();

		// 缩放
		float width = this.swidth * scale;
		float height = this.sheight * scale;
		float x = this.center.x;
		float y = this.center.y;

		// 获取当前控制点的位置
		float tmpx = width / 2 + strokeWidth / 2;
		float tmpy = height / 2 + strokeWidth / 2;
		sltp = new PointF(x - tmpx, y - tmpy);
		srtp = new PointF(x + tmpx, y - tmpy);
		srbp = new PointF(x + tmpx, y + tmpy);
		slbp = new PointF(x - tmpx, y + tmpy);
		stopp = new PointF(x, y - tmpy);
		sbotp = new PointF(x, y + tmpy);

		// 添加角度变换
		sltp = rotateByPointF(x, y, sltp.x, sltp.y, this.rotate);
		srbp = rotateByPointF(x, y, srbp.x, srbp.y, this.rotate);
		slbp = rotateByPointF(x, y, slbp.x, slbp.y, this.rotate);
		srtp = rotateByPointF(x, y, srtp.x, srtp.y, this.rotate);
		stopp = rotateByPointF(x, y, stopp.x, stopp.y, this.rotate);
		sbotp = rotateByPointF(x, y, sbotp.x, sbotp.y, this.rotate);

		// 修正偏移
		sltp.x += dx;
		sltp.y += dy;
		srbp.x += dx;
		srbp.y += dy;
		slbp.x += dx;
		slbp.y += dy;
		srtp.x += dx;
		srtp.y += dy;
		stopp.x += dx;
		stopp.y += dy;
		sbotp.x += dx;
		sbotp.y += dy;
		center.x += dx;
		center.y += dy;

	}

	private void transformFromBoard() {
		// 转为屏幕上位置坐标
		center = boardEntity.boardToScreenCoodTrans(this.centerXonBoard,
				this.centerYonBoard);
		// 转为屏幕上宽高
		swidth = boardEntity.boardToScreenSizeTrans(this.width);
		sheight = boardEntity.boardToScreenSizeTrans(this.height);

		// 对图片随board进行缩放
		bMatrix = new Matrix();
		bMatrix.setScale((float) boardEntity.getTotalScale(),
				(float) boardEntity.getTotalScale());
	}

	private void drawBitmap(Canvas c) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		transformBitmapMatrix();
		c.drawBitmap(mBitmap, mMatrix, p);
	}

	private void transformBitmapMatrix() {
		float x = center.x - dx;
		float y = center.y - dy;

		mMatrix.reset();
		mMatrix.postConcat(bMatrix);
		mMatrix.postTranslate(x - this.swidth / 2, y - this.sheight / 2);
		mMatrix.postScale(this.scale, this.scale, x, y);
		mMatrix.postRotate(this.rotate, x, y);
		mMatrix.postTranslate(dx, dy);
	}

	// 通过uri获得bitmap
	public static Bitmap getBitmapFromUri(String uriString, Context context) {
		try {
			// 读取uri所在的图片
			Bitmap bitmap = BitmapFactory.decodeFile(uriString.substring(7));
			@SuppressWarnings("static-access")
			Bitmap tmp = bitmap.createScaledBitmap(bitmap,
					bitmap.getWidth() / 5, bitmap.getHeight() / 5, true);
			bitmap.recycle();
			bitmap = tmp;
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 判断一个点(x,y)是否在一条有向线的左边，这条线是从p1指向p2
	 * 
	 * @param p1
	 * @param p2
	 * @param x
	 * @param y
	 * @return 是否在左边
	 */
	private boolean isOnLeftHand(PointF p1, PointF p2, float x, float y) {
		float A = -(p2.y - p1.y);
		float B = p2.x - p1.x;
		float C = -(A * p1.x + B * p1.y);

		if (0 < A * x + B * y + C) {
			return true;
		}
		return false;
	}

	/**
	 * 判断一个点(x,y)是否在当前框之内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean containPointFInRect(float x, float y) {
		if (isOnLeftHand(sltp, srtp, x, y) && isOnLeftHand(srtp, srbp, x, y)
				&& isOnLeftHand(srbp, slbp, x, y)
				&& isOnLeftHand(slbp, sltp, x, y)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断点(x,y)是否在p点所在的touchPointFSize矩形范围内
	 * 
	 * @param p
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean containPointF(PointF p, float x, float y) {
		if (x > p.x - touchPointSize && x < p.x + touchPointSize
				&& y > p.y - touchPointSize && y < p.y + touchPointSize) {
			return true;
		}
		return false;
	}

	/**
	 * 当点下时触发事件
	 * 
	 * @param x
	 * @param y
	 */
	public void onTouchDown(float x, float y) {
		touchWherre = whereIs(x, y);
		if (touchWherre == a_out) {
			setFocus(false);
		} else {
			setFocus(true);
		}
	}

	/**
	 * 移动时出发一系列事件
	 * 
	 * @param sx
	 *            上次触发事件的位置
	 * @param sy
	 * @param x
	 *            本次的位置
	 * @param y
	 */
	public void onTouchMove(float sx, float sy, float x, float y) {
		if (touchWherre == a_center) {// 如果点击位置是控制框内部则设置box为红色，移动位置
			boxPaint.setColor(Color.RED);
			float dx = (float) boardEntity.screenToBoardSizeTrans(x - sx);
			float dy = (float) boardEntity.screenToBoardSizeTrans(y - sy);
			translate(dx, dy);
		} else {// 如果是控制点则设置box为黑色
			boxPaint.setColor(Color.BLACK);
			switch (touchWherre) {// 具体判断点击了哪个控制点
			case a_left_top:
			case a_right_top:
			case a_left_bottom:
			case a_right_bottom:
				computeScale(x, y, touchWherre);
				break;
			case a_rotate_handle:

				float l = this.sheight / 2;
				float b = (float) Math.sqrt(Math.pow(y - center.y, 2)
						+ Math.pow(x - center.x, 2));
				float sb = (float) Math.sqrt(Math.pow(sy - center.y, 2)
						+ Math.pow(sx - center.x, 2));

				float ny = (y - center.y) / b * l;
				float nx = (x - center.x) / b * l;
				float nsy = (sy - center.y) / sb * l;
				float nsx = (sx - center.x) / sb * l;

				float a = (float) Math.sqrt(Math.pow((ny - nsy), 2)
						+ Math.pow(nx - nsx, 2));

				float dAngle = (float) Math.toDegrees(Math.acos(((Math
						.pow(l, 2) * 2 - Math.pow(a, 2)) / (2 * l * l))));

				int sign = isOnLeftHand(center, new PointF(sx, sy), x, y) == true ? 1
						: -1;
				this.rotate += dAngle * sign;

				break;
			case a_delete_handle:
				boardEntity.delEntity(this);
				break;
			}
		}
	}

	private void computeScale(float x, float y, int vertex) {
		float ddx = 0, ddy = 0;

		switch (vertex) {
		case PictureEntity.a_right_bottom:
			ddx = x - this.sltp.x;
			ddy = y - this.sltp.y;
			break;
		case PictureEntity.a_right_top:
			ddx = x - this.slbp.x;
			ddy = y - this.slbp.y;
			break;
		case PictureEntity.a_left_bottom:
			ddx = x - this.srtp.x;
			ddy = y - this.srtp.y;
			break;
		case PictureEntity.a_left_top:
			ddx = x - this.srbp.x;
			ddy = y - this.srbp.y;
			break;

		}
		float d1 = (float) Math.sqrt(Math.pow(ddx, 2) + Math.pow(ddy, 2));
		float d2 = (float) Math.sqrt(Math.pow(this.swidth, 2)
				+ Math.pow(this.sheight, 2));
		float tmp = scale;
		scale = d1 / d2;
		Log.e(TAG, "scale=" + scale);

		// 中心偏移的距离
		double dl = (float) Math
				.sqrt(Math.pow(
						(this.scale * this.swidth - this.swidth * tmp) / 2, 2)
						+ Math.pow((this.scale * this.sheight - this.sheight
								* tmp) / 2, 2));
		// 中点与左上角连线与ｘ轴正向的夹角角度
		double wangle;
		if (vertex == PictureEntity.a_left_top
				|| vertex == PictureEntity.a_right_bottom) {
			wangle = -Math.toRadians(this.rotate)
					+ (Math.atan(this.width / this.height))
					+ Math.toRadians(90);
		} else {
			wangle = -Math.toRadians(this.rotate)
					- (Math.atan(this.width / this.height))
					+ Math.toRadians(90);
		}
		float sign = scale > tmp ? 1 : -1;
		if (vertex == PictureEntity.a_left_top
				|| vertex == PictureEntity.a_right_top) {
			dx += (float) (Math.cos(wangle) * dl) * sign;
			dy += -(float) (Math.sin(wangle) * dl) * sign;
		} else {
			dx += -(float) (Math.cos(wangle) * dl) * sign;
			dy += (float) (Math.sin(wangle) * dl) * sign;
		}
	}

	/**
	 * 判定所触的位置
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int whereIs(float x, float y) {

		if (containPointF(sltp, x, y)) {
			return a_left_top;
		} else if (containPointF(srtp, x, y)) {
			return a_right_top;
		} else if (containPointF(slbp, x, y)) {
			return a_left_bottom;
		} else if (containPointF(srbp, x, y)) {
			return a_right_bottom;
		} else if (containPointF(stopp, x, y)) {
			return a_rotate_handle;
		} else if (containPointF(sbotp, x, y)) {
			return a_delete_handle;
		}  else if (containPointFInRect(x, y)) {
			return a_center;
		} else {
			return a_out;
		}
	}

	/**
	 * 设置是否获取焦点
	 * 
	 * @param b
	 */
	public void setFocus(boolean b) {
		this.isFocused = b;

	}

	/**
	 * 判断点是否在控制点区域
	 * 
	 * @param cx
	 * @param cy
	 * @return
	 */
	public boolean containPointFInContronPointF(float cx, float cy) {
		if (containPointF(sltp, cx, cy) || containPointF(srtp, cx, cy)
				|| containPointF(slbp, cx, cy) || containPointF(srbp, cx, cy)
				|| containPointF(stopp, cx, cy)||containPointF(sbotp, cx, cy)) {
			return true;
		}
		return false;
	}

	@Override
	public void onEntityTouchEvent(MotionEvent event) {
		cx = event.getX();
		cy = event.getY();
		// 判断action
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			onTouchDown(cx, cy);
			sx = cx;
			sy = cy;
			boardEntity.invalidateView();
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(cx - sx) > 5 || Math.abs(cy - sy) > 5) {
				onTouchMove(sx, sy, cx, cy);
				sx = cx;
				sy = cy;
				boardEntity.invalidateView();
			}
			break;
		}
	}

	@Override
	public int getType() {
		return BoardEntity.TYPE_PIC_ENTITY;
	}

	@Override
	public boolean isInRange(float x, float y) {
		if (isFocused) {
			return containPointFInRect(x, y)
					|| containPointFInContronPointF(x, y);
		} else {
			return containPointFInRect(x, y);
		}
	}

	@Override
	public void removeFocus() {
		this.isFocused = false;
		// 请求重绘
		boardEntity.invalidateView();
	}
	
	@Override
	public ContentValues getContentValues() {
		// TODO Auto-generated method stub
		return null;
	}
}
