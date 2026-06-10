package z.app.allever.android.sample.learning.android.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import app.allever.android.lib.core.app.App;

public class Uitls {

    @SuppressLint("WrongConstant")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri;
        if (requestCode == 1000 && (uri = data.getData()) != null) {
            App.context.getContentResolver().takePersistableUriPermission(uri, data.getFlags() & (
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startFor(Activity activity) {
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, uri);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        assert documentFile != null;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        activity.startActivityForResult(intent, 1000);
    }

    public static Uri pathToUri(String path) {
        return Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" +path.replace("/storage/emulated/0/", "").replace("/", "%2F"));
    }
}