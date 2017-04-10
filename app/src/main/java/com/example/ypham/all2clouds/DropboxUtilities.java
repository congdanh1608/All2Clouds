package com.example.ypham.all2clouds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class DropboxUtilities {

    //////////////////////////////////////////
    //		Your app-specific settings		//
    ////////////////////////////////////////

    //Them khoa ung dung va khoa bi mat da dang ky voi Dropbox

    final static private String APP_KEY = "jm77oxg56erbwbw";//thay khoa
    final static private String APP_SECRET = "utdbtbfxqkf6bh6";//thay doi khoa cho A2C

    ////////////////
    //Luu cac du lieu cookie chung thuc - trang thai lien ket,
    // khong phai chung thuc moi khi su dung app
    ///////////////
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private static final boolean USE_OAUTH1 = false;

    DropboxAPI<AndroidAuthSession> mApi;
    //De kiem tra trang thai link ket voi tai khoan dropbox
    private boolean mLoggedIn;
    //Ngu canh cua Activity Main gui den
    Context mainDB;

    //Phuong thuc Khoi tao, thuc hien chung thuc
    //Tao mot AuthSession moi su dung Dropbox API
    public DropboxUtilities(Context MainDB){
        mainDB = MainDB;
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        setmLoggedIn(mApi.getSession().isLinked());
        //AuthenticateToUse();
    }

    public void LinkToDropbox(){
        if(USE_OAUTH1) {
            mApi.getSession().startAuthentication(mainDB);
        } else {
            mApi.getSession().startOAuth2Authentication(mainDB);
        }
        //setmLoggedIn(mApi.getSession().isLinked());
    }

    public DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }

    //Kiem tra trang thai lien ket voi dropbox
    public boolean getmLoggedIn() {
        return this.mLoggedIn;
    }
    //Ham thuc hien thay doi trang thai giao dien nguoi dung khi dang nhap/dang xuat

    private void setmLoggedIn(boolean mLoggedIn) {
        this.mLoggedIn = mLoggedIn;
    }

    protected void logOut() {
        //Remove credentials from the session
        mApi.getSession().unlink();
        //Clear stored keys
        clearKeys();
        //thay doi trang thai UI to hien thi da logged out
        setmLoggedIn(false);
        //mLoggedIn = false;
    }
    //Xoa cac khoa da luu trong pref
    private void clearKeys() {
        SharedPreferences prefs = mainDB.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    //Ham tra ve false bao loi can phai co app key va secret
    public boolean checkAppKeySetup() {
        //kiem tra de chan rang app key dung
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            //showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            //finish();
            return false;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = mainDB.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            /*showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();*/
            return false;//kiem gia tri ham tra ve bao loi nhu tren
        }
        return true;
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    //Load phan chung thuc da luu truoc do
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = mainDB.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0)
            return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    public void storeAuth() {
        AndroidAuthSession session = mApi.getSession();
        //phan tiep theo phai duoc them vao ham onResume
        //cua activity from which session.startAuthentication()
        //Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                //Mandatory call to complete the auth
                session.finishAuthentication();

                //Store it locally in our app for later use
                //storeAuth(session);
                String oauth2AccessToken = session.getOAuth2AccessToken();
                if (oauth2AccessToken != null) {
                    SharedPreferences prefs = mainDB.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(ACCESS_KEY_NAME, "oauth2:");
                    edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
                    edit.commit();
                    //return;
                }
                //Luu OAuth 1 access token, neu co. Chi can thiet
                //neu van dang su dung OAuth 1.
                AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
                if (oauth1AccessToken != null) {
                    SharedPreferences prefs = mainDB.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
                    edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
                    edit.commit();
                    //return;
                }
                //mLoggedIn = true;
                setmLoggedIn(true);
            } catch (IllegalStateException e) {
                //showToast("Khong the chung thuc voi Dropbox:");
                Log.i("All2Clouds", "Error when authenticating", e);
            }
            //Luu OAuth 2 access token, neu co
        }
    }
}
