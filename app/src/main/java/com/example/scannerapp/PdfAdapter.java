package com.example.scannerapp; // Thay bằng package của bạn

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Thêm import này

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
        // Chắc chắn rằng bạn đang dùng đúng layout, ví dụ: R.layout.item_pdf
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdf, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfFile pdfFile = pdfFiles.get(position);
        holder.pdfName.setText(pdfFile.getName());

        // Định dạng kích thước file và ngày tháng
        String fileSize = Formatter.formatShortFileSize(context, pdfFile.getSize());
        // Sửa lại: Không cần nhân với 1000 vì timestamp đã ở dạng mili giây
        String fileDate = DateFormat.format("dd/MM/yyyy HH:mm", pdfFile.getDateModified()).toString();
        holder.pdfDetails.setText(String.format("%s • %s", fileSize, fileDate));

        // ✅ THÊM CHỨC NĂNG MỞ FILE KHI CLICK VÀO
        holder.itemView.setOnClickListener(v -> {
            Uri fileUri = pdfFile.getUri();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");

            // Cờ này rất quan trọng để cấp quyền cho ứng dụng khác đọc file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Xử lý trường hợp máy không có ứng dụng đọc PDF
                Toast.makeText(context, "Không tìm thấy ứng dụng để mở file PDF.", Toast.LENGTH_SHORT).show();
            }
        });
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
            // Chắc chắn rằng các ID này khớp với file layout item_pdf.xml của bạn
            pdfName = itemView.findViewById(R.id.pdf_name);
            pdfDetails = itemView.findViewById(R.id.pdf_details);
            pdfIcon = itemView.findViewById(R.id.pdf_icon);
        }
    }
}