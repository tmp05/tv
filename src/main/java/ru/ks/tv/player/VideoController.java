package ru.ks.tv.player;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.kvlib.R;

import org.videolan1.vlc.Util;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class VideoController extends FrameLayout {
	static VideoInterface mVideo;

	ImageButton mPause;
	ImageButton mBackward;
	ImageButton mForward;
	SeekBar mSeekbar;
	TextView mTime;
	TextView mLeight;
	ImageButton mSize;
	ImageButton mAudio;
	ImageButton mSubtitle;
	ScheduledExecutorService service;
	Future<?> timer;

	int time = 0;

	private static final int CHECK_TRACKS = 42;
	private static final int UPDATE_REMOTE_PROGRESS = 43;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private static final int SURFACE_FROM_SETTINGS = 7;

	static Map<String, Object> mMap;

	public VideoController(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public VideoController(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public VideoController(Context context) {
		super(context);
		init();
	}

	private void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.kv_controller_video, this, true);
		mPause = (ImageButton)findViewById(R.id.player_overlay_play);
		mPause.setOnClickListener(listener);
		mBackward = (ImageButton)findViewById(R.id.player_overlay_backward);
		mBackward.setOnClickListener(listener);
		mForward = (ImageButton)findViewById(R.id.player_overlay_forward);
		mForward.setOnClickListener(listener);
		mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
		mSeekbar.setOnSeekBarChangeListener(mSeekListener);
		mTime = (TextView)findViewById(R.id.player_overlay_time);
		mLeight = (TextView)findViewById(R.id.player_overlay_length);
		mSize = (ImageButton)findViewById(R.id.player_overlay_size);
		mSize.setOnClickListener(listener);
		mAudio = (ImageButton)findViewById(R.id.player_overlay_audio);
		mAudio.setOnClickListener(listener);
		mSubtitle = (ImageButton)findViewById(R.id.player_overlay_subtitle);
		mSubtitle.setOnClickListener(listener);
		//mSubtitle.setVisibility(View.VISIBLE);
		service = Executors.newSingleThreadScheduledExecutor();
	}

	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//switch(v.getId()){
			if(v.getId()== R.id.player_overlay_subtitle) {
				//	case R.id.player_overlay_subtitle:
				((VideoActivity)getContext()).showInfo(mVideo.changeSubtitle(), 1000);
				//	break;
			} else if(v.getId() == R.id.player_overlay_audio) {
				//case R.id.player_overlay_audio:
				((VideoActivity)getContext()).showInfo(mVideo.changeAudio(), 1000);
				//	break;
			} else if(v.getId() == R.id.player_overlay_play) {
				//case R.id.player_overlay_play:
				if(mVideo.isPlaying()) {
					mVideo.pause();
					mPause.setBackgroundResource(R.drawable.ic_new_play);
					((VideoActivity)getContext()).showOverlay(false);
				} else {
					mVideo.play();
					mPause.setBackgroundResource(R.drawable.ic_new_pause);
				}
				//break;
			}
			else if(v.getId()==R.id.player_overlay_backward) {
				//case R.id.player_overlay_backward:
				goBackward();
				//break;
			} else if(v.getId()==R.id.player_overlay_forward) {
				//case R.id.player_overlay_forward:
				goForward();
				//	break;
			} else if(v.getId()==R.id.player_overlay_size) {
				//case R.id.player_overlay_size:
				String msg = "";
				switch(mVideo.changeSizeMode()) {
				case SURFACE_BEST_FIT:
					msg = "Оптимально";
					break;
				case SURFACE_FIT_HORIZONTAL:
					msg = "По горизонтали";
					break;
				case SURFACE_FIT_VERTICAL:
					msg = "По вертикали";
					break;
				case SURFACE_FILL:
					msg = "Заполнение";
					break;
				case SURFACE_16_9:
					msg = "16 на 9";
					break;
				case SURFACE_4_3:
					msg = "4 на 3";
					break;
				case SURFACE_ORIGINAL:
					msg = "По центру";
					break;
				case SURFACE_FROM_SETTINGS:
					msg = "Из настроек";
					break;
				}

				((VideoActivity)getContext()).showInfo(msg, 1000);
				//break;
			}
		}
	};

	Handler mHandler = new VideoControllerHandler(this);
	private static class VideoControllerHandler extends Handler {
		VideoController mTVController;

		VideoControllerHandler(VideoController tv) {
			super();
			mTVController = tv;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// case VideoController.FADE_OUT_INFO:
			//	mTVController.fadeOutInfo();
			//	break;
			case VideoController.CHECK_TRACKS:
				mTVController.setESTrackLists();
				break;
			case VideoController.UPDATE_REMOTE_PROGRESS: break;
			}
		}
	}

	private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			//   mDragging = true;
			//   showOverlay(OVERLAY_INFINITE);
			Log.i("Debug", "Юзер start touch ");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.i("Debug", "Юзер stop touch");
			//  mDragging = false;
			//  showOverlay();
			//  hideInfo();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			//Log.i("Debug", "кто-то перемотал видео");
			if (fromUser) {
				Log.i("Debug", "Юзер перемотал видео");
				//  mLibVLC.setTime(progress);
				//  setOverlayProgress();
				//  mTime.setText(Util.millisToString(progress));
				//  showInfo(Util.millisToString(progress));

				//	mLibVLC.setTime(progress);
				//    setOverlayProgress();
				mVideo.setTime(progress);
				showProgress();
			} else {
				mSeekbar.setMax(mVideo.getLeight());
				mSeekbar.setProgress(progress);
			}
		}
	};

	public void setVideo(VideoInterface video) {
		mVideo = video;
		mVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				((VideoActivity)getContext()).showInfo("Невозможно воспроизвести видео");
				((VideoActivity)getContext()).showOverlay(false);
				return true;
			}
		});
		if(mVideo.getClass().equals(VLCView.class)) {
			Drawable d = getResources().getDrawable(R.drawable.po_seekbar);
			mSeekbar.setProgressDrawable(d);
			d = getResources().getDrawable(R.drawable.ic_seekbar_thumb);
			//android:thumb="@drawable/ic_seekbar_thumb"
			mSeekbar.setThumb(d);
		} else if(mVideo.getClass().equals(AVideoView.class)) {}
	}

	public void showProgress() {
		/*Throwable trace = new Exception();
		Log.d("Debug", "Показ прогресса " + Util.millisToString(mVideo.getTime()), trace);*/
		mSeekListener.onProgressChanged(mSeekbar, mVideo.getProgress(), false);
		mTime.setText("" + Util.millisToString(mVideo.getTime()));
		mLeight.setText("" + Util.millisToString(mVideo.getLeight()));
		//Updater.updateProgress(id);
	}

	private void goBackward() {
		mVideo.setTime(mVideo.getTime() - 10000);
		showProgress();
	}

	private void goForward() {
		mVideo.setTime(mVideo.getTime() + 10000);
		showProgress();
	}
	String id;

	public void setMap(Map<String, Object> map) {
		time = 0;
		mMap = map;
		//SentProgressRunnable

		AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... arg0) {
				id = (String)mMap.get("id");
				Boolean rt = (Boolean)mMap.get("rt");
				mMap.remove("rt");
				if((rt == null && !(Boolean)mMap.get("request_time"))|| rt == false ) {
					//Log.i("Debug", "VideoController не запрашивать время " +rt);
					return 0;
				} else {
					//Log.i("Debug", "VideoController запрашивать время");
				}
				return 0;
			}

			protected void onPostExecute(Integer result) {
				time = result;
				mVideo.setVideoAndStart((String) mMap.get("uri"));
				//((VideoActivity)getContext()).showInfo("поставлено время " + Util.millisToString(time), 3000);
				mVideo.setTime(time);
				showProgress();
				//timer = service.scheduleAtFixedRate(new Updater.SentProgressRunnable(id),20,20, TimeUnit.SECONDS);
			}
		};

		task.execute();
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		//Log.d("Debug","нажата клавиша");
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_DEL:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				listener.onClick(mPause);
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				listener.onClick(mBackward);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				listener.onClick(mForward);
				return true;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				mVideo.stop();
				mPause.setBackgroundResource(R.drawable.ic_new_play);
				return true;
			default: Log.i("Debug", "Нажата клавиша: " + event.getKeyCode());
			}
		}
		return false;
	}

	public void checkTrack() {
		mHandler.removeMessages(CHECK_TRACKS);
		mHandler.sendEmptyMessageDelayed(CHECK_TRACKS, 2500);
	}

	private void setESTrackLists() {
		mAudio.setVisibility(View.GONE);
		if(mVideo.getAudioTracksCount()>2) {
			mAudio.setVisibility(View.VISIBLE);
		}
		mSubtitle.setVisibility(View.GONE);
		//Log.i("Debug", "Число дорожек субтитров " + mVideo.getSpuTracksCount());
		if(mVideo.getSpuTracksCount()>0) {
			mSubtitle.setVisibility(View.VISIBLE);
		} else {
			//mHandler.removeMessages(CHECK_TRACKS);
			//mHandler.sendEmptyMessageDelayed(CHECK_TRACKS, 2500);
		}
	}

	public void end() {
		timer.cancel(true);
		Log.i("Debug", "end");
	}
	public void next() {
		((VideoActivity)getContext()).onNext(false);
	}

}
