package com.snaplab.cameraft.models

import androidx.compose.ui.graphics.Color
import com.snaplab.cameraft.ui.theme.*
import java.util.UUID

enum class WatermarkCategory(val nameVi: String, val nameEn: String) {
    ENGINEERING("Công trình", "Engineering"),
    OFFICE("Văn phòng", "Office"),
    ATTENDANCE("Chấm công", "Attendance"),
    PATROL("Tuần tra", "Patrol"),
    TRAVEL("Du lịch", "Travel"),
    MINIMAL("Tối giản", "Minimal"),
    CUSTOM("Tùy chỉnh", "Custom");

    fun getDisplayName(isVietnamese: Boolean): String {
        return if (isVietnamese) nameVi else nameEn
    }
}

enum class WatermarkBadgeStyle(val titleVi: String, val titleEn: String) {
    GLASSMORPHISM("Thấu kính mờ", "Glassmorphism"),
    DARK_CARD("Thẻ đen hiện đại", "Dark Card"),
    BORDERED_STAMP("Khung tem kỹ thuật", "Bordered Stamp"),
    MINIMAL_TRANSPARENT("Trong suốt tối giản", "Minimal Transparent");

    fun getTitle(isVietnamese: Boolean): String {
        return if (isVietnamese) titleVi else titleEn
    }
}

enum class WatermarkColorTheme(val hex: Long, val displayName: String) {
    SAFETY_ORANGE(0xFFFF6B00, "Safety Orange"),
    BLUEPRINT_BLUE(0xFF0066FF, "Blueprint Blue"),
    EMERALD_GREEN(0xFF00C853, "Emerald Green"),
    GOLDEN_YELLOW(0xFFFFD600, "Golden Yellow"),
    CRIMSON_RED(0xFFFF1744, "Crimson Red"),
    CRISP_WHITE(0xFFFFFFFF, "Crisp White");

    val composeColor: Color
        get() = Color(hex)

    val androidColor: Int
        get() = hex.toInt()
}

enum class WatermarkPosition(val titleVi: String, val titleEn: String) {
    BOTTOM_LEFT("Góc dưới trái", "Bottom Left"),
    BOTTOM_RIGHT("Góc dưới phải", "Bottom Right"),
    TOP_LEFT("Góc trên trái", "Top Left"),
    TOP_RIGHT("Góc trên phải", "Top Right"),
    CENTER_BOTTOM("Chính giữa dưới", "Center Bottom");

    fun getTitle(isVietnamese: Boolean): String {
        return if (isVietnamese) titleVi else titleEn
    }
}

