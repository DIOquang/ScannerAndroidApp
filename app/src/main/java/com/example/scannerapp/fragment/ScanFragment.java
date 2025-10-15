package com.example.scannerapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scannerapp.PdfAdapter;
import com.example.scannerapp.PdfFile;
import com.example.scannerapp.R;
import com.example.scannerapp.activity.LoginActivity;
import com.example.scannerapp.activity.MainActivity2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    // --- Biến thành viên ---
    private RecyclerView recyclerView;
    private PdfAdapter adapter;
    private List<PdfFile> pdfFileList;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private LinearLayout permissionRequestLayout;
    private Button selectFolderButton;

    private SharedPreferences prefs;
    private static final int REQ_CAMERA = 1001;
    private Uri photoUri;

    // --- ActivityResultLaunchers ---
    private final ActivityResultLauncher<Uri> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    prefs.edit().putString(getUserSpecificFolderKey(), uri.toString()).apply();
                    permissionRequestLayout.setVisibility(View.GONE);
                    loadPdfFilesFromSafUri(uri);
                } else {
                    Toast.makeText(getContext(), "Bạn đã không chọn thư mục.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && photoUri != null) {
                    goToAdjustScreen(photoUri);
                } else {
                    Toast.makeText(getContext(), "Chưa chụp ảnh nào", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    goToAdjustScreen(uri);
                }
            });

    // =================================================================================
    // SECTION 1: CÁC HÀM KHỞI TẠO (LIFECYCLE METHODS)
    // =================================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_pdfs);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyView = view.findViewById(R.id.empty_view_text);
        permissionRequestLayout = view.findViewById(R.id.layout_permission_request);
        selectFolderButton = view.findViewById(R.id.btn_select_folder);

        selectFolderButton.setOnClickListener(v -> folderPickerLauncher.launch(null));
        view.findViewById(R.id.button).setOnClickListener(v -> openCamera());
        view.findViewById(R.id.Gallery).setOnClickListener(v -> openGalleryPicker());

        setupRecyclerView();
        checkFolderPermission();
    }

    // =================================================================================
    // SECTION 2: CÁC HÀM LIÊN QUAN ĐẾN RECYCLERVIEW VÀ TẢI FILE PDF
    // =================================================================================

    private void setupRecyclerView() {
        pdfFileList = new ArrayList<>();
        adapter = new PdfAdapter(requireContext(), pdfFileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void checkFolderPermission() {
        String savedUriString = prefs.getString(getUserSpecificFolderKey(), null);
        if (savedUriString != null) {
            Uri folderUri = Uri.parse(savedUriString);
            loadPdfFilesFromSafUri(folderUri);
        } else {
            permissionRequestLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadPdfFilesFromSafUri(Uri folderUri) {
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<PdfFile> foundFiles = new ArrayList<>();
            Context context = getContext();
            if (context == null) return;

            ContentResolver contentResolver = context.getContentResolver();
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, DocumentsContract.getTreeDocumentId(folderUri));

            String[] projection = {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID, //hang so xác định duy nhất một tài liệu hoặc thư mục trong ContentProvider của Android.
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE
            };

            String selection = DocumentsContract.Document.COLUMN_MIME_TYPE + " = ?";
            String[] selectionArgs = {"application/pdf"};

            try (Cursor cursor = contentResolver.query(childrenUri, projection, selection, selectionArgs, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String docId = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                        long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE));
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

    private String getUserSpecificFolderKey() {
        String userEmail = prefs.getString(LoginActivity.LOGGED_IN_USER_EMAIL, null);
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalStateException("User email not found in SharedPreferences. User might not be logged in.");
        }
        return "folderUri_" + userEmail;
    }

    // =================================================================================
    // SECTION 3: CÁC HÀM LIÊN QUAN ĐẾN CHỤP ẢNH, CHỌN ẢNH VÀ QUYỀN
    // =================================================================================

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }

        String fileName = "Scan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Scan2Form");
        }

        photoUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGalleryPicker() {
        pickImageLauncher.launch("image/*");
    }

    private void goToAdjustScreen(Uri uri) {
        Intent intent = new Intent(getActivity(), MainActivity2.class);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // Cấp quyền thành công, mở lại camera
            } else {
                Toast.makeText(getContext(), "Bạn đã từ chối quyền sử dụng camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}