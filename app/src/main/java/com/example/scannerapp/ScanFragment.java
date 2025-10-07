package com.example.scannerapp; // Thay thế bằng package của bạn

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private RecyclerView recyclerView;
    private PdfAdapter adapter;
    private List<PdfFile> pdfFileList;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private LinearLayout permissionRequestLayout;
    private Button selectFolderButton;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "ScannerAppPrefs";
    private static final String KEY_FOLDER_URI = "folderUri";

    // Launcher để xử lý kết quả trả về từ trình chọn thư mục
    private final ActivityResultLauncher<Uri> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    // Người dùng đã chọn một thư mục
                    // Lấy quyền truy cập vĩnh viễn
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    // Lưu lại URI vào SharedPreferences
                    prefs.edit().putString(KEY_FOLDER_URI, uri.toString()).apply();

                    // Ẩn giao diện yêu cầu và bắt đầu tải file
                    permissionRequestLayout.setVisibility(View.GONE);
                    loadPdfFilesFromSafUri(uri);
                } else {
                    // Người dùng đã hủy
                    Toast.makeText(getContext(), "Bạn đã không chọn thư mục.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        recyclerView = view.findViewById(R.id.recycler_view_pdfs);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyView = view.findViewById(R.id.empty_view_text);
        permissionRequestLayout = view.findViewById(R.id.layout_permission_request);
        selectFolderButton = view.findViewById(R.id.btn_select_folder);

        setupRecyclerView();

        selectFolderButton.setOnClickListener(v -> {
            // Mở trình chọn thư mục của hệ thống
            folderPickerLauncher.launch(null);
        });

        checkFolderPermission();
    }

    private void setupRecyclerView() {
        pdfFileList = new ArrayList<>();
        adapter = new PdfAdapter(getContext(), pdfFileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Kiểm tra xem đã có quyền truy cập thư mục chưa.
     * Nếu có, tải file. Nếu chưa, hiển thị yêu cầu.
     */
    private void checkFolderPermission() {
        String savedUriString = prefs.getString(KEY_FOLDER_URI, null);
        if (savedUriString != null) {
            // Đã có URI được lưu, bắt đầu tải file
            Uri folderUri = Uri.parse(savedUriString);
            loadPdfFilesFromSafUri(folderUri);
        } else {
            // Chưa có, hiển thị giao diện yêu cầu
            permissionRequestLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Tải danh sách tệp PDF từ một URI thư mục đã được cấp quyền bằng SAF.
     * @param folderUri URI của thư mục.
     */
    private void loadPdfFilesFromSafUri(Uri folderUri) {
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<PdfFile> foundFiles = new ArrayList<>();
            if (getContext() == null) return;

            ContentResolver contentResolver = getContext().getContentResolver();
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri,
                    DocumentsContract.getTreeDocumentId(folderUri));

            String[] projection = {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
            };

            // Chỉ tìm các file PDF
            String selection = DocumentsContract.Document.COLUMN_MIME_TYPE + " = ?";
            String[] selectionArgs = {"application/pdf"};

            try (Cursor cursor = contentResolver.query(childrenUri, projection, selection, selectionArgs, null)) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                    int dateColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
                    int sizeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE);

                    while (cursor.moveToNext()) {
                        String docId = cursor.getString(idColumn);
                        String name = cursor.getString(nameColumn);
                        long dateModified = cursor.getLong(dateColumn);
                        long size = cursor.getLong(sizeColumn);

                        // Tạo URI cho từng tệp con
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId);
                        foundFiles.add(new PdfFile(fileUri, name, dateModified, size));
                    }
                }
            } catch (Exception e) {
                Log.e("ScanFragment", "Lỗi khi truy vấn file bằng SAF", e);
            }

            updateUiOnMainThread(foundFiles);
        });
    }

    private void updateUiOnMainThread(final List<PdfFile> foundFiles) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                loadingIndicator.setVisibility(View.GONE);
                if (foundFiles.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    pdfFileList.clear();
                    pdfFileList.addAll(foundFiles);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


}