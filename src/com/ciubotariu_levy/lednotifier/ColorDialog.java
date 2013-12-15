package com.ciubotariu_levy.lednotifier;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;

public class ColorDialog extends DialogFragment {

	//0-255
	int red;
	int green;
	int blue;

	//Bars for selecting RGB
	SeekBar redBar;
	SeekBar greenBar;
	SeekBar blueBar;

	//0xFF000000 to 0xFFFFFFFF
	int color;

	ColorView sample;


	public interface OnColorChosenListener {
		public void onColorChosen (int color, String lookupKey);
	}

	private final static String LOOKUP_KEY_VALUE = "row_id";
	private final static String USER_COLOR = "user_color";

	public static ColorDialog getInstance (String lookupKey, int color){
		ColorDialog dialog = new ColorDialog ();
		Bundle args = new Bundle();
		args.putString (LOOKUP_KEY_VALUE, lookupKey);
		args.putInt (USER_COLOR, color);		
		dialog.setArguments(args);
		dialog.color = color;
		dialog.red = Color.red(color);
		dialog.green = Color.green(color);
		dialog.blue = Color.blue(color);
		return dialog;
	}

	public ColorDialog(){
		//Required Empty Constructor
	}

	public void setRed(int red){
		this.red = red;
		updateColor();
	}

	public void setBlue(int blue){
		this.blue = blue;
		updateColor();
	}

	public void setGreen(int green){
		this.green = green;
		updateColor();
	}

	public void updateColor(){
		color = Color.argb(255, red, green, blue);
		sample.setColor(color);
		onColorChosen(color);
	}

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		view.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onColorChosen (Color.MAGENTA);
				dismiss();
			}
		});
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.color_dialog, container);
		sample = (ColorView) view.findViewById(R.id.color_view);
		sample.lockWidth=true;
		sample.setColor(color);
		redBar = (SeekBar)view.findViewById(R.id.red);
		redBar.setMax(255);
		redBar.setProgress(red);
		redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setRed(progress);
			}
		});				
		greenBar = (SeekBar)view.findViewById(R.id.green);
		greenBar.setMax(255);
		greenBar.setProgress(green);
		greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {				
				setGreen(progress);
			}
		});
		blueBar = (SeekBar)view.findViewById(R.id.blue);
		blueBar.setMax(255);
		blueBar.setProgress(blue);
		blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setBlue(progress);
			}
		});
		return view;
	}

	//called when user chooses a color
	private void onColorChosen (int color){
		OnColorChosenListener listener = null;
		try{
			listener =(OnColorChosenListener) getParentFragment();

		}
		catch (ClassCastException e){
			e.printStackTrace();
		}
		if (listener != null){
			listener.onColorChosen(color, getArguments().getString(LOOKUP_KEY_VALUE));
		}
	}
}
