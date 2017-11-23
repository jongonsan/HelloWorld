package com.jgs.helloworld.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jgs.helloworld.R;
import com.jgs.helloworld.fragments.UserListFragment;
import com.jgs.helloworld.model.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private ArrayList<User> users;
    private Fragment fragment;

    public UserAdapter(ArrayList<User> users, Fragment fragment) {
        this.users = users;
        this.fragment = fragment;
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout ll = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_user, parent, false);
        return new ViewHolder(ll);
    }

    @Override
    public void onBindViewHolder(final UserAdapter.ViewHolder holder, final int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getName());
        holder.tvDate.setText(user.getStrBirthdate());
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((UserListFragment)fragment).showEditUserFragment(position);
                //holder.ll.setBackgroundColor(fragment.getResources().getColor(R.color.colorPrimary));
            }
        });
        holder.ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((UserListFragment)fragment).showDeleteDialog(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout ll;
        TextView tvName;
        TextView tvDate;

        public ViewHolder(LinearLayout _ll) {
            super(_ll);
            ll = _ll;
            tvName = (TextView)ll.findViewById(R.id.tv_rUser_name);
            tvDate = (TextView)ll.findViewById(R.id.tv_rUser_date);
        }
    }
}
