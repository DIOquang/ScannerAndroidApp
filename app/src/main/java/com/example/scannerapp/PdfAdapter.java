package com.example.scannerapp; // Thay bằng package của bạn

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {

    private final List<PdfFile> pdfFiles;
    private final Context context;

    public PdfAdapter(Context context, List<PdfFile> pdfFiles) {
        this.context = context;
        this.pdfFiles = pdfFiles;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfFile pdfFile = pdfFiles.get(position);
        holder.pdfName.setText(pdfFile.getName());

        // Định dạng kích thước file và ngày tháng
        String fileSize = Formatter.formatShortFileSize(context, pdfFile.getSize());
        String fileDate = DateFormat.format("dd/MM/yyyy", pdfFile.getDateModified() * 1000).toString();
        holder.pdfDetails.setText(String.format("%s - %s", fileSize, fileDate));
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView pdfName, pdfDetails;
        ImageView pdfIcon;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);
            pdfDetails = itemView.findViewById(R.id.pdf_details);
            pdfIcon = itemView.findViewById(R.id.pdf_icon);
        }
    }
}