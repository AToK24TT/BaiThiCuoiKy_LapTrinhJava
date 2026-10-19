package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import model.HoatDong;

/** Lớp xử lý nghiệp vụ: thêm, xóa, tìm, xem, nhắc nhở, thống kê. */
public class QuanLyThoiKhoaBieu {

    private ArrayList<HoatDong> danhSach = new ArrayList<>();

    /** Thêm hoạt động. Trả về hoạt động bị trùng giờ (null nếu không trùng và đã thêm). */
    public HoatDong them(HoatDong hd) {
        for (HoatDong h : danhSach) {
            if (h.trungGio(hd)) {
                return h; // trùng giờ -> không thêm
            }
        }
        danhSach.add(hd);
        Collections.sort(danhSach);
        return null;
    }

    public HoatDong timTheoId(int id) {
        for (HoatDong h : danhSach) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

    public boolean xoa(int id) {
        HoatDong h = timTheoId(id);
        if (h == null) {
            return false;
        }
        return danhSach.remove(h);
    }

    public boolean danhDauHoanThanh(int id) {
        HoatDong h = timTheoId(id);
        if (h == null) {
            return false;
        }
        h.setHoanThanh(true);
        return true;
    }

    public ArrayList<HoatDong> getTatCa() {
        return danhSach;
    }

    public ArrayList<HoatDong> getTheoNgay(LocalDate ngay) {
        ArrayList<HoatDong> kq = new ArrayList<>();
        for (HoatDong h : danhSach) {
            if (h.getNgay().equals(ngay)) {
                kq.add(h);
            }
        }
        return kq;
    }

    /** Tìm theo từ khóa trong tên (không phân biệt hoa thường). */
    public ArrayList<HoatDong> timTheoTen(String tuKhoa) {
        ArrayList<HoatDong> kq = new ArrayList<>();
        for (HoatDong h : danhSach) {
            if (h.getTen().toLowerCase().contains(tuKhoa.toLowerCase())) {
                kq.add(h);
            }
        }
        return kq;
    }

    /** Thống kê số lượng theo loại, dùng getLoai() (đa hình). */
    public String thongKe() {
        int hocTap = 0, congViec = 0, sinhHoat = 0, xong = 0;
        for (HoatDong h : danhSach) {
            switch (h.getLoai()) {
                case "Học tập": hocTap++; break;
                case "Công việc": congViec++; break;
                default: sinhHoat++; break;
            }
            if (h.isHoanThanh()) {
                xong++;
            }
        }
        return "Tổng: " + danhSach.size() + " | Học tập: " + hocTap
                + " | Công việc: " + congViec + " | Sinh hoạt: " + sinhHoat
                + " | Đã hoàn thành: " + xong;
    }
}