package snow.player.debug;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import snow.player.audio.AbstractMusicPlayer;
import snow.player.audio.ErrorCode;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkMusicPlayer extends AbstractMusicPlayer {

    private String TAG = "JkPlayer";
    private IjkMediaPlayer exoMediaPlayer;

    private final Context mContext;
    private final Uri mUri;

    @Nullable
    private OnCompletionListener mCompletionListener;

    @Nullable
    private OnStalledListener mStalledListener;

    @Nullable
    private OnErrorListener mErrorListener;
    @Nullable
    private OnRepeatListener mRepeatListener;

    private boolean mStalled;
    private boolean mInvalid;
    private boolean mLooping;

    public IjkMusicPlayer(Context context, Uri uri) {

        mContext = context;
        mUri = uri;

        exoMediaPlayer = new IjkMediaPlayer();
        mInvalid = false;
        exoMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            exoMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 0);
        }else{
            exoMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        }
        exoMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzemaxduration",100L);
        exoMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        exoMediaPlayer.setOption( IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        exoMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1);

        exoMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.e(TAG, "MediaPlayer Error[what: " + i + ", extra: " + i1 + "]");

                setInvalid();

                if (mErrorListener != null) {
                    mErrorListener.onError(IjkMusicPlayer.this, toErrorCode(i1));
                }
                return true;
            }
        });

        exoMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        setStalled(true);
                        return true;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        setStalled(false);
                        return true;
                }
                return false;
            }
        });

        exoMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                if (mLooping) {
                    iMediaPlayer.start();
                    notifyOnRepeat();
                    return;
                }

                notifyOnComplete();
            }
        });
    }

    private int toErrorCode(int extra) {
        switch (extra) {
            case IMediaPlayer.MEDIA_ERROR_IO:
            case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return ErrorCode.DATA_LOAD_FAILED;
            case IMediaPlayer.MEDIA_ERROR_MALFORMED:
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
            case -2147483648/*低级系统错误*/:
                return ErrorCode.PLAYER_ERROR;
            default:
                return ErrorCode.UNKNOWN_ERROR;
        }
    }

    private void notifyOnRepeat() {
        if (mRepeatListener != null) {
            mRepeatListener.onRepeat(this);
        }
    }

    private void notifyOnComplete() {
        if (mCompletionListener != null) {
            mCompletionListener.onCompletion(this);
        }
    }

    private void setStalled(boolean stalled) {
        mStalled = stalled;
        if (mStalledListener != null) {
            mStalledListener.onStalled(mStalled);
        }
    }

    private synchronized void setInvalid() {
        mInvalid = true;
    }

    @Override
    public void startEx() {
        exoMediaPlayer.start();
    }

    @Override
    public void pauseEx() {
        exoMediaPlayer.pause();
    }

    @Override
    public void stopEx() {
        exoMediaPlayer.stop();
    }

    @Override
    public void releaseEx() {
        setInvalid();
        if (exoMediaPlayer != null) {
            exoMediaPlayer.reset();
            exoMediaPlayer.release();
            exoMediaPlayer = null;
        }
    }

    @Override
    public void prepare() throws Exception {
        if (isInvalid()) {
            return;
        }
        try {
            exoMediaPlayer.setDataSource(mContext, mUri);
            exoMediaPlayer.prepareAsync();
        } catch (Exception e) {
            setInvalid();
            throw e;
        }
    }

    @Override
    public void setLooping(boolean looping) {
        mLooping = looping;
        exoMediaPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return exoMediaPlayer.isLooping();
    }

    @Override
    public boolean isStalled() {
        return mStalled;
    }

    @Override
    public boolean isPlaying() {
        return exoMediaPlayer.isPlaying();
    }

    @Override
    public int getDuration() {
        return (int) exoMediaPlayer.getDuration();
    }

    @Override
    public int getProgress() {
        return (int) exoMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        exoMediaPlayer.seekTo(pos);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        exoMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void setSpeed(float speed) {
        exoMediaPlayer.setSpeed(speed);
    }

    @Override
    public boolean isInvalid() {
        return mInvalid;
    }

    @Override
    public int getAudioSessionId() {
        return exoMediaPlayer.getAudioSessionId();
    }

    @Override
    public void setOnPreparedListener(@Nullable OnPreparedListener listener) {
        if (listener == null) {
            exoMediaPlayer.setOnPreparedListener(null);
            return;
        }

        exoMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                listener.onPrepared(IjkMusicPlayer.this);
            }
        });
    }

    @Override
    public void setOnCompletionListener(@Nullable OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    @Override
    public void setOnRepeatListener(@Nullable OnRepeatListener listener) {
        mRepeatListener = listener;
    }

    @Override
    public void setOnSeekCompleteListener(@Nullable OnSeekCompleteListener listener) {
        if (listener == null) {
            exoMediaPlayer.setOnSeekCompleteListener(null);
            return;
        }

        exoMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                listener.onSeekComplete(IjkMusicPlayer.this);
            }
        });
    }

    @Override
    public void setOnStalledListener(@Nullable OnStalledListener listener) {
        mStalledListener = listener;
    }

    @Override
    public void setOnBufferingUpdateListener(@Nullable OnBufferingUpdateListener listener) {
        if (listener == null) {
            exoMediaPlayer.setOnBufferingUpdateListener(null);
            return;
        }

        exoMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                listener.onBufferingUpdate(IjkMusicPlayer.this, i, true);
            }
        });
    }

    @Override
    public void setOnErrorListener(@Nullable OnErrorListener listener) {
        mErrorListener = listener;
    }
}
