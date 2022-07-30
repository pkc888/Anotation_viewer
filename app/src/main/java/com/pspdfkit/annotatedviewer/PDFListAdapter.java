package com.pspdfkit.annotatedviewer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class PDFListAdapter extends RecyclerView.Adapter<PDFListAdapter.ViewHolder> {
    private List<PDFModel> pdfList;
    private ClickListener listener;
    private Context context;

    public PDFListAdapter(Context context, List<PDFModel> pdfList, ClickListener listener) {
        this.pdfList = pdfList;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PDFModel pdfModel = pdfList.get(position);
        try {
            Uri returnUri = pdfModel.getName();
            Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            holder.textView.setText(returnCursor.getString(nameIndex));

            Log.d("size ", Long.toString(returnCursor.getLong(sizeIndex) / 1000000) + "MB");
            holder.size.setText((returnCursor.getLong(sizeIndex) / 1000000) + "MB");
        } catch (Exception e) {
            String[] namePart = pdfModel.getName().getPath().split("/");
            holder.textView.setText(namePart[namePart.length - 1]);
        }

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(pdfModel, view);
            }
        });
    }

    public void generateImageFromPdf(Uri pdfUri, ImageView imageView) {

//        int pageNumber = 0;
//        PdfiumCore pdfiumCore = new PdfiumCore(context);
//        try {
//            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
//            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
//            pdfiumCore.openPage(pdfDocument, pageNumber);
//            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
//            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
//            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
//            imageView.setImageBitmap(bmp);
//            pdfiumCore.closeDocument(pdfDocument);
//        } catch(Exception e) {
//            //todo with exception
//        }
    }


    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView, size;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.icon);
            this.textView = (TextView) itemView.findViewById(R.id.pdfname);
            this.size = (TextView) itemView.findViewById(R.id.size);
            this.linearLayout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public interface ClickListener {
        public void onClick(PDFModel pdfModel, View v);
    }
}