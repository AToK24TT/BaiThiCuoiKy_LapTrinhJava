package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp trừu tượng HoatDong: chứa thông tin chung của mọi hoạt động trong thời khóa biểu.
 * Các lớp con: HocTap, CongViec, SinhHoat (tính kế thừa).
 */
public abstract class HoatDong implements NhacNho, Comparable<HoatDong> {

    // Biến static dùng để tự tăng mã hoạt động
    private static int demId = 1;

    // Tính đóng gói: thuộc tính để private
    private int id;
    private String ten;
    private LocalDate ngay;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String ghiChu;
    private boolean hoanThanh;

    public HoatDong(String ten, LocalDate ngay, LocalTime gioBatDau,
                    LocalTime gioKetThuc, String ghiChu) {
        this.id = demId++;
        this.ten = ten;
        this.ngay = ngay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.ghiChu = ghiChu;
        this.hoanThanh = false;
    }

    // ===== Phương thức trừu tượng: lớp con bắt buộc phải viết lại =====
    public abstract String getLoai();

    public abstract String moTaThem();

    // ===== Getter / Setter =====
    public int getId() { return id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isHoanThanh() { return hoanThanh; }
    public void setHoanThanh(boolean hoanThanh) { this.hoanThanh = hoanThanh; }

    /** Kiểm tra hai hoạt động có bị trùng giờ trong cùng một ngày không. */
    public boolean trungGio(HoatDong other) {
        if (!this.ngay.equals(other.ngay)) {
            return false;
        }
        return this.gioBatDau.isBefore(other.gioKetThuc)
                && other.gioBatDau.isBefore(this.gioKetThuc);
    }

    // Sắp xếp theo ngày rồi đến giờ bắt đầu
    @Override
    public int compareTo(HoatDong other) {
        int kq = this.ngay.compareTo(other.ngay);
        if (kq == 0) {
            kq = this.gioBatDau.compareTo(other.gioBatDau);
        }
        return kq;
    }

    @Override
    public String toString() {
        DateTimeFormatter fd = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fh = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("[%d] %-10s | %s | %s-%s | %s | %s%s",
                id, getLoai(), ngay.format(fd),
                gioBatDau.format(fh), gioKetThuc.format(fh),
                ten, moTaThem(),
                hoanThanh ? " | ✔ Đã xong" : "");
    }
}