package ru.krasview.tv.player;

import java.util.Map;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Context;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static ru.krasview.tv.player.VideoController.mVideo;

public class KExoPlayer extends SurfaceView implements VideoInterface, ExoPlayer.EventListener {
	private SurfaceView mSurface;
	SimpleExoPlayer player;
	SimpleExoPlayerView simpleExoPlayerView;
	DataSource.Factory dataSourceFactory;

	TVController mTVController;
	VideoController mVideoController;
	Map<String, Object> mMap;
	public final static String TAG = "Krasview/KExoPlayer";

	String pref_aspect_ratio = "default";
	String pref_aspect_ratio_video = "default";

	public KExoPlayer(Context context, SimpleExoPlayerView view) {
		super(context);
		simpleExoPlayerView = view;
		init();
	}

	private void init() {
		mSurface = this;

		// 1. Create a default TrackSelector
		//BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(null);
		TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		// 3. Create the player
		player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
		//SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
		simpleExoPlayerView.requestFocus();
		simpleExoPlayerView.setPlayer(player);

		dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "krasview"), null);
	}

	private void setSize() {
		if(pref_aspect_ratio_video.equals("default")) {
			simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
		} else if(pref_aspect_ratio_video.equals("fullscreen")) {
			simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
		} else {
			simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
		}
		calcSize();
	}

	private void calcSize() {
		// get screen size
		int w = ((Activity)this.getContext()).getWindow().getDecorView().getWidth();
		int h = ((Activity)this.getContext()).getWindow().getDecorView().getHeight();
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		// sanity check
		if (w * h == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}
		if (w > h && isPortrait || w < h && !isPortrait) {
			int d = w;
			w = h;
			h = d;
		}

		double ar = 1;
		double dar = (double) w / (double) h;
		//double mult = dar;
		if(pref_aspect_ratio_video.equals("4:3")) {
			ar = 4.0 / 3.0;
			//ar = ar/mult*dar;
			if (dar < ar)
				h = (int) (w / ar);
			else
				w = (int) (h * ar);
		}
		if(pref_aspect_ratio_video.equals("16:9")) {
			ar = 16.0 / 9.0;
			//ar = ar/mult*dar;
			if (dar < ar)
				h = (int) (w / ar);
			else
				w = (int) (h * ar);
		}
		getHolder().setFixedSize(w, h);
		forceLayout();
		invalidate();
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
		Log.d(TAG, "aspect ratio: " + pref_aspect_ratio_video);
	}

	@Override
	public void setVideoAndStart(String address) {
		Log.d("ExoPlayer", "setVideoAndStart");
		Uri uri = Uri.parse(address);
		MediaSource mediaSource;
		if(address.indexOf("mpd") != -1) {
			mediaSource = new DashMediaSource(uri, dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
		} else {
			DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
			if(address.indexOf("t.kraslan.ru") != -1)
				extractorsFactory.setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS | DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES);
			mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
		}
		player.prepare(mediaSource);
		//Log.d(TAG, "after prepare");
		player.setPlayWhenReady(true);
		Log.d(TAG, "after play");
		player.addListener(this);
	}

	@Override
	public void stop() {
		player.setPlayWhenReady(false);
	}

	@Override
	public void pause() {
		player.setPlayWhenReady(false);
	}

	@Override
	public void play() {
		player.setPlayWhenReady(true);
		Log.d(TAG, "play");
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
		Log.d(TAG, "setMap");
		mMap = map;
		if(mTVController != null) {
			mTVController.setMap(mMap);
		}
		if(mVideoController != null) {
			mVideoController.setMap(mMap);
		}
		getPrefs();
	}

	@Override
	public boolean isPlaying() {
		Log.d(TAG, "isPlaying");
		return player.getPlayWhenReady();
	}

	@Override
	public boolean showOverlay() {
		return true;
	}

	@Override
	public boolean hideOverlay() {
		return false;
	}

	@Override
	public int getProgress() {
		return (int)player.getCurrentPosition();
	}

	@Override
	public int getLeight() {
		return (int)player.getDuration();
	}

	@Override
	public int getTime() {
		return (int)player.getCurrentPosition();
	}

	@Override
	public void setTime(int time) {
		player.seekTo(time);
	}

	@Override
	public int changeSizeMode() {
		setSize();
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
		player.stop();
		player.release();
		player = null;
	}

	// ExoPlayer.EventListener implementation

	@Override
	public void onLoadingChanged(boolean isLoading) {
		Log.d(TAG, "isLoading: " + isLoading);
		if(isLoading) setSize();
		// Do nothing.
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		Log.d(TAG, "playbackState: " + playbackState);
		if (playbackState == ExoPlayer.STATE_ENDED) {
		}
	}

	@Override
	public void onPositionDiscontinuity() {
	}

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
		// Do nothing.
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest) {
		// Do nothing.
	}

	@Override
	public void onPlayerError(ExoPlaybackException e) {
		String errorString = null;
		if (e.type == ExoPlaybackException.TYPE_RENDERER) {
			Exception cause = e.getRendererException();
			if (cause instanceof DecoderInitializationException) {
				// Special case for decoder initialization failures.
				DecoderInitializationException decoderInitializationException =
						(DecoderInitializationException) cause;
				if (decoderInitializationException.decoderName == null) {
					if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
						errorString = "error_querying_decoders";
					} else if (decoderInitializationException.secureDecoderRequired) {
						errorString = "secureDecoderRequired";
					} else {
						errorString = "Decoder not found";
					}
				} else {
					errorString = "error_instantiating_decoder";
				}
			}
		}
		if (errorString != null) {
			Toast.makeText(getContext(), "Player error! " + errorString, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

	}
}
