package jp.gr.java_conf.ya.wearmusicplayer; //  Copyright (c) 2014 YA<ya.androidapp@gmail.com> All rights reserved.

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private AudioManager am;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private Handler mHandler = new Handler();
    private ImageButton btnPlay;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private int currentSongIndex = 0;
    private MediaPlayer mp;
    private SeekBar songProgressBar;
    private TextView songCurrentDurationLabel;
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
                    playpause();
                } else if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    playpause();
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }

    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setContentView(R.layout.player);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
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

        // By default play first song
        if (songsList == null) {
            Toast.makeText(getApplicationContext(), "songsList is NULL", Toast.LENGTH_SHORT).show();
        } else if (songsList.size() > 0) {
            playSong(0);
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
                    Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                    mp.reset();
                    mp.setDataSource(songsList.get(songIndex).get("songPath"));
                    mp.prepare();
                    mp.start();
                    // Displaying Song title
                    String songTitle = songsList.get(songIndex).get("songTitle");
                    songTitleLabel.setText(songTitle);

                    // Changing Button Image to pause image
                    btnPlay.setImageResource(R.drawable.btn_pause);

                    // set Progress bar values
                    songProgressBar.setProgress(0);
                    songProgressBar.setMax(100);

                    // Updating progress bar
                    updateProgressBar();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void next() {
        if (!(songsList == null)) {
            try {
                if (songsList.size() > 0) {
                    // check if next song is there or not
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSong(0);
                        currentSongIndex = 0;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playpause() {
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.pause();
                    // Changing button image to play button
                    btnPlay.setImageResource(R.drawable.btn_play);
                } else {
                    mp.start();
                    // Changing button image to pause button
                    btnPlay.setImageResource(R.drawable.btn_pause);
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlayList() {
        try {
            Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
            startActivityForResult(i, 100);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Exception: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
