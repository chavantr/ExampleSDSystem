package generalknowledge.mywings.com.smartdustbinsystem.process;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {

    public ProgressDialogUtil(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private ProgressDialog progressDialog;

    public void show() {
        progressDialog.show();
    }

    public void hide() {
        progressDialog.hide();
    }

}
