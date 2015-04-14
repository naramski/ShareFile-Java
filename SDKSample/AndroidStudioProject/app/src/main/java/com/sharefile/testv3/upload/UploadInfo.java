package com.sharefile.testv3.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.sharefile.api.utils.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UploadInfo
{
	private static final String TAG ="UploadInfo";
	
	private String fullPathToFile = null; // only for "from content provider"?
	private String path = null;
	private String filename = null;
	private String targetfilename = null; //this is set by the User from the Upload UI.
	private String targetFolderId = null;
	private long filesize = 0L;
	private boolean isFromContentProvider = false;
	private String v3URL = null;//V3!!!FileUploadTAG
	private String details = null;
	private boolean overwrite = false;
	private boolean deleteAfterUpload = false;
	private String itemId = null;


	public String getFullPathToFile() {
		return fullPathToFile;
	}

	public void setFullPathToFile(String fullPathToFile) {
		this.fullPathToFile = fullPathToFile;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * get target file name
	 * defaults to the filename
	 * @return
	 */
	public String getTargetFilename() {
		return targetfilename!=null ? targetfilename : filename;
	}

	public void setTargetFilename(String targetfilename) {
		this.targetfilename = targetfilename;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public boolean isFromContentProvider() {
		return isFromContentProvider;
	}

	public void setFromContentProvider(boolean isFromContentProvider) {
		this.isFromContentProvider = isFromContentProvider;
	}

	public String getV3URL() {
		return v3URL;
	}

	public void setV3URL(String v3url) {
		v3URL = v3url;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public boolean isDeleteAfterUpload() {
		return deleteAfterUpload;
	}

	public void setDeleteAfterUpload(boolean deleteAfterUpload) {
		this.deleteAfterUpload = deleteAfterUpload;
	}

	public String getTargetFolderId() {
		return targetFolderId;
	}

	public void setTargetFolderId(String targetFolderId) {
		this.targetFolderId = targetFolderId;
	}


    public static UploadInfo getUploadInfoFromContentProvider(Context context, Uri uriToFile)
    {
        UploadInfo result = null;
        if(context!=null && uriToFile != null)
        {
            //query the provider for basic info about the file
            Cursor c = null;
            result = new UploadInfo();
            result.setFromContentProvider(true);
            try
            {
                final ContentResolver conRes = context.getContentResolver();
                c = conRes.query(uriToFile, null, null, null, null);

                if(c!=null && c.getCount()>0) {
                    c.moveToFirst();
                    final String[] columnNames = c.getColumnNames();

                    for(String s : columnNames)
                    {
                        if(s.equalsIgnoreCase(MediaStore.MediaColumns.SIZE))
                        {
                            result.setFilesize(getFileSize(conRes, uriToFile,c));
                            continue;
                        }

                        if(s.equalsIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME))
                        {
                            result.setFilename(c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));
                        }
                    }
                }

                // result.setFullPathToFile(Uri.decode(uriToFile.toString()));
                result.setFullPathToFile(uriToFile.toString());

            }
            finally
            {
                if(c!=null)
                {
                    c.close();
                    c = null;
                }
            }
        }

        if (result==null) return null;

        //check for errors
        if(Utils.isEmpty(result.getFilename()) || Utils.isEmpty(result.getFullPathToFile()))
        {
            result = null;
        }

        return result;
    }

    /**
     * Alternative for getting the file size:
     * Sometimes apps like the android sound recorder will save 0L for the column "MediaStore.MediaColumns.SIZE" (Android bug??),
     * even if the file has bytes. Use this as a backup for finding the file size.
     */
    private static final long getFileSizeFromFileStat(ContentResolver conRes, Uri uriToFile)
    {
        long ret = -1;
        try
        {
            final ParcelFileDescriptor f = conRes.openFileDescriptor(uriToFile, "r");
            ret = f.getStatSize();
            f.close();
            Log.d(TAG, "File size from file stat = " + ret);
        }
        catch (Exception e)
        {
            Log.e(TAG,"",e);
        }

        return ret;
    }

    private static final long getFileSizeFromDBColumn(Cursor c)
    {
        long ret = c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));

        Log.d(TAG, "File size from db column = " + ret);

        return ret;
    }

    /**
     On Android 4.4.4 the Content provider columns dont return the correct file size. Lets try to
     get the file size from the FD first and then the DB column.
     */
    private static final long getFileSize(ContentResolver conRes, Uri uriToFile, Cursor c)
    {
        long ret = getFileSizeFromFileStat(conRes , uriToFile);

        if(ret <= 0 )
        {
            ret = getFileSizeFromDBColumn(c);
        }

        Log.d(TAG,"Size of file to upload: " + ret + "\n File URI = " + uriToFile.toString());

        return ret;
    }

    public static InputStream getInputStreamFromContentProvider(String fullPathToFile, Context context) throws FileNotFoundException
    {
        InputStream stream = null;

        ContentResolver conRes = context.getContentResolver();

        if(conRes!=null)
        {
            if(isKitKatAndBeyond())
            {
                stream = conRes.openInputStream(getOriginalUriFromFilePath(fullPathToFile));
            }
            else
            {
                stream = conRes.openInputStream(Uri.parse(fullPathToFile));
            }
        }

        return stream;
    }

    public static InputStream getInputStreamFromPath(String fullPathToFile, Context context) throws FileNotFoundException
    {
        if(fullPathToFile.startsWith("content://"))
        {
            return getInputStreamFromContentProvider(fullPathToFile,context);
        }
        else
        {
            return new FileInputStream(fullPathToFile);
        }
    }

    public static boolean isKitKatAndBeyond()
    {
        int version = android.os.Build.VERSION.SDK_INT;

        if(version >= android.os.Build.VERSION_CODES.KITKAT)
        {
            return true;
        }

        return false;
    }

    public static Uri getOriginalUriFromFilePath(String fullpathtoFile)
    {
        Uri encodedUri  = null;
        try
        {
            Uri rawUri = Uri.parse(fullpathtoFile);
            String path = rawUri.getAuthority() + rawUri.getPath().replace(":", "%3A");
            encodedUri = Uri.parse("content://" + path);
            Log.d(TAG, "PATH from rebuiltURI = " + path);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Exception: ", e);
        }

        return encodedUri;
    }

}
