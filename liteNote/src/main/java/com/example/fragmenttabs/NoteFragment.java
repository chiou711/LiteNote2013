package com.example.fragmenttabs;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

// main control
public class NoteFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<String>> 
{
	private static Cursor mNotesCursor;
	private static DB mDbHelper;
    public static SharedPreferences setting;
    private static String editString1;
	private static int mNoteNumber1 = 1;
	private static String mNoteString1;
	private static int mMarkingIndex1;
	private static int mNoteNumber2 ;
	private static String mNoteString2;
	private static int mMarkingIndex2;
	private static List<Boolean> mHighlightList = new ArrayList<Boolean>();
  	
	// This is the Adapter being used to display the list's data.
	NoteListAdapter mAdapter;
//	DragNDropListView mDndListView;
	DragSortListView mDndListView;
	ImageView mView2;
	private DragSortController mController;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		  
		mDndListView = (DragSortListView)getActivity().findViewById(R.id.list1);
    	mDbHelper = new DB(getActivity()); 
    	
    	//edit listener
    	OnItemLongClickListener editListener = new OnItemLongClickListener()
    	{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) 
			{
//				System.out.println("_onItemLongClick position = " + position);
//				System.out.println("_onItemLongClick mNotesCursor.getCount()" + mNotesCursor.getCount());
				
				DB_Open();
				if(position >= DB_GetAllCount()) // avoid footer error
					return true;
				else
				{
					editNote(mDndListView, view, position, id);
					DB_Close();
					return true;
				}
			}
    		
    	};
//    	mDndListView.setOnItemClickListener(editListener);
    	mDndListView.setOnItemLongClickListener(editListener);
    	
    	
    	//check list view
    	 mDndListView.setOnItemClickListener(new OnItemClickListener()
                                        {
                                             public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                                             {		

//                                                  System.out.println("_MarkListener position = " + position);
	                            				
                                                  DB_Open();
                                             	 
                                                  if(position >= mNotesCursor.getCount()) //end of list
                                                	  return;
                                                  
                                                  CheckedTextView chkItem = (CheckedTextView) v.findViewById(R.id.text);
                                                  chkItem.setChecked(!chkItem.isChecked());
                                                     
                                                  String strNote = DB_GetNoteString(position);
                                                  int idNote = DB_GetNoteId(position);
	                            				
                                                  parent = mDndListView;
                                                  if( DB_GetNoteMarking(position) == 0)                
                                                  {
                                                	  mDbHelper.update(idNote, strNote,1);
                                                  }
                                                  else
                                                  {
                                                	  mDbHelper.update(idNote, strNote,0);
                                                  }
                                                  DB_Close();
	                                            
                                                  // save index and top position
                                                  int index = parent.getFirstVisiblePosition();
//                                                  View v = parent.getChildAt(0);
                                                  v = parent.getChildAt(0);
                                                  int top = (v == null) ? 0 : v.getTop();
	                                            
                                                  fillData();
	                                            
                                                  // restore index and top position
                                                  ((DragSortListView) parent).setSelectionFromTop(index, top);
                                             }
                                        }
                                       );
  
    	
		
        mController = buildController(mDndListView);
        mDndListView.setFloatViewManager(mController);
        mDndListView.setOnTouchListener(mController);
        mDndListView.setDragEnabled(true);
		
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new NoteListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false); //cw@ / 不用progress indicator看起來比較快

		// Prepare the loader. Either re-connect with an existing one or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}
	
    
    private DragSortListView.DragListener onDrag =
            new DragSortListView.DragListener() {
                @Override
                public void drag(int startPosition, int endPosition) {
    				System.out.println("_DragListener startPosition = " + startPosition);
                }
            };
	
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int startPosition, int endPosition) {
                	System.out.println("DragSortListView _DropListener");
    				DB_Open();
    				if(startPosition >= DB_GetAllCount()) // avoid footer error
    					return;
    		    	DB_Close();
    				
    				mHighlightList.set(startPosition, true);
    				mHighlightList.set(endPosition, true);
    				
    				System.out.println("startPosition = " + startPosition);
    				System.out.println("endPosition = " + endPosition);
    
    				//reorder data base storage
    				int loop = Math.abs(startPosition-endPosition);
    				for(int i=0;i< loop;i++)
    				{
    					swapRows(startPosition,endPosition);
    					if((startPosition-endPosition) >0)
    						endPosition++;
    					else
    						endPosition--;
    				}
    				fillData();
                }
            };
            
        private DragSortListView.MarkListener onMark =
                new DragSortListView.MarkListener() {
                    @Override
                    public void mark(int position) {
                    }
                };        
   
    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        //drag
        controller.setDragHandleId(R.id.dragHandler);// handler
        controller.setDragInitMode(DragSortController.ON_DOWN);
        controller.setBackgroundColor(Color.rgb(255,128,0));
        //mark
