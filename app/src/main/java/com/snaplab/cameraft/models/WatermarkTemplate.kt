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

    // Content fields (Base / fallback)
    var titleText: String = "",
    var projectName: String = "",
    var workItem: String = "",
    var contractorName: String = "",
    var inspectorName: String = "",
    var customNotes: String = "",

    // Bilingual fields
    var nameVi: String = "",
    var nameEn: String = "",
    var titleTextVi: String = "",
    var titleTextEn: String = "",
    var projectNameVi: String = "",
    var projectNameEn: String = "",
    var workItemVi: String = "",
    var workItemEn: String = "",
    var contractorNameVi: String = "",
    var contractorNameEn: String = "",
    var inspectorNameVi: String = "",
    var inspectorNameEn: String = "",
    var customNotesVi: String = "",
    var customNotesEn: String = "",

    // Filter & Sticker integration
    var activeFilter: PhotoFilter = PhotoFilter.ORIGINAL,
    var activeSticker: PhotoSticker? = null,

    // Adjustments
    var opacity: Float = 0.92f, // 0.3f - 1.0f
    var scale: Float = 1.0f     // 0.7f - 1.4f
) {
    fun getName(isVi: Boolean): String = if (isVi) nameVi.ifBlank { name } else nameEn.ifBlank { translateTextToEn(name) }
    fun getTitleText(isVi: Boolean): String = if (isVi) titleTextVi.ifBlank { titleText } else titleTextEn.ifBlank { translateTextToEn(titleText) }
    fun getProjectName(isVi: Boolean): String = if (isVi) projectNameVi.ifBlank { projectName } else projectNameEn.ifBlank { translateTextToEn(projectName) }
    fun getWorkItem(isVi: Boolean): String = if (isVi) workItemVi.ifBlank { workItem } else workItemEn.ifBlank { translateTextToEn(workItem) }
    fun getContractorName(isVi: Boolean): String = if (isVi) contractorNameVi.ifBlank { contractorName } else contractorNameEn.ifBlank { translateTextToEn(contractorName) }
    fun getInspectorName(isVi: Boolean): String = if (isVi) inspectorNameVi.ifBlank { inspectorName } else inspectorNameEn.ifBlank { translateTextToEn(inspectorName) }
    fun getCustomNotes(isVi: Boolean): String = if (isVi) customNotesVi.ifBlank { customNotes } else customNotesEn.ifBlank { translateTextToEn(customNotes) }

    companion object {
        fun translateTextToEn(text: String): String {
            if (text.isBlank()) return text
            // 1. Direct dictionary matches
            when (text.trim()) {
                "TIÊU CHUẨN XÂY DỰNG" -> return "CONSTRUCTION STANDARD"
                "BIÊN BẢN HIỆN TRƯỜNG" -> return "FIELD INSPECTION REPORT"
                "BÀN GIAO & KÝ NHẬN HỒ SƠ" -> return "DOCUMENT HANDOVER & RECEIPT"
                "ĐIỂM DANH LÀM VIỆC" -> return "WORK ATTENDANCE"
                "TUẦN TRA AN TOÀN" -> return "SAFETY PATROL"
                "KỶ NIỆM ĐÁNG NHỚ" -> return "FLEETING MEMORIES"
                "FLEETING MEMORIES" -> return "FLEETING MEMORIES"
                "XÁC THỰC SNAPLAB" -> return "SNAPLAB VERIFIED"
                "SNAPLAB VERIFIED" -> return "SNAPLAB VERIFIED"

                "Dự án Khu Đô Thị Nam Sài Gòn" -> return "South Saigon Urban Area Project"
                "Nghiệm thu cốp pha sàn tầng 15" -> return "15th Floor Formwork Acceptance"
                "Tổng thầu Xây dựng Nam Á" -> return "Nam A Construction General Contractor"
                "Kỹ sư: Nguyễn Văn Hưng" -> return "Engineer: Nguyen Van Hung"
                "Đạt chuẩn an toàn kỹ thuật thi công" -> return "Complies with construction safety standards"

                "Cầu Vượt Ngã Tư Thủ Đức" -> return "Thu Duc Intersection Overpass Bridge"
                "Đổ bê tông dầm mố M1" -> return "Pouring concrete for abutment beam M1"
                "Công ty Cổ phần Cầu Đường 1" -> return "Road & Bridge Joint Stock Co. 1"
                "Tư vấn giám sát: Phạm Hoàng" -> return "Supervision Consultant: Pham Hoang"
                "Mẫu R-28 đạt mác M350" -> return "R-28 sample reached M350 grade"

                "Hồ sơ nghiệm thu thanh quyết toán" -> return "Settlement & Acceptance Dossier"
                "Biên nhận bàn giao chứng từ gốc đợt 3" -> return "Phase 3 original vouchers delivery receipt"
                "Phòng Hành chính - Kế toán" -> return "Administration & Accounting Dept"
                "Người nhận: Trần Mai Anh" -> return "Recipient: Tran Mai Anh"
                "Đã kiểm đếm đầy đủ chữ ký & con dấu niêm phong" -> return "Verified signatures & official sealed stamps"

                "Ca sáng - Ban Chỉ Huy" -> return "Morning Shift - Site Command"
                "Nhân sự: Lê Thanh Hải" -> return "Personnel: Le Thanh Hai"
                "Phòng QLDA & Giám sát chất lượng" -> return "PMO & QA/QC Department"
                "Mã NV: SNAP-8842" -> return "Staff ID: SNAP-8842"
                "Check-in đúng giờ ca 08:00" -> return "On-time check-in for 08:00 shift"

                "Kho cảng Logistic Cát Lái" -> return "Cat Lai Logistics Port Terminal"
                "Kiểm tra hệ thống chữa cháy tự động" -> return "Automatic fire suppression system inspection"
                "Đội phản ứng nhanh PCCC" -> return "Fire Safety Rapid Response Team"
                "Đội trưởng: Vũ Mạnh Thắng" -> return "Team Leader: Vu Manh Thang"
                "Áp suất bình chữa cháy bình thường" -> return "Fire extinguisher pressure normal"

                "Khám phá Việt Nam" -> return "Discover Vietnam"
                "Đà Lạt - Thành phố ngàn hoa" -> return "Da Lat - City of Eternal Spring"
                "Thời tiết se lạnh 19°C tuyệt đẹp" -> return "Crisp cool 19°C beautiful weather"

                "Công trình tiêu chuẩn" -> return "Standard Construction"
                "Tem giám sát kỹ thuật" -> return "Technical Supervision Stamp"
                "Văn phòng & Ký nhận hồ sơ" -> return "Office & Document Signing"
                "Chấm công hiện trường" -> return "Field Attendance"
                "Tuần tra an toàn PCCC" -> return "Fire Safety Patrol"
                "Check-in Du lịch" -> return "Travel Check-in"
                "Tối giản thời gian & GPS" -> return "Minimal Time & GPS"
            }

            // 2. Pattern replacements
            var result = text
            result = result.replace("Kỹ sư: ", "Engineer: ")
            result = result.replace("Kỹ sư ", "Engineer ")
            result = result.replace("Tư vấn giám sát: ", "Supervision Consultant: ")
            result = result.replace("Tư vấn giám sát ", "Supervision Consultant ")
            result = result.replace("Người thực hiện: ", "Inspector: ")
            result = result.replace("Người nhận: ", "Recipient: ")
            result = result.replace("Đội trưởng: ", "Team Leader: ")
            result = result.replace("Chỉ huy trưởng: ", "Site Commander: ")
            result = result.replace("Nhân sự: ", "Personnel: ")
            result = result.replace("Mã NV: ", "Staff ID: ")
            result = result.replace("Dự án ", "Project ")
            result = result.replace("Tổng thầu ", "General Contractor ")
            result = result.replace("Công ty ", "Company ")
            result = result.replace("Nghiệm thu ", "Acceptance of ")
            result = result.replace("Kiểm tra ", "Inspection of ")
            result = result.replace("Thi công ", "Construction of ")
            result = result.replace("Đạt chuẩn ", "Compliant with ")
            result = result.replace("Đạt chuẩn", "Compliant")
            result = result.replace("Đạt yêu cầu", "Qualified")
            result = result.replace("Không đạt", "Failed")
            result = result.replace("Hoàn thành", "Completed")
            result = result.replace("An toàn", "Safe")
            result = result.replace("Bình thường", "Normal")
            return result
        }

        val presets: List<WatermarkTemplate> = listOf(
            // 1. Engineering / Construction
            WatermarkTemplate(
                id = "eng_construction_pro",
                name = "Công trình tiêu chuẩn",
                nameVi = "Công trình tiêu chuẩn",
                nameEn = "Standard Construction",
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
                titleTextVi = "TIÊU CHUẨN XÂY DỰNG",
                titleTextEn = "CONSTRUCTION STANDARD",
                projectName = "Dự án Khu Đô Thị Nam Sài Gòn",
                projectNameVi = "Dự án Khu Đô Thị Nam Sài Gòn",
                projectNameEn = "South Saigon Urban Area Project",
                workItem = "Nghiệm thu cốp pha sàn tầng 15",
                workItemVi = "Nghiệm thu cốp pha sàn tầng 15",
                workItemEn = "15th Floor Formwork Acceptance",
                contractorName = "Tổng thầu Xây dựng Nam Á",
                contractorNameVi = "Tổng thầu Xây dựng Nam Á",
                contractorNameEn = "Nam A Construction General Contractor",
                inspectorName = "Kỹ sư: Nguyễn Văn Hưng",
                inspectorNameVi = "Kỹ sư: Nguyễn Văn Hưng",
                inspectorNameEn = "Engineer: Nguyen Van Hung",
                customNotes = "Đạt chuẩn an toàn kỹ thuật thi công",
                customNotesVi = "Đạt chuẩn an toàn kỹ thuật thi công",
                customNotesEn = "Complies with construction safety standards",
                activeFilter = PhotoFilter.WARM_SUNLIGHT,
                activeSticker = PhotoSticker.PASSED
            ),

            // 2. Construction Blue Stamp
            WatermarkTemplate(
                id = "eng_blueprint_stamp",
                name = "Tem giám sát kỹ thuật",
                nameVi = "Tem giám sát kỹ thuật",
                nameEn = "Technical Supervision Stamp",
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
                titleTextVi = "BIÊN BẢN HIỆN TRƯỜNG",
                titleTextEn = "FIELD INSPECTION REPORT",
                projectName = "Cầu Vượt Ngã Tư Thủ Đức",
                projectNameVi = "Cầu Vượt Ngã Tư Thủ Đức",
                projectNameEn = "Thu Duc Intersection Overpass Bridge",
                workItem = "Đổ bê tông dầm mố M1",
                workItemVi = "Đổ bê tông dầm mố M1",
                workItemEn = "Pouring concrete for abutment beam M1",
                contractorName = "Công ty Cổ phần Cầu Đường 1",
                contractorNameVi = "Công ty Cổ phần Cầu Đường 1",
                contractorNameEn = "Road & Bridge Joint Stock Co. 1",
                inspectorName = "Tư vấn giám sát: Phạm Hoàng",
                inspectorNameVi = "Tư vấn giám sát: Phạm Hoàng",
                inspectorNameEn = "Supervision Consultant: Pham Hoang",
                customNotes = "Mẫu R-28 đạt mác M350",
                customNotesVi = "Mẫu R-28 đạt mác M350",
                customNotesEn = "R-28 sample reached M350 grade",
                activeFilter = PhotoFilter.CINEMATIC,
                activeSticker = PhotoSticker.APPROVED
            ),

            // 3. Office & Administrative Work Handover
            WatermarkTemplate(
                id = "office_admin_handover",
                name = "Văn phòng & Ký nhận hồ sơ",
                nameVi = "Văn phòng & Ký nhận hồ sơ",
                nameEn = "Office & Document Signing",
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
                titleTextVi = "BÀN GIAO & KÝ NHẬN HỒ SƠ",
                titleTextEn = "DOCUMENT HANDOVER & RECEIPT",
                projectName = "Hồ sơ nghiệm thu thanh quyết toán",
                projectNameVi = "Hồ sơ nghiệm thu thanh quyết toán",
                projectNameEn = "Settlement & Acceptance Dossier",
                workItem = "Biên nhận bàn giao chứng từ gốc đợt 3",
                workItemVi = "Biên nhận bàn giao chứng từ gốc đợt 3",
                workItemEn = "Phase 3 original vouchers delivery receipt",
                contractorName = "Phòng Hành chính - Kế toán",
                contractorNameVi = "Phòng Hành chính - Kế toán",
                contractorNameEn = "Administration & Accounting Dept",
                inspectorName = "Người nhận: Trần Mai Anh",
                inspectorNameVi = "Người nhận: Trần Mai Anh",
                inspectorNameEn = "Recipient: Tran Mai Anh",
                customNotes = "Đã kiểm đếm đầy đủ chữ ký & con dấu niêm phong",
                customNotesVi = "Đã kiểm đếm đầy đủ chữ ký & con dấu niêm phong",
                customNotesEn = "Verified signatures & official sealed stamps",
                activeFilter = PhotoFilter.ELEGANT,
                activeSticker = PhotoSticker.HANDOVER
            ),

            // 4. Work Attendance / Check-in
            WatermarkTemplate(
                id = "att_standard_checkin",
                name = "Chấm công hiện trường",
                nameVi = "Chấm công hiện trường",
                nameEn = "Field Attendance",
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
                titleTextVi = "ĐIỂM DANH LÀM VIỆC",
                titleTextEn = "WORK ATTENDANCE",
                projectName = "Ca sáng - Ban Chỉ Huy",
                projectNameVi = "Ca sáng - Ban Chỉ Huy",
                projectNameEn = "Morning Shift - Site Command",
                workItem = "Nhân sự: Lê Thanh Hải",
                workItemVi = "Nhân sự: Lê Thanh Hải",
                workItemEn = "Personnel: Le Thanh Hai",
                contractorName = "Phòng QLDA & Giám sát chất lượng",
                contractorNameVi = "Phòng QLDA & Giám sát chất lượng",
                contractorNameEn = "PMO & QA/QC Department",
                inspectorName = "Mã NV: SNAP-8842",
                inspectorNameVi = "Mã NV: SNAP-8842",
                inspectorNameEn = "Staff ID: SNAP-8842",
                customNotes = "Check-in đúng giờ ca 08:00",
                customNotesVi = "Check-in đúng giờ ca 08:00",
                customNotesEn = "On-time check-in for 08:00 shift",
                activeFilter = PhotoFilter.ORIGINAL,
                activeSticker = PhotoSticker.APPROVED
            ),

            // 5. Field Patrol & Safety Inspection
            WatermarkTemplate(
                id = "patrol_safety_inspect",
                name = "Tuần tra an toàn PCCC",
                nameVi = "Tuần tra an toàn PCCC",
                nameEn = "Fire Safety Patrol",
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
                titleTextVi = "TUẦN TRA AN TOÀN",
                titleTextEn = "SAFETY PATROL",
                projectName = "Kho cảng Logistic Cát Lái",
                projectNameVi = "Kho cảng Logistic Cát Lái",
                projectNameEn = "Cat Lai Logistics Port Terminal",
                workItem = "Kiểm tra hệ thống chữa cháy tự động",
                workItemVi = "Kiểm tra hệ thống chữa cháy tự động",
                workItemEn = "Automatic fire suppression system inspection",
                contractorName = "Đội phản ứng nhanh PCCC",
                contractorNameVi = "Đội phản ứng nhanh PCCC",
                contractorNameEn = "Fire Safety Rapid Response Team",
                inspectorName = "Đội trưởng: Vũ Mạnh Thắng",
                inspectorNameVi = "Đội trưởng: Vũ Mạnh Thắng",
                inspectorNameEn = "Team Leader: Vu Manh Thang",
                customNotes = "Áp suất bình chữa cháy bình thường",
                customNotesVi = "Áp suất bình chữa cháy bình thường",
                customNotesEn = "Fire extinguisher pressure normal",
                activeFilter = PhotoFilter.ORIGINAL,
                activeSticker = PhotoSticker.SAFETY_FIRST
            ),

            // 6. Travel & Lifestyle Check-in
            WatermarkTemplate(
                id = "travel_lifestyle",
                name = "Check-in Du lịch",
                nameVi = "Check-in Du lịch",
                nameEn = "Travel Check-in",
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
                titleTextVi = "KỶ NIỆM ĐÁNG NHỚ",
                titleTextEn = "FLEETING MEMORIES",
                projectName = "Khám phá Việt Nam",
                projectNameVi = "Khám phá Việt Nam",
                projectNameEn = "Discover Vietnam",
                workItem = "Đà Lạt - Thành phố ngàn hoa",
                workItemVi = "Đà Lạt - Thành phố ngàn hoa",
                workItemEn = "Da Lat - City of Eternal Spring",
                contractorName = "",
                inspectorName = "",
                customNotes = "Thời tiết se lạnh 19°C tuyệt đẹp",
                customNotesVi = "Thời tiết se lạnh 19°C tuyệt đẹp",
                customNotesEn = "Crisp cool 19°C beautiful weather",
                activeFilter = PhotoFilter.FLEETING_YEARS,
                activeSticker = PhotoSticker.VIP_SEAL
            ),

            // 7. Minimal Clean
            WatermarkTemplate(
                id = "minimal_timestamp",
                name = "Tối giản thời gian & GPS",
                nameVi = "Tối giản thời gian & GPS",
                nameEn = "Minimal Time & GPS",
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
                titleTextVi = "XÁC THỰC SNAPLAB",
                titleTextEn = "SNAPLAB VERIFIED",
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
