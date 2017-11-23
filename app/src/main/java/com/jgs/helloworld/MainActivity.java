package com.jgs.helloworld;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jgs.helloworld.fragments.EditUserDialogFragment;


public class MainActivity extends AppCompatActivity {
    public boolean landscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Se comprueba si la orientación de la pantalla es horizontal
        landscape = findViewById(R.id.landscape_layoutR) != null;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            FragmentManager fm = getSupportFragmentManager();
            EditUserDialogFragment editUserFragment = (EditUserDialogFragment) fm.findFragmentByTag(getString(R.string.edit_user_frag_tag));
            boolean fromLandscape = savedInstanceState.getBoolean("LANDSCAPE");
            // Si se ha cambiado de orientación horizontal a vertical y estabamos editando usuario,
            // se oculta la lista y se muestra el fragmento de edición
            if (fromLandscape && !landscape && editUserFragment != null){
                fm.beginTransaction().remove(editUserFragment).commit();
                fm.executePendingTransactions();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fm.findFragmentByTag(getString(R.string.user_list_frag_tag)));
                ft.add(R.id.main_layout, editUserFragment, getString(R.string.edit_user_frag_tag))
                        .addToBackStack(null).commit();
            }
            // Si se ha cambiado de orientación vertical a horizontal y estabamos editando usuario,
            // se elimina el fragmento de edición y se crea en su nuevo contenedor
            else if(!fromLandscape && landscape && editUserFragment != null){
                editUserFragment.dismiss();
                fm.popBackStack();
                fm.executePendingTransactions();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.landscape_layoutR, editUserFragment, getString(R.string.edit_user_frag_tag));
                ft.commit();
            }

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("LANDSCAPE", landscape);
        super.onSaveInstanceState(outState);
    }

}
