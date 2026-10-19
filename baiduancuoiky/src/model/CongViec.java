package model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Công việc: có thêm mức độ ưu tiên (1 = thấp, 2 = trung bình, 3 = cao). */
public class CongViec extends HoatDong {

    private int mucUuTien;

    public CongViec(String ten, LocalDate ngay, LocalTime gioBatDau,
                    LocalTime gioKetThuc, String ghiChu, int mucUuTien) {
        super(ten, ngay, gioBatDau, gioKetThuc, ghiChu);
        this.mucUuTien = mucUuTien;
    }

    public int getMucUuTien() { return mucUuTien; }
    public void setMucUuTien(int mucUuTien) { this.mucUuTien = mucUuTien; }

    private String tenMucUuTien() {
        if (mucUuTien == 3) return "Cao";
        if (mucUuTien == 2) return "Trung bình";
        return "Thấp";
    }

    @Override
    public String getLoai() {
        return "Công việc";
    }

    @Override
    public String moTaThem() {
        return "Ưu tiên: " + tenMucUuTien();
    }

    @Override
    public String taoLoiNhac() {
        return "💼 Công việc (ưu tiên " + tenMucUuTien() + "): " + getTen()
                + " lúc " + getGioBatDau();
    }
}