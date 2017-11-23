package com.jgs.helloworld.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.jgs.helloworld.R;
import com.jgs.helloworld.model.User;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteDialogFragment extends DialogFragment {

    public DeleteDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.del_dialog_title);
        builder.setMessage(R.string.del_dialog_message);
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteUserTask().execute("remove", getTag());
                closeTargetFragment();
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }

    public void closeTargetFragment() {
        EditUserDialogFragment editUserDialogFragment = (EditUserDialogFragment) getTargetFragment();
        if (editUserDialogFragment != null){
            editUserDialogFragment.close();
        }
    }

    public class DeleteUserTask extends AsyncTask<String, Void, Integer> {
        UserListFragment targetFragment = (UserListFragment)getFragmentManager()
                .findFragmentByTag(getString(R.string.user_list_frag_tag));

        @Override
        protected Integer doInBackground(String... params) {
            Integer responseCode = null;

            Uri uri = new Uri.Builder().scheme("https").authority("hello-world.innocv.com").path("api/user")
                    .appendPath(params[0])
                    .appendQueryParameter("id", params[1])
                    .build();
            try {
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                responseCode = connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == null){
                Toast.makeText(targetFragment.getContext(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
            else if (responseCode == HttpURLConnection.HTTP_OK){
                // Se obtienen los argumentos (usuario eliminado y posición de este en el Adapter), para actualizar la lista
                Bundle b = getArguments();
                int position = b.getInt("POSITION");
                targetFragment.al_users.remove(position);
                targetFragment.rv.getAdapter().notifyItemRemoved(position);
                Toast.makeText(targetFragment.getContext(), R.string.deleted_confirm, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(targetFragment.getContext(), R.string.deleted_failed, Toast.LENGTH_LONG).show();
            }
        }
    }
}
