package com.example.fragmenttabs;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ConfigAct extends Activity {
//	public class ConfigAct extends Fragment {

	private CheckBox chkDelWarn;
	private CheckBox chkDelPageWarn;
	private Button btnBack;

	SharedPreferences setting;
	
	public ConfigAct(){};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		if(Build.VERSION.SDK_INT >= 11)
		{
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setting = getSharedPreferences("delete_warn", 0);
		
		
		chkDelWarn = (CheckBox) findViewById(R.id.chkDeleteWarn);
		chkDelPageWarn = (CheckBox) findViewById(R.id.chkDeletePageWarn);
		
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
		
		
		addListenerOnButton();
		
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

	public void addListenerOnButton(){
		btnBack = (Button) findViewById(R.id.btnConfig);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	    		finish();
	    		Intent intent=new Intent(getBaseContext(),FragmentTabsHost.class);
	    		startActivity(intent);
			}
		});
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    switch (item.getItemId()) {
//	    // Respond to the action bar's Up/Home button
//	    case android.R.id.home:
//	    return super.onOptionsItemSelected(item);
//	}
//		return false;
//	}
}