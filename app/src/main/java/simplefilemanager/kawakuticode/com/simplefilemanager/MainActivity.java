package simplefilemanager.kawakuticode.com.simplefilemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import simplefilemanager.kawakuticode.com.simplefilemanager.activities.SettingsActivity;
import simplefilemanager.kawakuticode.com.simplefilemanager.adapters.SimpleFileAdapter;
import simplefilemanager.kawakuticode.com.simplefilemanager.fragments.FolderFragment;
import simplefilemanager.kawakuticode.com.simplefilemanager.models.DirsFilesModel;
import simplefilemanager.kawakuticode.com.simplefilemanager.utilities.Utilities;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<File>>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback, FolderFragment.OnFragmentInteractionListener {

    private static final String TAG = "MAIN ACTIVICTY";
    private ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;
    private SimpleFileAdapter mFilesAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private static final int FILES_LOADER_ID = 0;
    private LoaderManager mLoadManager;
    private static String KEY_LIST_PREFERENCE = "PREF_DEFAULT_FOLDER_LIST";
    private static String KEY_LIST_LOAD = "FILES";
    private static long back_pressed_time;
    private static final int ANTIMATION_TIMEOUT = 1000;
    private static long PERIODTOEXIT = 2000;
    String default_folder;
    private DirsFilesModel mDirFiles;
    private LoaderManager.LoaderCallbacks<List<File>> mFilesLoader;
    private Utilities mUtilities;
    private int REQUEST_STORAGE = 200;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Loader<List<File>> mLoadFiles;
    private View mLayout;

    private Bundle bundleForLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        defaultPreferences();
        initViewElements();
        switchView(this.getResources().getConfiguration().orientation);

    }

    public void initViewElements() {
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_files_progressbar);
        requestStoragePermission();
        mDirFiles = new DirsFilesModel(this, default_folder);
        bundleForLoader = new Bundle();
        mLoadManager = getSupportLoaderManager();
        bundleForLoader.putParcelable(KEY_LIST_LOAD, mDirFiles);
        mLoadFiles = mLoadManager.initLoader(FILES_LOADER_ID, bundleForLoader, this);


    }


    public void defaultPreferences() {
        SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        default_folder = loadDefaultFolder(sharedpref);
        sharedpref.registerOnSharedPreferenceChangeListener(this);
    }

    private String loadDefaultFolder(SharedPreferences shared) {
        return shared.getString(KEY_LIST_PREFERENCE, getString(R.string.pref_default_folder));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(KEY_LIST_PREFERENCE)) {
            default_folder = loadDefaultFolder(sharedPreferences);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


    public void switchView(int orientation) {
        switch (orientation) {
            //PORTAIT
            case 1:
                initPortaitView();
                break;
            //LANDSCAPE
            case 2:
                initLandscapeView();
                break;
        }


    }

    public LoaderManager.LoaderCallbacks<List<File>> myLoaderCallback() {
        return this;
    }

    public Bundle updateBundle(Bundle toUpdate, DirsFilesModel newFile) {
        if (toUpdate == null) {
            toUpdate = new Bundle();
            toUpdate.putParcelable(KEY_LIST_LOAD, newFile);
        } else {
            toUpdate.remove(KEY_LIST_LOAD);
            toUpdate.putParcelable(KEY_LIST_LOAD, newFile);
        }
        return toUpdate;
    }


    public void initPortaitView() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = (RecyclerView) findViewById(R.id.dir_files_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mFilesAdapter = new SimpleFileAdapter(this, new SimpleFileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File item) {
                if (item.isDirectory()) {
                    mDirFiles.setPreviousDir(mDirFiles.getCurrentDir());
                    mDirFiles.setCurrentDir(item);
                    final Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateBundle(bundleForLoader, mDirFiles);
                            manageLoader(mLoadFiles, bundleForLoader, myLoaderCallback());



                            }

                    }, ANTIMATION_TIMEOUT);
                } else {
                    openFile(Uri.fromFile(item));
                }
            }
        }, new SimpleFileAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(File item) {
                showDialog(item);
            }
        });
        mRecyclerView.setAdapter(mFilesAdapter);
        manageLoader(mLoadFiles, bundleForLoader, MainActivity.this);
    }

    public void initLandscapeView() {
        mGridLayoutManager = new GridLayoutManager(this, 5);
        mRecyclerView = (RecyclerView) findViewById(R.id.dir_files_recyclerview);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mFilesAdapter = new SimpleFileAdapter(this, new SimpleFileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File item) {
                if (item.isDirectory()) {
                    mDirFiles.setPreviousDir(mDirFiles.getCurrentDir());
                    mDirFiles.setCurrentDir(item);

                    final Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateBundle(bundleForLoader, mDirFiles);
                            manageLoader(mLoadFiles, bundleForLoader, myLoaderCallback());
                        }
                    }, ANTIMATION_TIMEOUT);
                } else {
                    openFile(Uri.fromFile(item));
                }

            }
        }, new SimpleFileAdapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(File item) {
                showDialog(item);
            }
        });
        mRecyclerView.setAdapter(mFilesAdapter);
        manageLoader(mLoadFiles, bundleForLoader, MainActivity.this);
    }


    public void manageLoader(Loader loader, Bundle bundle, LoaderManager.LoaderCallbacks callback) {

        if (loader == null) {
            mLoadManager.initLoader(FILES_LOADER_ID, bundle, callback);
        } else {
            mLoadManager.restartLoader(FILES_LOADER_ID, bundle, callback);
        }
    }

    public void refreshContent() {
        invalidateData();
        getSupportLoaderManager().restartLoader(FILES_LOADER_ID, bundleForLoader, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switchView(newConfig.orientation);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDirFiles.hasPreviousDir()) {
            mDirFiles.setCurrentDir(mDirFiles.getPreviousDir());
            updateBundle(bundleForLoader, mDirFiles);
            manageLoader(mLoadFiles, bundleForLoader, this);
        } else {
            if (back_pressed_time + PERIODTOEXIT > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                mUtilities.showSnackBar(this, R.string.alert_dialog_exit);
                back_pressed_time = System.currentTimeMillis();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent default_folder_activity = new Intent(this, SettingsActivity.class);
            startActivity(default_folder_activity);
            return true;
        }
        if (id == R.id.refresh) {
            refreshContent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openFile(Uri fileUri) {

        String mimeType = mUtilities.getMimeType(fileUri);
        if (mimeType != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            if (intent.resolveActivity(getPackageManager()) != null) {
                this.startActivity(intent);
            }
        } else {
            mUtilities.showSnackBar(this, R.string.alert_dialog_no_app);
        }
    }


    @Override
    public Loader<List<File>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<File>>(this) {

            /* List to cache  */
            List<File> mFolderData = new ArrayList<>();


            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (!mFolderData.isEmpty()) {
                    deliverResult(mFolderData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<File> loadInBackground() {
                DirsFilesModel mPath = args.getParcelable(KEY_LIST_LOAD);
                return mUtilities.getAllContentDir(mPath.getCurrentDir());
            }

            @Override
            public void deliverResult(List<File> data) {
                mFolderData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (null == data || data.isEmpty()) {
            showEmptyMessage();
            mFilesAdapter.setDirContentData(data);
            showDirContentData();
        } else {
            mFilesAdapter.setDirContentData(data);
            showDirContentData();
        }

    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {

    }

    private void showEmptyMessage() {
        Toast.makeText(this, R.string.empty_directory, Toast.LENGTH_SHORT).show();
    }

    private void invalidateData() {
        mFilesAdapter.setDirContentData(new ArrayList<File>());
    }

    private void showDirContentData() {
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showDialog(File file) {
        DeleteAlertDialogFragment newFragment = DeleteAlertDialogFragment.newInstance(
                file, mFilesAdapter, mLoadManager, bundleForLoader, FILES_LOADER_ID, this);
        newFragment.show(getFragmentManager(), "dialog");
    }


    /**
     * Requests the read and write storage permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestStoragePermission() {
        View rootView = this.getWindow().getDecorView().findViewById(android.R.id.content);
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(rootView, R.string.ask_permission_read_write,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.alert_dialog_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    PERMISSIONS_STORAGE,
                                    REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {

            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        View rootView = this.getWindow().getDecorView().findViewById(android.R.id.content);

        if (requestCode == REQUEST_STORAGE) {

            // We have requested multiple permissions for storage, so all of them need to be
            // checked.
            if (mUtilities.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display folder fragment.
                Snackbar.make(rootView, R.string.permission_read_write_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                Snackbar.make(rootView, R.string.permission_read_write_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public static class DeleteAlertDialogFragment extends DialogFragment {

        private static File mfile;
        private static LoaderManager.LoaderCallbacks mCallbacks;
        private static int mloadId;
        private static LoaderManager mLoadManager;
        private static SimpleFileAdapter mSimpleFileAdapter;
        private static Bundle mBundle;


        public static DeleteAlertDialogFragment newInstance(File file, SimpleFileAdapter simpleFileAdapter, LoaderManager loaderManager, Bundle bundle, int loadId, LoaderManager.LoaderCallbacks callback) {
            mfile = file;
            mSimpleFileAdapter = simpleFileAdapter;
            mLoadManager = loaderManager;
            mloadId = loadId;
            mCallbacks = callback;
            mSimpleFileAdapter = simpleFileAdapter;
            mBundle = bundle;
            DeleteAlertDialogFragment frag = new DeleteAlertDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int title = R.string.alert_delete_dialog_title;

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.alert_dialog_icon)
                    .setTitle(title)
                    .setMessage(this.mGetFile().isDirectory() ? R.string.alert_dialog_question_folder : R.string.alert_dialog_question_file)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    doPositiveClick(mSimpleFileAdapter, mLoadManager, mloadId, mBundle, mCallbacks);
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    doNegativeClick();
                                }
                            }
                    )
                    .create();
        }

        private File mGetFile() {
            return mfile;
        }

        public void doPositiveClick(SimpleFileAdapter simpleFileAdapter, LoaderManager loaderManager, int idLoader, Bundle bundle, LoaderManager.LoaderCallbacks
                callback) {
            if (mfile.delete()) {
                //invalidate data before refresh
                simpleFileAdapter.setDirContentData(new ArrayList<File>());
                //refresh after delete
                loaderManager.restartLoader(idLoader, bundle, callback);
            } else {
                Utilities.showSnackBar(getActivity(), R.string.alert_delete_dialog_no_sucess);
            }
        }

        public void doNegativeClick() {

        }

    }

}


