package vn.springboot.seed;

import java.util.List;

/**
 * The 28-row real altar catalog (decision D6-D11), transcribed from {@code plan.md}'s canonical
 * catalog table (catalog-table order == {@code priority} order 100-127): 25 ceramic products
 * built from the {@code tasks/seeder-alter-customize} photo set, each carrying an altar item
 * group and altar style, plus 3 non-ceramic accessories carrying an altar item group only —
 * glaze style doesn't apply to consumables (ash, incense-bowl core material, herb bundle). That
 * is the one legitimate {@code altarStyle = null} case among the altar set, asserted explicitly
 * in the seed null audit ({@code SeedRunnerIntegrationTest#altarSeedDataHasNoUnexpectedNulls}).
 *
 * <p>Split out of {@link ProductSeeder} once that class passed ~600 lines, per the repo's
 * modularization rule — {@link ProductSeeder} keeps the persistence logic, this class is pure
 * data plus the one derived-path helper.
 */
final class AltarProductCatalog {

    private AltarProductCatalog() {
    }

    /**
     * One altar-catalog row: product copy + how many Phase-1-built assets it owns.
     * {@code borrowedImagePath} is only set for the 3 non-ceramic accessories, which own no
     * Phase 1 asset directory of their own and instead borrow one existing ceramic product's
     * {@code 01.png} as both thumb and sole gallery image (decision D3).
     */
    record AltarSeedItem(
            String name, String slug, String sku, long price, int soldCount, boolean featured,
            String description, int priority, String groupSlug, String styleSlug, int imageCount,
            String borrowedImagePath) {

        /** Bare relative path of the n-th (1-based) asset; index 1 is the altar overlay. */
        String imagePath(int n) {
            if (borrowedImagePath != null) {
                return borrowedImagePath;
            }
            return "assets/images/altar-customizer/products/%s/%02d.png".formatted(slug, n);
        }

        /** Product thumb/seoImage: the overlay for ceramic products, the borrowed asset for accessories. */
        String thumbPath() {
            return imagePath(1);
        }
    }