data class WatermarkTemplate(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var category: WatermarkCategory,
    var badgeStyle: WatermarkBadgeStyle = WatermarkBadgeStyle.GLASSMORPHISM,
    var colorTheme: WatermarkColorTheme = WatermarkColorTheme.SAFETY_ORANGE,
    var position: WatermarkPosition = WatermarkPosition.BOTTOM_LEFT,

    // Toggles
    var showTime: Boolean = true,
    var showSeconds: Boolean = true,
    var showLocation: Boolean = true,
    var showCoordinates: Boolean = true,
    var showAltitude: Boolean = true,
    var showWeather: Boolean = true,
    var showCompass: Boolean = true,
    var showDeviceInfo: Boolean = true,
    var showAntiCounterfeitQR: Boolean = true,

    // Content fields
    var titleText: String = "",
    var projectName: String = "",
    var workItem: String = "",
    var contractorName: String = "",
    var inspectorName: String = "",
    var customNotes: String = "",

    // Filter & Sticker integration
    var activeFilter: PhotoFilter = PhotoFilter.ORIGINAL,
    var activeSticker: PhotoSticker? = null,

    // Adjustments
    var opacity: Float = 0.92f, // 0.3f - 1.0f
    var scale: Float = 1.0f     // 0.7f - 1.4f
) {
    companion object {
        val presets: List<WatermarkTemplate> = listOf(
            // 1. Engineering / Construction
            WatermarkTemplate(
                id = "eng_construction_pro",
                name = "Công trình tiêu chuẩn",
                category = WatermarkCategory.ENGINEERING,
                badgeStyle = WatermarkBadgeStyle.GLASSMORPHISM,
                colorTheme = WatermarkColorTheme.SAFETY_ORANGE,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = true,
                showWeather = true,
                showCompass = true,
                showDeviceInfo = true,
                showAntiCounterfeitQR = true,
                titleText = "TIÊU CHUẨN XÂY DỰNG",
                projectName = "Dự án Khu Đô Thị Nam Sài Gòn",
                workItem = "Nghiệm thu cốp pha sàn tầng 15",
                contractorName = "Tổng thầu Xây dựng Nam Á",
                inspectorName = "Kỹ sư: Nguyễn Văn Hưng",
                customNotes = "Đạt chuẩn an toàn kỹ thuật thi công",
                activeFilter = PhotoFilter.WARM_SUNLIGHT,
                activeSticker = PhotoSticker.PASSED
            ),

            // 2. Construction Blue Stamp
            WatermarkTemplate(
                id = "eng_blueprint_stamp",
                name = "Tem giám sát kỹ thuật",
                category = WatermarkCategory.ENGINEERING,
                badgeStyle = WatermarkBadgeStyle.BORDERED_STAMP,
                colorTheme = WatermarkColorTheme.BLUEPRINT_BLUE,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = true,
                showWeather = false,
                showCompass = true,
                showDeviceInfo = false,
                showAntiCounterfeitQR = true,
                titleText = "BIÊN BẢN HIỆN TRƯỜNG",
                projectName = "Cầu Vượt Ngã Tư Thủ Đức",
                workItem = "Đổ bê tông dầm mố M1",
                contractorName = "Công ty Cổ phần Cầu Đường 1",
                inspectorName = "Tư vấn giám sát: Phạm Hoàng",
                customNotes = "Mẫu R-28 đạt mác M350",
                activeFilter = PhotoFilter.CINEMATIC,
                activeSticker = PhotoSticker.APPROVED
            ),

            // 3. Office & Administrative Work Handover
            WatermarkTemplate(
                id = "office_admin_handover",
                name = "Văn phòng & Ký nhận hồ sơ",
                category = WatermarkCategory.OFFICE,
                badgeStyle = WatermarkBadgeStyle.DARK_CARD,
                colorTheme = WatermarkColorTheme.BLUEPRINT_BLUE,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = false,
                showWeather = false,
                showCompass = false,
                showDeviceInfo = true,
                showAntiCounterfeitQR = true,
                titleText = "BÀN GIAO & KÝ NHẬN HỒ SƠ",
                projectName = "Hồ sơ nghiệm thu thanh quyết toán",
                workItem = "Biên nhận bàn giao chứng từ gốc đợt 3",
                contractorName = "Phòng Hành chính - Kế toán",
                inspectorName = "Người nhận: Trần Mai Anh",
                customNotes = "Đã kiểm đếm đầy đủ chữ ký & con dấu niêm phong",
                activeFilter = PhotoFilter.ELEGANT,
                activeSticker = PhotoSticker.HANDOVER
            ),

            // 4. Work Attendance / Check-in
            WatermarkTemplate(
                id = "att_standard_checkin",
                name = "Chấm công hiện trường",
                category = WatermarkCategory.ATTENDANCE,
                badgeStyle = WatermarkBadgeStyle.DARK_CARD,
                colorTheme = WatermarkColorTheme.EMERALD_GREEN,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = false,
                showWeather = true,
                showCompass = false,
                showDeviceInfo = true,
                showAntiCounterfeitQR = true,
                titleText = "ĐIỂM DANH LÀM VIỆC",
                projectName = "Ca sáng - Ban Chỉ Huy",
                workItem = "Nhân sự: Lê Thanh Hải",
                contractorName = "Phòng QLDA & Giám sát chất lượng",
                inspectorName = "Mã NV: SNAP-8842",
                customNotes = "Check-in đúng giờ ca 08:00",
                activeFilter = PhotoFilter.ORIGINAL,
                activeSticker = PhotoSticker.APPROVED
            ),

            // 5. Field Patrol & Safety Inspection
            WatermarkTemplate(
                id = "patrol_safety_inspect",
                name = "Tuần tra an toàn PCCC",
                category = WatermarkCategory.PATROL,
                badgeStyle = WatermarkBadgeStyle.GLASSMORPHISM,
                colorTheme = WatermarkColorTheme.GOLDEN_YELLOW,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = true,
                showWeather = true,
                showCompass = true,
                showDeviceInfo = true,
                showAntiCounterfeitQR = true,
                titleText = "TUẦN TRA AN TOÀN",
                projectName = "Kho cảng Logistic Cát Lái",
                workItem = "Kiểm tra hệ thống chữa cháy tự động",
                contractorName = "Đội phản ứng nhanh PCCC",
                inspectorName = "Đội trưởng: Vũ Mạnh Thắng",
                customNotes = "Áp suất bình chữa cháy bình thường",
                activeFilter = PhotoFilter.ORIGINAL,
                activeSticker = PhotoSticker.SAFETY_FIRST
            ),

            // 6. Travel & Lifestyle Check-in
            WatermarkTemplate(
                id = "travel_lifestyle",
                name = "Check-in Du lịch",
                category = WatermarkCategory.TRAVEL,
                badgeStyle = WatermarkBadgeStyle.MINIMAL_TRANSPARENT,
                colorTheme = WatermarkColorTheme.CRISP_WHITE,
                position = WatermarkPosition.BOTTOM_RIGHT,
                showTime = true,
                showSeconds = false,
                showLocation = true,
                showCoordinates = true,
                showAltitude = true,
                showWeather = true,
                showCompass = false,
                showDeviceInfo = false,
                showAntiCounterfeitQR = false,
                titleText = "FLEETING MEMORIES",
                projectName = "Khám phá Việt Nam",
                workItem = "Đà Lạt - Thành phố ngàn hoa",
                contractorName = "",
                inspectorName = "",
                customNotes = "Thời tiết se lạnh 19°C tuyệt đẹp",
                activeFilter = PhotoFilter.FLEETING_YEARS,
                activeSticker = PhotoSticker.VIP_SEAL
            ),

            // 7. Minimal Clean
            WatermarkTemplate(
                id = "minimal_timestamp",
                name = "Tối giản thời gian & GPS",
                category = WatermarkCategory.MINIMAL,
                badgeStyle = WatermarkBadgeStyle.DARK_CARD,
                colorTheme = WatermarkColorTheme.CRISP_WHITE,
                position = WatermarkPosition.BOTTOM_LEFT,
                showTime = true,
                showSeconds = true,
                showLocation = true,
                showCoordinates = true,
                showAltitude = false,
                showWeather = false,
                showCompass = false,
                showDeviceInfo = false,
                showAntiCounterfeitQR = true,
                titleText = "SNAPLAB VERIFIED",
                projectName = "",
                workItem = "",
                contractorName = "",
                inspectorName = "",
                customNotes = "",
                activeFilter = PhotoFilter.ORIGINAL,
                activeSticker = null
            )
        )
    }
}
