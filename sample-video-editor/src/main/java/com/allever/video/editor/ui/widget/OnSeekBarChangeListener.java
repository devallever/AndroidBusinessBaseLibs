package com.allever.video.editor.ui.widget;

public interface OnSeekBarChangeListener {
	
	/**
     * Notification that the progress level has changed. Clients can use the fromUser parameter
     * to distinguish user-initiated changes from those that occurred programmatically.
     * 
     * @param seekBar The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range 0..max where max
     *        was set by {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
	void onProgressChanged(CustomNumSeekBar seekBar, int progress, boolean fromUser);
    
    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar. 
     * @param seekBar The SeekBar in which the touch gesture began
     */
    void onStartTrackingTouch(CustomNumSeekBar seekBar);
    
    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar. 
     * @param seekBar The SeekBar in which the touch gesture began
     */
    void onStopTrackingTouch(CustomNumSeekBar seekBar);
}
