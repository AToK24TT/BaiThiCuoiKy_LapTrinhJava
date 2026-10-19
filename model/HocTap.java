package model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Hoạt động học tập: có thêm môn học. */
public class HocTap extends HoatDong {

    private String monHoc;

    public HocTap(String ten, LocalDate ngay, LocalTime gioBatDau,
                  LocalTime gioKetThuc, String ghiChu, String monHoc) {
        super(ten, ngay, gioBatDau, gioKetThuc, ghiChu);
        this.monHoc = monHoc;
    }

    public String getMonHoc() { return monHoc; }
    public void setMonHoc(String monHoc) { this.monHoc = monHoc; }

    @Override
    public String getLoai() {
        return "Học tập";
    }

    @Override
    public String moTaThem() {
        return "Môn: " + monHoc;
    }

    @Override
    public String taoLoiNhac() {
        return "📚 Đến giờ học môn " + monHoc + ": " + getTen()
                + " (" + getGioBatDau() + ")";
    }
}