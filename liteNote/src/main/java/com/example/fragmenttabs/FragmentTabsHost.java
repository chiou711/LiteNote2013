package com.example.fragmenttabs;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.EditText;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Switching between the tabs of a TabHost through fragments, using FragmentTabHost.
 */
public class FragmentTabsHost extends FragmentActivity 
{
    private FragmentTabHost mTabHost; 
    static int mTabCount = 5;
	String TAB_SPEC_PREFIX = "tab";
	String TAB_SPEC;
	boolean bTabNameByDefault = true; 
	// for DB
	private static DB mDbHelper;
	private static Cursor mNotesCursor;
	
	private static SharedPreferences lastPageViewPreferences;
	private static SharedPreferences setting;
	private static int mFinalTabIndex;
	private static int mCurrentTabIndex;
	private ArrayList<String> tabIndicatorArrayList = new ArrayList<String>();
	private static Context mContext;
	private static int mFirstExistTabId =0;
	private static int mLastExistTabId =0;
	private static HorizontalScrollView mHorScrollView;
	private static Menu mMenu;
	@Override
    protected void onCreate(final Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        System.out.println("================start==================");
        // set content view
        setContentView(R.layout.activity_main);
        
        // set overflow menu
        setOverflowMenu();
        
        // declare tab widget
        TabWidget tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        
        // declare linear layout
        LinearLayout linearLayout = (LinearLayout) tabWidget.getParent();
        
        // set horizontal scroll view
        HorizontalScrollView horScrollView = new HorizontalScrollView(this);
//        mHorScrollView = horScrollView;
        horScrollView.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(horScrollView, 0);
        linearLayout.removeView(tabWidget);
        horScrollView.addView(tabWidget);
        horScrollView.setHorizontalScrollBarEnabled(true); //set scroll bar
        horScrollView.setHorizontalFadingEdgeEnabled(true); // set fading edge
        mHorScrollView = horScrollView;

		// horizontal scroll tab host
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.getTabWidget().setStripEnabled(false);//no use???
        
        //set tab indicator
    	setTabIndicator();

    	// set listener
    	setTabListener();
    	
    	// set global context
    	mContext = this.getBaseContext();
    	
    	// set divider
    	if(Build.VERSION.SDK_INT >= 11)
    	{
        	mTabHost.getTabWidget().setDividerDrawable(R.drawable.ic_tab_divider);
    	    mTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
    	}
    } 
    
