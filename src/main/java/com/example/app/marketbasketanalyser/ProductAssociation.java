package com.example.app.marketbasketanalyser;

import android.app.ProgressDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Rangana on 5/31/2017.
 */

public class ProductAssociation extends Fragment {

    private ArrayList<String> i1 = new ArrayList<String>();
    private ArrayList<String> i2 = new ArrayList<String>();
    private ArrayList<String> con = new ArrayList<String>();
    private ArrayList<String> li = new ArrayList<String>();

    private String col1;
    private String col2;

    private TableLayout tl;
    private TypedArray profile_pics;

    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private ProgressDialog pd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.productassociation, container, false);
        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        mRef = database.getReference().child("Total").child("Country");

        pd = new ProgressDialog(getView().getContext());

        profile_pics = getResources().obtainTypedArray(R.array.profile_pics);
        tl = (TableLayout) getView().findViewById(R.id.tableLayout1);

        for(int j = 0; j<5; j++){
            deleteTable();
        }

        firebaseData();

    }

    private void deleteTable(){
        int count = tl.getChildCount();
        for(int i = 0; i < count; i++){
            View child = tl.getChildAt(i);
            tl.removeView(child);
        }
        i1.clear();
        i2.clear();
        con.clear();
        li.clear();
    }

    private void firebaseData() {
        pd.setMessage("Loading please wait..");
        pd.show();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String it1 = postSnapshot.child("antecedents").getValue().toString();
                    String it2 = postSnapshot.child("consequents").getValue().toString();
                    String co =  postSnapshot.child("confidence").getValue().toString();
                    String lis = postSnapshot.child("lift").getValue().toString();
//
                    i1.add(it1);
                    i2.add(it2);
                    con.add(co);
                    li.add(lis);

                }
                display();
                pd.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void display() {
        for (int x = 0; x < i1.size(); x++) {

            col1 = "\t" + i1.get(x) + "\n\t" + i2.get(x);
            col2 = "<b>" + " Confidence: " + "</b>" + con.get(x) + "%<br/> &emsp &emsp &emsp <b>" + "Lift: " + "</b>" + li.get(x);

            TableRow newRow = new TableRow(getView().getContext());

            TextView column1 = new TextView(getView().getContext());
            TextView column2 = new TextView(getView().getContext());
            ImageView valueTV = new ImageView(getView().getContext());

            valueTV.setImageResource(profile_pics.getResourceId(x, -1));
            valueTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            column1.setText(col1);
            column2.setText(Html.fromHtml(col2));

            newRow.addView(valueTV);
            newRow.addView(column1);
            newRow.addView(column2);

            tl.addView(newRow, new TableLayout.LayoutParams());
        }
    }
}
