package app;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import model.CongViec;
import model.HoatDong;
import model.HocTap;
import model.SinhHoat;
import service.QuanLyThoiKhoaBieu;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static QuanLyThoiKhoaBieu ql = new QuanLyThoiKhoaBieu();
    static DateTimeFormatter FD = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        taoDuLieuMau();
        int chon;
        do {
            hienMenu();
            chon = docSoNguyen("Chọn chức năng: ");
            switch (chon) {
                case 1: themHoatDong(); break;
                case 2: inDanhSach(ql.getTatCa()); break;
                case 3: xemTheoNgay(); break;
                case 4: timKiem(); break;
                case 5: danhDauXong(); break;
                case 6: xoaHoatDong(); break;
                case 7: nhacNhoHomNay(); break;
                case 8: System.out.println(ql.thongKe()); break;
                case 0: System.out.println("Tạm biệt!"); break;
                default: System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (chon != 0);
    }

    static void hienMenu() {
        System.out.println("\n===== QUẢN LÝ THỜI KHÓA BIỂU CÁ NHÂN =====");
        System.out.println("1. Thêm hoạt động");
        System.out.println("2. Xem toàn bộ thời khóa biểu");
        System.out.println("3. Xem theo ngày");
        System.out.println("4. Tìm kiếm theo tên");
        System.out.println("5. Đánh dấu hoàn thành");
        System.out.println("6. Xóa hoạt động");
        System.out.println("7. Nhắc nhở hôm nay");
        System.out.println("8. Thống kê");
        System.out.println("0. Thoát");
    }

    // ================== CÁC CHỨC NĂNG ==================

    static void themHoatDong() {
        System.out.println("Loại: 1-Học tập  2-Công việc  3-Sinh hoạt");
        int loai = docSoNguyen("Chọn loại: ");
        if (loai < 1 || loai > 3) {
            System.out.println("Loại không hợp lệ!");
            return;
        }
        System.out.print("Tên hoạt động: ");
        String ten = sc.nextLine().trim();
        LocalDate ngay = docNgay("Ngày (dd/MM/yyyy): ");
        LocalTime batDau = docGio("Giờ bắt đầu (HH:mm): ");
        LocalTime ketThuc = docGio("Giờ kết thúc (HH:mm): ");
        if (!ketThuc.isAfter(batDau)) {
            System.out.println("Giờ kết thúc phải sau giờ bắt đầu!");
            return;
        }
        System.out.print("Ghi chú: ");
        String ghiChu = sc.nextLine().trim();

        HoatDong hd;
        if (loai == 1) {
            System.out.print("Môn học: ");
            hd = new HocTap(ten, ngay, batDau, ketThuc, ghiChu, sc.nextLine().trim());
        } else if (loai == 2) {
            int uuTien = docSoNguyen("Mức ưu tiên (1-Thấp, 2-Trung bình, 3-Cao): ");
            hd = new CongViec(ten, ngay, batDau, ketThuc, ghiChu, uuTien);
        } else {
            System.out.print("Địa điểm: ");
            hd = new SinhHoat(ten, ngay, batDau, ketThuc, ghiChu, sc.nextLine().trim());
        }

        HoatDong trung = ql.them(hd);
        if (trung == null) {
            System.out.println("Đã thêm thành công!");
        } else {
            System.out.println("Trùng giờ với: " + trung);
        }
    }

    static void inDanhSach(ArrayList<HoatDong> ds) {
        if (ds.isEmpty()) {
            System.out.println("(Không có hoạt động nào)");
            return;
        }
        for (HoatDong h : ds) {
            System.out.println(h); // đa hình: gọi đúng phương thức của từng lớp con
        }
    }

    static void xemTheoNgay() {
        LocalDate ngay = docNgay("Nhập ngày cần xem (dd/MM/yyyy): ");
        inDanhSach(ql.getTheoNgay(ngay));
    }

    static void timKiem() {
        System.out.print("Nhập từ khóa: ");
        inDanhSach(ql.timTheoTen(sc.nextLine().trim()));
    }

    static void danhDauXong() {
        int id = docSoNguyen("Nhập mã hoạt động: ");
        System.out.println(ql.danhDauHoanThanh(id) ? "Đã cập nhật!" : "Không tìm thấy mã " + id);
    }

    static void xoaHoatDong() {
        int id = docSoNguyen("Nhập mã hoạt động cần xóa: ");
        System.out.println(ql.xoa(id) ? "Đã xóa!" : "Không tìm thấy mã " + id);
    }

    static void nhacNhoHomNay() {
        ArrayList<HoatDong> homNay = ql.getTheoNgay(LocalDate.now());
        System.out.println("--- NHẮC NHỞ NGÀY " + LocalDate.now().format(FD) + " ---");
        boolean co = false;
        for (HoatDong h : homNay) {
            if (!h.isHoanThanh()) {
                System.out.println(h.taoLoiNhac()); // gọi qua interface NhacNho
                co = true;
            }
        }
        if (!co) {
            System.out.println("Không có việc nào cần nhắc hôm nay.");
        }
    }

    // ================== HÀM TIỆN ÍCH NHẬP LIỆU ==================
    // (Dùng try-catch để bắt lỗi nhập sai - sẽ học kỹ ở Chương 9)

    static int docSoNguyen(String thongBao) {
        while (true) {
            System.out.print(thongBao);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập một số nguyên!");
            }
        }
    }

    static LocalDate docNgay(String thongBao) {
        while (true) {
            System.out.print(thongBao);
            try {
                return LocalDate.parse(sc.nextLine().trim(), FD);
            } catch (DateTimeParseException e) {
                System.out.println("Ngày không hợp lệ, ví dụ: 20/10/2026");
            }
        }
    }

    static LocalTime docGio(String thongBao) {
        while (true) {
            System.out.print(thongBao);
            try {
                return LocalTime.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Giờ không hợp lệ, ví dụ: 07:30");
            }
        }
    }

    static void taoDuLieuMau() {
        LocalDate hn = LocalDate.now();
        ql.them(new SinhHoat("Chạy bộ buổi sáng", hn, LocalTime.of(6, 0), LocalTime.of(6, 45), "", "Công viên"));
        ql.them(new HocTap("Học Java chương 8", hn, LocalTime.of(8, 0), LocalTime.of(10, 0), "Ôn kế thừa", "Lập trình Java"));
        ql.them(new CongViec("Làm báo cáo nhóm", hn, LocalTime.of(14, 0), LocalTime.of(16, 0), "", 3));
    }
}