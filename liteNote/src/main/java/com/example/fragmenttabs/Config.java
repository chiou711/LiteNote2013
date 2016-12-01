package com.example.fragmenttabs;


import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
// Config
	public class Config extends Fragment {

	private CheckBox chkDelWarn;
	private CheckBox chkDelPageWarn;

	SharedPreferences setting;
	
	public Config(){};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setContentView(R.layout.config);
		
		if(Build.VERSION.SDK_INT >= 11)
		{
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setting = getActivity().getSharedPreferences("delete_warn", 0);
		
		
		chkDelWarn = (CheckBox) getActivity().findViewById(R.id.chkDeleteWarn);
		chkDelPageWarn = (CheckBox) getActivity().findViewById(R.id.chkDeletePageWarn);
		
		// disable button 
		getActivity().findViewById(R.id.btnConfig).setVisibility(View.INVISIBLE); // 1 invisible
		
		//initialization: item
		if(setting.getString("KEY_DELETE_WARN","").equalsIgnoreCase("yes")){
			chkDelWarn.setChecked(true);
			setting.edit().putString("KEY_DELETE_WARN", "yes").commit();
		}else{
			chkDelWarn.setChecked(false);
			setting.edit().putString("KEY_DELETE_WARN", "no").commit();
		}
		
		//initialization: page
		if(setting.getString("KEY_DELETE_PAGE_WARN","").equalsIgnoreCase("yes")){
			chkDelPageWarn.setChecked(true);
			setting.edit().putString("KEY_DELETE_PAGE_WARN", "yes").commit();
		}else{
			chkDelPageWarn.setChecked(false);
			setting.edit().putString("KEY_DELETE_PAGE_WARN", "no").commit();
		}
		
		
//		addListenerOnButton();
		
		chkDelWarn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{     @Override
			   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
				   if(isChecked)
					   setting.edit().putString("KEY_DELETE_WARN", "yes").commit();
				   else
					   setting.edit().putString("KEY_DELETE_WARN", "no").commit();}
		});
		
		chkDelPageWarn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{	   @Override
			   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				   if(isChecked)
					   setting.edit().putString("KEY_DELETE_PAGE_WARN", "yes").commit();
				   else
					   setting.edit().putString("KEY_DELETE_PAGE_WARN", "no").commit();}
		});
	}
}