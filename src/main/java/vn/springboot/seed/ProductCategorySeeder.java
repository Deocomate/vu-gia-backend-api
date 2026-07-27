package vn.springboot.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.repository.ProductCategoryRepository;

import java.util.List;

/**
 * Ported 1:1 from {@code V2__seed_db.sql} "PRODUCT CATEGORIES" section (6 rows, one per
 * {@link CategoryType}), then extended with {@code detailContent} — the source SQL never
 * populated that column, leaving {@code CategoryDetailContent} on the storefront category
 * page with nothing to render. Blocks use the category-blocks schema
 * ({@code rich-paragraph}/{@code heading}/{@code image} — see the client's
 * {@code category-blocks/schema.js}), not the {@code paragraph}/{@code quote}/
 * {@code image-grid}/{@code list-section} shape used by {@link NewsSeeder}'s {@code des}.
 */
@Component
@RequiredArgsConstructor
public class ProductCategorySeeder implements DomainSeeder {

    private final ProductCategoryRepository productCategoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isEmpty() {
        return productCategoryRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        productCategoryRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        productCategoryRepository.saveAll(List.of(
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.BO_DO_THO)
                        .name("Bộ đồ thờ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(1)
                        .shortDescription("Bộ đồ thờ gốm sứ Bát Tràng chế tác thủ công, trang nghiêm cho không gian thờ cúng gia tiên.")
                        .slug("bo-do-tho")
                        .isActive(true)
                        .seoTitle("Bộ đồ thờ Bát Tràng")
                        .seoDescription("Bộ đồ thờ gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(boDoThoDetail())
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.BINH_PHONG_THUY)
                        .name("Bình phong thủy")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(2)
                        .shortDescription("Bình phong thủy gốm sứ Bát Tràng, hoạ tiết tài lộc phú quý, hợp mệnh gia chủ.")
                        .slug("binh-phong-thuy")
                        .isActive(true)
                        .seoTitle("Bình phong thủy Bát Tràng")
                        .seoDescription("Bình phong thủy gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(binhPhongThuyDetail())
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.LUC_BINH_GOM_SU)
                        .name("Lục bình gốm sứ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(3)
                        .shortDescription("Lục bình gốm sứ Bát Tràng dáng cổ, hoạ tiết tinh xảo, trang trí không gian sang trọng.")
                        .slug("luc-binh-gom-su")
                        .isActive(true)
                        .seoTitle("Lục bình gốm sứ Bát Tràng")
                        .seoDescription("Lục bình gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(lucBinhDetail())
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.AM_CHEN_BAT_TRANG)
                        .name("Ấm chén Bát Tràng")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(4)
                        .shortDescription("Ấm chén Bát Tràng men lam, men rạn cổ, phù hợp dùng hàng ngày hoặc làm quà tặng.")
                        .slug("am-chen-bat-trang")
                        .isActive(true)
                        .seoTitle("Ấm chén Bát Tràng")
                        .seoDescription("Ấm chén gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(amChenDetail())
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.QUA_TANG_GOM_SU)
                        .name("Quà tặng gốm sứ")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(5)
                        .shortDescription("Quà tặng gốm sứ Bát Tràng cao cấp, phù hợp biếu tặng dịp lễ, tết, khai trương.")
                        .slug("qua-tang-gom-su")
                        .isActive(true)
                        .seoTitle("Quà tặng gốm sứ Bát Tràng")
                        .seoDescription("Quà tặng gốm sứ Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(quaTangDetail())
                        .build(),
                ProductCategoryEntity.builder()
                        .categoryType(CategoryType.CHUM_SANH_NGAM_RUOU)
                        .name("Chum sành ngâm rượu")
                        .thumb("assets/images/products/product-category-thumb.png")
                        .priority(6)
                        .shortDescription("Chum sành ngâm rượu Bát Tràng, chất sành tự nhiên giúp rượu êm và tròn vị hơn theo thời gian.")
                        .slug("chum-sanh-ngam-ruou")
                        .isActive(true)
                        .seoTitle("Chum sành ngâm rượu Bát Tràng")
                        .seoDescription("Chum sành ngâm rượu Bát Tràng chính hãng Vũ Gia.")
                        .detailContent(chumSanhDetail())
                        .build()));
    }

    private String boDoThoDetail() {
        return detail(
                heading("Bộ đồ thờ Bát Tràng - trọn vẹn nghi lễ, vẹn tròn thành kính", 2),
                richParagraph(
                        "Một <strong>bộ đồ thờ</strong> hoàn chỉnh không đơn thuần là vật dụng bày biện, mà là nơi gửi gắm lòng thành kính của con cháu hướng về tổ tiên, thần linh. Tại Vũ Gia, mỗi bộ đồ thờ đều được nghệ nhân làng gốm Bát Tràng chế tác thủ công từ khâu tạo hình, vẽ họa tiết đến nung men, đảm bảo sự trang nghiêm và bền bỉ theo năm tháng.",
                        "lead"),
                richParagraph(
                        "Bộ sản phẩm thường bao gồm bát hương, bát thờ, chóe thờ, mâm bồng, kỷ chén, nậm rượu, ống hương, lọ hoa, đèn thờ và chân nến - đầy đủ để bài trí một ban thờ chuẩn phong thủy, hài hòa giữa công năng sử dụng và giá trị thẩm mỹ.",
                        "body"),
                image("assets/images/home/HomeCraftsmanship-1.png", "Bộ đồ thờ gốm sứ Bát Tràng bày biện trang nghiêm trên ban thờ gia tiên."),
                heading("Chất men và họa tiết đặc trưng", 3),
                richParagraph(
                        "Vũ Gia cung cấp đa dạng dòng men: <strong>men lam</strong> cổ điển xanh dịu, <strong>men rạn</strong> mang vẻ đẹp hoài cổ và dòng vẽ vàng 24k sang trọng, đi kèm các họa tiết truyền thống như rồng chầu, hoa sen, chữ Thọ - mỗi họa tiết đều mang một tầng ý nghĩa tâm linh riêng.",
                        "body"),
                richParagraph(
                        "Sản phẩm được nung ở nhiệt độ trên 1.200 độ C, giúp cốt gốm đanh chắc, không thấm nước và giữ màu bền vững, đáp ứng nhu cầu sử dụng lâu dài của mọi gia đình Việt.",
                        "body"));
    }

    private String binhPhongThuyDetail() {
        return detail(
                heading("Bình phong thủy - hài hòa Ngũ hành, chiêu tài đón lộc", 2),
                richParagraph(
                        "<strong>Bình phong thủy</strong> gốm sứ Bát Tràng không chỉ là vật phẩm trang trí mà còn được xem như một pháp khí cân bằng năng lượng trong không gian sống. Chọn đúng dáng bình, đúng màu men hợp mệnh gia chủ giúp gia tăng vượng khí và tài lộc.",
                        "lead"),
                richParagraph(
                        "Vũ Gia tuyển chọn kỹ lưỡng từng dáng bình - từ bình hút lộc cổ điển, bình tỳ bà đến bình đắp nổi rồng phượng - để phù hợp với nhiều không gian: phòng khách, phòng làm việc hay sảnh đón khách của các công trình lớn.",
                        "body"),
                image("assets/images/home/home-new-1.png", "Bình phong thủy men lam vẽ vàng đặt trang trọng trong phòng khách."),
                heading("Cách chọn bình theo bản mệnh", 3),
                richParagraph(
                        "Mệnh Kim - Thủy hợp gam men trắng, xanh dương, ánh kim; mệnh Mộc - Hỏa hợp men xanh lá, đỏ, nâu đất; mệnh Thổ hợp gam men vàng, nâu đất trầm ấm. Đội ngũ tư vấn của Vũ Gia luôn sẵn sàng hỗ trợ khách hàng chọn được chiếc bình <strong>tương sinh</strong> nhất với bản mệnh của mình.",
                        "body"));
    }

    private String lucBinhDetail() {
        return detail(
                heading("Lục bình gốm sứ Bát Tràng - dáng cổ, hoạ tiết tinh xảo", 2),
                richParagraph(
                        "<strong>Lục bình</strong> là dòng sản phẩm chủ lực làm nên tên tuổi gốm sứ Bát Tràng, với dáng bình cao, cổ thon, thân phình tròn đầy đặn - biểu tượng cho sự sung túc, viên mãn trong đời sống gia đình Việt.",
                        "lead"),
                richParagraph(
                        "Từng chiếc lục bình tại Vũ Gia được vuốt tay hoặc đắp nổi thủ công, phối cùng các họa tiết rồng chầu, hoa sen, tùng - cúc - trúc - mai, thể hiện tay nghề điêu luyện của nghệ nhân qua từng đường nét chạm khắc.",
                        "body"),
                image("assets/images/home/HomeCraftsmanship-3.png", "Nghệ nhân đắp nổi hoa văn rồng chầu trên thân lục bình Bát Tràng."),
                heading("Ứng dụng trong không gian sống", 3),
                richParagraph(
                        "Một đôi lục bình đặt cân xứng hai bên phòng khách, sảnh chính hay tiền sảnh biệt thự không chỉ tôn lên vẻ sang trọng, cổ kính mà còn mang ý nghĩa phong thủy tốt lành, cân bằng âm dương cho không gian sống.",
                        "body"));
    }

    private String amChenDetail() {
        return detail(
                heading("Ấm chén Bát Tràng - tinh hoa nghệ thuật thưởng trà Việt", 2),
                richParagraph(
                        "Bộ <strong>ấm chén Bát Tràng</strong> là người bạn đồng hành quen thuộc trong văn hóa thưởng trà của người Việt. Chất đất mịn, dáng ấm cân đối cùng nắp đậy kín khít giúp giữ trọn hương vị trà qua từng lần pha.",
                        "lead"),
                richParagraph(
                        "Vũ Gia mang đến các dòng men lam xanh biếc, men rạn cổ hoài niệm và những họa tiết vẽ tay tỉ mỉ, phù hợp cả cho nhu cầu sử dụng hàng ngày lẫn làm quà biếu sang trọng, ý nghĩa.",
                        "body"),
                image("assets/images/home/home-new-2.png", "Bộ ấm chén men lam Bát Tràng bày biện tinh tế trên bàn trà."),
                heading("Bí quyết chọn ấm chén bền đẹp", 3),
                richParagraph(
                        "Ưu tiên sản phẩm có xương gốm đanh chắc, tráng men đều màu, vòi ấm rót nước không bị nhỏ giọt. Với dòng ấm tử sa, nên dưỡng ấm thường xuyên để thân ấm lên nước bóng đẹp tự nhiên theo thời gian sử dụng.",
                        "body"));
    }

    private String quaTangDetail() {
        return detail(
                heading("Quà tặng gốm sứ - món quà mang giá trị văn hoá bền lâu", 2),
                richParagraph(
                        "Trong văn hóa Á Đông, một món <strong>quà tặng gốm sứ</strong> tinh xảo không chỉ thể hiện sự trân trọng mà còn gửi gắm lời chúc phúc sâu sắc đến người nhận, phù hợp cho dịp lễ, Tết, khai trương hay tân gia.",
                        "lead"),
                richParagraph(
                        "Vũ Gia cung cấp đa dạng sản phẩm quà tặng từ bình hoa, tượng phong thủy đến bộ ấm chén đóng hộp sang trọng, có thể tùy chỉnh khắc tên hoặc lời chúc theo yêu cầu của khách hàng.",
                        "body"),
                image("assets/images/products/product-content-image-thumb.png", "Hộp quà gốm sứ Bát Tràng được đóng gói sang trọng, tinh tế."),
                heading("Dịch vụ đóng gói và giao hàng quà tặng", 3),
                richParagraph(
                        "Mỗi sản phẩm quà tặng đều được bọc lót cẩn thận, đóng hộp chắc chắn kèm thiệp chúc theo yêu cầu, đảm bảo món quà đến tay người nhận trọn vẹn và ý nghĩa dù ở bất kỳ đâu.",
                        "body"));
    }

    private String chumSanhDetail() {
        return detail(
                heading("Chum sành ngâm rượu - chuẩn vị truyền thống Bát Tràng", 2),
                richParagraph(
                        "<strong>Chum sành</strong> ngâm rượu Bát Tràng được làm từ đất sét nung ở nhiệt độ cao, giữ được độ xốp tự nhiên của chất sành - yếu tố then chốt giúp rượu \"thở\", loại bỏ độc tố và êm dịu hơn theo thời gian ủ.",
                        "lead"),
                richParagraph(
                        "Khác với chum sứ tráng men bóng bẩy, chum sành giữ nguyên vẻ mộc mạc, gần gũi nhưng lại vượt trội về khả năng lọc rượu, phù hợp để ngâm rượu thuốc, rượu trái cây hay rượu nếp truyền thống.",
                        "body"),
                image("assets/images/home/HomeCraftsmanship-2.png", "Chum sành Bát Tràng dùng ngâm rượu, giữ nguyên vẻ mộc mạc truyền thống."),
                heading("Lưu ý khi chọn và sử dụng chum sành", 3),
                richParagraph(
                        "Nên chọn chum có thành dày đều, không rạn nứt, nắp đậy kín khít. Trước khi sử dụng lần đầu, nên súc rửa và ngâm nước sạch vài ngày để loại bỏ mùi đất, giúp rượu lên hương chuẩn vị nhất.",
                        "body"));
    }

    private String detail(ObjectNode... blocks) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode array = root.putArray("blocks");
        for (ObjectNode block : blocks) {
            array.add(block);
        }
        return writeJson(root);
    }

    private ObjectNode heading(String text, int level) {
        return objectMapper.createObjectNode().put("type", "heading").put("text", text).put("level", level);
    }

    private ObjectNode richParagraph(String html, String variant) {
        return objectMapper.createObjectNode().put("type", "rich-paragraph").put("html", html).put("variant", variant);
    }

    private ObjectNode image(String url, String caption) {
        return objectMapper.createObjectNode().put("type", "image").put("url", url).put("caption", caption);
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize seed JSON", e);
        }
    }
}
