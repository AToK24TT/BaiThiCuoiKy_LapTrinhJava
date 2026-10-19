package app;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.CongViec;
import model.HoatDong;
import model.HocTap;
import model.SinhHoat;
import service.QuanLyThoiKhoaBieu;

/** Chạy file này (Run As > Java Application): trình duyệt tự mở http://localhost:8080 */
public class WebServer {

    static final QuanLyThoiKhoaBieu ql = new QuanLyThoiKhoaBieu();

    public static void main(String[] args) throws IOException {
        taoDuLieuMau();
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
        server.createContext("/", WebServer::trangChu);
        server.createContext("/api/hoatdong", WebServer::apiHoatDong);
        server.createContext("/api/nhacnho", WebServer::apiNhacNho);
        server.createContext("/api/xong", WebServer::apiXong);
        server.createContext("/api/xoa", WebServer::apiXoa);
        server.start();
        System.out.println("Server đang chạy: http://localhost:8085  (bấm nút đỏ ở Console để dừng)");
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8085"));
            }
        } catch (Exception e) {
            System.out.println("Hãy tự mở trình duyệt và vào http://localhost:8085S");
        }
    }

    // ============ Trang chủ: trả về giao diện HTML nằm ngay cuối file này ============
    static void trangChu(HttpExchange ex) throws IOException {
        gui(ex, 200, "text/html", TRANG_CHU);
    }

    // ============ GET: danh sách (lọc theo ten / ngay)  |  POST: thêm ============
    static synchronized void apiHoatDong(HttpExchange ex) throws IOException {
        Map<String, String> p = thamSo(ex);
        if (ex.getRequestMethod().equals("POST")) {
            themHoatDong(ex, p);
            return;
        }
        ArrayList<HoatDong> ds;
        if (p.containsKey("ten") && !p.get("ten").isBlank()) {
            ds = ql.timTheoTen(p.get("ten").trim());
        } else if (p.containsKey("ngay") && !p.get("ngay").isBlank()) {
            try {
                ds = ql.getTheoNgay(LocalDate.parse(p.get("ngay")));
            } catch (DateTimeParseException e) {
                ds = new ArrayList<>();
            }
        } else {
            ds = ql.getTatCa();
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(json(ds.get(i)));
        }
        gui(ex, 200, "application/json", sb.append("]").toString());
    }

    static void themHoatDong(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            String loai = p.getOrDefault("loai", "");
            String ten = p.getOrDefault("ten", "").trim();
            LocalDate ngay = LocalDate.parse(p.getOrDefault("ngay", ""));
            LocalTime bd = LocalTime.parse(p.getOrDefault("bd", ""));
            LocalTime kt = LocalTime.parse(p.getOrDefault("kt", ""));
            String ghiChu = p.getOrDefault("ghiChu", "").trim();
            String them = p.getOrDefault("them", "").trim();

            if (ten.isEmpty()) { loi(ex, 400, "Hãy nhập tên hoạt động."); return; }
            if (!kt.isAfter(bd)) { loi(ex, 400, "Giờ kết thúc phải sau giờ bắt đầu."); return; }

            HoatDong hd;
            if (loai.equals("hoc")) {
                hd = new HocTap(ten, ngay, bd, kt, ghiChu, them);
            } else if (loai.equals("viec")) {
                hd = new CongViec(ten, ngay, bd, kt, ghiChu, Integer.parseInt(them));
            } else if (loai.equals("sinh")) {
                hd = new SinhHoat(ten, ngay, bd, kt, ghiChu, them);
            } else {
                loi(ex, 400, "Loại không hợp lệ.");
                return;
            }
            HoatDong trung = ql.them(hd);
            if (trung == null) {
                gui(ex, 200, "application/json", "{\"ok\":true}");
            } else {
                loi(ex, 409, "Trùng giờ với: " + trung.getTen() + " (" + trung.getGioBatDau()
                        + "-" + trung.getGioKetThuc() + ").");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            loi(ex, 400, "Ngày, giờ hoặc mức ưu tiên không hợp lệ.");
        }
    }

    // ============ Nhắc nhở hôm nay: gọi taoLoiNhac() qua interface NhacNho ============
    static synchronized void apiNhacNho(HttpExchange ex) throws IOException {
        StringBuilder sb = new StringBuilder("[");
        boolean dau = true;
        for (HoatDong h : ql.getTheoNgay(LocalDate.now())) {
            if (!h.isHoanThanh()) {
                if (!dau) sb.append(",");
                sb.append("\"").append(thoat(h.taoLoiNhac())).append("\"");
                dau = false;
            }
        }
        gui(ex, 200, "application/json", sb.append("]").toString());
    }

    static synchronized void apiXong(HttpExchange ex) throws IOException {
        int id = docId(ex);
        HoatDong h = ql.timTheoId(id);
        if (h == null) { loi(ex, 404, "Không tìm thấy mã " + id); return; }
        h.setHoanThanh(!h.isHoanThanh()); // bấm lần nữa để bỏ hoàn thành
        gui(ex, 200, "application/json", "{\"ok\":true}");
    }

    static synchronized void apiXoa(HttpExchange ex) throws IOException {
        int id = docId(ex);
        if (ql.xoa(id)) gui(ex, 200, "application/json", "{\"ok\":true}");
        else loi(ex, 404, "Không tìm thấy mã " + id);
    }

    // ============ Hàm tiện ích ============
    static String json(HoatDong h) {
        String khoa = "sinh", them = "";
        if (h instanceof HocTap) { khoa = "hoc"; them = ((HocTap) h).getMonHoc(); }
        else if (h instanceof CongViec) { khoa = "viec"; them = String.valueOf(((CongViec) h).getMucUuTien()); }
        else if (h instanceof SinhHoat) { them = ((SinhHoat) h).getDiaDiem(); }
        return "{\"id\":" + h.getId()
                + ",\"khoa\":\"" + khoa + "\""
                + ",\"loai\":\"" + thoat(h.getLoai()) + "\""
                + ",\"ten\":\"" + thoat(h.getTen()) + "\""
                + ",\"ngay\":\"" + h.getNgay() + "\""
                + ",\"bd\":\"" + h.getGioBatDau() + "\""
                + ",\"kt\":\"" + h.getGioKetThuc() + "\""
                + ",\"ghiChu\":\"" + thoat(h.getGhiChu()) + "\""
                + ",\"moTa\":\"" + thoat(h.moTaThem()) + "\""
                + ",\"xong\":" + h.isHoanThanh() + "}";
    }

    static String thoat(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    static int docId(HttpExchange ex) throws IOException {
        try {
            return Integer.parseInt(thamSo(ex).getOrDefault("id", "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Gộp tham số trên URL và trong body (dạng a=b&c=d) vào một Map. */
    static Map<String, String> thamSo(HttpExchange ex) throws IOException {
        Map<String, String> m = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        for (String chuoi : new String[] { q == null ? "" : q, body }) {
            for (String cap : chuoi.split("&")) {
                if (cap.isEmpty()) continue;
                int i = cap.indexOf('=');
                String k = i < 0 ? cap : cap.substring(0, i);
                String v = i < 0 ? "" : cap.substring(i + 1);
                m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
            }
        }
        return m;
    }

    static void loi(HttpExchange ex, int ma, String thongBao) throws IOException {
        gui(ex, ma, "application/json", "{\"loi\":\"" + thoat(thongBao) + "\"}");
    }

    static void gui(HttpExchange ex, int ma, String kieu, String noiDung) throws IOException {
        byte[] b = noiDung.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", kieu + "; charset=utf-8");
        ex.sendResponseHeaders(ma, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    static void taoDuLieuMau() {
        LocalDate hn = LocalDate.now();
        ql.them(new SinhHoat("Chạy bộ buổi sáng", hn, LocalTime.of(6, 0), LocalTime.of(6, 45), "", "Công viên"));
        ql.them(new HocTap("Học Java chương 8", hn, LocalTime.of(8, 0), LocalTime.of(10, 0), "Ôn kế thừa", "Lập trình Java"));
        ql.them(new CongViec("Làm báo cáo nhóm", hn, LocalTime.of(14, 0), LocalTime.of(16, 0), "", 3));
    }

    // ============ Giao diện web (HTML/CSS/JS) ============
    static final String TRANG_CHU = """
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>Quản lý hoạt động cá nhân</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;700&display=swap" rel="stylesheet">
<style>
:root{--bg:#eef3f1;--panel:#fff;--ink:#17302c;--mute:#5d7570;--line:#d3e0dc;--acc:#0f766e;--acc-ink:#fff;
--hoc:#2f5da8;--viec:#b7791f;--sinh:#2f8a55;--bad:#b42318;
box-sizing:border-box;padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px)}
@media(prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#0f1a18;--panel:#172725;--ink:#e3efec;--mute:#93aba6;--line:#2a403d;--acc:#38b2a3;--acc-ink:#07201d;--hoc:#7aa2e8;--viec:#e0aa4f;--sinh:#5fc088;--bad:#ff8b7d}}
:root[data-theme="dark"]{--bg:#0f1a18;--panel:#172725;--ink:#e3efec;--mute:#93aba6;--line:#2a403d;--acc:#38b2a3;--acc-ink:#07201d;--hoc:#7aa2e8;--viec:#e0aa4f;--sinh:#5fc088;--bad:#ff8b7d}
*{box-sizing:border-box}
html{scroll-padding-top:env(safe-area-inset-top,0px)}
body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 "Be Vietnam Pro",system-ui,"Segoe UI",sans-serif}
header{padding:22px 20px 8px;max-width:1080px;margin:0 auto}
h1{font-size:24px;margin:0 0 4px;font-weight:700}
header p{margin:0;color:var(--mute)}
.stats{display:flex;flex-wrap:wrap;gap:8px;margin-top:14px}
.chip{padding:4px 12px;border:1px solid var(--line);border-radius:99px;background:var(--panel);font-size:13px}
main{max-width:1080px;margin:0 auto;padding:12px 20px 40px;display:grid;grid-template-columns:320px 1fr;gap:20px}
@media(max-width:820px){main{grid-template-columns:1fr}}
section.box{background:var(--panel);border:1px solid var(--line);border-radius:10px;padding:16px}
h2{font-size:16px;margin:0 0 12px}
label{display:block;font-size:13px;color:var(--mute);margin:10px 0 3px}
input,select,textarea,button{font:inherit;color:inherit}
input,select,textarea{width:100%;padding:8px 10px;border:1px solid var(--line);border-radius:6px;background:var(--bg)}
input:focus-visible,select:focus-visible,button:focus-visible,textarea:focus-visible{outline:2px solid var(--acc);outline-offset:1px}
.row{display:grid;grid-template-columns:1fr 1fr;gap:10px}
button{cursor:pointer;border:1px solid var(--line);background:var(--panel);border-radius:6px;padding:7px 12px}
button.main{background:var(--acc);color:var(--acc-ink);border-color:var(--acc);width:100%;margin-top:14px;padding:10px;font-weight:500}
button.small{padding:3px 9px;font-size:13px}
button.del{color:var(--bad)}
.msg{margin-top:10px;font-size:13px;min-height:20px}
.msg.err{color:var(--bad)}.msg.ok{color:var(--sinh)}
.bar{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px}
.bar input{flex:1;min-width:150px}.bar input[type=date]{flex:0 0 auto;width:auto}
.nhac{border-left:4px solid var(--acc);background:var(--panel);border-radius:0 8px 8px 0;padding:10px 14px;margin-bottom:14px}
.nhac h2{margin-bottom:6px}.nhac div{padding:2px 0}
.day{margin:18px 0 6px;font-weight:700;color:var(--mute);font-size:14px}
.item{display:grid;grid-template-columns:96px 1fr auto;gap:12px;align-items:center;background:var(--panel);border:1px solid var(--line);border-left:5px solid var(--c);border-radius:8px;padding:10px 12px;margin-bottom:8px}
.item.done{opacity:.55}.item.done .ten{text-decoration:line-through}
.time{font-variant-numeric:tabular-nums;font-weight:500}
.tag{font-size:12px;color:var(--c);font-weight:700}
.sub{color:var(--mute);font-size:13px}
.acts{display:flex;gap:6px}
.empty{padding:28px;text-align:center;color:var(--mute);border:1px dashed var(--line);border-radius:8px}
@media(max-width:520px){.item{grid-template-columns:1fr}.acts{justify-content:flex-start}}
</style>
</head>
<body>
<header>
  <h1>Quản lý hoạt động cá nhân</h1>
  
  <div class="stats" id="thongKe"></div>
</header>
<main>
  <section class="box">
    <h2>Thêm hoạt động</h2>
    <label for="loai">Loại</label>
    <select id="loai"><option value="hoc">Học tập</option><option value="viec">Công việc</option><option value="sinh">Sinh hoạt</option></select>
    <label for="ten">Tên hoạt động</label><input id="ten" autocomplete="off">
    <label for="ngay">Ngày</label><input id="ngay" type="date">
    <div class="row">
            <div><label for="bd">Giờ bắt đầu (24h)</label><input id="bd" type="text" inputmode="numeric" placeholder="18:00" maxlength="5" autocomplete="off"></div>
      <div><label for="kt">Giờ kết thúc (24h)</label><input id="kt" type="text" inputmode="numeric" placeholder="19:00" maxlength="5" autocomplete="off"></div>
    </div>
    <label for="ghiChu">Ghi chú</label><input id="ghiChu" autocomplete="off">
    <div id="extra"></div>
    <button class="main" id="btnThem">Thêm hoạt động</button>
    <div class="msg" id="msg" role="status"></div>
  </section>

  <div>
    <div class="bar">
      <input id="tim" type="search" placeholder="Tìm theo tên hoạt động" aria-label="Tìm theo tên">
      <input id="loc" type="date" aria-label="Xem theo ngày">
      <button id="btnTatCa">Xem tất cả</button>
    </div>
    <div class="nhac" id="nhac"></div>
    <div id="ds"></div>
  </div>
</main>

<script>
const $ = id => document.getElementById(id);
const homNay = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`; };
const dmy = s => s.split("-").reverse().join("/");
const esc = s => String(s).replace(/[&<>"]/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c]));
const MAU = { hoc: "var(--hoc)", viec: "var(--viec)", sinh: "var(--sinh)" };

// Gọi API của server Java (WebServer.java)
async function goi(url, tuyChon) {
  const r = await fetch(url, tuyChon);
  const d = await r.json();
  if (!r.ok) throw new Error(d.loi || "Có lỗi xảy ra.");
  return d;
}
const post = (url, obj) => goi(url, { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded" }, body: new URLSearchParams(obj) });

function veExtra() {
  const l = $("loai").value;
  $("extra").innerHTML = l === "hoc" ? '<label for="them">Môn học</label><input id="them" autocomplete="off">'
    : l === "viec" ? '<label for="them">Mức ưu tiên</label><select id="them"><option value="1">Thấp</option><option value="2" selected>Trung bình</option><option value="3">Cao</option></select>'
    : '<label for="them">Địa điểm</label><input id="them" autocomplete="off">';
}
function thongBao(t, loai) { const m = $("msg"); m.textContent = t; m.className = "msg " + loai; }

async function themHoatDong() {
  try {
    await post("/api/hoatdong", { loai: $("loai").value, ten: $("ten").value, ngay: $("ngay").value, bd: $("bd").value,
      kt: $("kt").value, ghiChu: $("ghiChu").value, them: $("them").value });
    thongBao("Đã thêm hoạt động.", "ok");
    $("ten").value = ""; $("ghiChu").value = ""; $("them").value = $("them").tagName === "SELECT" ? "2" : "";
    ve();
  } catch (e) { thongBao(e.message, "err"); }
}

async function ve() {
  const tim = $("tim").value.trim(), loc = $("loc").value;
  const [tatCa, ds, nhac] = await Promise.all([
    goi("/api/hoatdong"),
    goi("/api/hoatdong?" + new URLSearchParams(tim ? { ten: tim } : loc ? { ngay: loc } : {})),
    goi("/api/nhacnho")
  ]);
  const dem = l => tatCa.filter(h => h.loai === l).length;
  $("thongKe").innerHTML = [`Tổng ${tatCa.length}`, `Học tập ${dem("Học tập")}`, `Công việc ${dem("Công việc")}`,
    `Sinh hoạt ${dem("Sinh hoạt")}`, `Đã hoàn thành ${tatCa.filter(h => h.xong).length}`].map(s => `<span class="chip">${s}</span>`).join("");

  $("nhac").innerHTML = `<h2>Nhắc nhở hôm nay (${dmy(homNay())})</h2>` +
    (nhac.length ? nhac.map(s => `<div>${esc(s)}</div>`).join("") : '<div class="sub">Không có việc nào cần nhắc hôm nay.</div>');

  if (!ds.length) { $("ds").innerHTML = '<div class="empty">Không có hoạt động nào. Thêm hoạt động ở khung bên trái.</div>'; return; }
  let out = "", ngayCu = "";
  for (const h of ds) {
    if (h.ngay !== ngayCu) { ngayCu = h.ngay; out += `<div class="day">${dmy(ngayCu)}</div>`; }
    out += `<div class="item ${h.xong ? "done" : ""}" style="--c:${MAU[h.khoa]}">
      <div class="time">${h.bd} – ${h.kt}</div>
      <div><div class="ten"><b>${esc(h.ten)}</b></div>
        <div class="sub"><span class="tag">${h.loai}</span> · ${esc(h.moTa)}${h.ghiChu ? " · " + esc(h.ghiChu) : ""}</div></div>
      <div class="acts">
        <button class="small" data-xong="${h.id}">${h.xong ? "Bỏ hoàn thành" : "Hoàn thành"}</button>
        <button class="small del" data-xoa="${h.id}">Xóa</button></div></div>`;
  }
  $("ds").innerHTML = out;
}

$("loai").addEventListener("change", veExtra);
$("btnThem").addEventListener("click", themHoatDong);
$("tim").addEventListener("input", () => { $("loc").value = ""; ve(); });
$("loc").addEventListener("change", () => { $("tim").value = ""; ve(); });
$("btnTatCa").addEventListener("click", () => { $("tim").value = ""; $("loc").value = ""; ve(); });
$("ds").addEventListener("click", async e => {
  const x = e.target.dataset;
  try {
    if (x.xong) await post("/api/xong?id=" + x.xong, {});
    else if (x.xoa) await post("/api/xoa?id=" + x.xoa, {});
    else return;
    ve();
  } catch (err) { thongBao(err.message, "err"); }
});

$("ngay").value = homNay();
veExtra();
ve();
</script>
</body>
</html>
""";
}