    static final List<AltarSeedItem> ALTAR_ITEMS = List.of(
            new AltarSeedItem("Bát hương men lam vẽ rồng H20", "bat-huong-men-lam-ve-rong-h20", "VG-ALT001",
                    850_000L, 52, true,
                    "Bát hương men lam cao 20cm, vẽ tay họa tiết rồng chầu và biểu tượng âm dương quanh thân, miệng loe tròn truyền thống, dùng cắm hương thờ Thần linh - Gia tiên.",
                    100, "bat-huong", "men-lam", 5, null),
            new AltarSeedItem("Lọ hoa men lam H35", "lo-hoa-men-lam-h35", "VG-ALT002",
                    1_200_000L, 18, true,
                    "Lọ hoa men lam cao 35cm, cổ loe rộng, thân thon vẽ rồng phượng và hoa sen cách điệu, phù hợp cắm hoa tươi hoặc hoa lụa trên ban thờ lớn.",
                    101, "lo-hoa", "men-lam", 1, null),
            new AltarSeedItem("Lọ hoa men lam H30", "lo-hoa-men-lam-h30", "VG-ALT003",
                    950_000L, 37, false,
                    "Lọ hoa men lam cao 30cm dáng cổ điển, có 16 mẫu họa tiết rồng phượng khác nhau để lựa chọn, thích hợp làm đôi lọ hoa hai bên ban thờ.",
                    102, "lo-hoa", "men-lam", 16, null),
            new AltarSeedItem("Lọ hoa men lam H20", "lo-hoa-men-lam-h20", "VG-ALT004",
                    480_000L, 64, false,
                    "Lọ hoa men lam cỡ nhỏ cao 20cm, cổ loe, thân bầu vẽ rồng mây, phù hợp ban thờ Thần tài - Thổ địa hoặc không gian thờ cúng nhỏ gọn.",
                    103, "lo-hoa", "men-lam", 3, null),
            new AltarSeedItem("Chóe thờ men lam H19", "choe-tho-men-lam-h19", "VG-ALT005",
                    780_000L, 29, false,
                    "Chóe thờ men lam cao 19cm, thân tròn có nắp đậy hình búp sen, vẽ rồng chầu và song hỷ, dùng đựng gạo, muối hoặc nước trên ban thờ.",
                    104, "choe-tho", "men-lam", 15, null),
            new AltarSeedItem("Chóe thờ men lam H14", "choe-tho-men-lam-h14", "VG-ALT006",
                    520_000L, 41, false,
                    "Chóe thờ men lam cỡ nhỏ cao 14cm, dáng lọ có nắp thu gọn, vẽ rồng mây, dùng đựng gạo hoặc muối trong bộ tam sự.",
                    105, "choe-tho", "men-lam", 2, null),
            new AltarSeedItem("Bát sâm men lam vẽ rồng phượng", "bat-sam-men-lam-ve-rong-phuong", "VG-ALT007",
                    750_000L, 22, false,
                    "Bát sâm có nắp và đĩa lót, vẽ rồng phượng chầu vòng âm dương, miệng loe hình chuông, dùng dâng nước, trà hoặc sâm lên ban thờ.",
                    106, "bat-tho-bat-sam", "men-lam", 2, null),
            new AltarSeedItem("Bát sâm men lam có nắp", "bat-sam-men-lam-co-nap", "VG-ALT008",
                    690_000L, 26, false,
                    "Bát sâm men lam có nắp đậy và đĩa lót đi kèm, họa tiết rồng phượng giản lược hơn bản vẽ rồng phượng, dùng dâng nước hoặc trà cúng.",
                    107, "bat-tho-bat-sam", "men-lam", 2, null),
            new AltarSeedItem("Bát thờ men lam có nắp", "bat-tho-men-lam-co-nap", "VG-ALT009",
                    380_000L, 58, false,
                    "Bát thờ men lam có nắp, đi kèm đĩa lót, miệng loe hình chuông vẽ rồng phượng và hoa sen, dùng dâng cơm trắng và lễ vật lên ban thờ.",
                    108, "bat-tho-bat-sam", "men-lam", 6, null),
            new AltarSeedItem("Bát thờ men lam nhỏ", "bat-tho-men-lam-nho", "VG-ALT010",
                    290_000L, 71, false,
                    "Bát thờ men lam cỡ nhỏ có nắp và đĩa lót, cùng kiểu dáng với bát thờ có nắp nhưng thu gọn kích thước, phù hợp ban thờ diện tích hạn chế.",
                    109, "bat-tho-bat-sam", "men-lam", 2, null),
            new AltarSeedItem("Nậm rượu men lam H25", "nam-ruou-men-lam-h25", "VG-ALT011",
                    520_000L, 33, false,
                    "Nậm rượu men lam dáng hồ lô cao 25cm, cổ thắt eo, vẽ rồng phượng chầu vòng âm dương, dùng đựng và dâng rượu cúng.",
                    110, "nam-ruou-ky-chen", "men-lam", 2, null),
            new AltarSeedItem("Nậm rượu men lam H20", "nam-ruou-men-lam-h20", "VG-ALT012",
                    390_000L, 47, false,
                    "Nậm rượu men lam dáng hồ lô cỡ nhỏ cao 20cm, cùng họa tiết rồng phượng với bản H25 nhưng thu gọn, phù hợp ban thờ nhỏ.",
                    111, "nam-ruou-ky-chen", "men-lam", 1, null),
            new AltarSeedItem("Kỷ 5 chén men lam đế rồng", "ky-5-chen-men-lam-de-rong", "VG-ALT013",
                    650_000L, 39, true,
                    "Kỷ 5 chén men lam gắn liền trên đế chạm đầu rồng, bố cục 5 chén đối xứng, dùng đựng nước sạch hoặc rượu cúng trang nghiêm.",
                    112, "nam-ruou-ky-chen", "men-lam", 2, null),
            new AltarSeedItem("Kỷ 3 chén men lam đế rồng", "ky-3-chen-men-lam-de-rong", "VG-ALT014",
                    450_000L, 44, false,
                    "Kỷ 3 chén men lam gắn liền trên đế chạm đầu rồng, kích thước gọn hơn kỷ 5 chén, dùng đựng nước hoặc rượu cúng cho ban thờ nhỏ.",
                    113, "nam-ruou-ky-chen", "men-lam", 2, null),
            new AltarSeedItem("Chén thờ men lam (bộ 3)", "chen-tho-men-lam-bo-3", "VG-ALT015",
                    220_000L, 68, false,
                    "Bộ 3 chén thờ men lam rời, vẽ rồng chầu vòng âm dương, dùng đựng nước sạch hoặc rượu cúng, sắp xếp linh hoạt trên ban thờ.",
                    114, "nam-ruou-ky-chen", "men-lam", 3, null),
            new AltarSeedItem("Bộ nậm rượu & kỷ chén men lam", "bo-nam-ruou-ky-chen-men-lam", "VG-ALT016",
                    890_000L, 25, false,
                    "Bộ phối gồm nậm rượu dáng hồ lô và kỷ chén trên đế rồng, đồng bộ họa tiết men lam, dùng đựng và dâng rượu cúng theo nghi thức truyền thống.",
                    115, "nam-ruou-ky-chen", "men-lam", 5, null),
            new AltarSeedItem("Đèn dầu thờ men lam H28", "den-dau-tho-men-lam-h28", "VG-ALT017",
                    420_000L, 31, false,
                    "Đèn dầu thờ men lam cao 28cm, thân gốm vẽ rồng phượng đỡ bầu đèn thủy tinh và núm vặn bấc bằng đồng, dùng thắp sáng tạo sự trang nghiêm cho ban thờ.",
                    116, "den-tho-chan-nen", "men-lam", 6, null),
            new AltarSeedItem("Đèn dầu thờ men lam vẽ phượng", "den-dau-tho-men-lam-ve-phuong", "VG-ALT018",
                    450_000L, 24, false,
                    "Đèn dầu thờ men lam nhấn họa tiết phượng múa nổi bật trên thân gốm, đi kèm bầu đèn thủy tinh và núm đồng, dùng thắp sáng ban thờ.",
                    117, "den-tho-chan-nen", "men-lam", 2, null),
            new AltarSeedItem("Đèn dầu thờ men lam (đôi)", "den-dau-tho-men-lam-doi", "VG-ALT019",
                    760_000L, 19, false,
                    "Cặp đèn dầu thờ men lam kích thước bằng nhau, thân vẽ rồng phượng đối xứng, dùng thắp sáng hai bên ban thờ theo cặp truyền thống.",
                    118, "den-tho-chan-nen", "men-lam", 2, null),
            new AltarSeedItem("Ống hương men lam H31", "ong-huong-men-lam-h31", "VG-ALT020",
                    620_000L, 27, false,
                    "Ống hương men lam cao 31cm, thân trụ vẽ rồng phượng chầu vòng âm dương, dùng cắm nhang chưa sử dụng, giữ ban thờ gọn gàng.",
                    119, "ong-huong-mam-bong", "men-lam", 8, null),
            new AltarSeedItem("Ống hương men lam H25", "ong-huong-men-lam-h25", "VG-ALT021",
                    450_000L, 35, false,
                    "Ống hương men lam cỡ nhỏ cao 25cm, cùng họa tiết rồng phượng với bản H31 nhưng thu gọn, phù hợp ban thờ diện tích nhỏ.",
                    120, "ong-huong-mam-bong", "men-lam", 2, null),
            new AltarSeedItem("Đĩa thờ men lam Ø20", "dia-tho-men-lam-d20", "VG-ALT022",
                    260_000L, 53, false,
                    "Đĩa thờ men lam đường kính 20cm, lòng đĩa và vành đĩa vẽ hoa sen dây leo, dùng bày hoa quả hoặc lễ vật trên ban thờ.",
                    121, "ong-huong-mam-bong", "men-lam", 6, null),
            new AltarSeedItem("Bộ nậm rượu & kỷ chén men lam vẽ vàng", "bo-nam-ruou-ky-chen-men-lam-ve-vang", "VG-ALT023",
                    1_650_000L, 9, true,
                    "Bộ phối nậm rượu dáng hồ lô và kỷ chén trên đế rồng, viền họa tiết vẽ tay bằng vàng 24k trên nền men lam, dùng đựng và dâng rượu cúng cho ban thờ cao cấp.",
                    122, "nam-ruou-ky-chen", "men-lam-ve-vang", 5, null),
            new AltarSeedItem("Nậm rượu men lam vẽ vàng H28", "nam-ruou-men-lam-ve-vang-h28", "VG-ALT024",
                    980_000L, 14, false,
                    "Nậm rượu dáng hồ lô cao 28cm, họa tiết rồng phượng viền vàng 24k nổi bật trên nền men lam, dùng đựng và dâng rượu cúng.",
                    123, "nam-ruou-ky-chen", "men-lam-ve-vang", 4, null),
            new AltarSeedItem("Đôi hạc thờ men lam vẽ vàng", "doi-hac-tho-men-lam-ve-vang", "VG-ALT025",
                    2_400_000L, 6, true,
                    "Cặp hạc thờ đứng trên lưng rùa, men lam điểm xuyết họa tiết vẽ vàng 24k ở mỏ và cánh, biểu trưng cho sự trường thọ, đặt hai bên ban thờ.",
                    124, "den-tho-chan-nen", "men-lam-ve-vang", 1, null),
            new AltarSeedItem("Tro nếp", "tro-nep", "VG-ALT026",
                    120_000L, 82, false,
                    "Tro nếp sạch dùng đổ bát hương, đã qua xử lý, không gây ẩm mốc, an toàn khi thắp hương.",
                    125, "phu-kien-di-kem", null, 1,
                    "assets/images/altar-customizer/products/choe-tho-men-lam-h14/01.png"),
            new AltarSeedItem("Cốt bát hương", "cot-bat-huong", "VG-ALT027",
                    150_000L, 63, false,
                    "Cốt bát hương (thất bảo) dùng lót đáy bát hương trước khi đổ tro, theo phong tục truyền thống.",
                    126, "phu-kien-di-kem", null, 1,
                    "assets/images/altar-customizer/products/bat-tho-men-lam-nho/01.png"),
            new AltarSeedItem("Bộ thất thảo", "bo-that-thao", "VG-ALT028",
                    250_000L, 40, false,
                    "Bộ thất thảo (7 vị thảo dược) dùng lót cốt bát hương, theo nghi thức an vị bát hương truyền thống.",
                    127, "phu-kien-di-kem", null, 1,
                    "assets/images/altar-customizer/products/dia-tho-men-lam-d20/01.png"));
}
