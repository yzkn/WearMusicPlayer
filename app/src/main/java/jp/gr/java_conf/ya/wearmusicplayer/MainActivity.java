package jp.gr.java_conf.ya.wearmusicplayer; //  Copyright (c) 2014 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private AudioManager am;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private Handler mHandler = new Handler();
    private ImageButton btnPlay;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private ImageView imageView;
    private int currentSongIndex = 0;
    private final int REQUEST_PLAYLIST = 100;
    private final int REQUEST_ENABLE_BT = 101;
    private MediaPlayer mp;
    private SeekBar songProgressBar;
    private final String mSongIndex = "songIndex";
    private final String mSongPath = "songPath";
    private final String mSongTitle = "songTitle";
    private TextView songCurrentDurationLabel;
    private TextView songArtistLabel;
    private TextView songTitleLabel;
    private TextView songTotalDurationLabel;
    private Utilities utils;

    public boolean dispatchKeyEvent(@NonNull KeyEvent ke) {
        try {
            if (ke.getAction() == KeyEvent.ACTION_DOWN) {
                int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (ke.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (currentVolume < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
                    }
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    if (currentVolume > 0) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
                    }
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
                    showPlayList();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    playpause();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    play();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    pause();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_STOP) {
                    stop();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    next();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    previous();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                    forward();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_REWIND) {
                    backward();
                }
            }
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } catch (Error e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        return super.dispatchKeyEvent(ke);
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_PLAYLIST) {
            currentSongIndex = data.getExtras().getInt(mSongIndex);
            // play selected song
            if (songsList == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.songlist_null), Toast.LENGTH_SHORT).show();
            } else if (songsList.size() > 0) {
                playSong(currentSongIndex);
            }
        } else if (resultCode == REQUEST_ENABLE_BT) {
            if (songsList == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.songlist_null), Toast.LENGTH_SHORT).show();
            } else if (songsList.size() > 0) {
                playSong(0);
            }
        }

    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (isBTEnabled()) {
            try {
                if (!(songsList == null)) {
                    if (songsList.size() > 0) {

                        if (isRepeat) {
                            playSong(currentSongIndex);
                        } else if (isShuffle) {
                            Random rand = new Random();
                            currentSongIndex = rand.nextInt((songsList.size() - 1) + 1);
                            playSong(currentSongIndex);
                        } else {
                            if (currentSongIndex < (songsList.size() - 1)) {
                                playSong(currentSongIndex + 1);
                                currentSongIndex = currentSongIndex + 1;
                            } else {
                                playSong(0);
                                currentSongIndex = 0;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
                songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

                int progress = utils.getProgressPercentage(currentDuration, totalDuration);
                songProgressBar.setProgress(progress);

                mHandler.postDelayed(this, 100);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    };

    private boolean isBTEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!(mBluetoothAdapter == null)) {
            if (mBluetoothAdapter.isEnabled()) {
                if (isContainBTHeadphone(mBluetoothAdapter)) {
                    return true;
                }
            }
            try {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isContainBTHeadphone(BluetoothAdapter mBluetoothAdapter) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getBluetoothClass().getDeviceClass() ==
                        BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO) {
                    return true;
                } else if (device.getBluetoothClass().getDeviceClass() ==
                        BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
                    return true;
                } else if (device.getBluetoothClass().getDeviceClass() ==
                        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
                    return true;
                } else if (device.getBluetoothClass().getDeviceClass() ==
                        BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER) {
                    return true;
                } else if (device.getBluetoothClass().getDeviceClass() ==
                        BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        setContentView(R.layout.player);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        imageView = (ImageView) findViewById(R.id.imageView);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songArtistLabel = (TextView) findViewById(R.id.songArtist);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        ImageButton btnAbout = (ImageButton) findViewById(R.id.about);
        ImageButton btnForward = (ImageButton) findViewById(R.id.btnForward);
        ImageButton btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        ImageButton btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        ImageButton btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);

        // am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
        if (am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
        }

        mp = new MediaPlayer();
        utils = new Utilities();

        songProgressBar.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener(this);

        SongsManager songManager = new SongsManager();
        if (!(songManager.getPlayList() == null)) {
            songsList = songManager.getPlayList();
        }

        // By default play first song if bluetooth headphone is enabled
        if (songsList == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.songlist_null), Toast.LENGTH_SHORT).show();
        } else if (songsList.size() > 0) {
            if (isBTEnabled()) {
                playSong(0);
            }
        }

        btnAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showAbout();
            }
        });

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                // Resume song
                playpause();
            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                forward();
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                backward();
            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                next();
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                previous();
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    if (isRepeat) {
                        isRepeat = false;
                        Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                        btnRepeat.setImageResource(R.drawable.btn_repeat);
                    } else {
                        isRepeat = true;
                        Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                        isShuffle = false;
                        btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                        btnShuffle.setImageResource(R.drawable.btn_shuffle);
                    }
                } catch (Exception e) {
                    // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    if (isShuffle) {
                        isShuffle = false;
                        Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                        btnShuffle.setImageResource(R.drawable.btn_shuffle);
                    } else {
                        isShuffle = true;
                        Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                        isRepeat = false;
                        btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                        btnRepeat.setImageResource(R.drawable.btn_repeat);
                    }
                } catch (Exception e) {
                    // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showPlayList();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            mp.release();
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        if (!(songsList == null)) {
            try {
                if (songsList.size() > 0) {
                    try {
                        mp.reset();
                        mp.setDataSource(songsList.get(songIndex).get(mSongPath));
                        mp.prepare();
                        mp.start();
                    } catch (Exception e) {
                    }

                    try {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(songsList.get(songIndex).get(mSongPath));
                        String songArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        songArtistLabel.setText(songArtist);
                        songTitleLabel.setText(songTitle);

                        try {
                            byte[] data = mmr.getEmbeddedPicture();
                            imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                        } catch (Exception e) {
                        }
                    } catch (Exception e) {
                        String songTitle = songsList.get(songIndex).get(mSongTitle);
                        songTitleLabel.setText(songTitle);
                    }

                    try {
                        // Changing Button Image to pause image
                        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                    } catch (Exception e) {
                    }

                    try {
                        // set Progress bar values
                        songProgressBar.setProgress(0);
                        songProgressBar.setMax(100);

                        // Updating progress bar
                        updateProgressBar();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void backward() {
        if (!(mp == null)) {
            try {
                int currentPosition = mp.getCurrentPosition();
                final int seekBackwardTime = 10000;
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void forward() {
        if (!(mp == null)) {
            try {
                int currentPosition = mp.getCurrentPosition();
                final int seekForwardTime = 10000;
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void next() {
        if (!(songsList == null)) {
            try {
                if (songsList.size() > 0) {
                    try {
                        // check if next song is there or not
                        if (currentSongIndex < (songsList.size() - 1)) {
                            try {
                                playSong(currentSongIndex + 1);
                                currentSongIndex = currentSongIndex + 1;
                            } catch (Exception e) {
                            }
                        } else {
                            // play first song
                            playSong(0);
                            currentSongIndex = 0;
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + " [1] " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void play() {
        if (mp != null) {
            try {
                if (!mp.isPlaying()) {
                    mp.start();
                    // Changing button image to pause button
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pause() {
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.pause();
                    // Changing button image to play button
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playpause() {
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.pause();
                    // Changing button image to play button
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mp.start();
                    // Changing button image to pause button
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void previous() {
        if (!(songsList == null)) {
            try {
                if (songsList.size() > 0) {
                    if (currentSongIndex > 0) {
                        playSong(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        // play last song
                        playSong(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
                }
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAbout() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.copyright)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlayList() {
        try {
            Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
            startActivityForResult(i, REQUEST_PLAYLIST);
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stop() {
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.pause();
                }
                mp.seekTo(0);
            } catch (Exception e) {
                // Toast.makeText(getApplicationContext(), getString(R.string.exception) + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
