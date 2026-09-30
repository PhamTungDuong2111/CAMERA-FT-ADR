package com.snaplab.cameraft.services

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.snaplab.cameraft.SnapLabApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    VIETNAMESE("vi", "Tiếng Việt 🇻🇳"),
    ENGLISH("en", "English 🇺🇸")
}

class LocalizationManager private constructor() {

    private val prefs = SnapLabApplication.instance.getSharedPreferences("snaplab_settings", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "vi") == "en") AppLanguage.ENGLISH else AppLanguage.VIETNAMESE
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun isVietnamese(): Boolean = _currentLanguage.value == AppLanguage.VIETNAMESE

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        if (_currentLanguage.value == AppLanguage.VIETNAMESE) {
            setLanguage(AppLanguage.ENGLISH)
        } else {
            setLanguage(AppLanguage.VIETNAMESE)
        }
    }

    fun t(key: String): String {
        val isVi = isVietnamese()
        val entry = translations[key] ?: return key
        return if (isVi) entry.first else entry.second
    }

    private val translations = mapOf(
        // Navigation & Common
        "close" to Pair("Đóng", "Close"),
        "cancel" to Pair("Hủy", "Cancel"),
        "apply" to Pair("Áp dụng", "Apply"),
        "done" to Pair("Xong", "Done"),
        "save" to Pair("Lưu", "Save"),
        "delete" to Pair("Xóa", "Delete"),
        "share" to Pair("Chia sẻ", "Share"),
        "back" to Pair("Quay lại", "Back"),
        "confirm" to Pair("Xác nhận", "Confirm"),

        // Camera Screen
        "photo" to Pair("ẢNH", "PHOTO"),
        "video" to Pair("VIDEO", "VIDEO"),
        "templates" to Pair("Mẫu dấu", "Templates"),
        "verify" to Pair("Đối soát", "Verify"),
        "settings" to Pair("Cài đặt", "Settings"),
        "gallery" to Pair("Thư viện", "Gallery"),
        "simulatedCamera" to Pair("CAMERA MÔ PHỎNG HD (Giả lập)", "SIMULATED HD CAMERA (Emulator)"),
        "flashOff" to Pair("Tắt", "Off"),
        "flashOn" to Pair("Bật", "On"),
        "flashAuto" to Pair("Tự động", "Auto"),
        "flashTorch" to Pair("Đèn pin", "Torch"),

        // Watermark Live Labels
        "timeLabel" to Pair("Thời gian: ", "Time: "),
        "projectLabel" to Pair("Dự án: ", "Project: "),
        "workItemLabel" to Pair("Hạng mục: ", "Work Item: "),
        "contractorLabel" to Pair("Đơn vị: ", "Contractor: "),
        "inspectorLabel" to Pair("Người thực hiện: ", "Inspector: "),
        "locationLabel" to Pair("Địa điểm: ", "Location: "),
        "coordinatesLabel" to Pair("Tọa độ: ", "Coordinates: "),
        "altitudeLabel" to Pair("Cao độ: ", "Altitude: "),
        "compassLabel" to Pair("Hướng: ", "Heading: "),
        "notesLabel" to Pair("Ghi chú: ", "Notes: "),
        "verifyCodeLabel" to Pair("Mã xác thực: ", "Verify Code: "),
        "scanToVerify" to Pair("QUÉT ĐỂ XÁC THỰC", "SCAN TO VERIFY"),
        "scanQuery" to Pair("QUÉT ĐỐI SOÁT", "SCAN QUERY"),
        "snaplabVerified" to Pair("Xác thực SnapLab: ", "SnapLab Verified: "),

        // Categories
        "all" to Pair("Tất cả", "All"),
        "catEngineering" to Pair("Công trình", "Engineering"),
        "catAttendance" to Pair("Chấm công", "Attendance"),
        "catPatrol" to Pair("Tuần tra", "Patrol"),
        "catTravel" to Pair("Du lịch", "Travel"),
        "catMinimal" to Pair("Tối giản", "Minimal"),
        "catCustom" to Pair("Tùy chỉnh", "Custom"),

        // Template Editor
        "customizeTemplate" to Pair("Tùy biến mẫu dấu", "Customize Watermark"),
        "templateTitle" to Pair("Tiêu đề mẫu", "Template Title"),
        "badgeStyle" to Pair("Kiểu khung dấu", "Badge Style"),
        "colorTheme" to Pair("Màu sắc chủ đạo", "Color Theme"),
        "position" to Pair("Vị trí hiển thị", "Position"),
        "contentInformation" to Pair("Thông tin nội dung", "Content Information"),
        "projectNameField" to Pair("Tên dự án / Công trình", "Project / Site Name"),
        "workItemField" to Pair("Hạng mục / Công việc", "Work Item / Task"),
        "contractorField" to Pair("Đơn vị thi công / Phòng ban", "Contractor / Dept"),
        "inspectorField" to Pair("Kỹ sư / Người thực hiện", "Inspector / Performer"),
        "notesField" to Pair("Ghi chú hiện trường", "Field Notes"),
        "displayOptions" to Pair("Tùy chọn hiển thị", "Display Options"),
        "showTimeToggle" to Pair("Hiển thị ngày & giờ", "Show Date & Time"),
        "showSecondsToggle" to Pair("Hiển thị giây", "Show Seconds"),
        "showLocationToggle" to Pair("Hiển thị địa chỉ thực tế", "Show Real Address"),
        "showCoordinatesToggle" to Pair("Hiển thị tọa độ GPS", "Show GPS Coordinates"),
        "showAltitudeToggle" to Pair("Hiển thị cao độ", "Show Altitude"),
        "showWeatherToggle" to Pair("Hiển thị thời tiết", "Show Weather"),
        "showCompassToggle" to Pair("Hiển thị hướng la bàn", "Show Compass Heading"),
        "showDeviceInfoToggle" to Pair("Hiển thị thông tin máy", "Show Device Info"),
        "showAntiCounterfeitQRToggle" to Pair("Mã QR chống giả mạo", "Anti-Counterfeiting QR"),
        "opacitySlider" to Pair("Độ trong suốt nền", "Background Opacity"),
        "scaleSlider" to Pair("Kích thước dấu", "Watermark Scale"),

        // Anti Counterfeit Screen
        "queryTitle" to Pair("Truy Vấn Chống Giả Mạo", "Anti-Counterfeit Query"),
        "queryHeaderTitle" to Pair("Hệ Thống Đối Soát Độc Quyền", "Authoritative Verification System"),
        "queryHeaderDesc" to Pair(
            "Xác minh tính nguyên bản của ảnh chụp từ SnapLab dựa trên mã băm SHA-256 và chữ ký điện tử.",
            "Verify the authenticity and integrity of photos taken with SnapLab using SHA-256 cryptographic hashing."
        ),
        "enterIdHint" to Pair("Nhập mã hồ sơ (VD: SL-20260930-1234)", "Enter record ID (e.g. SL-20260930-1234)"),
        "checkButton" to Pair("Kiểm tra ngay", "Verify Now"),
        "selectPhotoToCheck" to Pair("Chọn ảnh từ thiết bị để kiểm tra chỉnh sửa", "Select photo from device to inspect edits"),
        "scanQRCode" to Pair("Quét mã QR trên ảnh", "Scan QR Code on photo"),
        "analyzing" to Pair("Đang phân tích dữ liệu điểm ảnh SHA-256...", "Analyzing SHA-256 pixel checksum..."),
        "certTitle" to Pair("CHỨNG THƯ XÁC THỰC KỸ THUẬT SỐ", "DIGITAL VERIFICATION CERTIFICATE"),
        "recordId" to Pair("Mã định danh:", "Record ID:"),
        "captureTime" to Pair("Thời gian chụp gốc:", "Original Capture Time:"),
        "deviceUsed" to Pair("Thiết bị ghi nhận:", "Registered Device:"),
        "gpsOrigin" to Pair("Tọa độ GPS gốc:", "Original GPS Coords:"),
        "sha256Hash" to Pair("Mã băm SHA-256:", "SHA-256 Checksum:"),
        "digitalSig" to Pair("Chữ ký mật mã:", "Digital Signature:"),
        "integrityIntact" to Pair("Dữ liệu nguyên bản, không phát hiện can thiệp Photoshop.", "Data intact, no Photoshop manipulation detected."),
        "integrityTampered" to Pair("CẢNH BÁO: Dữ liệu điểm ảnh không khớp với mã băm ban đầu!", "WARNING: Pixel data does not match original cryptographic hash!"),

        // Gallery & Album Editor
        "galleryTitle" to Pair("Thư Viện SnapLab", "SnapLab Gallery"),
        "noMediaYet" to Pair("Chưa có ảnh hoặc video nào", "No photos or videos yet"),
        "albumEditorTitle" to Pair("Đóng Dấu Cho Ảnh Có Sẵn", "Stamp Existing Photo"),
        "chooseFromAlbum" to Pair("Chọn ảnh từ Album máy", "Pick photo from Gallery"),
        "saveWatermarked" to Pair("Đóng Dấu & Lưu Ảnh", "Stamp & Save Image"),
        "imageSavedSuccess" to Pair("Đã lưu ảnh thành công vào Thư viện!", "Image successfully saved to Gallery!"),
        "processing" to Pair("Đang xử lý hình ảnh...", "Processing image..."),

        // Settings Screen
        "settingsTitle" to Pair("Cài Đặt SnapLab", "SnapLab Settings"),
        "languageSection" to Pair("Ngôn Ngữ Ứng Dụng", "Application Language"),
        "languageDesc" to Pair("Chuyển đổi tức thì song ngữ Tiếng Việt và English", "Instant toggle between Vietnamese and English"),
        "cameraSection" to Pair("Tùy Chọn Camera", "Camera Options"),
        "gridlines" to Pair("Lưới bố cục 3x3", "Rule of Thirds Grid"),
        "saveToGallery" to Pair("Tự động lưu vào Bộ sưu tập thiết bị", "Auto-save to Device Gallery"),
        "highQualityRender" to Pair("Xuất ảnh độ nét cao Ultra HD", "Ultra HD High Resolution Export"),
        "securitySection" to Pair("Bảo Mật & Chống Giả Mạo", "Security & Anti-Tamper"),
        "securityDesc" to Pair(
            "Tự động tính toán mã băm SHA-256 cho mỗi bức ảnh được chụp và cấp mã định danh duy nhất vào sổ lưu trữ.",
            "Automatically computes SHA-256 checksum for every captured photo and registers it in the local cryptographic ledger."
        ),
        "appInfoSection" to Pair("Thông Tin Ứng Dụng", "App Information"),
        "version" to Pair("Phiên bản", "Version"),
        "build" to Pair("Bản dựng", "Build")
    )

    companion object {
        val shared by lazy { LocalizationManager() }
    }
}
