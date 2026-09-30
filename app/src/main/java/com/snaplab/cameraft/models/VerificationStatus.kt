package com.snaplab.cameraft.models

enum class VerificationStatus(val displayNameVi: String, val displayNameEn: String) {
    AUTHENTIC(
        "ĐÃ XÁC THỰC CHÍNH CHỦ",
        "OFFICIALLY AUTHENTICATED"
    ),
    TAMPERED(
        "CẢNH BÁO: DỮ LIỆU ĐÃ BỊ CHỈNH SỬA",
        "WARNING: DATA HAS BEEN TAMPERED"
    ),
    UNTRUSTED(
        "KHÔNG TÌM THẤY BẢN GỐC",
        "ORIGINAL RECORD NOT FOUND"
    );

    val isSuccess: Boolean
        get() = this == AUTHENTIC

    fun getDisplayName(isVietnamese: Boolean): String {
        return if (isVietnamese) displayNameVi else displayNameEn
    }
}
