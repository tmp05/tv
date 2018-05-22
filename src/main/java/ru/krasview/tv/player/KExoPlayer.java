package ru.krasview.tv.player;

import java.util.Map;

import com.google.android.exoplayer2.C;
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
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
//import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.Player.EventListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Context;
import android.app.AlertDialog;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import static ru.krasview.tv.player.VideoController.mVideo;

public class KExoPlayer extends SurfaceView implements VideoInterface, EventListener {
	private SurfaceView mSurface;
	SimpleExoPlayer player;
    PlayerView simpleExoPlayerView;
	DataSource.Factory dataSourceFactory;
    DefaultTrackSelector trackSelector;

	TVController mTVController;
	VideoController mVideoController;
	Map<String, Object> mMap;
	public final static String TAG = "Krasview/KExoPlayer";

	String pref_aspect_ratio = "default";
	String pref_aspect_ratio_video = "default";

	public KExoPlayer(Context context, PlayerView view) {
		super(context);
		simpleExoPlayerView = view;
		init();
	}

	private void init() {
		mSurface = this;

		// 1. Create a default TrackSelector
		//BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		//TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(null);
		TrackSelection.Factory TrackSelectionFactory = new FixedTrackSelection.Factory();
		trackSelector = new DefaultTrackSelector(TrackSelectionFactory);
		trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage("ru"));

		// 3. Create the player
		player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
		simpleExoPlayerView.requestFocus();
		simpleExoPlayerView.setPlayer(player);

		//dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "krasview"), null);
		dataSourceFactory = new DefaultDataSourceFactory(getContext(), "http://kadu.ru", null);
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

	private void displayTrackSelector(Activity activity) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if(mappedTrackInfo != null) {
            Pair<AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog(activity, "Звуковая дорожка", trackSelector, 1);
            dialogPair.second.setShowDisableOption(true);
            //dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
            dialogPair.first.show();
        }
    }

	@Override
	public void setVideoAndStart(String address) {
        Log.d("ExoPlayer", "setVideoAndStart");
        Uri uri = Uri.parse(address);
        MediaSource mediaSource;
        if (address.indexOf("mpd") != -1) {
            mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                    .createMediaSource(uri);
        } else {
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            if (address.indexOf("t.kraslan.ru") != -1)
                extractorsFactory.setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS | DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .setExtractorsFactory(extractorsFactory)
                    .createMediaSource(uri);
        }
        if(player == null) return; // sanity check
        player.prepare(mediaSource);

        /*MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            int rendererIndex = 0;
            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
            SelectionOverride selectionOverride = new SelectionOverride(0, 0);
            trackSelector.setParameters(trackSelector.buildUponParameters()
                    .setSelectionOverride(rendererIndex, rendererTrackGroups, selectionOverride));
        }*/
		//Log.d(TAG, "after prepare");

		player.setPlayWhenReady(true);
		Log.d(TAG, "after play");
		player.addListener(this);

		// todo subtitles: https://github.com/google/ExoPlayer/issues/1183
	}

	@Override
	public void stop() {
		if(player != null) player.setPlayWhenReady(false);
	}

	@Override
	public void pause() {
		if(player != null) player.setPlayWhenReady(false);
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
		if(mVideoController != null) mVideoController.showProgress();
		return true;
	}

	@Override
	public boolean hideOverlay() {
		return false;
	}

	@Override
	public int getProgress() {
	    if(player == null) return 0;
		Log.d(TAG, "progress " + player.getCurrentPosition()); return (int)player.getCurrentPosition();
	}

	@Override
	public int getLeight() {
	    if(player == null) return 0;
		Log.d(TAG, "duration " + player.getDuration()); return (int)player.getDuration();
	}

	@Override
	public int getTime() {
	    if(player == null) return 0;
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
		return "Следующая дорожка";
	}

	@Override
	public String changeSubtitle() {
		return null;
	}

	@Override
	public int getAudioTracksCount() {
        int tracks = 0;
        if(trackSelector == null) return tracks;
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if(mappedTrackInfo != null) {
            for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                if(player.getRendererType(i) == C.TRACK_TYPE_AUDIO) tracks++;
            }
        }

        Log.d(TAG, "tracks: " + tracks);
		return tracks;
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
		if(player != null) {
            player.stop();
            player.release();
            player = null;
        }
	}

	// ExoPlayer.EventListener implementation

	@Override
	public void onLoadingChanged(boolean isLoading) {
		Log.d(TAG, "isLoading: " + isLoading);
		Log.d(TAG, "duration " + player.getDuration());
		if(isLoading) {setSize(); if(mVideoController!=null) {mVideoController.showProgress();}}
		// Do nothing.
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		Log.d(TAG, "playbackState: " + playbackState);
		Log.d(TAG, "duration " + player.getDuration());
		if (playbackState == 4) {
			//if(mTVController != null) mTVController.end();
			if(mVideoController != null) mVideoController.end();
			//end();
		}
	}

	@Override
	public void onPositionDiscontinuity(int reason) {
	}

	@Override
	public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
	}

	@Override public void onSeekProcessed() {
	}

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
		// Do nothing.
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
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
	@Override
	public void onRepeatModeChanged(int repeatMode) {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		//Log.d("Debug","нажата клавиша exo");
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            displayTrackSelector((VideoActivity) getContext());
            return true;
        }

        if(mTVController!=null) {
			return mTVController.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchKeyEvent(event)	;
		}
		if(mVideoController!=null) {
			return mVideoController.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchKeyEvent(event);
		}
		return true;
	}
}
