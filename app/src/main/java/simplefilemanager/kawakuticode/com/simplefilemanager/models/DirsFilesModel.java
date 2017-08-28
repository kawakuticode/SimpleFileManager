package simplefilemanager.kawakuticode.com.simplefilemanager.models;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.Stack;


/**
 * Created by russeliusernestius on 20/07/17.
 */

public class DirsFilesModel implements Parcelable {

    private File currentDir; //current location.
    private File previousDir; //previous location.
    private Stack<File> navigationTrack; //navigation track tree path.
    private Context mContext;

    public static final String TAG = "Current dir";


    public File chooseDefaultFolder(String defaultfolder) {


        File tmpDefaultFolder = Environment.getExternalStorageDirectory();

        switch (defaultfolder) {

            case "System":
                tmpDefaultFolder = Environment.getExternalStorageDirectory();
                break;
            case "Downloads":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                break;
            case "Documents":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                break;
            case "Alarms":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
                break;
            case "DCIM":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                break;
            case "Music":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                break;
            case "Pictures":
                tmpDefaultFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                break;

        }
        //Log.d("choose default", default_folder_tmp);
        return tmpDefaultFolder;
    }

    public DirsFilesModel(Context context, String defaultFolder) {


        navigationTrack = new Stack<>();
        this.mContext = context;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

            Log.d("Default folder ", defaultFolder);

            currentDir = chooseDefaultFolder(defaultFolder);

            Log.d("Current dir ", currentDir.getName());
        } else {
            currentDir = mContext.getFilesDir();
        }
    }

    public File getCurrentDir() {
        return this.currentDir;
    }

    public void setCurrentDir(File newDir) {
        this.currentDir = newDir;
    }

    public File getPreviousDir() {
        return navigationTrack.pop();
    }

    public void setPreviousDir(File previousDir) {
        this.previousDir = previousDir;
        navigationTrack.add(previousDir);

    }

    public boolean hasPreviousDir() {
        return !navigationTrack.isEmpty();
    }

    public Stack<File> getNavigationTrack() {
        return navigationTrack;
    }

    public void setNavigationTrack(Stack<File> navigationTrack) {
        this.navigationTrack = navigationTrack;
    }

    @Override
    public String toString() {
        return "DirsFilesModel{" +
                "currentDir=" + currentDir.toString() +
                ", previousDir=" + previousDir.toString() +
                ", navigationTrack=" + navigationTrack +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.currentDir);
        dest.writeSerializable(this.previousDir);
        dest.writeList(this.navigationTrack);
        // dest.writeParcelable(this.mContext, flags);
    }

    protected DirsFilesModel(Parcel in) {
        this.currentDir = (File) in.readSerializable();
        this.previousDir = (File) in.readSerializable();
        this.navigationTrack = new Stack<>();
        in.readList(this.navigationTrack, File.class.getClassLoader());
        this.mContext = in.readParcelable(Context.class.getClassLoader());
    }

    public static final Creator<DirsFilesModel> CREATOR = new Creator<DirsFilesModel>() {
        @Override
        public DirsFilesModel createFromParcel(Parcel source) {
            return new DirsFilesModel(source);
        }

        @Override
        public DirsFilesModel[] newArray(int size) {
            return new DirsFilesModel[size];
        }
    };
}
