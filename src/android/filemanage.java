package filemanage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.nononsenseapps.filepicker.diy.MultimediaPickerActivity2;
import com.nononsenseapps.filepicker.diy.SUPickerActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class filemanage extends CordovaPlugin {
  private CordovaActivity cordovaActivity;
  private static Context mContext;
  public CallbackContext callbackContextt;
  public CordovaInterface cordovaInterface;
  static final int CODE_SD = 0;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    cordovaActivity = (CordovaActivity) cordova.getActivity();
    mContext = cordova.getActivity().getApplicationContext();
    cordovaInterface = cordova;
  }

  /**
   * 安卓6以上动态权限相关
   */
  private static final int REQUEST_CODE = 100001;

  private boolean needsToAlertForRuntimePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return !cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    } else {
      return false;
    }
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.callbackContextt = callbackContext;
    if (action.equals("filemanageOpen")) {
      if (!needsToAlertForRuntimePermission()) {
        this.coolMethod();
      } else {
        requestPermission();
      }
      return true;
    }
    return false;
  }

  private void coolMethod() {
    startActivity(CODE_SD, MultimediaPickerActivity2.class);
  }

  private void requestPermission() {
    ArrayList<String> permissionsToRequire = new ArrayList<String>();
    if (!cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
      permissionsToRequire.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    String[] _permissionsToRequire = new String[permissionsToRequire.size()];
    _permissionsToRequire = permissionsToRequire.toArray(_permissionsToRequire);
    cordova.requestPermissions(this, REQUEST_CODE, _permissionsToRequire);
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    if (requestCode != REQUEST_CODE)
      return;
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(mContext, "权限被拒绝,请手动打开权限", Toast.LENGTH_SHORT).show();
        return;
      }
    }
    this.coolMethod();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  protected void startActivity(final int code, final Class<?> klass) {
    final Intent i = new Intent(cordovaActivity, klass);
    i.setAction(Intent.ACTION_GET_CONTENT);
    i.putExtra(SUPickerActivity.EXTRA_ALLOW_MULTIPLE, true);
    i.putExtra(FilePickerActivity.EXTRA_MODE, AbstractFilePickerFragment.MODE_FILE);
    i.putExtras(cordovaActivity.getIntent());
    cordovaInterface.startActivityForResult(this, i, code);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    JSONObject jsonObject;
    JSONArray jsonArray = new JSONArray();
    if (resultCode == Activity.RESULT_OK &&
      (CODE_SD == requestCode)) {
      List<Uri> files = Utils.getSelectedFilesFromResult(data);
      for (Uri file : files) {
        jsonObject = new JSONObject();
        try {
          jsonObject.put("path", Utils.getFileForUri(file).getPath());
          jsonObject.put("name", Utils.getFileForUri(file).getName());
          jsonObject.put("size", Utils.FormetFileSize(Utils.getFileForUri(file).length()));
          jsonObject.put("time", Utils.getFileLastModifiedTime(Utils.getFileForUri(file)));
          jsonArray.put(jsonObject);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      callbackContextt.success(jsonArray.toString());
    }
  }
}
