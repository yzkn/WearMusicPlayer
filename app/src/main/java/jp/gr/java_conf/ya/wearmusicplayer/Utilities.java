package jp.gr.java_conf.ya.wearmusicplayer; //  Copyright (c) 2014 YA<ya.androidapp@gmail.com> All rights reserved.


public class Utilities {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public String milliSecondsToTimer(long milliseconds) {
        try {
            String finalTimerString = "";
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            String secondsString;
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }
            finalTimerString = finalTimerString + minutes + ":" + secondsString;
            return finalTimerString;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration c
     * @param totalDuration   t
     */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        try {
            long currentSeconds = (int) (currentDuration / 1000);
            long totalSeconds = (int) (totalDuration / 1000);
            Double percentage = (((double) currentSeconds) / totalSeconds) * 100;
            return percentage.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      p
     * @param totalDuration t
     *                      returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        try {
            totalDuration = (totalDuration / 1000);
            int currentDuration = (int) ((((double) progress) / 100) * totalDuration);
            return currentDuration * 1000;
        } catch (Exception e) {
            return 0;
        }
    }
}