//        controller.setMarkEnabled(true);
//        controller.setClickMarkId(R.id.markHandler);
//        controller.setMarkMode(DragSortController.ON_DOWN);
        return controller;
    }        
    
	protected void swapRows(int startPosition, int endPosition) {

		DB_Open();
		DB_GetAllCount();

		mNoteNumber1 = DB_GetNoteId(startPosition);
        mNoteString1 = DB_GetNoteString(startPosition);
        mMarkingIndex1 = DB_GetNoteMarking(startPosition);
		
		mNoteNumber2 = DB_GetNoteId(endPosition);
        mNoteString2 = DB_GetNoteString(endPosition);
        mMarkingIndex2 = DB_GetNoteMarking(endPosition);
		
        mDbHelper.update(mNoteNumber2,
				 mNoteString1,
				 mMarkingIndex1);		        
		
		mDbHelper.update(mNoteNumber1,
		 		 mNoteString2,
		 		 mMarkingIndex2);	
    	DB_Close();

	}

	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. 
		return new NoteListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<String>> loader,
							   List<String> data) 
	{
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) 
		{
			setListShown(true);
		} 
		else 
		{
			setListShownNoAnimation(true);
		}
		fillData();
	}

	@Override
	public void onLoaderReset(Loader<List<String>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}
	
    
	/**
	 * 顯示資料
	 */
    public void fillData()
    {
    	DB_Open();
        
        // save index and top position
        int index = mDndListView.getFirstVisiblePosition();
        View v = mDndListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        
        String[] from = new String[] { DB.KEY_NOTE };
        int[] to = new int[] { R.id.text };
        
        SimpleDragSortCursorAdapter adapter = new SimpleDragSortCursorAdapter(getActivity().getBaseContext(), 
													R.layout.select_item,
													mNotesCursor, 
														from, 
														to, 
														0);
									//					R.id.markHandler,//drag
//														R.id.dragHandler); //Mark click 
        
        //change list item color
        // 1. create a new ViewBinder
        SimpleDragSortCursorAdapter.ViewBinder binder = new SimpleDragSortCursorAdapter.ViewBinder() 
		  {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
            {
                String empname = cursor.getString(cursor.getColumnIndex("note"));
                TextView tv = (TextView) view;
                tv.setText(empname);

                CheckedTextView chkItem = (CheckedTextView) view.findViewById(R.id.text);
                if( cursor.getLong(cursor.getColumnIndex("marking")) == 1)                
                {
                    chkItem.setChecked(true);
//                    tv.setBackgroundColor(Color.rgb(55,127,19));
                }
                else
                {
                    chkItem.setChecked(false);
//                    tv.setBackgroundColor(Color.rgb(186,249,142));
                }
                return true;
            }   
        };

        //2. set the new ViewBinder for adapter
        adapter.setViewBinder(binder);
        
        //footer
        if(mDndListView.getFooterViewsCount() == 0)
        {
            mDndListView.addFooterView(getActivity().getLayoutInflater().inflate(R.layout.footer, null));
        }
        
        mDndListView.setAdapter(adapter);
        
		// for highlight
		for(int i=0; i< DB_GetAllCount() ; i++ )
		{
			mHighlightList.add(true);
			mHighlightList.set(i,true);
		}
		
        // restore index and top position
        mDndListView.setSelectionFromTop(index, top);
        DB_Close();
        mDndListView.setDropListener(onDrop);//??? 
        mDndListView.setDragListener(onDrag);//??? 
        mDndListView.setMarkListener(onMark);//??? 
    }
    
	/**
	 *  新增  note
	 */
	public void addNewNote() 
	{
		final Context context = getActivity();
		String noteName = "";
        final EditText editText2 = (EditText)getActivity()
        		.getLayoutInflater()
        		.inflate(R.layout.edit_text, null);
        
		editText2.setText(noteName);
		editText2.setSelection(noteName.length()); // set cursor start
    	editText2.setBackgroundColor(Color.rgb(186,249,142));
    	editText2.setTextColor(Color.rgb(0,0,0));
    	
		Builder builder1 = new Builder(context);
		builder1.setTitle(R.string.add_new_note_title)
				.setMessage(R.string.add_new_note_message)
				.setView(editText2)
				.setPositiveButton(R.string.add_new_note_button_add, 
						new OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						editString1 = editText2.getText().toString();
						if (!editString1.equals("")) 
						{	
							DB_Open();
							mDbHelper.insert(editString1);
							DB_Close();
						}
						fillData();
					}
				});
				AlertDialog alert = builder1.create();
				alert.show();
				alert.getWindow().getAttributes();
			    Button btn1 = alert.getButton(DialogInterface.BUTTON_POSITIVE);
			    btn1.setTextSize(22);
	}   
	
	/**
	 *  修改|劃掉 |刪除 note
	 */
