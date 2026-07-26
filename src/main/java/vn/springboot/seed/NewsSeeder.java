package vn.springboot.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.entity.news.NewsEntity;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ported 1:1 from {@code V2__seed_db.sql} "NEWS" section (22 rows, ~1509 source lines
 * — the single largest domain in the seed). {@code news_category_id} is resolved via a
 * slug lookup against {@link NewsCategorySeeder}'s already-saved rows, not the source
 * SQL's literal ids 1-3. Every row's {@code seo_title}/{@code seo_description}/
 * {@code seo_image} equal its {@code title}/{@code short_content}/{@code thumb}
 * exactly (verified against the source SQL row by row), so the builder below derives
 * them instead of repeating three more parameters per call.
 */
@Component
@RequiredArgsConstructor
public class NewsSeeder implements DomainSeeder {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter SEED_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NewsRepository newsRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isEmpty() {
        return newsRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        newsRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        Map<String, NewsCategoryEntity> categoriesBySlug = newsCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(NewsCategoryEntity::getSlug, Function.identity()));

        newsRepository.saveAll(buildNews(categoriesBySlug));
    }

    private List<NewsEntity> buildNews(Map<String, NewsCategoryEntity> categoriesBySlug) {
        return List.of(
                // Kiến thức phong thuỷ (phong-thuy)
                news("Ý nghĩa chữ Thọ tròn trong không gian tâm linh Việt", "assets/images/home/home-new-1.png",
                        "Khám phá chiều sâu văn hóa truyền thống và ý nghĩa biểu tượng của họa tiết chữ Thọ tròn tinh xảo trên các tác phẩm thờ cúng.",
                        des(
                                paragraph("Khám phá chiều sâu văn hóa truyền thống và ý nghĩa biểu tượng của họa tiết chữ Thọ tròn tinh xảo trên các tác phẩm thờ cúng."),
                                image("assets/images/home/HomeCraftsmanship-1.png", "Họa tiết chữ Thọ tròn được chạm khắc tinh xảo trên đồ thờ gốm sứ."),
                                paragraph("Chữ Thọ tròn thường xuất hiện trên các vật phẩm thờ cúng, tượng trưng cho ước vọng trường tồn, viên mãn của gia chủ."),
                                quote("Chữ Thọ tròn không chỉ là họa tiết trang trí mà còn gửi gắm lời chúc phúc trường thọ, an khang cho cả gia đình.")),
                        "y-nghia-chu-tho-tron-trong-tam-linh-viet", 0, "2026-02-03 09:00:00", "phong-thuy", categoriesBySlug),
                news("Cách bài trí bàn thờ gia tiên chuẩn phong thủy rước tài lộc", "assets/images/home/home-new-2.png",
                        "Hướng dẫn chi tiết từ nghệ nhân làng nghề cách sắp xếp vị trí bát hương, kỷ nước và mâm bồng mang lại bình an.",
                        des(
                                paragraph("Bàn thờ gia tiên là nơi linh thiêng nhất trong mỗi gia đình Việt, không chỉ là nơi tưởng nhớ tổ tiên mà còn ảnh hưởng trực tiếp đến vượng khí và tài lộc của cả nhà."),
                                paragraph("Việc sắp xếp đúng vị trí bát hương, kỷ nước, mâm bồng theo nguyên tắc phong thủy truyền thống sẽ giúp gia chủ đón nhận nhiều may mắn, bình an trong cuộc sống."),
                                image("assets/images/home/home-new-2.png", "Bố cục bàn thờ gia tiên chuẩn mực với bát hương đặt chính giữa."),
                                heading("Nguyên tắc sắp xếp bát hương và kỷ nước trên bàn thờ"),
                                paragraph("Bát hương luôn được đặt ở vị trí trung tâm, ngay hàng thẳng lối với trục giữa bàn thờ, tượng trưng cho sự cân bằng âm dương."),
                                listSection(
                                        item("1. Vị trí bát hương:", "Đặt chính giữa bàn thờ, không xê dịch, tránh động bát hương gây ảnh hưởng đến vượng khí gia đình."),
                                        item("2. Kỷ nước và mâm bồng:", "Kỷ nước đặt phía trước bát hương, mâm bồng hoa quả đặt hai bên đối xứng, tạo sự hài hòa cân đối.")),
                                imageGrid(
                                        gridImage("assets/images/home/HomeCraftsmanship-1.png", "Kỷ nước ba chén tượng trưng cho sự thanh khiết."),
                                        gridImage("assets/images/home/HomeCraftsmanship-2.png", "Mâm bồng hoa quả bày biện đối xứng hai bên bàn thờ.")),
                                quote("Một bàn thờ được sắp xếp gọn gàng, đúng lễ nghi chính là cách con cháu bày tỏ lòng thành kính sâu sắc nhất với tổ tiên."),
                                paragraph("Ngoài việc sắp xếp đúng vị trí, gia chủ cũng nên thường xuyên lau dọn, thay nước và hương để bàn thờ luôn sạch sẽ, trang nghiêm quanh năm.")),
                        "cach-bai-tri-ban-tho-gia-tien-chuan-phong-thuy", 10, "2026-02-10 10:30:00", "phong-thuy", categoriesBySlug),
                news("Chọn bình hút tài lộc theo bản mệnh gia chủ cát tường", "assets/images/home/home-new-1.png",
                        "Bí quyết chọn lựa màu men, kiểu dáng bình hút lộc Bát Tràng tương sinh theo Ngũ hành của gia chủ.",
                        des(
                                paragraph("Bí quyết chọn lựa màu men, kiểu dáng bình hút lộc Bát Tràng tương sinh theo Ngũ hành của gia chủ."),
                                image("assets/images/home/home-new-3.png", "Bình hút lộc với nhiều màu men phù hợp từng bản mệnh."),
                                paragraph("Mỗi bản mệnh Ngũ hành sẽ tương ứng với một gam màu men khác nhau, giúp gia chủ chọn được bình hút lộc hợp phong thủy nhất."),
                                listSection(
                                        item("1. Mệnh Kim - Mệnh Thủy:", "Nên chọn bình men trắng, men xanh dương hoặc ánh kim."),
                                        item("2. Mệnh Mộc - Mệnh Hỏa:", "Phù hợp với bình men xanh lá, men đỏ hoặc nâu đất."))),
                        "chon-binh-hut-tai-loc-theo-ban-menh", 0, "2026-02-18 14:00:00", "phong-thuy", categoriesBySlug),
                news("Vị trí đặt lộc bình phòng khách mang lại tài lộc dồi dào", "assets/images/home/home-new-1.png",
                        "Cách bố trí đôi lộc bình trong phòng khách vừa tăng tính thẩm mỹ sang trọng vừa hợp phong thủy.",
                        des(
                                paragraph("Cách bố trí đôi lộc bình trong phòng khách vừa tăng tính thẩm mỹ sang trọng vừa hợp phong thủy."),
                                image("assets/images/home/home-new-1.png", "Đôi lộc bình đặt trang trọng hai bên phòng khách."),
                                paragraph("Lộc bình thường được đặt thành đôi ở hai bên cửa chính hoặc góc phòng khách để cân bằng năng lượng và thu hút vượng khí."),
                                quote("Một đôi lộc bình đặt đúng vị trí không chỉ tô điểm không gian sống mà còn mang lại sinh khí tốt lành cho gia chủ.")),
                        "vi-tri-dat-loc-binh-phong-khach", 10, "2026-04-10 13:20:00", "phong-thuy", categoriesBySlug),
                news("Nguyên tắc tam hợp trong lựa chọn họa tiết gốm tâm linh", "assets/images/home/home-new-2.png",
                        "Tìm hiểu về nguyên tắc kết hợp họa tiết phong thủy giúp gia đạo êm ấm, công danh thăng tiến.",
                        des(
                                paragraph("Tìm hiểu về nguyên tắc kết hợp họa tiết phong thủy giúp gia đạo êm ấm, công danh thăng tiến."),
                                image("assets/images/home/home-new-2.png", "Họa tiết tam hợp được phối hợp hài hòa trên sản phẩm gốm."),
                                paragraph("Nguyên tắc tam hợp giúp kết hợp các họa tiết phong thủy tương sinh, tránh xung khắc, mang lại sự hài hòa cho không gian thờ cúng."),
                                listSection(
                                        item("1. Tam hợp bản mệnh:", "Chọn họa tiết tương sinh với bản mệnh gia chủ để tăng cường vượng khí."),
                                        item("2. Tam hợp không gian:", "Phối hợp họa tiết phù hợp với diện tích và ánh sáng của không gian thờ."))),
                        "nguyen-tac-tam-hop-hoa-tiet-gom", 0, "2026-04-18 09:00:00", "phong-thuy", categoriesBySlug),
                news("Ý nghĩa của đôi hạc chầu trên bàn thờ gia tiên Việt", "assets/images/home/home-new-1.png",
                        "Hạc thờ bằng gốm sứ Bát Tràng biểu tượng cho sự trường thọ, thanh cao và kết nối tâm linh bền chặt.",
                        des(
                                paragraph("Hạc thờ bằng gốm sứ Bát Tràng biểu tượng cho sự trường thọ, thanh cao và kết nối tâm linh bền chặt."),
                                image("assets/images/home/home-new-1.png", "Đôi hạc thờ gốm sứ đứng chầu trang nghiêm trên bàn thờ."),
                                paragraph("Hình ảnh đôi hạc đứng trên lưng rùa là biểu tượng quen thuộc trên bàn thờ gia tiên, thể hiện sự trường tồn và kết nối giữa trời và đất."),
                                quote("Đôi hạc chầu không chỉ tô điểm không gian thờ cúng mà còn nhắc nhở con cháu về đạo lý uống nước nhớ nguồn.")),
                        "y-nghia-doi-hac-chau-tren-ban-tho", 0, "2026-04-25 15:10:00", "phong-thuy", categoriesBySlug),
                news("Cách bao sái bàn thờ cuối năm không lo động bát hương", "assets/images/home/home-new-1.png",
                        "Quy trình lau dọn bàn thờ thành kính, đúng cách để giữ gìn phước đức và may mắn cho năm mới.",
                        des(
                                paragraph("Bao sái bàn thờ cuối năm là nghi lễ quan trọng giúp gia đình tiễn năm cũ, đón năm mới với không gian thờ cúng sạch sẽ, trang nghiêm."),
                                paragraph("Tuy nhiên nhiều gia chủ lo lắng việc lau dọn có thể \"động\" bát hương, ảnh hưởng đến vượng khí nếu không thực hiện đúng cách."),
                                image("assets/images/home/home-new-1.png", "Nghi thức bao sái bàn thờ cần được thực hiện thành kính, tỉ mỉ."),
                                heading("Các bước bao sái bàn thờ đúng chuẩn, không lo động bát hương"),
                                paragraph("Trước khi lau dọn, gia chủ cần thắp hương xin phép tổ tiên và thần linh, sau đó mới tiến hành rút chân hương và lau dọn từng vật phẩm."),
                                listSection(
                                        item("1. Rút chân hương:", "Giữ lại số chân hương lẻ (thường là 3, 5, 7), phần còn lại đem hóa tro sạch sẽ, tuyệt đối không đổ bỏ tùy tiện."),
                                        item("2. Lau dọn đồ thờ:", "Dùng khăn sạch, nước ấm pha rượu gừng lau nhẹ nhàng từng món đồ thờ, tránh xê dịch vị trí bát hương.")),
                                imageGrid(
                                        gridImage("assets/images/products/product-content-image-thumb.png", "Chân hương được rút gọn gàng trước khi hóa tro."),
                                        gridImage("assets/images/home/home-new-2.png", "Đồ thờ được lau chùi sáng bóng sau khi bao sái.")),
                                quote("Bao sái không phải là xáo trộn mà là làm mới không gian thờ cúng trong sự thành kính và cẩn trọng."),
                                paragraph("Sau khi hoàn tất, gia chủ nên thắp hương tạ lễ và sắp xếp lại đồ thờ đúng vị trí ban đầu để đón năm mới an lành, sung túc.")),
                        "cach-bao-sai-ban-tho-cuoi-nam", 0, "2026-05-02 10:00:00", "phong-thuy", categoriesBySlug),
                // Kiến thức sản phẩm (san-pham)
                news("Ý nghĩa lịch sử hào hùng ngày giải phóng miền Nam 30/4/1975", "assets/images/home/home-new-2.png",
                        "Nhìn lại dòng chảy lịch sử vẻ vang và những cảm hứng nghệ thuật gốm sứ truyền tải thông điệp yêu nước.",
                        des(
                                paragraph("Nhìn lại dòng chảy lịch sử vẻ vang và những cảm hứng nghệ thuật gốm sứ truyền tải thông điệp yêu nước."),
                                image("assets/images/home/home-new-2.png", "Tác phẩm gốm sứ lấy cảm hứng từ tinh thần yêu nước hào hùng."),
                                paragraph("Nhiều nghệ nhân Bát Tràng đã khéo léo lồng ghép hình ảnh lịch sử vào các tác phẩm gốm sứ, như một cách tri ân thế hệ cha anh đi trước."),
                                quote("Mỗi tác phẩm gốm mang dấu ấn lịch sử là một lời nhắc nhở về giá trị hòa bình mà cha ông đã đánh đổi.")),
                        "giai-phong-mien-nam-30-4-1975-lich-su", 0, "2026-01-28 08:15:00", "san-pham", categoriesBySlug),
                news("Phân biệt men rạn cổ và men lam truyền thống Bát Tràng", "assets/images/home/home-new-1.png",
                        "Giúp người sưu tầm gốm nhận diện rõ nét đặc trưng độc bản của hai dòng men trứ danh ngàn năm tuổi.",
                        des(
                                paragraph("Men rạn và men lam là hai dòng men trứ danh làm nên tên tuổi gốm sứ Bát Tràng suốt hàng trăm năm qua."),
                                paragraph("Dù cùng xuất phát từ một làng nghề, hai dòng men này lại mang những đặc trưng hoàn toàn khác biệt về kỹ thuật chế tác lẫn vẻ đẹp thẩm mỹ."),
                                image("assets/images/home/home-new-1.png", "Men rạn cổ với những đường rạn tự nhiên độc đáo trên bề mặt gốm."),
                                heading("Đặc trưng nhận diện men rạn cổ và men lam Bát Tràng"),
                                paragraph("Men rạn được tạo ra nhờ sự chênh lệch độ co giãn giữa men và xương gốm khi nung, tạo nên các đường rạn chân chim tự nhiên, không món nào giống món nào."),
                                listSection(
                                        item("1. Men rạn cổ:", "Bề mặt có các đường rạn nhỏ như mạng nhện, màu men ngả vàng ngà, mang vẻ đẹp hoài cổ, trầm mặc."),
                                        item("2. Men lam truyền thống:", "Sử dụng oxit coban vẽ họa tiết xanh lam trên nền men trắng, nét vẽ tinh tế, màu sắc trong trẻo, tươi sáng.")),
                                imageGrid(
                                        gridImage("assets/images/home/home-new-2.png", "Men lam xanh biếc với họa tiết hoa văn tinh xảo."),
                                        gridImage("assets/images/home/HomeCraftsmanship-2.png", "So sánh trực quan giữa men rạn và men lam trên cùng dáng bình.")),
                                quote("Mỗi vết rạn, mỗi nét vẽ lam đều là dấu ấn thời gian và bàn tay tài hoa của nghệ nhân Bát Tràng."),
                                paragraph("Khi lựa chọn, người sưu tầm nên cân nhắc không gian trưng bày và sở thích cá nhân để chọn dòng men phù hợp, vừa tôn lên giá trị thẩm mỹ vừa thể hiện gu thưởng thức tinh tế.")),
                        "phan-biet-men-ran-co-va-men-lam", 10, "2026-03-05 09:45:00", "san-pham", categoriesBySlug),
                news("Quy trình chế tác đôi lục bình đắp nổi thủ công cầu kỳ", "assets/images/home/home-new-1.png",
                        "Hành trình từ đất sét thô sơ qua đôi tay nghệ nhân tạo tác hoa văn rồng chầu, hoa sen tinh xảo.",
                        des(
                                paragraph("Hành trình từ đất sét thô sơ qua đôi tay nghệ nhân tạo tác hoa văn rồng chầu, hoa sen tinh xảo."),
                                image("assets/images/home/HomeCraftsmanship-3.png", "Nghệ nhân đắp nổi hoa văn rồng chầu trên thân lục bình."),
                                paragraph("Mỗi họa tiết đắp nổi đều được thực hiện hoàn toàn thủ công, đòi hỏi sự kiên nhẫn và tay nghề cao của người nghệ nhân."),
                                listSection(
                                        item("1. Tạo hình thân bình:", "Đất sét được vuốt tay hoặc đổ khuôn tạo dáng cơ bản cho lục bình."),
                                        item("2. Đắp nổi hoa văn:", "Nghệ nhân đắp từng chi tiết hoa văn rồng, hoa sen lên thân bình trước khi nung."))),
                        "quy-trinh-che-tac-luc-binh-dap-noi", 0, "2026-03-12 11:00:00", "san-pham", categoriesBySlug),
                news("Đặc điểm nhận biết gốm vuốt tay thủ công cao cấp", "assets/images/home/home-new-2.png",
                        "Cách cảm nhận xương đất, độ dày và đường nét độc bản của các tác phẩm được chế tác hoàn toàn thủ công.",
                        des(
                                paragraph("Cách cảm nhận xương đất, độ dày và đường nét độc bản của các tác phẩm được chế tác hoàn toàn thủ công."),
                                image("assets/images/home/home-new-2.png", "Đôi bàn tay nghệ nhân vuốt tạo hình gốm trên bàn xoay."),
                                paragraph("Gốm vuốt tay thường có độ dày không đồng đều tự nhiên và những dấu vân tay nhẹ, khác biệt hoàn toàn so với gốm đổ khuôn công nghiệp."),
                                quote("Mỗi sản phẩm gốm vuốt tay là một bản độc bản, không có sản phẩm nào hoàn toàn giống nhau.")),
                        "dac-diem-nhan-biet-gom-vuot-tay", 0, "2026-05-10 09:20:00", "san-pham", categoriesBySlug),
                news("Cách bảo quan và làm sạch bộ đồ thờ men rạn đắp nổi", "assets/images/home/home-new-1.png",
                        "Mẹo nhỏ giúp giữ gìn độ sáng bóng, tránh bụi bẩn mà không ảnh hưởng đến lớp men rạn quý hiếm.",
                        des(
                                paragraph("Mẹo nhỏ giúp giữ gìn độ sáng bóng, tránh bụi bẩn mà không ảnh hưởng đến lớp men rạn quý hiếm."),
                                image("assets/images/home/home-new-1.png", "Bộ đồ thờ men rạn đắp nổi cần được vệ sinh nhẹ nhàng."),
                                paragraph("Men rạn có cấu trúc đặc biệt nên khi vệ sinh cần tránh dùng vật cứng chà xát mạnh làm ảnh hưởng đến các đường rạn men."),
                                listSection(
                                        item("1. Vệ sinh định kỳ:", "Dùng khăn mềm ẩm lau nhẹ bụi bẩn hàng tuần để giữ độ sáng bóng."),
                                        item("2. Tránh hóa chất mạnh:", "Không dùng chất tẩy rửa mạnh vì có thể làm phai màu men rạn theo thời gian."))),
                        "cach-bao-quan-do-tho-men-ran", 0, "2026-05-18 11:40:00", "san-pham", categoriesBySlug),
                news("Nghệ thuật vẽ vàng 24k trên nền gốm sứ tâm linh Vũ Gia", "assets/images/home/home-new-1.png",
                        "Tìm hiểu quy trình vẽ nhũ vàng và nung hấp nhiệt độ cao giúp vàng bám chặt bền bỉ cùng thời gian.",
                        des(
                                paragraph("Vẽ vàng 24k là kỹ thuật trang trí cao cấp, đòi hỏi sự tỉ mỉ và tay nghề điêu luyện của nghệ nhân Bát Tràng."),
                                paragraph("Lớp vàng thật sau khi nung ở nhiệt độ cao sẽ bám chặt vào cốt gốm, tạo nên vẻ đẹp sang trọng, bền bỉ theo thời gian."),
                                image("assets/images/home/home-new-1.png", "Nghệ nhân tỉ mỉ vẽ từng nét vàng 24k lên bề mặt gốm."),
                                heading("Quy trình vẽ vàng 24k chuẩn nghệ nhân Bát Tràng"),
                                paragraph("Trước khi vẽ vàng, sản phẩm gốm phải được nung sơ và làm nguội hoàn toàn để đảm bảo bề mặt láng mịn, không bong tróc."),
                                listSection(
                                        item("1. Pha chế vàng nước:", "Vàng 24k nguyên chất được pha cùng dung môi chuyên dụng để tạo độ bám và độ mịn khi vẽ."),
                                        item("2. Nung hấp nhiệt độ cao:", "Sau khi vẽ, sản phẩm được nung hấp ở nhiệt độ chuẩn để lớp vàng bám chắc, không phai màu theo thời gian.")),
                                imageGrid(
                                        gridImage("assets/images/home/home-new-2.png", "Chi tiết họa tiết vàng 24k sau khi hoàn thiện."),
                                        gridImage("assets/images/products/product-content-image-thumb.png", "Sản phẩm gốm dát vàng lấp lánh dưới ánh sáng.")),
                                quote("Một sản phẩm dát vàng đẹp không chỉ nằm ở độ tinh xảo của nét vẽ mà còn ở sự kiên nhẫn của người nghệ nhân."),
                                paragraph("Chính vì độ khó và giá trị nguyên liệu cao, các sản phẩm gốm dát vàng 24k thường được lựa chọn làm quà tặng cao cấp hoặc vật phẩm thờ cúng trang trọng.")),
                        "nghe-thuat-ve-vang-24k-tren-gom", 0, "2026-05-25 08:50:00", "san-pham", categoriesBySlug),
                news("Tại sao ấm trà tử sa Bát Tràng lại được người trà hữu tin dùng?", "assets/images/home/home-new-2.png",
                        "Sự kết hợp hoàn hảo giữa chất đất khoáng tự nhiên và kỹ thuật nung chuẩn xác giữ trọn hương vị trà.",
                        des(
                                paragraph("Sự kết hợp hoàn hảo giữa chất đất khoáng tự nhiên và kỹ thuật nung chuẩn xác giữ trọn hương vị trà."),
                                image("assets/images/home/home-new-2.png", "Ấm trà tử sa với đường nét tinh xảo, chuẩn form dáng cổ điển."),
                                paragraph("Chất đất tử sa có khả năng giữ nhiệt tốt và hấp thụ hương trà qua nhiều lần pha, giúp hương vị trà ngày càng đậm đà."),
                                quote("Người sành trà thường ví ấm tử sa như một \"người bạn\" đồng hành, càng dùng lâu càng thêm quý.")),
                        "tai-sao-am-tra-tu-sa-duoc-tin-dung", 0, "2026-06-01 09:15:00", "san-pham", categoriesBySlug),
                // Cẩm nang làng nghề (lang-nghe)
                news("Lịch sử ngàn năm gìn giữ và thổi bùng ngọn lửa Bát Tràng", "assets/images/home/home-new-1.png",
                        "Làng cổ Bát Tràng vượt qua thăng trầm thời gian để giữ vững thương hiệu gốm sứ tinh hoa số một Việt Nam.",
                        des(
                                paragraph("Làng gốm Bát Tràng đã trải qua hơn 700 năm hình thành và phát triển, trở thành cái nôi gốm sứ nổi tiếng bậc nhất Việt Nam."),
                                paragraph("Từ những lò nung thủ công đơn sơ ban đầu, Bát Tràng dần khẳng định vị thế với những sản phẩm gốm sứ tinh xảo, vươn ra thị trường trong và ngoài nước."),
                                image("assets/images/home/home-new-1.png", "Một góc làng nghề Bát Tràng với những lò gốm truyền thống."),
                                heading("Hành trình gìn giữ ngọn lửa nghề gốm qua bao thế hệ"),
                                paragraph("Trải qua nhiều thăng trầm lịch sử, người dân Bát Tràng vẫn kiên trì giữ lửa nghề, truyền nghề từ đời này sang đời khác."),
                                listSection(
                                        item("1. Thời kỳ hình thành:", "Bát Tràng ra đời từ thời Lý, ban đầu chỉ sản xuất gốm gia dụng phục vụ đời sống thường nhật."),
                                        item("2. Thời kỳ phát triển:", "Đến nay, làng nghề đã mở rộng sang gốm mỹ nghệ, gốm tâm linh cao cấp, xuất khẩu đi nhiều quốc gia.")),
                                imageGrid(
                                        gridImage("assets/images/home/HomeCraftsmanship-1.png", "Lò nung gốm truyền thống vẫn được gìn giữ đến ngày nay."),
                                        gridImage("assets/images/home/HomeCraftsmanship-3.png", "Sản phẩm gốm Bát Tràng hiện diện trong đời sống hiện đại.")),
                                quote("Ngọn lửa lò gốm Bát Tràng đã cháy suốt bảy thế kỷ và sẽ còn tiếp tục cháy mãi nhờ tâm huyết của những người thợ gốm."),
                                paragraph("Ngày nay, Bát Tràng không chỉ là làng nghề mà còn là điểm đến du lịch văn hóa hấp dẫn, nơi du khách có thể tận mắt chứng kiến quy trình làm gốm truyền thống.")),
                        "lich-su-ngan-nam-ngon-lua-bat-trang", 10, "2026-02-25 10:00:00", "lang-nghe", categoriesBySlug),
                news("Nghệ nhân ưu tú Giang Cao và khát vọng đánh thức đất sét", "assets/images/home/home-new-1.png",
                        "Lắng nghe những chia sẻ tâm huyết từ thế hệ giữ lửa Bát Tràng truyền thụ tình yêu nghề gốm cho người trẻ.",
                        des(
                                paragraph("Lắng nghe những chia sẻ tâm huyết từ thế hệ giữ lửa Bát Tràng truyền thụ tình yêu nghề gốm cho người trẻ."),
                                image("assets/images/home/home-new-1.png", "Nghệ nhân ưu tú Giang Cao bên tác phẩm gốm tâm huyết của mình."),
                                paragraph("Hơn 30 năm gắn bó với nghề, nghệ nhân đã truyền lửa đam mê cho nhiều thế hệ học trò trẻ tại làng gốm Bát Tràng."),
                                quote("Đất sét vô tri, nhưng qua bàn tay người nghệ nhân, nó có thể kể lại cả một câu chuyện văn hóa ngàn năm.")),
                        "nghe-nhan-uu-tu-giang-cao-va-khay-vong-dat", 0, "2026-03-20 09:30:00", "lang-nghe", categoriesBySlug),
                news("Bí quyết dưỡng ấm trà tử sa chuẩn nghệ thuật thưởng trà", "assets/images/home/home-new-2.png",
                        "Làm thế nào để chiếc ấm đất của bạn có độ bóng đẹp tự nhiên và giữ trọn hương vị trà thượng hạng.",
                        des(
                                paragraph("Ấm trà tử sa được giới sành trà yêu thích không chỉ bởi chất đất quý hiếm mà còn bởi khả năng \"dưỡng ấm\" theo thời gian sử dụng."),
                                paragraph("Một chiếc ấm tử sa được dưỡng đúng cách sẽ ngày càng lên màu bóng đẹp tự nhiên, giữ trọn hương vị trà thơm ngon hơn."),
                                image("assets/images/home/home-new-2.png", "Ấm trà tử sa Bát Tràng với chất đất mịn màng đặc trưng."),
                                heading("Bí quyết dưỡng ấm tử sa đúng chuẩn nghệ thuật thưởng trà"),
                                paragraph("Trước khi sử dụng lần đầu, ấm mới nên được luộc qua nước trà đặc để loại bỏ mùi đất và mở lỗ khí trên thành ấm."),
                                listSection(
                                        item("1. Dưỡng ấm hàng ngày:", "Sau mỗi lần pha trà, dùng khăn mềm thấm nước trà lau đều thân ấm để tạo lớp bóng tự nhiên."),
                                        item("2. Vệ sinh đúng cách:", "Chỉ tráng ấm bằng nước nóng, không dùng xà phòng hay hóa chất tẩy rửa làm mất đi lớp dưỡng đã hình thành.")),
                                imageGrid(
                                        gridImage("assets/images/home/home-new-1.png", "Thân ấm lên nước bóng đẹp sau thời gian dài sử dụng."),
                                        gridImage("assets/images/home/HomeCraftsmanship-2.png", "Nghệ nhân kiểm tra độ kín khít của nắp ấm tử sa.")),
                                quote("Một chiếc ấm tử sa được dưỡng lâu năm chính là người bạn tri kỷ của những ai đam mê nghệ thuật thưởng trà."),
                                paragraph("Kiên trì dưỡng ấm mỗi ngày không chỉ giúp ấm đẹp hơn theo thời gian mà còn mang lại trải nghiệm thưởng trà trọn vẹn, đậm đà hương vị truyền thống.")),
                        "bi-quyet-duong-am-tra-tu-sa-chuan", 0, "2026-04-02 08:45:00", "lang-nghe", categoriesBySlug),
                news("Kỹ thuật chế tác men ngọc độc bản truyền thừa dòng họ Vũ", "assets/images/home/home-new-1.png",
                        "Học hỏi phương pháp phối trộn tro trấu và đất sét trắng tạo nên sắc men trong vắt như ngọc bích.",
                        des(
                                paragraph("Học hỏi phương pháp phối trộn tro trấu và đất sét trắng tạo nên sắc men trong vắt như ngọc bích."),
                                image("assets/images/home/home-new-1.png", "Sản phẩm men ngọc trong vắt, đặc trưng của dòng họ Vũ."),
                                paragraph("Bí quyết pha chế men ngọc được gia tộc gìn giữ và truyền lại qua nhiều thế hệ, tạo nên sắc men độc bản khó nhầm lẫn."),
                                listSection(
                                        item("1. Nguyên liệu men ngọc:", "Tro trấu kết hợp đất sét trắng tinh khiết theo tỷ lệ bí truyền."),
                                        item("2. Kỹ thuật nung men:", "Nung ở nhiệt độ và thời gian chuẩn xác để men lên màu ngọc bích trong vắt."))),
                        "ky-thuat-che-tac-men-ngoc-truyen-thua", 0, "2026-06-08 10:05:00", "lang-nghe", categoriesBySlug),
                news("Hành trình đưa gốm sứ Việt vươn tầm thế giới", "assets/images/home/home-new-1.png",
                        "Những bước chuyển mình xuất khẩu gốm mỹ nghệ sang thị trường Nhật Bản và châu Âu của nghệ nhân Bát Tràng.",
                        des(
                                paragraph("Những bước chuyển mình xuất khẩu gốm mỹ nghệ sang thị trường Nhật Bản và châu Âu của nghệ nhân Bát Tràng."),
                                image("assets/images/home/home-new-1.png", "Sản phẩm gốm sứ Việt được trưng bày tại triển lãm quốc tế."),
                                paragraph("Nhiều lô hàng gốm mỹ nghệ Bát Tràng đã được xuất khẩu thành công sang các thị trường khó tính như Nhật Bản, châu Âu."),
                                quote("Gốm sứ Việt không chỉ là sản phẩm thủ công mà còn là sứ giả văn hóa mang hình ảnh đất nước ra thế giới.")),
                        "hanh-trinh-dua-gom-viet-vuon-tam-the-gioi", 0, "2026-06-15 14:30:00", "lang-nghe", categoriesBySlug),
                news("Lễ hội đình làng Bát Tràng - nét đẹp văn hóa tâm linh", "assets/images/home/home-new-2.png",
                        "Tìm hiểu các hoạt động rước nước, dâng hương tôn vinh các vị tổ nghề gốm truyền thống hàng năm.",
                        des(
                                paragraph("Tìm hiểu các hoạt động rước nước, dâng hương tôn vinh các vị tổ nghề gốm truyền thống hàng năm."),
                                image("assets/images/home/home-new-2.png", "Lễ rước nước truyền thống trong ngày hội đình làng Bát Tràng."),
                                paragraph("Hàng năm, người dân Bát Tràng tổ chức lễ hội để tưởng nhớ công ơn các vị tổ nghề đã khai sinh ra nghề gốm truyền thống."),
                                quote("Lễ hội đình làng không chỉ là dịp tri ân tổ nghề mà còn là sợi dây gắn kết cộng đồng làng nghề qua bao thế hệ.")),
                        "le-hoi-dinh-lang-bat-trang", 0, "2026-06-22 09:00:00", "lang-nghe", categoriesBySlug),
                news("Đất sét trắng - bầu sữa mẹ nuôi dưỡng làng nghề gốm sứ", "assets/images/home/home-new-1.png",
                        "Cách tuyển chọn và xử lý nguồn đất sét dẻo mịn tạo nên cốt gốm đanh chắc đặc trưng Bát Tràng.",
                        des(
                                paragraph("Cách tuyển chọn và xử lý nguồn đất sét dẻo mịn tạo nên cốt gốm đanh chắc đặc trưng Bát Tràng."),
                                image("assets/images/home/home-new-1.png", "Nguồn đất sét trắng quý giá được tuyển chọn kỹ lưỡng."),
                                paragraph("Chất lượng đất sét quyết định trực tiếp đến độ bền và vẻ đẹp của sản phẩm gốm sau khi nung."),
                                listSection(
                                        item("1. Tuyển chọn đất:", "Đất sét được sàng lọc kỹ để loại bỏ tạp chất, sạn cát."),
                                        item("2. Xử lý đất:", "Ngâm ủ đất trong thời gian dài để đạt độ dẻo mịn tối ưu trước khi tạo hình."))),
                        "dat-set-trang-bau-sua-me-nuoi-duong", 0, "2026-07-01 08:40:00", "lang-nghe", categoriesBySlug),
                // Bài chi tiết demo (NewsDetailView.jsx) — có nội dung đầy đủ
                news("Giải đáp ý nghĩa bát hương rồng 4 móng và rồng 5 móng?", "assets/images/home/home-new-2.png",
                        "Phân biệt ý nghĩa hình tượng rồng 4 móng và rồng 5 móng trên bát hương thờ cúng theo văn hóa tâm linh Việt.",
                        des(
                                paragraph("Nhắc đến gốm sứ người ta sẽ nghĩ ngay đến làng gốm hàng nghìn năm Bát Tràng. Làng nghề truyền thống Bát Tràng chính là cái nôi sinh ra các nghệ nhân làm gốm cũng như các sản phẩm gốm đều được thổi hồn mang một nét đẹp riêng biệt của truyền thống."),
                                paragraph("Qua bàn tay tài hoa và cái tâm của nghệ nhân làm gốm mà trở nên có hồn. Gốm Bát Tràng có rất nhiều các mẫu gốm phục vụ mọi nhu cầu của người tiêu dùng, từ gốm sứ gia dụng, đồ thờ cho đến vật phẩm phong thủy."),
                                image("assets/images/home/home-new-2.png", "Hình 1: Nghệ nhân tại xưởng sản xuất gốm tâm linh Vũ Gia đang thảo luận thiết kế độc quyền."),
                                heading("Phân biệt Rồng 4 móng và Rồng 5 móng trên Bát Hương thờ cúng"),
                                paragraph("Trong văn hóa tâm linh Việt Nam, hình tượng Rồng chầu mặt nguyệt luôn mang ý nghĩa tối thượng về quyền lực, tài lộc và sự bình an."),
                                listSection(
                                        item("1. Rồng 5 móng (Ngũ Trảo Kim Long):", "Rồng 5 móng xưa nay vốn là biểu tượng của Hoàng đế, đại diện cho thiên tử và quyền lực hoàng gia tuyệt đối. Trên các đồ thờ cúng, họa tiết rồng 5 móng thường được đắp nổi tinh xảo, thể hiện sự trang nghiêm tối cao."),
                                        item("2. Rồng 4 móng (Mãng Long):", "Rồng 4 móng thường được dùng cho các quan lại, hoàng thân quốc thích hoặc đền chùa dân gian, mang tính chất gần gũi hơn với đời sống tâm linh của nhân dân.")),
                                imageGrid(
                                        gridImage("assets/images/home/home-new-2.png", "Quy trình tạo hình gốm vuốt tay thủ công từ phôi đất sét Bát Tràng."),
                                        gridImage("assets/images/home/home-new-1.png", "Nghệ nhân vẽ nhũ vàng và họa màu rồng đắp nổi trực tiếp trên cốt gốm.")),
                                quote("Mỗi họa tiết rồng ngậm ngọc trên bát hương thờ cúng Vũ Gia đều được các nghệ nhân dồn toàn bộ tâm huyết, vẽ tay tỉ mỉ và nung ở nhiệt độ chuẩn 1300 độ C để tạo ra nước men bóng bẩy, bền bỉ ngàn năm."),
                                paragraph("Để chọn được bộ đồ thờ chuẩn phong thủy, quý khách nên chú ý đến kích thước bát hương hài hòa với không gian ban thờ, cũng như màu men tương sinh với bản mệnh gia chủ.")),
                        "giai-dap-y-nghia-bat-huong-rong-4-mong-va-5-mong", 20, "2026-05-18 09:00:00", "san-pham", categoriesBySlug));
    }

    private NewsEntity news(String title, String thumb, String shortContent, String desJson, String slug,
            int priority, String publishedAt, String categorySlug, Map<String, NewsCategoryEntity> categoriesBySlug) {
        return NewsEntity.builder()
                .title(title)
                .thumb(thumb)
                .shortContent(shortContent)
                .des(desJson)
                .slug(slug)
                .priority(priority)
                .status(ContentStatus.PUBLISHED)
                .publishedAt(toInstant(publishedAt))
                .newsCategory(categoriesBySlug.get(categorySlug))
                .seoTitle(title)
                .seoDescription(shortContent)
                .seoImage(thumb)
                .build();
    }

    private Instant toInstant(String localDateTime) {
        return LocalDateTime.parse(localDateTime, SEED_DATETIME).atZone(VN_ZONE).toInstant();
    }

    private String des(ObjectNode... blocks) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode array = root.putArray("blocks");
        for (ObjectNode block : blocks) {
            array.add(block);
        }
        return writeJson(root);
    }

    private ObjectNode block(String type) {
        return objectMapper.createObjectNode().put("type", type);
    }

    private ObjectNode paragraph(String text) {
        return block("paragraph").put("text", text);
    }

    private ObjectNode heading(String text) {
        return block("heading").put("text", text);
    }

    private ObjectNode quote(String text) {
        return block("quote").put("text", text);
    }

    private ObjectNode image(String url, String caption) {
        return block("image").put("url", url).put("caption", caption);
    }

    private ObjectNode imageGrid(ObjectNode... images) {
        ObjectNode node = block("image-grid");
        ArrayNode array = node.putArray("images");
        for (ObjectNode img : images) {
            array.add(img);
        }
        return node;
    }

    private ObjectNode gridImage(String url, String caption) {
        return objectMapper.createObjectNode().put("url", url).put("caption", caption);
    }

    private ObjectNode listSection(ObjectNode... items) {
        ObjectNode node = block("list-section");
        ArrayNode array = node.putArray("items");
        for (ObjectNode i : items) {
            array.add(i);
        }
        return node;
    }

    private ObjectNode item(String label, String text) {
        return objectMapper.createObjectNode().put("label", label).put("text", text);
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize seed JSON", e);
        }
    }
}
