package com.dev.sendit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev.sendit.Models.ListModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditListingActivity extends AppCompatActivity {

    String facebook_id, firstName;

    ImageView imgBack, imgLocation;
    TextView txtLocation, txtPrice;
    Button btnSubmit;
    EditText listingDescription;
    Spinner spType;

    String desc;
    String key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlisting);

        facebook_id = MainActivity.shared.getString("ID", null);
        firstName = MainActivity.shared.getString("FIRST_NAME", null);

        imgBack = findViewById(R.id.imgBack);
        imgLocation = findViewById(R.id.imgLocation);
        txtLocation = findViewById(R.id.txtLocation);
        txtPrice = findViewById(R.id.txtPrice);
        listingDescription = findViewById(R.id.addListingDesc);
        spType = findViewById(R.id.listingType);
        btnSubmit = findViewById(R.id.btnSubmit);

        Intent gintent = getIntent();
        Bundle b = gintent.getExtras();

        if (b != null) {
            String type = (String) b.get("Type");
            String loc = (String) b.get("Location");
            desc = (String) b.get("Description");
            String price = (String) b.get("Price");

            spType.setSelection(getIndex(spType, type));
            txtLocation.setText(loc);
            listingDescription.setText(desc);
            txtPrice.setText(price);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(listingDescription.toString()) && TextUtils.isEmpty(txtLocation.toString()) &&
                        TextUtils.isEmpty(txtPrice.toString())) {
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ListModel listing = snapshot.getValue(ListModel.class);

                                if (listing.getDescription().equals(desc)) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("Author", facebook_id);
                                    hashMap.put("Name", firstName);
                                    hashMap.put("Type", spType.getSelectedItem().toString());
                                    hashMap.put("Description", listingDescription.getText().toString());
                                    hashMap.put("Location", txtLocation.getText().toString());
                                    hashMap.put("Price", txtPrice.getText().toString());

                                    ref.child(snapshot.getKey()).updateChildren(hashMap);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    Toast.makeText(EditListingActivity.this, "Successfully edited a listing!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditListingActivity.this, BrowseActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditListingActivity.this, "You must specify the description, location and price!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        imgLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                } else {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    try {
                        String city = getLocation(location.getLatitude(), location.getLongitude());

                        txtLocation.setText(city);
                        txtLocation.setEnabled(false);
                    } catch(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditListingActivity.this, "Location service unable to locate device!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        overridePendingTransition(0, 0);
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    try {
                        String city = getLocation(location.getLatitude(), location.getLongitude());

                        txtLocation.setText(city);
                    } catch(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditListingActivity.this, "Location service unable to locate device!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Location service unable to locate device!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String getLocation(double lat, double lon) {
        String city = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(lat, lon, 10);

            if (addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                        city = adr.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return city;
    }
}