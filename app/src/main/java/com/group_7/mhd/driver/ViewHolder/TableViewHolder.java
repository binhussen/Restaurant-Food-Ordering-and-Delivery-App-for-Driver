package com.group_7.mhd.driver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.group_7.mhd.driver.R;
import com.rey.material.widget.CheckBox;


public class TableViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnCreateContextMenuListener*/{

    public TextView txtOrderId,txtOrderStatus,txtOrderAddress,txtOrderPhone,txtOrderDate;
    // ContextMenu contextMenu;
    public ImageView btn_shipped;
    public CheckBox chkpayemnt;

    public TableViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress=itemView.findViewById(R.id.order_address);
        txtOrderStatus=itemView.findViewById(R.id.order_status);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderId=itemView.findViewById(R.id.order_name);
        txtOrderDate=itemView.findViewById(R.id.order_date);

        btn_shipped = (ImageView) itemView.findViewById(R.id.btn_shipped);
        chkpayemnt = itemView.findViewById(R.id.chkpayment);

    }
}
