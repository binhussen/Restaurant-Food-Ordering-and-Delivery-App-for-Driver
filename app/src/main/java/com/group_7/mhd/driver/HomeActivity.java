package com.group_7.mhd.driver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.group_7.mhd.driver.Common.Common;
import com.group_7.mhd.driver.Model.Request;
import com.group_7.mhd.driver.Model.Token;
import com.group_7.mhd.driver.ViewHolder.OrderViewHolder;
import com.group_7.mhd.driver.ViewHolder.TableViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^"+
                    "(?=.*[a-z])" +     //at least one lowercase
                    "(?=.*[A-Z])" +     //at least one upercase
                    "(?=.*[0-9])" +     //at least one digit
                    "(?=.*[@#$%^&+=])" +    //at least one special character
                    "(?=\\S+$)" +          //no white space
                    ".{6,}" +               //at least six digit
                    "$");

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Name in top of navigation drawer
    TextView txtFullName,textphone;

    FirebaseDatabase database;
    DatabaseReference shipperOrders;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseRecyclerAdapter<Request, TableViewHolder> tadapter;

    Location mLastLocation;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.diver);
        setSupportActionBar(toolbar);


        //int paper
        Paper.init(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name for user
        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.text_fullName);
        txtFullName.setText(Common.currentShipper.getName().toString());

        textphone = headerView.findViewById(R.id.text_phone);
        textphone.setText(Common.currentShipper.getPhone().toString());

        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, Common.REQUEST_CODE);

        } else {
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        }

        database = FirebaseDatabase.getInstance();
        shipperOrders = database.getReference(Common.ORDER_NEED_SHIPPERS_TABLE)/*.child(Common.currentShipper.getPhone())*/;

        //view
        recyclerView = (RecyclerView)findViewById(R.id.recycler_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        updateTokenShipper(FirebaseInstanceId.getInstance().getToken());


        if (Common.currentShipper.getType().equals("Waiter")) {
            loadAllOrdertable(Common.currentShipper.getPhone());
        }else if (Common.currentShipper.getType().equals("Driver")){
            loadAllOrderNeedShip(Common.currentShipper.getPhone());
        }

    }

    private void loadAllOrdertable(String phone) {
        DatabaseReference orderInChildOfShipper = shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> listOrders = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChildOfShipper,Request.class)
                .build();
        tadapter = new FirebaseRecyclerAdapter<Request, TableViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull TableViewHolder viewHolder, final int position, @NonNull final Request model) {

                viewHolder.txtOrderId.setText(tadapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getTable_No());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(tadapter.getRef(position).getKey())));

                if (getItem(position).getPaymentMethod().equals("COD")){
                    viewHolder.chkpayemnt.setChecked(true);
                }

                viewHolder.btn_shipped.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Common.currentKey = tadapter.getRef(position).getKey();

                        shippedOrder();
                    }
                });

            }

            @NonNull
            @Override
            public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.table_layout,parent,false);
                return new TableViewHolder(itemView);
            }
        };
        tadapter.startListening();
        tadapter.notifyDataSetChanged();
        recyclerView.setAdapter(tadapter);
    }

    private void loadAllOrderNeedShip(String phone) {

        DatabaseReference orderInChildOfShipper = shipperOrders.child(phone);

        FirebaseRecyclerOptions<Request> listOrders = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChildOfShipper,Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddresslat()+" , "+model.getAddresslon());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));


                viewHolder.btn_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel: "+"0"+model.getPhone()));
                        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                        startActivity(intent);
                    }
                });

                viewHolder.btn_shipped.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //delete ordertable,orderneeeshiip,shippingorder,update status of oredr
                        /*Common.createShipperOrder(adapter.getRef(position).getKey(),
                                Common.currentShipper.getPhone(),
                                mLastLocation);
                        Common.currentRequest = model*/;
                        Common.currentKey = adapter.getRef(position).getKey();

                        shippedOrder();
                    }
                });

                viewHolder.btn_mapme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        show_Mapme_Customer_Location(Double.parseDouble(model.getAddresslat()), Double.parseDouble(model.getAddresslon()), model.getName());
                    }
                });

                /*viewHolder.btn_google.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Common.createShipperOrder(adapter.getRef(position).getKey(),
                                Common.currentShipper.getPhone(),
                                mLastLocation);
                        Common.currentRequest = model;
                        Common.currentKey = adapter.getRef(position).getKey();

                        Common.customer_addresslat = model.getAddresslat();
                        Common.customer_addresslon = model.getAddresslon();

                        startActivity(new Intent(HomeActivity.this,TrackingOrder.class));*//*
                        Toast.makeText(HomeActivity.this,"Implement late !!!",Toast.LENGTH_SHORT).show();*//*
                    }
                });*/
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void shippedOrder() {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_NEED_SHIPPERS_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> update_status= new HashMap<>();
                        update_status.put("status","4");

                        FirebaseDatabase.getInstance()
                                .getReference(Common.ORDER_TABLE)
                                .child(Common.currentKey)
                                .updateChildren(update_status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance()
                                                .getReference(Common.SHIPPERS_INFO_TABLE)
                                                .child(Common.currentKey)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(HomeActivity.this,R.string.delivered,Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void updateTokenShipper(String token) {
        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token,false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.currentShipper.getType().equals("Waiter")) {
            loadAllOrdertable(Common.currentShipper.getPhone());
        }else if (Common.currentShipper.getType().equals("Driver")){
            loadAllOrderNeedShip(Common.currentShipper.getPhone());
        }
    }

    @Override
    protected void onStop() {
        if (adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Common.REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        buildLocationRequest();
                        buildLocationCallBack();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                           return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    else
                    {
                        Toast.makeText(this,"You Should assign Permission !",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                Toast.makeText(HomeActivity.this,new StringBuilder("")
                .append(mLastLocation.getLatitude())
                .append("/")
                .append(mLastLocation.getLongitude())
                        .toString(),Toast.LENGTH_SHORT).show();

                super.onLocationResult(locationResult);
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        /*locationRequest.setPriority(5000);*/
        locationRequest.setFastestInterval(3000);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_order) {
            if (Common.currentShipper.getType().equals("Waiter")) {
                loadAllOrdertable(Common.currentShipper.getPhone());
            }else if (Common.currentShipper.getType().equals("Driver")){
                loadAllOrderNeedShip(Common.currentShipper.getPhone());
            }
        } else if (id == R.id.nav_log_out) {
            //Delete Rmwmber user password
            Paper.book().destroy();
            //Logout
            Intent signIn = new Intent(HomeActivity.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        } else if (id == R.id.nav_change_password){
            showChangePasswordDialog();
        } else if (id == R.id.nav_language) {
            showChangeLanguageDialog();
        }else if (id == R.id.nav_mapsme) {
            show_Mapme_Kana_Restaurant();
        }
        else if (id == R.id.nav_location) {
            show_Mapme_My_Location();
        }
        else if (item.getItemId() == R.id.nav_location) {
            startActivity(new Intent(HomeActivity.this, MapsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void show_Mapme_My_Location() {
        final Intent i = new Intent(HomeActivity.this, HomeActivity.class);
        /*i.putExtra(EXTRA_FROM_MWM, true);*/
        PendingIntent pintent = PendingIntent.getActivity(HomeActivity.this, 0, i, 0);

        ArrayList<MWMPoint> points = new ArrayList<>();
        points.add(new MWMPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude(), getString(R.string.myloc)));
        MwmRequest request = new MwmRequest()
                .setTitle(getString(R.string.myloc))
                .setZoomLevel(0.2)
                .setPendingIntent(pintent)
                .setCustomButtonName("Where am i?")
                .setPoints(points)
                .setReturnOnBalloonClick(true);
        MapsWithMeApi.sendRequest(HomeActivity.this, request);
    }

    private void show_Mapme_Kana_Restaurant() {
        /*if (MapsWithMeApi.isMapsWithMeInstalled(Context)){

        }*/
        final Intent i = new Intent(HomeActivity.this, HomeActivity.class);
        /*i.putExtra(EXTRA_FROM_MWM, true);*/
        PendingIntent pintent = PendingIntent.getActivity(HomeActivity.this, 0, i, 0);

        ArrayList<MWMPoint> points = new ArrayList<>();
        points.add(new MWMPoint(7.54457, 37.85178, getString(R.string.slogan)));
        MwmRequest request = new MwmRequest()
                .setTitle(getString(R.string.slogan))
                .setZoomLevel(0.2)
                .setPendingIntent(pintent)
                .setCustomButtonName("Where is you?")
                .setPoints(points)
                .setReturnOnBalloonClick(true);
        MapsWithMeApi.sendRequest(HomeActivity.this, request);
    }

    private void show_Mapme_Customer_Location(double lati, double longt, String name) {
       /*Double.parseDouble(lati), Double.parseDouble(longt)*/

        Intent intent = new Intent("com.mapswithme.maps.pro.action.BUILD_ROUTE");
        intent.setPackage("com.mapswithme.maps.pro");
        PackageManager pm = getPackageManager();

        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
// Check whether MapsMe is installed or not.
        if (infos == null || infos.size() == 0) {
            // If not - open MapsMe page on Google Play
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.mapswithme.maps.pro"));
        }
        else
        {
            intent.putExtra("lat_from", mLastLocation.getLatitude());
            intent.putExtra("lon_from", mLastLocation.getLongitude());
            intent.putExtra("saddr", getString(R.string.myloc));
            intent.putExtra("lat_to", lati);
            intent.putExtra("lon_to", longt);
            intent.putExtra("daddr", name);
            intent.putExtra("router", "vehicle");
        }
// Launch MapsMe.
        startActivity(intent);
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.changep);
        alertDialog.setMessage(R.string.fill);


        alertDialog.setIcon(R.drawable.ic_security_black_24dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //create Dialog and show
        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        //Get AlertDialog from dialog
        final AlertDialog diagview = ((AlertDialog) dialog);
        Button ok = (Button) diagview.findViewById(R.id.ok);
        Button cancel = (Button) diagview.findViewById(R.id.cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    //change password here
                    //fore use spot dialog
                    final android.app.AlertDialog waitDialog = new SpotsDialog(HomeActivity.this);
                    waitDialog.show();

                    //check old password
                    if (edtPassword.getText().toString().equals(Common.currentShipper.getPassword()))
                    {
                        //check new password and repeat password
                        if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                        {
                            Map<String,Object> passwordUpdate = new HashMap<>();
                            passwordUpdate.put("password",edtNewPassword.getText().toString());

                            Paper.book().write(Common.PWD_KEY,edtNewPassword.getText().toString());

                            //make update
                            DatabaseReference driver = FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE);
                            driver.child(Common.currentShipper.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitDialog.dismiss();
                                            Toast.makeText(HomeActivity.this,R.string.passu,Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            dialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(HomeActivity.this,R.string.newpassd,Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this,R.string.wropass,Toast.LENGTH_SHORT).show();
                    }
                }
            }

            private boolean validate() {
                boolean valid = true;

                String oldpass = edtPassword.getText().toString().trim();
                String newpass = edtNewPassword.getText().toString().trim();
                String reppass = edtRepeatPassword.getText().toString().trim();

                if (!PASSWORD_PATTERN.matcher(oldpass).matches()){
                    edtPassword.setError(getString(R.string.wrongpass));
                    valid = false;
                }if (!PASSWORD_PATTERN.matcher(newpass).matches()){
                    edtNewPassword.setError(getString(R.string.err_password));
                    valid = false;
                }if (!reppass.equals(newpass)){
                    edtRepeatPassword.setError(getString(R.string.err_password_confirmation));
                    valid = false;
                }

                return valid;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //langs
    private void showChangeLanguageDialog() {
        final String[] listItems = {"English","አማርኛ"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(R.string.choosel);
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    setLocale("en");
                    recreate();
                }
                else if(i==1){
                    setLocale("am");
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void setLocale(String langs) {
        Locale locale = new Locale(langs);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",langs);
        editor.apply();
    }
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang","");
        setLocale(language);
    }
}