	/**
	 * set tab indicator
	 * 
	 */
	protected void setTabIndicator()
	{
    	// set default tab indicator
    	if(bTabNameByDefault)
    	{
	        // get last view tab
	        lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
	        String strFinalPageViewNum = lastPageViewPreferences.getString("KEY_LAST_PAGE_VIEW","");
	        System.out.println("strLastPageViewNum = " + strFinalPageViewNum);
	        if(strFinalPageViewNum.equalsIgnoreCase("") )
	        {
		        // set default tab : first existence of tab
	        	strFinalPageViewNum = "1"; //initialization
	        }
	        else
	        {
	        	strFinalPageViewNum = lastPageViewPreferences.getString("KEY_LAST_PAGE_VIEW","");
	        }
	        
			Context context = getApplicationContext();
			
			mDbHelper = new DB(context);
			DB.setTableNumber(strFinalPageViewNum);
			
			mDbHelper.open();
			mNotesCursor = mDbHelper.getAllTab();

			// insert when table is empty, activated only for the first time 
			if(mNotesCursor.getCount() == 0)
			{
				mDbHelper.insertTab("TAB_INFO","N1"); 
				mDbHelper.insertTab("TAB_INFO","N2"); 
				mDbHelper.insertTab("TAB_INFO","N3"); 
				mDbHelper.insertTab("TAB_INFO","N4"); 
				mDbHelper.insertTab("TAB_INFO","N5"); 
				mNotesCursor = mDbHelper.getAllTab();
			}
			
			mTabCount = mNotesCursor.getCount();
			
			// get first tab id and last tab id
			int i = 0;
			while(i < mTabCount)
	    	{
	    		mNotesCursor.moveToPosition(i);
	    		String strTabName = mNotesCursor.getString(mNotesCursor.getColumnIndex("tab_name"));
	    		tabIndicatorArrayList.add(i,strTabName);  
	    		
				if(mNotesCursor.isFirst())
				{
					mFirstExistTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")) ;
				}
				if(mNotesCursor.isLast())
				{
					mLastExistTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")) ;
				}
				i++;
	    	}
	    	
			// get final view tab id of last time
	        mNotesCursor = mDbHelper.getAllTab();
			for(int iPosition =0;iPosition<mTabCount;iPosition++)
			{
				mNotesCursor.moveToPosition(iPosition);
				if(Integer.valueOf(strFinalPageViewNum) == 
						mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")))
				{
					mFinalTabIndex = iPosition;	// mLastTabIndex starts from 0
//					System.out.println("mLastTabIndex = " + mLastTabIndex);
				}
			}
			System.out.println("before DB close error");//not always occurs
			mDbHelper.close(); //???
//	    	System.out.println("_setTabIndicator mLastTabIndex = " + mLastTabIndex);
    	}
    	else
    	{
    		tabIndicatorArrayList.add(0,"購物");
    		tabIndicatorArrayList.add(1,"待辦");
    		tabIndicatorArrayList.add(2,"普通");
    		tabIndicatorArrayList.add(3,"重要");
    		tabIndicatorArrayList.add(4,"極重要");
    	}
    	
    	//add tab
        mDbHelper.open();
        mNotesCursor = mDbHelper.getAllTab();
        mTabHost.getTabWidget().setStripEnabled(false);
        int i = 0;
        while(i < mTabCount)
        {
        	mNotesCursor.moveToPosition(i);
        	int tabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
//        	System.out.println("_setListener tabId =" + tabId);
        	TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(tabId));
//        	System.out.println("tabIndicatorArrayList.get(i) = " + tabIndicatorArrayList.get(i));

            mTabHost.addTab(mTabHost
					.newTabSpec(TAB_SPEC)
					.setIndicator(tabIndicatorArrayList.get(i)),
    				NoteFragment.class, //interconnection
    				null);
            
            //set text color
	        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.rgb(0, 0, 0));

	        
	        //unselected background color
//            Drawable draw = getResources().getDrawable(R.drawable.ic_btn_square_unsel);
//            if(Build.VERSION.SDK_INT >= 16)
//            	mTabHost.getTabWidget().getChildAt(i).setBackground(draw);
//            else
//    	    	mTabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(draw);
	        mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.rgb(55,127,19));
            // set tab text center
	    	int tabCount = mTabHost.getTabWidget().getTabCount();
	    	for (int j = 0; j < tabCount; j++) {
	    	    final View view = mTabHost.getTabWidget().getChildTabViewAt(j);
	    	    if ( view != null ) {
	    	        //  get title text view
	    	        final View textView = view.findViewById(android.R.id.title);
	    	        if ( textView instanceof TextView ) {
	    	            ((TextView) textView).setGravity(Gravity.CENTER);
	    	            ((TextView) textView).setSingleLine(true);
	    	            ((TextView) textView).setPadding(6, 0, 6, 0);
	    	            ((TextView) textView).setMinimumWidth(96);
	    	            textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
	    	        }
	    	    }
	    	}
	    	i++;
        }
        
        //check high light position
        int highLightPosition = 0;
        mDbHelper.close();
		for(int iPosition =0;iPosition<mTabCount;iPosition++)
		{
			if(mFinalTabIndex == iPosition)
			{
				highLightPosition = iPosition;	
				System.out.println("highLightPosition = " + highLightPosition);
			}
		}
        //set background color to selected tab 
		mTabHost.setCurrentTab(highLightPosition); 

        //last selected background
		mTabHost.getTabWidget().getChildAt(highLightPosition).setBackgroundColor(Color.rgb(186,249,142)); //null error ????
