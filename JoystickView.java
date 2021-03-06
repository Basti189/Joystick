/*
  Copyright (c) 2014 Philipp Mandler
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
*/


package de.wolfsline.catplaycontroller.Joystick;

import java.util.ArrayList;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.wolfsline.catplaycontroller.R;

public class JoystickView extends View {
	
	private Bitmap m_knob;
	private Bitmap m_bg;
	
	private float m_touch_x = 0;
	private float m_touch_y = 0;
	
	private float m_dragStart_x = 0;
	private float m_dragStart_y = 0;

	private float m_maxDistance;
	private float m_touchRadius;

	private int m_lockAxis = -1;
	
	private ArrayList<JoystickListener> m_listeners;

	private DisplayMetrics m_metrics = new DisplayMetrics();

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public JoystickView(Context context) {
		super(context);
		init();		
	}

	/**
	 * 0 = no lock
	 * 1 = lock X
	 * 2 = lock Y
	 * 3 = lock all
	 * @param axis
	 */
	public void lockAxis (int axis) {
		m_lockAxis = axis;
	}
	
	private void init() {
		m_listeners = new ArrayList<>();

		m_knob = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_knob);
		m_bg = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_bg);


	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getMetrics(m_metrics);

		m_maxDistance = 40 * m_metrics.density;
		m_touchRadius = 50 * m_metrics.density;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		
		canvas.drawBitmap(m_bg, canvas.getHeight() / 2 - m_bg.getHeight() / 2, canvas.getWidth() / 2 - m_bg.getWidth() / 2, null);
		canvas.drawBitmap(m_knob, m_touch_x + 40 * m_metrics.density, m_touch_y + 40 * m_metrics.density, null);
		
		canvas.restore();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		boolean accepted = false;
		
		float eventX = event.getX() - 130 * m_metrics.density;
		float eventY = event.getY() - 130 * m_metrics.density;
		
		if(event.getAction() == MotionEvent.ACTION_MOVE) {
			switch (m_lockAxis) {
				case 0:
					m_touch_x = eventX - m_dragStart_x;
					m_touch_y = eventY - m_dragStart_y;
					break;
				case 1:
					m_touch_x = 0;
					m_touch_y = eventY - m_dragStart_y;
					break;
				case 2:
					m_touch_x = eventX - m_dragStart_x;
					m_touch_y = 0;
					break;
				case 3: default:
					m_touch_x = 0;
					m_touch_y = 0;
			}
			
			if(Math.sqrt(Math.pow(m_touch_x, 2) + Math.pow(m_touch_y, 2)) > m_maxDistance) {
				double alpha = Math.asin(m_touch_y / Math.sqrt(Math.pow(m_touch_x, 2) + Math.pow(m_touch_y, 2)));
				m_touch_x = (float) (Math.cos(alpha) * m_maxDistance * Math.signum(m_touch_x));
				m_touch_y = (float) (Math.sin(alpha) * m_maxDistance);
			}
			onPositionChanged(true);
			accepted = true;
		}
		else if(event.getAction() == MotionEvent.ACTION_DOWN) {
			if(Math.sqrt(Math.pow(eventX, 2) + Math.pow(eventY, 2)) <= m_touchRadius) {
				m_dragStart_x = eventX;
				m_dragStart_y = eventY;
				m_touch_x = 0;
				m_touch_y = 0;
				onPositionChanged(true);
				accepted = true;
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_UP) {
			m_touch_x = 0;
			m_touch_y = 0;
			onPositionChanged(false);
			accepted = true;
		}
		
		invalidate();
		
		return accepted;
	}
	
	private void onPositionChanged(boolean isPushed) {
		for(JoystickListener listener : m_listeners) {
			listener.joystickPositionChanged(this, m_touch_x / m_maxDistance, -m_touch_y / m_maxDistance, isPushed);
		}
	}
	
	public void addListener(JoystickListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeListener(JoystickListener listener) {
		m_listeners.remove(listener);
	}

}
