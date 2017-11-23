package com.jgs.helloworld.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.AttrRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jgs.helloworld.R;
import com.jgs.helloworld.model.User;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditUserDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    UserListFragment targetFragment;
    View view;
    EditText etName;
    EditText etBirthdate;
    ImageView ivCalendar;
    ActionBar actionBar;
    Bundle arguments;
    User user;

    boolean landscape;

    public EditUserDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_user, container, false);
        targetFragment = (UserListFragment) getTargetFragment();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Se comprueba si la orientación de la pantalla es horizontal
        landscape = getActivity().findViewById(R.id.landscape_layoutR) != null;

        etName = (EditText)view.findViewById(R.id.et_new_name);
        etName.requestFocus();
        etBirthdate = (EditText)view.findViewById(R.id.et_new_date);
        ivCalendar = (ImageView)view.findViewById(R.id.iv_calendar);
        ivCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        if ((arguments = getArguments()) != null){
            user = (User) arguments.getSerializable("USER");
            etName.setText(user.getName());
            etName.selectAll();
            etBirthdate.setText(user.getStrBirthdate());
        }

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (user != null){
                actionBar.setTitle(R.string.edit_dialog_title);
            } else {
                actionBar.setTitle(R.string.new_dialog_title);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        // En orientación horizontal se quita el botón de añadir al cargar este fragmento
        if (landscape){
            ((FloatingActionButton)getActivity().findViewById(R.id.fab_new_land)).setVisibility(View.INVISIBLE);
            // Y se ajusta el margen del layout
            ((ImageView)view.findViewById(R.id.ivUser)).setImageResource(R.drawable.user);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStop() {
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.app_name);
        }
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_dialog, menu);
        if (user == null){
            menu.removeItem(R.id.action_delete);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            String name = etName.getText().toString();
            String strDate = etBirthdate.getText().toString();

            // Se comprueba que no haya campos vacíos
            if (name.trim().equals("")){
                Toast.makeText(getContext(), R.string.invalid_name, Toast.LENGTH_SHORT).show();
                etName.selectAll();
                etName.requestFocus();
            }
            else if (strDate.equals("")){
                Toast.makeText(getContext(), R.string.invalid_date, Toast.LENGTH_SHORT).show();
                etBirthdate.requestFocus();
            }
            // Se convierte la fecha al formato adecuado para hacer uso del servicio REST
            else{
                SimpleDateFormat dateFormatUI = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateFormatJSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                try {
                    Date date = dateFormatUI.parse(strDate);
                    String jsonDate = dateFormatJSON.format(date);
                    if (user == null){
                        new CreateUserTask().execute("create", name, jsonDate);
                    } else {
                        // Si es una edición, se edita en la lista antes de hacer uso del servicio REST,
                        // por si el usuario decide 'Deshacer' la acción
                        int position = arguments.getInt("POSITION");
                        targetFragment.al_users.set(position, new User(user.getId(), name, date));
                        targetFragment.rv.getAdapter().notifyItemChanged(position);
                        targetFragment.rv.scrollToPosition(position);
                        showSnackBar(name, jsonDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                close();
            }
            return true;
        }
        else if (id == R.id.action_delete){
            DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
            dialogFragment.setArguments(arguments);
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(),""+user.getId());
        }
        else if (id == android.R.id.home) {
            close();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void close(){
        if (landscape){
            ((FloatingActionButton)getActivity().findViewById(R.id.fab_new_land)).setVisibility(View.VISIBLE);
            dismiss();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear+1, year);
        etBirthdate.setText(date);
    }

    private void showDatePicker(){
        int year = 2000;
        int month = 0;
        int day = 1;
        boolean chooseYear = true;
        if (user != null){
            year = user.getBirthdate().getYear() + 1900;
            month = user.getBirthdate().getMonth();
            day = user.getBirthdate().getDate();
            chooseYear = false;
        }
        DatePickerDialog dialog = DatePickerDialog.newInstance(this, year, month, day);
        dialog.showYearPickerFirst(chooseYear);
        Calendar today = Calendar.getInstance();
        dialog.setMaxDate(today);
        dialog.setTitle(getResources().getString(R.string.birthdate_picker_title));
        dialog.show(getActivity().getFragmentManager(), getString(R.string.date_picker_dialog_tag));
    }

    public void showSnackBar(final String name, final String date){
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.main_layout),
                R.string.updated_confirm, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se deshacen los cambios en la lista
                int position = arguments.getInt("POSITION");
                targetFragment.al_users.set(position, user);
                targetFragment.rv.getAdapter().notifyItemChanged(position);
                targetFragment.rv.scrollToPosition(position);
            }
        });
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                // Si el usuario no 'deshace' la acción de editar, se llama al REST para que la lleve a cabo
                if (event != DISMISS_EVENT_ACTION){
                    new CreateUserTask().execute("update", name, date, ""+user.getId());
                }
            }
        });
        snackbar.show();
    }

    public class CreateUserTask extends AsyncTask<String, Void, StringBuilder>{

        @Override
        protected StringBuilder doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            String line;

            Uri uri = new Uri.Builder().scheme("https").authority("hello-world.innocv.com").path("api/user")
                    .appendPath(params[0])
                    .build();
            try {
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonUser = new JSONObject();
                jsonUser.put("name", params[1]);
                jsonUser.put("birthdate", params[2]);
                if (user != null){
                    jsonUser.put("id", params[3]);
                }
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                bw.write(jsonUser.toString());
                bw.flush();
                bw.close();
                connection.getOutputStream().close();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    result = result.append(line);
                }

                connection.disconnect();
            } catch (Exception e) {
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {

            try {
                JSONObject jsonUser = new JSONObject(stringBuilder.toString());
                int idUser = jsonUser.getInt("id");
                String name = jsonUser.getString("name");
                String strDate = jsonUser.getString("birthdate");
                // Parse Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                Date birthdate = dateFormat.parse(strDate);
                User savedUser = new User(idUser, name, birthdate);

                // Se actualiza la lista de usuarios en el fragmento de la lista
                if (user == null){
                    targetFragment.al_users.add(savedUser);
                    targetFragment.rv.getAdapter().notifyItemInserted(targetFragment.al_users.size()-1);
                    targetFragment.rv.scrollToPosition(targetFragment.al_users.size()-1);
                    Toast.makeText(targetFragment.getContext(), R.string.created_confirm, Toast.LENGTH_SHORT).show();
                }

            } catch (NullPointerException e) {
                Toast.makeText(targetFragment.getContext(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
