package simplefilemanager.kawakuticode.com.simplefilemanager.utilities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by russeliusernestius on 20/07/17.
 */

public abstract class Utilities {


    /*  determinate de MIME TYPE FILE */
    public static  String getMimeType(Uri uri) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
        if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    public static void showSnackBar(Activity activity, Integer resource) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, resource, Toast.LENGTH_SHORT).show();
    }

    //list of all dirs and files .
    public static List<File> getAllContentDir(File f) {
        List<File> dirs = new ArrayList<>();
        List<File> files = new ArrayList<>();
        try {

            File[] allContents = f.listFiles();

            if (f.listFiles() != null) {
                for (File file : allContents) {
                    if (file.isDirectory()) {
                        dirs.add(file);
                    } else {
                        files.add(file);
                    }
                }
                Collections.sort(dirs);
                Collections.sort(files);
                dirs.addAll(files);
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
        return dirs;
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
