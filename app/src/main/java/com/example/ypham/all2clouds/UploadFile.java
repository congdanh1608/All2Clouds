package com.example.ypham.all2clouds;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class UploadFile extends AsyncTask<Void, Long, Boolean>{
    private DropboxAPI<?> mApi;
    private String mPath;
    private File mFile;

    private Backup_Tab.Backup_AsyncTask manager;

    private long mFileLen;
    private DropboxAPI.UploadRequest mRequest;
    private Context mContext;
    //private final ProgressDialog mDialog;

    private String mErrorMsg;

    private Tools tools = new Tools();
   /* public UploadFile(Backup_Tab manager_)
    {
        manager = manager_;
    }*/
    public UploadFile(Backup_Tab.Backup_AsyncTask mBackup, Context context, DropboxAPI<?> api, String dropboxPath, File file) {
        this.mApi = api;
        this.mPath = dropboxPath;
        this.mFileLen = file.length();
        this.mFile = file;
        this.mContext = context;
        this.manager = mBackup;
        //

        /*mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Uploading " + file.getName());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        *//*mDialog.setProgress(0);
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRequest.abort();
            }
        });*//*
        mDialog.show();
*/
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + mFile.getName();
            Log.i("TAG", mFile.getName());
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
                    new ProgressListener() {

                        @Override
                        public long progressInterval() {
                            // Update the progress bar every half-second or so
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });
            if (mRequest != null) {
                mRequest.upload();
                return true;
            }
        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error. Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error. Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error. Try again.";
        } catch (FileNotFoundException e) {
        }
        return false;
    }

    /*@Override
    protected void onProgressUpdate(Long... values) {
        //super.onProgressUpdate(values);
        int percent = (int)(100.0*(double)values[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }
*/
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        //mDialog.dismiss();

        manager.notifyFinished(this); //thuc hien tang bien dem count-so luong file da upload
        if (result) {
            if(manager.isFinished())
                manager.callBackXYZ("Upload completed!");
            //showToast("File successfully uploaded");
        } else {
            if(manager.isFinished())
            manager.callBackXYZ(mErrorMsg);
        }
    }
/*
    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }*/
}
