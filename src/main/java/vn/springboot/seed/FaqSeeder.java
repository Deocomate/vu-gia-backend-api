package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.faq.FaqEntity;
import vn.springboot.repository.FaqRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "FAQS" section (19 rows across 5 categories, no FK). */
@Component
@RequiredArgsConstructor
public class FaqSeeder implements DomainSeeder {

    private final FaqRepository faqRepository;

    @Override
    public boolean isEmpty() {
        return faqRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        faqRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        faqRepository.saveAll(List.of(
                // Sản phẩm
                faq("Gốm sứ xây dựng Vũ Gia có phải là hàng thủ công không?",
                        "Đúng vậy. Chúng tôi tự hào duy trì quy trình sản xuất thủ công truyền thống. Từ khâu chọn đất, tạo hình, đến tráng men và nung lò. Mỗi sản phẩm đều mang dấu ấn bàn tay khéo léo của các nghệ nhân. Điều này tạo nên vẻ đẹp độc bản mà các loại gạch ngói công nghiệp sản xuất hàng loạt không thể có được.",
                        "Sản phẩm", 1),
                faq("Tôi có thể mua hàng như thế nào?",
                        "Bạn có thể mua hàng trực tiếp tại showroom, qua Hotline hoặc các kênh mạng xã hội của chúng tôi.",
                        "Sản phẩm", 2),
                faq("Tôi có thể lấy mẫu thử không?",
                        "Chúng tôi sẵn sàng gửi mẫu thử cho khách hàng ở xa. Vui lòng liên hệ để được hỗ trợ.",
                        "Sản phẩm", 3),
                faq("Các sản phẩm của gốm sứ Vũ Gia có bền khi sử dụng ngoài trời hay không?",
                        "Tất cả sản phẩm của chúng tôi đều được nung ở nhiệt độ cao (1200°C), đảm bảo độ bền tuyệt đối khi sử dụng ngoài trời. Chất liệu đất sét Bát Tràng kết hợp men hỏa biến giúp sản phẩm chống thấm nước, chống rêu mốc vĩnh viễn.",
                        "Sản phẩm", 4),
                faq("Màu men có bị phai dưới ánh nắng mặt trời không?",
                        "Lớp men gốm được nung hỏa biến ở nhiệt độ 1200°C, cam kết không bao giờ phai màu dưới tác động của thời tiết. Màu men hòa quyện vào xương gốm trong quá trình nung, tạo nên độ bền màu vĩnh cửu.",
                        "Sản phẩm", 5),
                // Báo giá
                faq("Giá sản phẩm được tính như thế nào?",
                        "Giá gốm sứ xây dựng thường được tính theo mét vuông (m²), mét dài (md) hoặc theo viên/cặp đối với các dòng gạch, ngói và tính theo đơn vị đôi/chiếc đối với các sản phẩm đơn lẻ. Giá phụ thuộc vào kích thước, loại men, và độ phức tạp của hình dáng sản phẩm.",
                        "Báo giá", 6),
                faq("Đặt hàng số lượng lớn có được chiết khấu không?",
                        "Chúng tôi luôn có chính sách chiết khấu linh hoạt và cạnh tranh cho các đơn hàng số lượng lớn, đặc biệt là các dự án công trình trọng điểm. Vui lòng liên hệ trực tiếp để nhận báo giá ưu đãi nhất.",
                        "Báo giá", 7),
                faq("Có yêu cầu số lượng đặt hàng tối thiểu không?",
                        "Chúng tôi tiếp nhận mọi đơn hàng, từ một sản phẩm đơn lẻ đến các đơn hàng lớn cho công trình quy mô hàng nghìn mét vuông.",
                        "Báo giá", 8),
                faq("Màu sắc có ảnh hưởng đến giá sản phẩm không?",
                        "Một số màu men hỏa biến đặc biệt hoặc yêu cầu pha chế màu riêng theo thiết kế có thể có sự chênh lệch nhẹ về giá so với các màu men tiêu chuẩn.",
                        "Báo giá", 9),
                faq("Tại sao các kích thước nhỏ lại đắt hơn nhiều so với các kích thước lớn?",
                        "Kích thước nhỏ đòi hỏi sự tỉ mỉ cao hơn trong khâu tạo hình và hoàn thiện thủ công. Công sức cho mỗi cm² sản phẩm nhỏ lớn hơn đáng kể, đồng thời tỷ lệ hao hụt trong quá trình nung cũng cao hơn.",
                        "Báo giá", 10),
                // Vận chuyển & thời gian giao hàng
                faq("Thời gian sản xuất và giao hàng là bao lâu?",
                        "Đối với hàng có sẵn: Chúng tôi có thể giao hàng trong vòng 2-5 ngày làm việc.<br/>Đối với hàng đặt sản xuất: Thường mất từ 3-6 tuần tùy vào quy mô đơn hàng và điều kiện thời tiết (ảnh hưởng đến quá trình phơi gốm mộc).",
                        "Vận chuyển & thời gian giao hàng", 11),
                faq("Các bạn có giao hàng toàn quốc không?",
                        "Chúng tôi vận chuyển toàn quốc bằng xe tải chuyên dụng hoặc đối tác logistic uy tín. Hàng hóa được đóng gói cẩn thận, đảm bảo an toàn trong suốt quá trình vận chuyển.",
                        "Vận chuyển & thời gian giao hàng", 12),
                faq("Tôi nên lưu ý gì khi lắp đặt gốm thủ công?",
                        "Nên sử dụng thợ có tay nghề và am hiểu đặc tính gốm nung thủ công. Chúng tôi luôn cung cấp tài liệu hướng dẫn lắp đặt chi tiết kèm theo mỗi đơn hàng.",
                        "Vận chuyển & thời gian giao hàng", 13),
                faq("Các bạn có vận chuyển quốc tế không?",
                        "Có. Chúng tôi hỗ trợ đóng gói kiện gỗ xuất khẩu đạt chuẩn và làm thủ tục hải quan cần thiết cho các đơn hàng quốc tế.",
                        "Vận chuyển & thời gian giao hàng", 14),
                faq("Tôi có thể tự đến lấy hàng trực tiếp không?",
                        "Quý khách có thể nhận hàng trực tiếp tại xưởng sản xuất hoặc showroom của chúng tôi. Vui lòng liên hệ trước để chúng tôi chuẩn bị hàng sẵn sàng.",
                        "Vận chuyển & thời gian giao hàng", 15),
                // Chính sách bảo hành
                faq("Gốm sứ xây dựng Vũ Gia có chính sách bảo hành như thế nào?",
                        "Chúng tôi bảo hành độ bền màu men trọn đời đối với tất cả các dòng sản phẩm gốm sứ xây dựng và trang trí. Đối với độ bền xương gốm, cam kết bảo hành 10 năm trong điều kiện thời tiết tự nhiên thông thường.",
                        "Chính sách bảo hành", 16),
                faq("Làm thế nào để yêu cầu xử lý bảo hành?",
                        "Quý khách chỉ cần liên hệ Hotline chăm sóc khách hàng, cung cấp số điện thoại đặt hàng hoặc mã hóa đơn. Đội ngũ kỹ thuật của Vũ Gia sẽ phản hồi và tiến hành xác minh thực tế trong vòng 48h.",
                        "Chính sách bảo hành", 17),
                // Đổi trả
                faq("Chính sách đổi trả sản phẩm như thế nào?",
                        "Khách hàng được quyền đổi trả sản phẩm trong vòng 7 ngày kể từ khi nhận hàng đối với các trường hợp: sản phẩm bị nứt vỡ do lỗi vận chuyển, lỗi tráng men nghiêm trọng hoặc giao sai mẫu mã so với hợp đồng đã ký kết.",
                        "Đổi trả", 18),
                faq("Đơn hàng đặt riêng (sản xuất theo yêu cầu) có được đổi trả không?",
                        "Đối với các đơn hàng đặt riêng theo yêu cầu thiết kế đặc biệt của khách hàng, chúng tôi chỉ áp dụng chính sách đổi trả/thay thế đối với sản phẩm bị lỗi kỹ thuật trong khâu sản xuất hoặc nứt vỡ do vận chuyển.",
                        "Đổi trả", 19)));
    }

    private FaqEntity faq(String question, String answer, String category, int sortOrder) {
        return FaqEntity.builder()
                .question(question)
                .answer(answer)
                .category(category)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
    }
}
