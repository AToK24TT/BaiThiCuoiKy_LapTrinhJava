package model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Sinh hoạt cá nhân (ăn uống, ngủ nghỉ, thể thao...): có thêm địa điểm. */
public class SinhHoat extends HoatDong {

    private String diaDiem;

    public SinhHoat(String ten, LocalDate ngay, LocalTime gioBatDau,
                    LocalTime gioKetThuc, String ghiChu, String diaDiem) {
        super(ten, ngay, gioBatDau, gioKetThuc, ghiChu);
        this.diaDiem = diaDiem;
    }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    @Override
    public String getLoai() {
        return "Sinh hoạt";
    }

    @Override
    public String moTaThem() {
        return "Địa điểm: " + diaDiem;
    }

    @Override
    public String taoLoiNhac() {
        return "🏃 Sinh hoạt: " + getTen() + " tại " + diaDiem
                + " lúc " + getGioBatDau();
    }
}