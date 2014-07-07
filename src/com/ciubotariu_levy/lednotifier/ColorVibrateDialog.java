package com.ciubotariu_levy.lednotifier;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciubotariu_levy.lednotifier.ColorWheel.ColorListener;
import com.larswerkman.holocolorpicker.LinearColorPicker;
import com.larswerkman.holocolorpicker.OnColorChangedListener;
import com.makeramen.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class ColorVibrateDialog extends DialogFragment implements ColorListener, OnColorChangedListener {
	//0xFF000000 to 0xFFFFFFFF
	private int color;
	private int originalColor;

	private LinearColorPicker picker;

	private String vibratePattern;
	private String prevVibratePattern;
	private CircularColorView colorState;
	
	public interface ContactDetailsUpdateListener {
		public void onContactDetailsUpdated (String lookupKey, int color, String vibratePattern);
	}

	private static final String LOOKUP_KEY_VALUE = "lookup_key";
	private static final String CONTACT_ID = "_id";
	private static final String USER_NAME = "user_name";
	private static final String USER_NUM = "user_number";
	private static final String USER_COLOR = "user_color";
	private static final String USER_CURRENT_COLOR = "user_color";
	private static final String USER_CUSTOM_VIB = "custom_vibrate_pattern";
	

	public static ColorVibrateDialog getInstance (String name, String number, String lookupKey,long id, int color,String vibratePattern){
		ColorVibrateDialog dialog = new ColorVibrateDialog ();
		Bundle args = new Bundle();
		args.putString(USER_NAME, name);
		args.putString(USER_NUM, number);
		args.putString (LOOKUP_KEY_VALUE, lookupKey);
		args.putLong(CONTACT_ID, id);
		args.putInt (USER_COLOR, color);		
		args.putString(USER_CUSTOM_VIB, vibratePattern);
		dialog.setArguments(args);
		return dialog;
	}

	public ColorVibrateDialog(){
		//Required Empty Constructor
	}

	@Override
	public void setColor(int color){
		this.color = color;
	}
	
	@Override
	public void onColorChanged(int color) {
		this.color = color;
		colorState.setColor(color);
	}

	@Override
	public void onViewCreated (final View view, Bundle savedInstanceState){
		view.findViewById(R.id.submit_color).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				vibratePattern = null;
				if (((CheckBox)view.findViewById(R.id.vibrate_checkbox)).isChecked()){
					vibratePattern = ((EditText)view.findViewById(R.id.vib_input)).getText().toString().trim();
				}
				onConfirm (color,vibratePattern);
				dismiss();
			}
		});
		view.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onConfirm (originalColor,prevVibratePattern);
				dismiss();
			}
		});
		view.findViewById(R.id.reset_color).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onColorChanged (Color.GRAY);
			}
		});
	}

	private String getString (Bundle b, String key, String defValue){ //method not available on API 11 and below
		if (b.getString(key) == null){
			return defValue;
		}
		
		return b.getString(key);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.color_vibrate_dialog, container,false);
		Bundle args = getArguments();
		
		((TextView)view.findViewById(R.id.contact_name)).setText (getString(args,USER_NAME, ""));
		((TextView)view.findViewById(R.id.contact_number)).setText (getString(args,USER_NUM, ""));
		
		Transformation transformation = new RoundedTransformationBuilder()
        .borderColor(Color.BLACK)
        .borderWidthDp(0)
        .cornerRadiusDp(30)
        .oval(false)
        .build();
		Uri contactUri = Contacts.getLookupUri(args.getLong(CONTACT_ID), getString(args, LOOKUP_KEY_VALUE, ""));
		ImageView contactPic = (ImageView) view.findViewById(R.id.contact_image);
		Picasso.with(getActivity())
	    .load(contactUri)
	    .placeholder(R.drawable.contact_picture_placeholder)
	    .fit()
	    .transform(transformation)
	    .into(contactPic);
		
		originalColor = args.getInt(USER_COLOR,Color.GRAY);
		prevVibratePattern = args.getString(USER_CUSTOM_VIB);
		color = originalColor;
		vibratePattern = prevVibratePattern;
		if (savedInstanceState != null){
			color = savedInstanceState.getInt(USER_CURRENT_COLOR, originalColor);
			vibratePattern = savedInstanceState.getString(USER_CUSTOM_VIB);
		}	
		colorState = (CircularColorView) view.findViewById(R.id.contact_display_color);
		colorState.setColor(color);
		picker = (LinearColorPicker) view.findViewById(R.id.colorbar);
		picker.setColor(color);	
		picker.setOnColorChangedListener(this);
		final View vibrateHint = view.findViewById(R.id.vib_hint);
		final EditText vibrateInput = (EditText) view.findViewById(R.id.vib_input);
		vibrateInput.setMaxHeight(vibrateInput.getHeight());
		CheckBox c = (CheckBox) view.findViewById(R.id.vibrate_checkbox);
		c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					vibrateHint.setVisibility(View.VISIBLE);
					vibrateInput.setVisibility(View.VISIBLE);
					if (!TextUtils.isEmpty(vibratePattern)){
						vibrateInput.setText(vibratePattern);
						vibrateInput.setSelection(vibratePattern.length());
					}
				}
				else{
					vibrateHint.setVisibility(View.GONE);
					vibrateInput.setVisibility(View.GONE);
				}
				
			}
		});
		c.setChecked(!TextUtils.isEmpty(vibratePattern));
		return view;
	}
	
	public void onSaveInstaceState (Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putInt(USER_CURRENT_COLOR, color);
		outState.putString(USER_CUSTOM_VIB, vibratePattern);
	}

	//called when user chooses a color
	private void onConfirm (int color, String vibrate){
		ContactDetailsUpdateListener listener = null;
		try{
			listener =(ContactDetailsUpdateListener) getParentFragment();
		}
		catch (ClassCastException e){
			e.printStackTrace();
		}
		if (listener == null){
			try{
				listener = (ContactDetailsUpdateListener) getActivity();
			}
			catch (ClassCastException e){
				e.printStackTrace();
			}
		}
		if (listener != null){
			listener.onContactDetailsUpdated(getArguments().getString(LOOKUP_KEY_VALUE), color, vibrate);
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog){
		super.onCancel(dialog);
		finishHostActivity();
	}

	@Override
	public void onDismiss(DialogInterface dialog){
		super.onDismiss(dialog);
		finishHostActivity();
	}

	private void finishHostActivity(){
		if (getArguments().getString(LOOKUP_KEY_VALUE) == null && getActivity() != null){
			getActivity().finish();
		}
	}
}
