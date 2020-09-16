package com.example.hospiton;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
    private List<androidcontacts>contacts;
    final private ListItemClickListener listItemClickListener;


    public interface ListItemClickListener
    {
        void onListItemClicked(int Position);
    }


    public ContactsAdapter(List<androidcontacts>contacts,ListItemClickListener listItemClickListener)
    {
        this.listItemClickListener=listItemClickListener;
        this.contacts=contacts;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context=viewGroup.getContext();
        int layoutidforlistitem=R.layout.contactlistitems;
        LayoutInflater inflater=LayoutInflater.from(context);
        View view = inflater.inflate(layoutidforlistitem,viewGroup,false);

        ContactsViewHolder contactsViewHolder=new ContactsViewHolder(view);
        return contactsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int position) {
        androidcontacts mandroidcontacts=contacts.get(position);
        contactsViewHolder.usercontacts.setText(mandroidcontacts.getName());
    }

    public void notify_data(List<androidcontacts>contacts)
    {
       this.contacts=contacts;
       notifyDataSetChanged();
    }

    public androidcontacts getitem(int Position)
    {
        return contacts.get(Position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView usercontacts;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.phone_logo);
            usercontacts=(TextView)itemView.findViewById(R.id.contacts);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("Contacts Adapter","On click invoked");
            int position=getAdapterPosition();
            listItemClickListener.onListItemClicked(position);
        }
    }
}