//	@Override
	public void editNote(final ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		if(position >= DB_GetAllCount()) // avoid footer error
			return;
		
		// get DB Id number
		mNoteNumber1 = DB_GetNoteId(position);
		
		//get Note string
        String str =  DB_GetNoteString(position);
//        System.out.println("str = " + str);
        
        final Context context = getActivity();
        
        final EditText editText1 = (EditText)getActivity()
							        		.getLayoutInflater()
							        		.inflate(R.layout.edit_text, null);
        
        editText1.setText(str);
        editText1.setSelection(str.length()); // set edit text start position
    	editText1.setBackgroundColor(Color.rgb(186,249,142));
    	editText1.setTextColor(Color.rgb(0,0,0));
    	
        Builder builder = new Builder(context);
        builder.setTitle(R.string.edit_note_title)
                .setMessage(R.string.edit_note_message)
                .setView(editText1)   
                .setNegativeButton(R.string.edit_note_button_save, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    	editString1 = editText1.getText().toString();

                        if(!editString1.equals(""))
                        { 
                        	DB_Open();
                        	mDbHelper.update(mNoteNumber1, editText1.getText().toString(),0);
                        	DB_Close();
                        	fillData();
                        }
                    }
                })	                
                .setNeutralButton(R.string.edit_note_button_back, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
//                    	editString1 = editText1.getText().toString();
//                    	DB_Open();
//                        mDbHelper.update(mNoteNumber1, editText1.getText().toString(),1);
//                        DB_Close();
//                        fillData();
                    }
                })
                .setPositiveButton(R.string.edit_note_button_delete, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
						//增加確認修改選擇:start
	                	setting = context.getSharedPreferences("delete_warn", 0);
	                	if(setting.getString("KEY_DELETE_WARN","").equalsIgnoreCase("yes"))
	                	{
	                		Builder builder1 = new Builder(context); 
	                		builder1.setTitle(R.string.confirm_dialog_title)
	                            .setMessage(R.string.confirm_dialog_message)
	                            .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
	                            {   @Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                            	/*nothing to do*/}})
	                            .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
	                            {   @Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                            		DB_Open();
	                            		mDbHelper.delete(mNoteNumber1);
	                            		DB_Close();
	                            		fillData();
	                            		
	                            }})
	                            .show();//增加確認修改選擇:end
	                	}
	                	else{
	                	    //不增加確認修改選擇:start
	                		DB_Open();
	                		mDbHelper.delete(mNoteNumber1);
	                		DB_Close();
	                		fillData();
	                	}
                    }
                })
                .setIcon(android.R.drawable.ic_menu_edit);
        
        AlertDialog d = builder.create();
        d.show();
        // android.R.id.button1 for positive: save 
        ((Button)d.findViewById(android.R.id.button1))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // android.R.id.button2 for negative: delete
        ((Button)d.findViewById(android.R.id.button2))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save, 0, 0, 0);
        // android.R.id.button3 for neutral: back
        ((Button)d.findViewById(android.R.id.button3))
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        
	}
	
	/*
	 * 功能選項
	 */
    // Menu identifiers
    static final int ADD_NEW_ID = R.id.ADD_NEW_ID;
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case ADD_NEW_ID:
            	addNewNote(); 
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	public void onDestroy() {
		DB_Close();
		super.onDestroy();
	}
	
	
	/*
	 * inner class for note list loader
	 */
	public static class NoteListLoader extends AsyncTaskLoader<List<String>> 
	{
		List<String> mApps;

		public NoteListLoader(Context context) {
			super(context);
		}

		@Override
		public List<String> loadInBackground() {
			List<String> entries = new ArrayList<String>();
			return entries;
		}

		@Override
		protected void onStartLoading() {
			System.out.println("_onStartLoading");
			DB_Open();
			forceLoad();
			DB_Close();
		}
	}

	/*
	 * 	inner class for note list adapter
	 */
	public static class NoteListAdapter extends ArrayAdapter<String> 
	{
		public NoteListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}
		public void setData(List<String> data) {
			clear();
			if (data != null) {		
				if(Build.VERSION.SDK_INT >= 11)
					addAll(data);
			}
		}
	}

	/*
	 * DB functions
	 * 
	 */
	static void DB_Open()
	{
		mDbHelper.open();
		mNotesCursor = mDbHelper.getAll();
	}
	
	int DB_GetAllCount()
	{
		return mNotesCursor.getCount();
	}
	
	String DB_GetNoteString(int position)
	{
		mNotesCursor.moveToPosition(position);
        return mNotesCursor.getString(mNotesCursor.getColumnIndex("note"));
	}
	
	int DB_GetNoteId(int position)
	{
		mNotesCursor.moveToPosition(position);
        return mNotesCursor.getInt(mNotesCursor.getColumnIndex("_id"));
	}
	
	int DB_GetNoteMarking(int position)
	{
		mNotesCursor.moveToPosition(position);
		return mNotesCursor.getInt(mNotesCursor.getColumnIndex("marking"));
	}
	
	static void DB_Close()
	{
		mDbHelper.close();
	}
}
