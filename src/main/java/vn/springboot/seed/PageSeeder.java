package vn.springboot.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.page.PageEntity;
import vn.springboot.repository.PageRepository;

import java.util.List;

/**
 * Ported 1:1 from {@code V2__seed_db.sql} "PAGES" section — 7 rows (not the 5 the plan
 * estimated; the real SQL has {@code home, about, factory, contact, privacy-policy,
 * shipping-policy, return-policy}), each with a structurally distinct {@code content}
 * JSON shape, so each is built individually below rather than through a shared builder.
 */
@Component
@RequiredArgsConstructor
public class PageSeeder implements DomainSeeder {

    private final PageRepository pageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isEmpty() {
        return pageRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        pageRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        pageRepository.saveAll(List.of(homePage(), aboutPage(), factoryPage(), contactPage(),
                privacyPolicyPage(), shippingPolicyPage(), returnPolicyPage()));
    }

    private PageEntity homePage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        productListSection(sections, "BỘ ĐỒ THỜ TRUYỀN THỐNG",
                "BỘ ĐỒ THỜ MEN LAM", "BỘ ĐỒ THỜ LAM VẼ VÀNG 24K", "BỘ ĐỒ THỜ MEN RẠN");
        productListSection(sections, "BÌNH PHONG THỦY", "BÌNH MEN MÀU", "BÌNH MEN LAM", "BÌNH ĐẮP NỔI");
        productListSection(sections, "LỤC BÌNH GỐM SỨ", "LỤC BÌNH MEN MÀU", "LỤC BÌNH MEN LAM", "LỤC BÌNH ĐẮP NỔI");

