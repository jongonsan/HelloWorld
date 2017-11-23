package com.jgs.helloworld.fragments;


import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jgs.helloworld.R;
import com.jgs.helloworld.adapters.UserAdapter;
import com.jgs.helloworld.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment {
    View view;
    ArrayList<User> al_users = new ArrayList<>();
    RecyclerView rv;
    boolean landscape;
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showEditUserFragment(-1);
        }
    };

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Se comprueba si la orientación de la pantalla es horizontal
        landscape = getActivity().findViewById(R.id.landscape_layoutR) != null;
        // Si ha cambiado a orientación horizontal desde la pantalla de edición de usuario,
        // se debe mostrar el fragmento con la lista
        if (landscape && isHidden()){
            getFragmentManager().beginTransaction().show(this).commit();
        }

        new GetAllUsersTask().execute("getall");
        rv = (RecyclerView)view.findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new UserAdapter(al_users, this));

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_new);
        if (!landscape){
            fab.setOnClickListener(fabListener);
        } else {
            fab.setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.fab_new_land).setOnClickListener(fabListener);
        }
    }

    public void showEditUserFragment(int position){
        Fragment fragment = new EditUserDialogFragment();
        fragment.setTargetFragment(this, 0);

        if (position >= 0){
            User user = al_users.get(position);
            Bundle b = new Bundle();
            b.putSerializable("USER", user);
            b.putInt("POSITION", position);
            fragment.setArguments(b);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // Si la pantalla está en orientación vertical, se oculta la lista
        if (!landscape){
            transaction.hide(this);
            transaction.add(R.id.main_layout, fragment, getString(R.string.edit_user_frag_tag))
                    .addToBackStack(null).commit();
        }
        // Si la pantalla está en orientación horizontal, se reemplaza el fragmento de edición a la dcha
        else{
            transaction.replace(R.id.landscape_layoutR, fragment, getString(R.string.edit_user_frag_tag)).commit();
        }
    }

    public void showDeleteDialog(int position){
        User user = al_users.get(position);
        Bundle b = new Bundle();
        b.putSerializable("USER", user);
        b.putInt("POSITION", position);
        DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
        dialogFragment.setArguments(b);
        dialogFragment.show(getFragmentManager(),""+user.getId());
    }

    public class GetAllUsersTask extends AsyncTask<String, Void, StringBuilder> {

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
                JSONArray allUsers = new JSONArray(stringBuilder.toString());
                for (int i=0; i<allUsers.length(); i++) {
                    JSONObject jsonUser = allUsers.getJSONObject(i);
                    int idUser = jsonUser.getInt("id");
                    String name = jsonUser.getString("name");
                    String strDate = jsonUser.getString("birthdate");
                    // Parse Date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                    Date birthdate = dateFormat.parse(strDate);
                    al_users.add(new User(idUser, name, birthdate));
                }
                rv.getAdapter().notifyDataSetChanged();

            } catch (NullPointerException e) {
                Toast.makeText(getContext(), R.string.connection_failed, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.data_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
