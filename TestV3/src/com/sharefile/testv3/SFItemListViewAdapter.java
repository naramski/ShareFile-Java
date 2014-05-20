package com.sharefile.testv3;

import java.util.List;

import com.sharefile.api.utils.SFLog;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SFItemListViewAdapter extends ArrayAdapter<SFItem>
{

	final List<SFItem> mItems;
	final LayoutInflater mLayoutInflater;
	
	public SFItemListViewAdapter(Activity activity, int resource,	List<SFItem> objects) 
	{
		super(activity, resource, objects);
		mItems = objects;
		mLayoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() 
	{		
		int count = 0;
		
		if(mItems == null || mItems.size()== 0)
		{
			count = 0;
		}
		else
		{
			count = mItems.size();
		}
							
		SFLog.d2("-SFItemListViewAdapter", "getCount = %d", count);
		
		return count;
	}
	
	@Override
	public View getView(int position, View v, ViewGroup parent)
	{
		
		if(mItems == null || mItems.size()== 0)
		{
			return  mLayoutInflater.inflate(R.layout.folder_listview_empty, null);
		}
			
		View cell = v;
		
		if(cell == null)
		{
			cell = mLayoutInflater.inflate(R.layout.sf_item, null);
			final SwipeListItemState stateObj = new SwipeListItemState();
			stateObj.mFileName = (TextView) cell.findViewById(R.id.FileName);
			stateObj.mDescription = (TextView) cell.findViewById(R.id.secondLine);
			stateObj.mIcon = (ImageView)cell.findViewById(R.id.sf_item_icon);
			cell.setTag(stateObj);
		}
		
		final SwipeListItemState mStateObj = (SwipeListItemState)cell.getTag();
		
		final SFItem item = mItems.get(position);
		
		if(item == null)
		{
			if(mItems.size()==1)
			{
				return  mLayoutInflater.inflate(R.layout.folder_listview_empty, null);				
			}
		}
		
		mStateObj.mPostion = position;
		mStateObj.mSFItem = item;
		mStateObj.mFileName.setText(item.getFileName());		
		mStateObj.mDescription.setText("Size: " + item.getFileSizeInKB() + " KB" );
		
		if(SFV3ElementType.isFolderType(item))
		{
			mStateObj.mIcon.setImageResource(R.drawable.icon_folder);						
		}
		else if(item instanceof SFFile)
		{
			mStateObj.mIcon.setImageResource(R.drawable.icon_file);						
		}
		else if(item instanceof SFLink)
		{
			mStateObj.mIcon.setImageResource(R.drawable.icon_url);						
		}
		else if(item instanceof SFNote)
		{
			mStateObj.mIcon.setImageResource(R.drawable.icon_note);						
		}
				
		return cell;
	}	

}