        return page("home", "Trang chủ", root,
                "Ưu đãi tháng 6 - 299.000đ - Sản phẩm gốm Bát Tràng", null, null,
                "assets/images/home/hero-image-1-top.png",
                "Trang chủ", "Gốm Sứ Vũ Gia - gốm sứ Bát Tràng chính hãng.");
    }

    private void productListSection(ArrayNode sections, String title, String... tabs) {
        ObjectNode section = sections.addObject();
        section.put("type", "productList").put("title", title);
        ArrayNode tabsArray = section.putArray("tabs");
        for (String tab : tabs) {
            tabsArray.add(tab);
        }
    }

    private PageEntity aboutPage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        sections.addObject().put("type", "heading").put("text", "HỘI TỤ TINH HOA LÀNG NGHỀ");
        sections.addObject().put("type", "block")
                .put("image", "assets/images/about/about-image-1.jpg")
                .put("text", "Gốm Sứ Vũ Gia kết tinh di sản nghìn năm lửa đỏ Bát Tràng, mỗi tác phẩm là dấu ấn bàn tay nghệ nhân.");
        sections.addObject().put("type", "block")
                .put("image", "assets/images/about/about-image-2.jpg")
                .put("text", "Chúng tôi kiên trì gìn giữ phương thức thủ công truyền thống trong từng công đoạn chế tác.");
        sections.addObject().put("type", "image")
                .put("image", "assets/images/about/about-image-3.jpg")
                .put("text", "Quy trình sản xuất Gốm Vũ Gia");

        return page("about", "Về chúng tôi", root, null, null,
                "Kết tinh di sản rực rỡ nghìn năm lửa đỏ Bát Tràng.",
                "assets/images/about/hero-bg.jpg",
                "Về chúng tôi", "Gốm Sứ Vũ Gia - kết tinh di sản rực rỡ nghìn năm lửa đỏ Bát Tràng.");
    }

    private PageEntity factoryPage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        sections.addObject().put("type", "paragraph").put("text",
                "Để đáp ứng những đơn hàng lớn cho các công trình trọng điểm như đình chùa, biệt thự hay khu nghỉ dưỡng, Vũ Gia đã đầu tư hệ thống nhà xưởng với tổng diện tích lên đến 5.000m², được thiết kế tối ưu với 3 tầng sản xuất. Việc mở rộng không gian không chỉ khẳng định năng lực cung ứng mạnh mẽ mà còn giúp chúng tôi kiểm soát chất lượng sản phẩm một cách khắt khe nhất.");
        sections.addObject().put("type", "quote").put("text",
                "Lựa chọn Gốm Vũ Gia, quý khách không chỉ mua một loại vật liệu xây dựng, mà đang đặt niềm tin vào một quy trình vận hành tận tâm, chuyên nghiệp và những giá trị văn hóa bền vững theo thời gian.");
        sections.addObject().put("type", "block")
                .put("heading", "Sức mạnh của sự kết hợp: Máy móc hiện đại & Bàn tay nghệ nhân")
                .put("text", "Dù sở hữu hệ thống máy móc hỗ trợ hiện đại để đảm bảo độ chuẩn xác về thông số kỹ thuật, tại Vũ Gia giá trị cốt lõi vẫn nằm ở đôi bàn tay con người. Chúng tôi kiên trì giữ vững phương thức thủ công truyền thống trong các khâu quan trọng.");
        ArrayNode gallery = sections.addObject().put("type", "gallery").putArray("images");
        gallery.add("assets/images/nha-xuong/slider-image-1.png");
        gallery.add("assets/images/nha-xuong/slider-image-2.png");
        gallery.add("assets/images/nha-xuong/slider-image-3.png");
        gallery.add("assets/images/nha-xuong/slider-image-4.png");
        gallery.add("assets/images/product-detail/product-detail-thumbnail.png");

        return page("factory", "Nhà xưởng", root,
                "Nghệ thuật chế tác", "Quy mô ấn tượng: 5000m² - 3 tầng vận hành chuyên biệt", null,
                "assets/images/about/hero-bg.jpg",
                "Nhà xưởng", "Khám phá nhà xưởng và năng lực sản xuất của Gốm Sứ Vũ Gia.");
    }

    private PageEntity contactPage() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("company", "Gốm Vũ Gia - Thanh Hai Co., LTD");
        root.put("label", "TRỤ SỞ CHÍNH & SHOWROOM 1");
        root.put("address", "Số 18 Giang Cao, Bát Tràng, Gia Lâm, Hà Nội");
        root.put("hotline", "091 7777 247");
        root.put("email", "gomvugia@gmail.com");
        root.put("image", "assets/images/about/hero-bg.jpg");

        return page("contact", "Liên hệ", root, "Liên hệ", null,
                "Liên hệ Gốm Sứ Vũ Gia để được tư vấn sản phẩm và dịch vụ.",
                "assets/images/about/hero-bg.jpg",
                "Liên hệ", "Liên hệ Gốm Sứ Vũ Gia để được tư vấn sản phẩm và dịch vụ.");
    }

    private PageEntity privacyPolicyPage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        headingTextSection(sections, "Giới thiệu",
                "Gốm Vũ Gia cam kết tôn trọng và bảo vệ quyền riêng tư của khách hàng. Chính sách này mô tả cách chúng tôi thu thập, sử dụng và bảo vệ thông tin cá nhân mà bạn cung cấp khi truy cập website hoặc sử dụng dịch vụ của chúng tôi.");
        headingTextSection(sections, "Mục đích thu thập thông tin cá nhân",
                "Xử lý đơn hàng, tư vấn chuyên sâu, cải thiện dịch vụ, tiếp thị và truyền thông (chỉ khi có sự đồng ý của bạn).");
        headingTextSection(sections, "Phạm vi sử dụng thông tin",
                "Họ và tên, số điện thoại, email; địa chỉ giao hàng; nội dung tư vấn/yêu cầu thiết kế riêng; thông tin thanh toán (không bao gồm thông tin thẻ tín dụng trực tiếp trên hệ thống).");
        headingTextSection(sections, "Thời gian lưu trữ thông tin",
                "Thông tin cá nhân được lưu trữ cho đến khi có yêu cầu hủy bỏ từ khách hàng hoặc khi không còn cần thiết. Thông tin luôn được bảo mật trên máy chủ của Gốm Vũ Gia.");
        headingTextSection(sections, "Chia sẻ thông tin với bên thứ ba",
                "Cam kết không bán, cho thuê hoặc chia sẻ thông tin cá nhân, ngoại trừ đơn vị vận chuyển và theo yêu cầu của cơ quan chức năng có thẩm quyền.");
        headingTextSection(sections, "Cam kết bảo mật thông tin",
                "Sử dụng giao thức mã hóa SSL, hệ thống tường lửa và kiểm soát truy cập nghiêm ngặt để bảo vệ thông tin cá nhân của bạn.");
        headingTextSection(sections, "Quyền của khách hàng",
                "Khách hàng có quyền kiểm tra, cập nhật, điều chỉnh, dừng sử dụng cho mục đích quảng cáo hoặc yêu cầu xóa bỏ hoàn toàn dữ liệu cá nhân.");
        headingTextSection(sections, "Thông tin liên hệ",
                "Thanh Hai Co.,Ltd - Số 18 Phố Gốm, Giang Cao, Bát Tràng, Hà Nội. Hotline: 091 7777 247. Email: gomvugia@gmail.com. Website: https://gomvugia.vn");

        return page("privacy-policy", "Chính sách bảo mật thông tin", root, null, null, null, null,
                "Bảo mật thông tin", "Chính sách bảo mật thông tin khách hàng của Gốm Sứ Vũ Gia.");
    }

    private PageEntity shippingPolicyPage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        headingTextSection(sections, "Giới thiệu",
                "Tại Gốm Vũ Gia, chúng tôi hiểu rằng hàng gốm sứ rất dễ vỡ. Vì vậy, quy trình vận chuyển được thiết lập chuyên nghiệp để đảm bảo sản phẩm đến tay khách hàng an toàn với chi phí tối ưu nhất.");
        headingTextSection(sections, "Phí vận chuyển & thời gian giao hàng",
                "Nội thành Hà Nội (ship ghép từ 70.000đ, trong/sau 1 ngày; ship riêng theo km, giao ngay). Tỉnh miền Bắc (xe tải chuyên dụng hoặc xe khách, 1-2 ngày). Miền Trung & Nam (xe tải ghép Bắc-Nam 3-7 ngày, xe khách 2-4 ngày).");
        headingTextSection(sections, "Quy định đóng gói & bảo hiểm nứt vỡ",
                "Đóng thùng carton cứng hoặc đóng đai chắc chắn; tùy chọn đóng kiện gỗ 50.000đ-300.000đ. Vũ Gia cam kết đền bù 100% giá trị sản phẩm nếu nứt vỡ do lỗi vận chuyển. Vui lòng quay video quá trình mở hộp để làm căn cứ.");
        headingTextSection(sections, "Chính sách kiểm hàng",
                "Khách hàng có quyền mở thùng kiểm tra, đối chiếu mẫu mã, xác nhận tình trạng trước khi thanh toán. Nếu có vấn đề, liên hệ ngay hotline để được xử lý.");
        headingTextSection(sections, "Xử lý khi hàng có sự cố (nứt/vỡ)",
                "Bước 1: Quay phim/chụp ảnh hiện trạng. Bước 2: Liên hệ Hotline/Zalo trong vòng 24h. Bước 3: Xác nhận mã và số lượng bị vỡ. Vũ Gia gửi bù sản phẩm mới 100% miễn phí hoặc hoàn tiền.");

        return page("shipping-policy", "Chính sách vận chuyển - đóng gói - kiểm hàng", root, null, null, null, null,
                "Chính sách vận chuyển", "Thông tin vận chuyển và giao nhận đơn hàng tại Gốm Sứ Vũ Gia.");
    }

    private void headingTextSection(ArrayNode sections, String heading, String text) {
        sections.addObject().put("heading", heading).put("text", text);
    }

    private PageEntity returnPolicyPage() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode sections = root.putArray("sections");
        sections.addObject().put("type", "image")
                .put("image", "assets/images/customer-services/chinh-sach-doi-tra.png").put("device", "desktop");
        sections.addObject().put("type", "image")
                .put("image", "assets/images/customer-services/chinh-sach-doi-tra-mobile.png").put("device", "mobile");
        sections.addObject().put("type", "image")
                .put("image", "assets/images/customer-services/chinh-sach-doi-tra-last.png").put("device", "mobile");

        return page("return-policy", "Chính sách đổi trả", root, null, null, null, null,
                "Chính sách đổi trả", "Chính sách đổi trả sản phẩm của Gốm Sứ Vũ Gia.");
    }

    private PageEntity page(String key, String title, JsonNode content, String heroTitle, String heroSubtitle,
            String heroDes, String heroImage, String seoTitle, String seoDescription) {
        return PageEntity.builder()
                .key(key)
                .title(title)
                .content(writeJson(content))
                .heroTitle(heroTitle)
                .heroSubtitle(heroSubtitle)
                .heroDes(heroDes)
                .heroImage(heroImage)
                .status(ContentStatus.PUBLISHED)
                .seoTitle(seoTitle)
                .seoDescription(seoDescription)
                .build();
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize seed JSON", e);
        }
    }
}