//        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(highLightPosition).findViewById(android.R.id.title);
//        tv.setBackgroundColor(Color.rgb(186,249,142)); //sel
		System.out.println("t3");

        mCurrentTabIndex = mFinalTabIndex;
        
        // scroll to last view
        mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	    		System.out.println("t4");

		        lastPageViewPreferences = getSharedPreferences("last_page_view", 0);

		        int scrollX = lastPageViewPreferences.getInt("KEY_LAST_SCROLL_X", 0);
				System.out.println("scrollX = " + scrollX);
	        	mHorScrollView.scrollTo(scrollX, 0);
	        } 
	    });
	}
	
	/**
	 * set tab listener
	 * 
	 */
	@SuppressWarnings("deprecation")
	protected void setTabListener()
	{
        // set on tab changed listener
	    mTabHost.setOnTabChangedListener(new OnTabChangeListener()
	    {
			@Override
			public void onTabChanged(String tabSpec)
			{
				// get scroll X
				int scrollX = mHorScrollView.getScrollX();
				System.out.println("getScrollX() = " + scrollX);
		        lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
				lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();
				
				mDbHelper.open();
				mNotesCursor = mDbHelper.getAllTab();
				for(int i=0;i<mTabCount;i++)
				{
					mNotesCursor.moveToPosition(i);
					int iTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
					TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(iTabId)); // TAB_SPEC starts from 1
			    	
					if(TAB_SPEC.equals(tabSpec) )
			    	{
//						System.out.println("_onTabChanged iTabId = " + iTabId);
			    		mCurrentTabIndex = i;
		                lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW",
		                		String.valueOf(iTabId)).commit();
				    	mTabHost.setCurrentTab(mCurrentTabIndex); 
			    		
				        //selected background
				    	mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.rgb(186,249,142)); 
				    	
				    	for(int j=0;j<mTabCount;j++){
				    		if(j != i){
						        //unselected background
				    			mTabHost.getTabWidget().getChildAt(j).setBackgroundColor(Color.rgb(55,127,19));
				    		}
				    	}
			    		DB.setTableNumber(String.valueOf(iTabId));
				    	new NoteFragment();
			    	} 
				}
				mDbHelper.close();
			}
		}
	    );    

	    // set listener for editing tab info
	    int i = 0;
	    while(i < mTabCount)
		{
			final int tabCursor = i;
			View tabView= mTabHost.getTabWidget().getChildAt(i);
			
			// on long click listener
			tabView.setOnLongClickListener(new OnLongClickListener() 
	    	{	
				@Override
				public boolean onLongClick(View v) 
				{
					mDbHelper.open();
					mNotesCursor = mDbHelper.getAllTab();
					mNotesCursor.moveToPosition(tabCursor);
					final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
					
					if(mNotesCursor.isFirst())
						mFirstExistTabId = tabId;
					
					if(tabCursor == mCurrentTabIndex )
					{
						// get tab name
						String tabName = mNotesCursor.getString(mNotesCursor.getColumnIndex("tab_name"));
						mDbHelper.close();
				        
				        final EditText editText1 = new EditText(getBaseContext());
				        editText1.setText(tabName);
				        editText1.setSelection(tabName.length()); // set edit text start position
				        //update tab info
				        Builder builder = new Builder(mTabHost.getContext());
				        builder.setTitle(R.string.edit_page_tab_title)
				                .setMessage(R.string.edit_page_tab_message)
				                .setView(editText1)   
				                .setNegativeButton(R.string.edit_page_button_update, new OnClickListener()
				                {   @Override
				                    public void onClick(DialogInterface dialog, int which)
				                    {
				                		mDbHelper.open();
			        					mNotesCursor = mDbHelper.getAllTab();
			        					mNotesCursor.moveToPosition(mCurrentTabIndex);
			        					final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
				                        mDbHelper.updateTab("TAB_INFO", tabId, editText1.getText().toString());
				                        // Before _recreate, store latest page number currently viewed
				                        lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW", String.valueOf(tabId)).commit();
				                        System.out.println("====recreate=====");
				                        mDbHelper.close();
				                        recreate();
				                    }
				                })	 
				                .setNeutralButton(R.string.edit_page_button_delete, new OnClickListener()
				                {   @Override
				                    public void onClick(DialogInterface dialog, int which){
										//增加確認修改選擇:start
					                	setting = mContext.getSharedPreferences("delete_warn", 0);
					                	if(setting.getString("KEY_DELETE_PAGE_WARN","").equalsIgnoreCase("yes")){
					                		Builder builder1 = new Builder(mTabHost.getContext()); 
					                		builder1.setTitle(R.string.confirm_dialog_title)
					                            .setMessage(R.string.confirm_dialog_message_page)
					                            .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener(){
					                            	@Override
					                                public void onClick(DialogInterface dialog1, int which1){
					                            		/*nothing to do*/}})
					                            .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener(){
					                            	@Override
					                                public void onClick(DialogInterface dialog1, int which1){
					                                	deletePage(tabId);}})
					                            .show();} //增加確認修改選擇:end
					                	else{
					                		deletePage(tabId);
					                	}
				                    }
				                })	
				                .setPositiveButton(R.string.edit_page_button_ignore, new OnClickListener(){   
				                	@Override
				                    public void onClick(DialogInterface dialog, int which)
				                    {/*nothing*/}})
				                .show();  
						}
					return true;
				}
			});
			i++;
		}

	}//setListener()

	/**
	 * 功能選項
	 */
    // Menu identifiers
    static final int ADD_NEW_ID = R.id.ADD_NEW_ID;
    static final int ADD_NEW_PAGE = R.id.ADD_NEW_PAGE;
	static final int CONFIG_ID = R.id.CONFIG_ID;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(Build.VERSION.SDK_INT >= 11)
		{
		    menu.add(0, ADD_NEW_ID, 1, R.string.add_new_note )
		    .setIcon(R.drawable.ic_menu_add_new)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);//???
		}
		else
		{	
			menu.add(0, ADD_NEW_ID, 1,  R.string.add_new_note)
		    .setIcon(R.drawable.ic_menu_add_new);
		}	
		mMenu = menu;	

	    SubMenu subMenu = menu.addSubMenu(0, 0, 2, R.string.options);

	    subMenu.add(0, ADD_NEW_PAGE, 2, R.string.add_new_page)
	    .setIcon(R.drawable.ic_menu_add_new_page);

	    subMenu.add(0, CONFIG_ID, 3, R.string.settings)
	    .setIcon(R.drawable.ic_menu_settings);

	    MenuItem subMenuItem = subMenu.getItem();
	    subMenuItem.setIcon(R.drawable.ic_menu_overflow);
		if(Build.VERSION.SDK_INT >= 11)
			subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);//???

		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * set overflow menu
	 */
	private void setOverflowMenu() {

	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	

	/**
	 * on options item selected
	 */
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	Config mConfigFragment;
    	FragmentManager fragmentManager = null;
    	
    	if(Build.VERSION.SDK_INT >= 11 )
    		fragmentManager = getSupportFragmentManager();
    	
    	System.out.println("item.getItemId() = " + item.getItemId());
        switch (item.getItemId()) {
            case ADD_NEW_PAGE:
                lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW",
                		    String.valueOf(mLastExistTabId + 1)).commit(); 
                addNewPage(mLastExistTabId + 1);
                return true;  
            case CONFIG_ID:
            	mMenu.setGroupVisible(0, false); //hide the menu
            	if(Build.VERSION.SDK_INT >= 11 )
            	{
                	mConfigFragment = new Config();
	            	FragmentTransaction ft = fragmentManager.beginTransaction();
	            	ft.add(android.R.id.content, mConfigFragment, "Config");
	            	ft.addToBackStack(null); 
	            	ft.commit();
	            	ft.show(mConfigFragment);
            	}
            	else
            	{
    	        	Intent intent = new Intent(mContext, ConfigAct.class);
    	        	startActivity(intent);
            	}
                return true;
            case android.R.id.home:
            	if(Build.VERSION.SDK_INT >= 11)
            		recreate();
            	else
            	{
            		Intent intent=new Intent(mContext,FragmentTabsHost.class);
            		startActivity(intent);
            		finish();
            	}
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * add new page
     */
	public  void addNewPage(int newTabId) {
		// set scroll X
        lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
		int scrollX = (mTabCount) * 60 * 5; //over the last scroll X
		System.out.println("_addNewPage scrollX = " + scrollX);
		lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();
		
 	    // insert tab name
		mDbHelper.open();
		mDbHelper.insertTab("TAB_INFO","N".concat(String.valueOf(newTabId)));
		
		// insert table for new tab
		mDbHelper.insertNewTable(newTabId);
		mTabCount++;
		mDbHelper.close();
		mTabHost.clearAllTabs(); //must add this in order to clear onTanChange event
        System.out.println("==== recreate new tab =====");
        
    	if(Build.VERSION.SDK_INT >= 11)
    	{
    		recreate();
    	}
    	else
    	{
    		finish();
    		Intent intent=new Intent(mContext,FragmentTabsHost.class);
    		startActivity(intent);
    	}
	}
	
	/**
	 * delete page
	 */
	public  void deletePage(int TabId) {
		
		//if current page is the first page and will be delete,
		//try to get next existence of page
		if(mNotesCursor.getCount() != 1){
			mDbHelper.open();
			mNotesCursor = mDbHelper.getAllTab();
			mNotesCursor.moveToPosition(mCurrentTabIndex);
			final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
	        if(tabId == mFirstExistTabId)
	        {
	        	int cGetNextExistIndex = mCurrentTabIndex+1;
	        	boolean bGotNext = false;
				while(!bGotNext){
	        		mNotesCursor = mDbHelper.getAllTab();
		        	mNotesCursor.moveToPosition(cGetNextExistIndex);					            		        	
		        	try{
		        	   	mFirstExistTabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
//    		        	System.out.println("tab is GOT!");
		        		bGotNext = true;
		        	}catch(Exception e){
//    		        	 System.out.println("tab is not GOT!");
    		        	 bGotNext = false;
    		        	 cGetNextExistIndex++;}}		            		        	
	        	System.out.println("change strLastPageViewNum to: " + mFirstExistTabId);
	        }
            //change to first existing page
			lastPageViewPreferences.edit()
            .putString("KEY_LAST_PAGE_VIEW",String.valueOf(mFirstExistTabId)).commit();
		}
		else{
             Toast.makeText(FragmentTabsHost.this, R.string.toast_keep_one_page , Toast.LENGTH_SHORT).show();
             return;
		}
		
		// set scroll X
        lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
		int scrollX = 0; //over the last scroll X
		System.out.println("_deletePage scrollX = " + scrollX);
		lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();
	 	  
 	    // delete tab name
		mNotesCursor = mDbHelper.getAllTab();
		mDbHelper.deleteTabInfo("TAB_INFO",TabId);
		
		// drop tab
		mDbHelper.dropTable(TabId);
		mTabCount--;
		mDbHelper.close();
		mTabHost.clearAllTabs();
    	System.out.println("==== recreate delete tab =====");
    	if(Build.VERSION.SDK_INT >= 11)
    		recreate();
    	else
    	{
    		finish();
    		Intent intent=new Intent(mContext,FragmentTabsHost.class);
    		startActivity(intent);
    	}
	}

	/**
	 * on destroy
	 */
	@Override
	protected void onDestroy() {
			mNotesCursor.close();
			mDbHelper.close();
		super.onDestroy();
	}
}