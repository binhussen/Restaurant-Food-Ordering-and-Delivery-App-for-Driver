package com.group_7.mhd.driver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.group_7.mhd.driver.R;
import com.rey.material.widget.CheckBox;


public class OrderViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnCreateContextMenuListener*/{

    public TextView txtOrderId,txtOrderStatus,txtOrderAddress,txtOrderPhone,txtOrderDate;
   // ContextMenu contextMenu;
    public LinearLayout btn_Shipping;
    public ImageView btn_call, btn_shipped, btn_mapme, btn_google, imglogo;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress=itemView.findViewById(R.id.order_address);
        txtOrderStatus=itemView.findViewById(R.id.order_status);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderId=itemView.findViewById(R.id.order_name);
        txtOrderDate=itemView.findViewById(R.id.order_date);

        btn_Shipping = (LinearLayout) itemView.findViewById(R.id.btnShipping);

        btn_call = (ImageView) itemView.findViewById(R.id.btn_call);
        btn_shipped = (ImageView) itemView.findViewById(R.id.btn_shipped);
        btn_mapme = (ImageView) itemView.findViewById(R.id.btn_maps_me);
        /*btn_google = (ImageView) itemView.findViewById(R.id.btn_googlemap);*/

        imglogo = (ImageView) itemView.findViewById(R.id.imagelogo);

    }
}
