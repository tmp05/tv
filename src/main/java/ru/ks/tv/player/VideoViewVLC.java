package ru.ks.tv.player;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class VideoViewVLC extends SurfaceView implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener, VideoInterface {
    public final static String TAG = "Ks/VideoViewVLC";

    private SurfaceView mSurface;
    private SurfaceHolder holder;

    private LibVLC libvlc;
    private org.videolan.libvlc.MediaPlayer mMediaPlayer = null;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;

    TVController mTVController;
    VideoController mVideoController;
    Map<String, Object> mMap;
    private Context video_context;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    boolean stopped = false;

    private static final int SHOW_PROGRESS = 2;
    private static final int SURFACE_SIZE = 3;

    private static final int SURFACE_FROM_SETTINGS = 7;
    private int mCurrentSize = SURFACE_FROM_SETTINGS;

    String pref_aspect_ratio = "default";
    String pref_aspect_ratio_video = "default";


    public VideoViewVLC(Context context) {
        super(context);
        video_context = context;
        init();
    }

    private void init() {
        mSurface = this;
        holder = mSurface.getHolder();

        this.setFocusable(false);
        this.setClickable(false);
    }

    @Override
    public void setVideoAndStart(String address) {
        this.createPlayer(address);
    }

    @Override
    public void stop() {
        if(mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.stop();
        stopped = true;
    }

    @Override
    public void pause() {
        if(mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.pause();
    }

    @Override
    public void play() {
        if(mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.play();
    }

    @Override
    public void setTVController(TVController tc) {
        mTVController = tc;
        mTVController.setVideo(this);
    }

    @Override
    public void setVideoController(VideoController vc) {
        mVideoController = vc;
        mVideoController.setVideo(this);
    }

    @Override
    public void setMap(Map<String, Object> map) {
        mMap = map;
        if(mTVController != null) {
            mTVController.setMap(mMap);
        }
        if(mVideoController != null) {
            mVideoController.setMap(mMap);
        }
        getPrefs();
        stopped = false;
    }

    @Override
    public boolean isPlaying() {

        return mMediaPlayer.isPlaying();
    }

    @Override
    public boolean showOverlay() {
        mHandler.removeMessages(SHOW_PROGRESS);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        return true;
    }

    @Override
    public boolean hideOverlay() {
        return false;
    }

    @Override
    public int getProgress() {
        if(mMediaPlayer == null) {
            return 0;
        }
        return (int) mMediaPlayer.getTime();
    }

    @Override
    public int getLeight() {
        if(mMediaPlayer == null) {
            return 0;
        }
        return (int) mMediaPlayer.getLength();
    }

    @Override
    public int getTime() {
        if(mMediaPlayer == null) {
            return 0;
        }
        return (int) mMediaPlayer.getTime();
    }

    @Override
    public void setTime(int time) {
        if(mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setTime(time);
    }

    @Override
    public int changeSizeMode() {
        return 0;
    }

    @Override
    public String changeAudio() {
        return null;
    }

    @Override
    public String changeSubtitle() {
        return null;
    }

    @Override
    public int getAudioTracksCount() {
        return 0;
    }

    @Override
    public int getSpuTracksCount() {
        return 0;
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {

    }

    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {

    }

    @Override
    public int changeOrientation() {
        setSize();
        return 0;
    }

    @Override
    public void end() {
        if(mTVController != null) {
            mTVController.end();
        }
        if(mVideoController != null) {
            mVideoController.end();
        }

        if(mHandler != null) mHandler.removeMessages(SHOW_PROGRESS);
        releasePlayer();
    }
    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        Log.d("MyVLC", "CreatePlayer " + media);
        //releasePlayer();
        try {
            /*if (media.toString().length() > 0) {
                Toast toast = Toast.makeText(getContext(), media.toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }*/
            if(mMediaPlayer == null) {
                // Create LibVLC
                ArrayList<String> options = new ArrayList<String>();
                //options.add("--subsdec-encoding <encoding>");
                options.add("--aout=opensles");
                options.add("--audio-time-stretch"); // time stretching
                //options.add("-vvv"); // verbosity
                libvlc = new LibVLC(video_context, options);
                holder.setKeepScreenOn(true);
            } else {
                releaseMedia();
            }

            // Create media player
            mMediaPlayer = new org.videolan.libvlc.MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            URL url = new URL(media);
            Uri uri = Uri.parse(media);

            Log.d("MyVLC", "uri scheme " + uri.getScheme());

            Media m = new Media(libvlc, uri);
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();

            if (mOnLayoutChangeListener == null) {
                mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                    private final Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            setSize();
                        }
                    };
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right,
                                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                            mHandler.removeCallbacks(mRunnable);
                            mHandler.post(mRunnable);
                        }
                    }
                };
                mSurface.addOnLayoutChangeListener(mOnLayoutChangeListener);
            } else setSize();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error creating player! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void releaseMedia() {
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void releasePlayer() {
        if (libvlc == null)
            return;

        if (mOnLayoutChangeListener != null) {
            mSurface.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }

        releaseMedia();
        holder = null;
        libvlc.release();

        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    public void setSize() {
        if(mMediaPlayer == null) return;
        // get screen size
        int w = ((Activity)this.getContext()).getWindow().getDecorView().getWidth();
        int h = ((Activity)this.getContext()).getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // sanity check
        if (w * h == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(w, h);

        if (mVideoWidth * mVideoHeight == 0) {
            ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSurface.setLayoutParams(lp);
            if(pref_aspect_ratio_video.equals("default")) {
                mMediaPlayer.setAspectRatio(null);
            } else if(pref_aspect_ratio_video.equals("fullscreen")) {
                Log.d("test", "isPortrait + " + isPortrait);
                mMediaPlayer.setAspectRatio(isPortrait ? ""+h+":"+w : ""+w+":"+h);
            } else {
                mMediaPlayer.setAspectRatio(pref_aspect_ratio_video);
            }
            mMediaPlayer.setScale(0);
//            changeMediaPlayerLayout(w, h);
            return;
        }

        if(holder == null || mSurface == null)
            return;

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    private org.videolan.libvlc.MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static class MyPlayerListener implements org.videolan.libvlc.MediaPlayer.EventListener {
        private WeakReference<VideoViewVLC> mOwner;

        public MyPlayerListener(VideoViewVLC owner) {
            mOwner = new WeakReference<VideoViewVLC>(owner);
        }

        @Override
        public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
            VideoViewVLC player = mOwner.get();

            switch(event.type) {
                case org.videolan.libvlc.MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.Playing:
                    //Log.d(TAG, "Play");
                    break;
                case org.videolan.libvlc.MediaPlayer.Event.Paused:
                case org.videolan.libvlc.MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    private void getPrefs() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        pref_aspect_ratio = prefs.getString("aspect_ratio", "default");
        if(mMap.get("type").equals("video")) {
            pref_aspect_ratio_video = prefs.getString("aspect_ratio_video", "default");
        } else {
            pref_aspect_ratio_video = prefs.getString("aspect_ratio_tv", "default");
        }
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SURFACE_SIZE:
                    //changeSurfaceSize();
                    break;
                case SHOW_PROGRESS:
                    setOverlayProgress();
                    break;
            }
        }
    };

    private void setOverlayProgress() {
        if(mVideoController != null) {
            mVideoController.showProgress();
        }
        mHandler.removeMessages(SHOW_PROGRESS);
        Message msg = mHandler.obtainMessage(SHOW_PROGRESS);
        mHandler.sendMessageDelayed(msg, 1000);
        return;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mTVController!=null) {
            return mTVController.dispatchKeyEvent(event);
        }
        if(mVideoController!=null) {
            return mVideoController.dispatchKeyEvent(event);
        }
        return true;
    }
}
