# SnapLab (CAMERA-FT Android) 📸

> **Ứng dụng Camera Chuyên Dụng Cho Hiện Trường, Kỹ Thuật, Xây Dựng & Chấm Công trên nền tảng Android**  
> *Tích hợp Đóng Dấu Thời Gian, Tọa Độ GPS, La Bàn, Thời Tiết & Hệ Thống Đối Soát Chống Giả Mạo Bản Quyền (SHA-256 Authenticated)*

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Android Studio](https://img.shields.io/badge/IDE-Android%20Studio-blue.svg)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![CameraX](https://img.shields.io/badge/Camera-CameraX%201.3.3-orange.svg)](https://developer.android.com/training/camerax)
[![Anti-Counterfeit](https://img.shields.io/badge/Security-SHA--256%20Cryptographic%20Hash-red.svg)](#hệ-thống-truy-vấn-đối-soát-chống-giả-mạo-độc-quyền)

---

## 🌟 Giới Thiệu Tổng Quan

**SnapLab (CAMERA-FT Android)** là phiên bản Android hoàn chỉnh được chuyển giao từ kiến trúc SnapLab iOS, thiết kế theo tiêu chuẩn hiện đại nhất bằng **Kotlin & Jetpack Compose (Material 3)**.

Ứng dụng đáp ứng các yêu cầu khắt khe trong:
- **Giám sát công trình & xây dựng**: Ghi nhận tiến độ thi công, nghiệm thu sắt thép, cốp pha, bê tông với dấu mộc tiêu chuẩn.
- **Chấm công & Điểm danh thực địa**: Chụp ảnh chấm công kèm giờ giấc chính xác từng giây và vị trí định vị vệ tinh GPS không thể giả lập.
- **Tuần tra an ninh & PCCC**: Ghi nhận kiểm tra thiết bị phòng cháy chữa cháy, trạm biến áp, kho bãi với la bàn và cao độ thực tế.
- **Đối soát chống gian lận & giả mạo**: Mỗi bức ảnh được nung dấu mã băm mật mã học **SHA-256** và **mã QR duy nhất** giúp phát hiện tức thì ảnh đã qua chỉnh sửa bằng Photoshop.

---

## 🚀 Các Tính Năng Nổi Bật

### 1. Camera Hiện Trường CameraX Đỉnh Cao
- Luồng xem trực tiếp (Viewfinder) mượt mà với **CameraX 1.3.3**, hỗ trợ Auto Focus, Touch to Focus, điều chỉnh phơi sáng.
- Chuyển đổi linh hoạt giữa camera trước & sau (Front / Back Camera).
- Đa dạng chế độ đèn Flash: **Tắt (Off)**, **Bật (On)**, **Tự động (Auto)**, **Đèn pin chiếu sáng liên tục (Torch)**.
- Tỉ lệ khung hình linh hoạt: **4:3** và **16:9**.
- Thu phóng mượt mà (Zoom): **0.5x (Góc siêu rộng)**, **1.0x (Tiêu chuẩn)**, **2.0x**, **5.0x**.
- Lưới bố cục tỷ lệ vàng **3x3 Grid**.
- **Chế độ Camera Mô Phỏng (Synthetic Live View)**: Tự động kích hoạt khi chạy trên máy ảo Android Studio (Emulator) chưa cấu hình webcam, hiển thị khung cảnh công trường ảo sinh động mà không bị crash.

### 2. Hệ Thống Mẫu Dấu Hiện Trường (Preset Watermark Templates)
Ứng dụng tích hợp sẵn 6 bộ mẫu tem dấu thực địa chuẩn hóa:
1. **Công trình tiêu chuẩn (Engineering Pro)**: Phong cách kính mờ Glassmorphism, viền cam an toàn Safety Orange, đầy đủ thông số dự án, kỹ sư giám sát, cao độ, thời tiết và mã QR.
2. **Tem giám sát kỹ thuật (Blueprint Stamp)**: Phong cách khung viền xanh kỹ thuật, phục vụ biên bản nghiệm thu đổ bê tông, mố trụ cầu đường.
3. **Chấm công hiện trường (Field Attendance)**: Thẻ đen sang trọng, viền ngọc lục bảo Emerald Green, chuyên dụng check-in ca làm việc nhân sự.
4. **Tuần tra an toàn PCCC (Patrol & Safety Inspection)**: Viền vàng cảnh báo Golden Yellow, phục vụ tuần tra an toàn kho bãi, áp suất bình chữa cháy.
5. **Check-in Du lịch (Travel Memories)**: Trong suốt tối giản, hiển thị địa danh, thời tiết và tọa độ danh lam thắng cảnh.
6. **Tối giản thời gian & GPS (Minimal Timestamp)**: Tinh gọn, chỉ hiển thị ngày giờ chuẩn xác và tọa độ.

### 3. Trình Tùy Biến Mẫu Dấu Chuyên Nghiệp (Template Editor)
- Tùy chỉnh chi tiết tiêu đề, tên dự án, hạng mục thi công, đơn vị nhà thầu, người giám sát, ghi chú.
- Chọn bảng màu thương hiệu: *Safety Orange, Blueprint Blue, Emerald Green, Golden Yellow, Crimson Red, Crisp White*.
- Chọn kiểu khung badge: *Glassmorphism (Kính mờ), Dark Card (Thẻ đen hiện đại), Bordered Stamp (Tem kỹ thuật), Minimal Transparent (Tối giản trong suốt)*.
- Điều chỉnh vị trí con dấu linh hoạt: *Góc dưới trái, Góc dưới phải, Góc trên trái, Góc trên phải, Chính giữa dưới*.
- Bật/tắt các trường dữ liệu tùy biến: Giây, Địa chỉ thực tế, Tọa độ GPS, Cao độ, Hướng la bàn, Thời tiết/Độ ẩm, Mã QR chống giả mạo.
- Tinh chỉnh độ trong suốt nền (Opacity 30% - 100%) và kích thước dấu (Scale 0.7x - 1.4x).

### 4. Hệ Thống Truy Vấn Đối Soát Chống Giả Mạo Độc Quyền (Authoritative Anti-Counterfeiting Query)
- **Thuật toán băm SHA-256**: Khi bấm chụp, ứng dụng trích xuất luồng byte điểm ảnh gốc và tính toán mã băm SHA-256 mật mã học kết hợp chữ ký số và tọa độ thời gian thực.
- **Tạo mã QR xác thực**: Tự động nhúng mã QR chứa token định danh duy nhất vào góc watermark.
- **Trung tâm đối soát & kiểm tra toàn diện**:
  - Nhập mã hồ sơ (VD: `SL-20260930-1234`) hoặc quét liên kết QR (`snaplab://verify?...`).
  - Chọn bất kỳ bức ảnh nào từ máy để giải mã và kiểm tra đối chiếu mã băm SHA-256: phát hiện ngay ảnh đã bị can thiệp cắt ghép, tẩy xóa, chỉnh sửa Photoshop.
  - Cấp **Chứng thư kỹ thuật số xác thực (Digital Verification Certificate)** hiển thị trạng thái `ĐÃ XÁC THỰC CHÍNH CHỦ` (màu xanh neon) hoặc `CẢNH BÁO: DỮ LIỆU ĐÃ BỊ CHỈNH SỬA` (màu đỏ) kèm thông tin thời gian chụp gốc, tọa độ GPS, thiết bị và chữ ký điện tử.

### 5. Đóng Dấu Cho Ảnh Có Sẵn Từ Album Thiết Bị
- Cho phép người dùng chọn ảnh đã chụp trước đó từ Bộ sưu tập của điện thoại.
- Xem trước trực tiếp với watermark được áp dụng.
- Đổi mẫu hoặc tinh chỉnh thông số con dấu tức thì.
- Xuất và lưu ảnh mới đã nung dấu với chất lượng cao nhất vào thư mục máy.

### 6. Quản Lý Thư Viện SnapLab & Chia Sẻ
- Thư viện ảnh/video riêng biệt của SnapLab với bộ lọc phân loại nhanh (*Tất cả, Ảnh, Video*).
- Trình xem chi tiết Media Detail: hiển thị kích thước, dung lượng, thời gian, vị trí thực địa, mã băm SHA-256.
- Nút chia sẻ trực tiếp qua Zalo, Messenger, Gmail, Google Drive, Bluetooth...
- Nút tắt mở nhanh trang đối soát bản quyền cho bức ảnh đang xem.

### 7. Hỗ Trợ Đa Ngôn Ngữ Song Ngữ (Tiếng Việt 🇻🇳 & English 🇺🇸)
- **Nút chuyển đổi nhanh 1-chạm** (`🇻🇳 VI` / `🇺🇸 EN`) được bố trí ngay trên thanh công cụ camera và trong trang Cài đặt.
- Toàn bộ nhãn, danh mục, mẫu dấu, thông báo và con dấu nung trên ảnh đều tự động cập nhật theo ngôn ngữ đã chọn.

---

## 📁 Cấu Trúc Dự Án

```
CAMERA-FT-ADR/
├── app/
│   ├── build.gradle.kts                   # Khai báo dependency CameraX, Compose, ZXing, Coil...
│   ├── proguard-rules.pro                 # Cấu hình tối ưu Proguard / R8
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml        # Khai báo quyền Camera, Audio, GPS, Storage, Deep Link
│           ├── java/com/snaplab/cameraft/
│           │   ├── MainActivity.kt        # Khởi chạy giao diện chính & cấp quyền Runtime
│           │   ├── SnapLabApplication.kt  # Lớp Application khởi tạo tài nguyên
│           │   ├── models/
│           │   │   ├── CapturedMedia.kt   # Model lưu trữ tập tin ảnh/video
│           │   │   ├── VerificationRecord.kt # Model chứng thư số & mã băm SHA-256
│           │   │   ├── VerificationStatus.kt # Enum trạng thái đối soát
│           │   │   └── WatermarkTemplate.kt  # Model mẫu dấu, màu sắc, vị trí, preset
│           │   ├── services/
│           │   │   ├── AntiCounterfeitingManager.kt # Xử lý SHA-256, chữ ký số, sổ lưu trữ
│           │   │   ├── CameraManager.kt             # Điều khiển CameraX, Flash, Zoom, Video
│           │   │   ├── ImageUtils.kt                # Tạo mã QR ZXing, sửa góc quay, scene giả lập
│           │   │   ├── LocalizationManager.kt       # Quản lý song ngữ VI / EN
│           │   │   ├── LocationWeatherManager.kt    # GPS vệ tinh, Geocoding, La bàn Sensor
│           │   │   ├── PhotoLibraryManager.kt       # Quản lý xuất ảnh ra MediaStore
│           │   │   └── WatermarkRenderer.kt         # Nung watermark lên ảnh bằng Canvas/Paint
│           │   └── ui/
│           │       ├── anticounterfeit/
│           │       │   └── AntiCounterfeitQueryScreen.kt # Màn hình đối soát chống giả mạo
│           │       ├── camera/
│           │       │   ├── CameraControlsOverlay.kt     # Thanh công cụ, nút chụp, zoom, mode
│           │       │   ├── CameraScreen.kt              # Màn hình Camera chính
│           │       │   ├── GridOverlay.kt               # Lưới bố cục 3x3
│           │       │   └── WatermarkOverlayView.kt      # Lớp watermark trực tiếp thời gian thực
│           │       ├── gallery/
│           │       │   ├── AlbumWatermarkEditorScreen.kt# Đóng dấu cho ảnh có sẵn trong máy
│           │       │   ├── GalleryScreen.kt             # Lưới thư viện ảnh/video
│           │       │   └── MediaDetailScreen.kt         # Xem chi tiết ảnh & chia sẻ
│           │       ├── settings/
│           │       │   └── SettingsScreen.kt            # Cài đặt ngôn ngữ, lưới, bảo mật
│           │       ├── templates/
│           │       │   ├── TemplateEditorDialog.kt      # Hộp thoại tùy biến mẫu dấu chi tiết
│           │       │   └── TemplateSelectorDrawer.kt    # Khay chọn mẫu dấu phía dưới
│           │       └── theme/
│           │           ├── Color.kt                     # Bảng màu SnapLab Dark & Neon
│           │           ├── Theme.kt                     # Material3 Dark Theme
│           │           └── Type.kt                      # Cấu hình phông chữ
│           └── res/
│               ├── drawable/
│               ├── mipmap-anydpi-v26/
│               ├── values/
│               └── xml/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties      # Gradle 8.4
├── build.gradle.kts                       # Root Gradle Plugin AGP 8.3.2 & Kotlin 1.9.23
├── gradle.properties                      # JVM args & AndroidX settings
├── gradlew                                # Bash wrapper script
├── gradlew.bat                            # Windows batch wrapper script
├── settings.gradle.kts                    # Root settings & repository config
└── README.md
```

---

## 🛠 Hướng Dẫn Mở & Chạy Dự Án Trên Android Studio

### Yêu Cầu Môi Trường
- **Android Studio**: Android Studio Hedgehog (2023.1.1), Iguana, Jellyfish, Koala hoặc mới hơn.
- **JDK**: Java Development Kit 17 (mặc định đã đi kèm trong Android Studio).
- **Hệ điều hành**: Windows 10/11, macOS, hoặc Linux.
- **Android SDK**: API 34 (Android 14) trở lên (hỗ trợ chạy từ Android 8.0 Oreo - API 26 đến Android 15).

### Các Bước Khởi Chạy:

1. **Mở Android Studio**:
   - Chọn **Open** (hoặc `File` -> `Open...`).
   - Trỏ tới thư mục: `c:\Users\Admin\Downloads\CAMERA-FT-ADR`.
   - Android Studio sẽ tự động nhận diện Gradle và bắt đầu quá trình **Sync Project with Gradle Files**.

2. **Cài Đặt / Kết Nối Thiết Bị Chạy (Run Target)**:
   - **Máy thật**: Bật chế độ *Gỡ lỗi USB (USB Debugging)* trên điện thoại Android của bạn và cắm cáp vào máy tính.
   - **Máy ảo (Emulator / AVD)**: Tạo một máy ảo như *Pixel 7 / Pixel 8 (Android 14 - API 34)* từ Device Manager.
     > *Lưu ý*: Nếu chạy trên Emulator không có webcam, ứng dụng tự động hiển thị khung cảnh công trường ảo sắc nét (Synthetic Live View) để bạn test chụp ảnh, nung dấu và kiểm tra chống giả mạo bình thường.

3. **Chạy Ứng Dụng**:
   - Nhấn nút tam giác màu xanh **Run** `▶` (hoặc phím tắt `Shift + F10`).
   - Ứng dụng sẽ được biên dịch và cài đặt lên máy/giả lập trong vài giây.
   - Lần đầu mở, hãy bấm **Cho phép (Allow)** các quyền:
     - 📷 **Camera**: Để chụp ảnh và quay video hiện trường.
     - 📍 **Vị trí (Location)**: Để tự động lấy tọa độ GPS kinh độ/vĩ độ, cao độ và địa chỉ đường phố.
     - 🎤 **Micro**: Để thu âm khi quay video.
     - 📁 **Bộ nhớ / Ảnh (Photos & Media)**: Để lưu ảnh đã đóng dấu và chọn ảnh từ máy để đối soát.

---

## 🔒 Kiểm Thử Tính Năng Chống Giả Mạo (Anti-Counterfeit Demo)

1. Mở ứng dụng và bấm nút chụp một bức ảnh bất kỳ.
2. Dưới góc bức ảnh sẽ có mã định danh `SL-YYYYMMDD-XXXX` kèm mã QR xác thực.
3. Bấm vào nút **Đối soát** (biểu tượng khiên bảo vệ màu xanh lá) ở thanh điều khiển dưới.
4. Chọn tính năng **Chọn ảnh từ thiết bị để kiểm tra chỉnh sửa**:
   - Chọn bức ảnh vừa chụp: Hệ thống sẽ tính toán mã băm SHA-256 và cấp chứng nhận `ĐÃ XÁC THỰC CHÍNH CHỦ` (màu xanh lá) với đầy đủ ngày giờ gốc và tọa độ GPS.
   - Nếu bạn dùng phần mềm sửa ảnh, cắt xén hoặc vẽ đè lên ảnh rồi đưa vào kiểm tra: Hệ thống sẽ lập tức cảnh báo `CẢNH BÁO: DỮ LIỆU ĐÃ BỊ CHỈNH SỬA` (màu đỏ) do mã băm điểm ảnh không trùng khớp với bản ghi bảo mật gốc.

---

## 👨‍💻 Tác Giả & Bản Quyền

- Dự án: **CAMERA-FT-ADR (SnapLab Android)**
- Tác giả: **Phạm Tùng Dương**
- GitHub: [PhamTungDuong2111/CAMERA-FT-ADR](https://github.com/PhamTungDuong2111/CAMERA-FT-ADR.git)
