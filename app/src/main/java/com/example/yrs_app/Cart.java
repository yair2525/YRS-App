package com.example.yrs_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yrs_app.Common.Common;
import com.example.yrs_app.Helper.RecyclerItemTouchHelper;
import com.example.yrs_app.Interface.RecyclerItemTouchHelperListener;
import com.example.yrs_app.Model.Order;
import com.example.yrs_app.Model.Request;
import com.example.yrs_app.ViewHolder.CartAdapter;
import com.example.yrs_app.ViewHolder.CartViewHolder;
import com.example.yrs_app.database.Database;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    RelativeLayout rooLayout;

    ProgressDialog progressDoalog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);


        setContentView(R.layout.activity_cart);

        rooLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (FButton) findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toasty.error(Cart.this, "Your Cart is Empty!!!!", Toast.LENGTH_SHORT, true).show();
            }
        });

        loadListFood();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address:");


        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        final MaterialEditText edtAddress = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);
        final CheckBox cashCheckBox = (CheckBox) order_address_comment.findViewById(R.id.cashCheckBox);
        final CheckBox creditCheckBox = (CheckBox) order_address_comment.findViewById(R.id.creditCheckBox);
        final ImageView cashCheckBoxIcon = (ImageView) order_address_comment.findViewById(R.id.cashCheckBoxIcon);
        final ImageView creditCheckBoxIcon = (ImageView) order_address_comment.findViewById(R.id.creditCheckBoxIcon);



        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_baseline_shopping_cart_24);





        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edtAddress.getText().toString().isEmpty())
                {
                    Toasty.error(Cart.this, "Error! Please write your address", Toast.LENGTH_SHORT, true).show();
                    showAlertDialog();
                }
                else
                {
                    if(cashCheckBox.isChecked() && creditCheckBox.isChecked()) {
                        Toasty.error(Cart.this, "Error! Please choose only one option for payment", Toast.LENGTH_SHORT, true).show();
                        showAlertDialog();
                    }
                    else if(cashCheckBox.isChecked())
                    {
                        // Create new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                edtAddress.getText().toString(),
                                txtTotalPrice.getText().toString(),
                                "0",// status
                                edtComment.getText().toString(),
                                cart
                        );

                        // Submit to Firebase
                        // We will use System.CurrentMilli to key
                        requests.child(String.valueOf(System.currentTimeMillis()))
                                .setValue(request);
                        // Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                        Toasty.success(Cart.this, "Your order has been Placed", Toast.LENGTH_SHORT, true).show();
                        finish();
                    }
                    else if(creditCheckBox.isChecked())
                    {
                        paymentCheckout(edtAddress, edtComment);
                    }
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void paymentCheckout(final MaterialEditText edtAddress, final MaterialEditText edtComment) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Last Step!");
        alertDialog.setMessage("Payment:");


        LayoutInflater inflater = this.getLayoutInflater();
        View order_checkout_payment = inflater.inflate(R.layout.order_checkout_payment, null);

        final MaterialEditText edtCardNumber = (MaterialEditText) order_checkout_payment.findViewById(R.id.edtCardNumber);
        final MaterialEditText edtCardNameHolder = (MaterialEditText) order_checkout_payment.findViewById(R.id.edtCardNameHolder);
        final MaterialEditText edtExpiryDateMonth = (MaterialEditText) order_checkout_payment.findViewById(R.id.edtExpiryDateMonth);
        final MaterialEditText edtExpiryDateYear = (MaterialEditText) order_checkout_payment.findViewById(R.id.edtExpiryDateYear);
        final MaterialEditText edtSecurityCode = (MaterialEditText) order_checkout_payment.findViewById(R.id.edtSecurityCode);


        alertDialog.setView(order_checkout_payment);
        alertDialog.setIcon(R.drawable.ic_baseline_payment_24);

        alertDialog.setPositiveButton("Confirm Order", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(edtCardNumber.getText().toString().isEmpty() && edtCardNameHolder.getText().toString().isEmpty() &&
                        edtExpiryDateMonth.getText().toString().isEmpty() && edtExpiryDateYear.getText().toString().isEmpty() &&
                        edtSecurityCode.getText().toString().isEmpty())
                {
                    Toasty.error(Cart.this, "Error! Please fill out the form as required!", Toast.LENGTH_SHORT, true).show();
                    paymentCheckout(edtAddress, edtComment);
                }
                else if(Integer.parseInt(edtExpiryDateMonth.getText().toString()) > 13)
                {
                    Toasty.error(Cart.this, "Error! Please make enter correct month!", Toast.LENGTH_SHORT, true).show();
                    paymentCheckout(edtAddress, edtComment);
                }
                else if(Integer.parseInt(edtExpiryDateYear.getText().toString()) < 2019)
                {
                    Toasty.error(Cart.this, "Error! Please make enter correct year!", Toast.LENGTH_SHORT, true).show();
                    paymentCheckout(edtAddress, edtComment);
                }
                else
                {
                    progressDialogForVerification();// יש קריסה אחרי שמתבצעת פונקציה שולית זו!

                    // Create new Request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            edtAddress.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            "0",// status
                            edtComment.getText().toString(),
                            cart
                    );

                    // Submit to Firebase
                    // We will use System.CurrentMilli to key
                    requests.child(String.valueOf(System.currentTimeMillis()))
                            .setValue(request);
                    // Delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    Toasty.success(Cart.this, "Your order has been Placed", Toast.LENGTH_SHORT, true).show();
                    finish();
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }


    private void progressDialogForVerification() {

        progressDoalog = new ProgressDialog(Cart.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Payment Verification");
        progressDoalog.setTitle("Please wait for confirmation!!");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDoalog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDoalog.getProgress() <= progressDoalog
                            .getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (progressDoalog.getProgress() == progressDoalog
                                .getMax()) {
                            progressDoalog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDoalog.incrementProgressBy(1);
        }
    };


   /* private void sendNotificationOrder(String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);// get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren())
                {
                    Token serverToken = postSnapShot.getValue(Token.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate Total Price
        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        // We will item at List<Order> by Position
        cart.remove(position);
        // After that, We will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        // And final, we will update new data from List<Order> to SQLite
        for(Order item:cart)
            new Database(this).addToCart(item);
        // Refresh after Cart change(such as Delete dish)
        loadListFood();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder)
        {
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId());

            // Update txttotal
            //Calculate Total Price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));//item שונה order
            Locale locale = new Locale("en","US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rooLayout, name+" removed from cart",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    // Update txttotal
                    //Calculate Total Price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));//item שונה order
                    Locale locale = new Locale("en","US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}

