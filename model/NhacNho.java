package model;

/**
 * Interface NhacNho: bất kỳ hoạt động nào muốn được nhắc nhở
 * đều phải mô tả cách tạo ra lời nhắc (tính trừu tượng - Chương 8).
 */
public interface NhacNho {
    // Mặc định là public abstract
    String taoLoiNhac();
}