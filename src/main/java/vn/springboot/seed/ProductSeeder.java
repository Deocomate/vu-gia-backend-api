package vn.springboot.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.repository.AltarItemGroupRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ported 1:1 from {@code V2__seed_db.sql} "PRODUCTS" (12 rows) and "PRODUCT IMAGES"
 * (15 rows) sections — handled together since {@code product_images} needs the
 * generated {@code product_id}s from this seeder's own inserts.
 *
 * <p>Highest-risk seeder in the framework: seed ids 9/10/11 are the combo's real
 * sub-products, and seed id 12 (the {@code COMBO}) references them by id in its
 * {@code combo_products} JSON. Those literal ids only work by coincidence on a fresh
 * DB with untouched auto-increment; here they're resolved from the actually-saved
 * entities instead. Same for {@code product_category_id}: the source SQL's literal
 * category ids (1-6) are translated below to their real {@link CategoryType}, then
 * resolved to whatever id {@link ProductCategorySeeder} actually generated.
 *
 * <p><b>Real altar-catalog extension.</b> {@code buildProducts()}'s original 12 rows
 * (indexes 0-11, including the positionally-resolved combo above) are left completely
 * untouched — the 28 real altar-catalog products from {@code buildAltarSetProducts()}
 * (25 ceramic products built on the {@code tasks/seeder-alter-customize} photo set, plus 3
 * non-ceramic accessories) are strictly <em>appended</em> after index 11 rather than inserted
 * in the middle. This is the deliberate, lower-risk choice over refactoring the combo's
 * {@code saved.get(8..11)} lookups to a slug-keyed map: appending changes zero bytes of the
 * already-tested index-8/9/10/11 resolution logic, so it carries no risk of silently shifting
 * those indices. The altar item group/style resolution, and the altar catalog itself, use a
 * slug-keyed {@link AltarSeedItem} list instead, since with 28 rows a hand-counted
 * {@code saved.get(n)} index per row is exactly the fragility this javadoc warns about for the
 * combo. {@code priority} 100-127 keeps the altar rows visually distinct from the original 12's
 * 0-10 when reading {@code ORDER BY p.priority ASC} output. Every ceramic product's first image
 * (its {@code 01.png}) is saved at {@code priority = 0} — {@code AltarCustomizerServiceImpl}
 * reduces each product to its first image by {@code priority ASC, id ASC} and only looks up a
 * placement for that exact image id, so a reordering here would silently break every placement.
 */
@Component
@RequiredArgsConstructor
public class ProductSeeder implements DomainSeeder {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AltarItemGroupRepository altarItemGroupRepository;
    private final AltarStyleRepository altarStyleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isEmpty() {
        return productRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        productImageRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        Map<CategoryType, ProductCategoryEntity> categories = new EnumMap<>(CategoryType.class);
        for (ProductCategoryEntity category : productCategoryRepository.findAll()) {
            categories.put(category.getCategoryType(), category);
        }
        Map<String, AltarItemGroupEntity> altarItemGroups = altarItemGroupRepository.findAll().stream()
                .collect(Collectors.toMap(AltarItemGroupEntity::getSlug, Function.identity()));
        Map<String, AltarStyleEntity> altarStyles = altarStyleRepository.findAll().stream()
                .collect(Collectors.toMap(AltarStyleEntity::getSlug, Function.identity()));

        List<ProductEntity> saved = new ArrayList<>();
        for (ProductEntity draft : buildProducts()) {
            saved.add(productRepository.save(resolveRefs(draft, categories, altarItemGroups, altarStyles)));
        }

        // Combo #12 (index 11) references sub-products #9/#10/#11 (indexes 8-10) by their
        // real generated ids — never the literal seed ids 9/10/11.
        ProductEntity combo = saved.get(11);
        combo.setComboProducts(comboProducts(
                saved.get(8).getId(), saved.get(9).getId(), saved.get(10).getId()));
        productRepository.save(combo);

        Map<String, ProductEntity> altarBySlug = saved.subList(12, saved.size()).stream()
                .collect(Collectors.toMap(ProductEntity::getSlug, Function.identity()));

        List<ProductImageEntity> images = new ArrayList<>(buildOriginalImages(saved));
        images.addAll(buildAltarImages(altarBySlug));
        productImageRepository.saveAll(images);
    }

    /**
     * Resolves the category placeholder every draft carries, plus the altar item
     * group/style placeholders the 28 appended altar-set drafts (only) carry — see
     * {@link #altarItemGroupPlaceholder(String)}/{@link #altarStylePlaceholder(String)}.
     * Drafts from the original 12 never set an altar placeholder, so
     * {@code draft.getAltarItemGroup()}/{@code getAltarStyle()} stay {@code null} for them,
     * exactly as before this phase.
     */
    private ProductEntity resolveRefs(ProductEntity draft, Map<CategoryType, ProductCategoryEntity> categories,
            Map<String, AltarItemGroupEntity> altarItemGroups, Map<String, AltarStyleEntity> altarStyles) {
        draft.setProductCategory(categories.get(draft.getProductCategory().getCategoryType()));
        if (draft.getAltarItemGroup() != null) {
            draft.setAltarItemGroup(altarItemGroups.get(draft.getAltarItemGroup().getSlug()));
        }
        if (draft.getAltarStyle() != null) {
            draft.setAltarStyle(altarStyles.get(draft.getAltarStyle().getSlug()));
        }
        return draft;
    }

    /** Category holder is a detached placeholder carrying only the {@link CategoryType} to resolve against. */
    private ProductCategoryEntity categoryPlaceholder(CategoryType type) {
        return ProductCategoryEntity.builder().categoryType(type).build();
    }

    /** Detached placeholder carrying only the slug {@link #resolveRefs} resolves against. */
    private AltarItemGroupEntity altarItemGroupPlaceholder(String slug) {
        return AltarItemGroupEntity.builder().slug(slug).build();
    }

    /** Detached placeholder carrying only the slug {@link #resolveRefs} resolves against. */
    private AltarStyleEntity altarStylePlaceholder(String slug) {
        return AltarStyleEntity.builder().slug(slug).build();
    }

    /**
     * Indexes 0-11: the original 12 rows, byte-for-byte unchanged (see the class javadoc on
     * why the altar-set rows are appended rather than inserted). Indexes 12-39:
     * {@link #buildAltarSetProducts()} — the 28 real altar-catalog rows (25 ceramic products
     * carrying an altar item group + altar style, plus 3 non-ceramic accessories carrying an
     * altar item group only).
     */
    private List<ProductEntity> buildProducts() {
        List<ProductEntity> products = new ArrayList<>(originalProducts());
        products.addAll(buildAltarSetProducts());
        return products;
    }

    private List<ProductEntity> originalProducts() {
        return List.of(
                product("Bát hương Men rạn Đắp nổi rồng chầu", "assets/images/products/product-image-thumb.png",
                        "VG-DT001", 1_200_000L, 1_500_000L, 24, true,
                        "Bát hương men rạn đắp nổi rồng chầu, chế tác thủ công tại làng nghề Bát Tràng, men rạn cổ trang nghiêm.",
                        "bat-huong-men-ran-dap-noi-rong-chau", 8, CategoryType.BINH_PHONG_THUY),
                product("Bình hoa cúc cổ Men lam vẽ vàng", "assets/images/products/product-new-thumb.png",
                        "VG-DT002", 850_000L, 1_000_000L, 15, false,
                        "Bình hoa cúc cổ men lam vẽ vàng 24k, hoa văn tinh xảo, tôn lên vẻ sang trọng cho không gian thờ.",
                        "binh-hoa-cuc-co-men-lam-ve-vang", 7, CategoryType.LUC_BINH_GOM_SU),
                product("Kỷ 5 chén thờ Men lam vẽ vàng", "assets/images/products/product-image-thumb.png",
                        "VG-DT003", 650_000L, 800_000L, 42, true,
                        "Kỷ 5 chén thờ men lam vẽ vàng dùng đựng nước sạch hoặc rượu cúng trên bàn thờ gia tiên.",
                        "ky-5-chen-tho-men-lam-ve-vang", 6, CategoryType.LUC_BINH_GOM_SU),
                product("Mâm bồng đựng quả Men rạn đắp nổi", "assets/images/products/product-image-thumb.png",
                        "VG-DT004", 950_000L, null, 19, false,
                        "Mâm bồng men rạn đắp nổi dùng bày hoa quả và lễ vật, đường nét chạm khắc thủ công sắc sảo.",
                        "mam-bong-dung-qua-men-ran-dap-noi", 5, CategoryType.AM_CHEN_BAT_TRANG),
                product("Đèn dầu thờ Men lam vẽ vàng kim", "assets/images/products/product-new-thumb.png",
                        "VG-DT005", 450_000L, 550_000L, 31, false,
                        "Đèn dầu thờ men lam vẽ vàng kim, thắp sáng và tạo sự trang nghiêm cho bàn thờ.",
                        "den-dau-tho-men-lam-ve-vang-kim", 4, CategoryType.BO_DO_THO),
                product("Ống hương thờ Men lam cổ điển", "assets/images/products/product-image-thumb.png",
                        "VG-DT006", 380_000L, 450_000L, 8, false,
                        "Ống hương thờ men lam cổ điển dùng cắm nhang chưa sử dụng, dáng thanh nhã.",
                        "ong-huong-tho-men-lam-co-dien", 3, CategoryType.BO_DO_THO),
                product("Nậm rượu thờ Men lam vẽ rồng chầu", "assets/images/products/product-image-thumb.png",
                        "VG-DT007", 290_000L, 350_000L, 54, true,
                        "Nậm rượu thờ men lam vẽ rồng chầu dùng đựng và dâng rượu cúng, họa tiết rồng uyển chuyển.",
                        "nam-ruou-tho-men-lam-ve-rong-chau", 2, CategoryType.BO_DO_THO),
                product("Chóe thờ đựng nước Men lam vẽ sen cổ", "assets/images/products/product-image-thumb.png",
                        "VG-DT008", 320_000L, 400_000L, 27, false,
                        "Chóe thờ men lam vẽ sen cổ dùng đựng gạo, muối và nước, họa tiết hoa sen thanh khiết.",
                        "choe-tho-dung-nuoc-men-lam-ve-sen-co", 1, CategoryType.BO_DO_THO),
                product("Bát hương rồng phượng men lam", "assets/images/products/product-image-thumb.png",
                        "VG-DT026-01", 2_000_000L, 2_500_000L, 12, false,
                        "Bát hương rồng phượng men lam, đắp nổi vẽ tay, thuộc bộ đồ thờ DT026.",
                        "bat-huong-rong-phuong-men-lam", 0, CategoryType.BO_DO_THO),
                product("Bát hương Phúc lộc liên hoa vẽ vàng (mẫu 1)", "assets/images/products/product-new-thumb.png",
                        "VG-DT026-02", 2_000_000L, 2_500_000L, 12, false,
                        "Bát hương Phúc lộc liên hoa vẽ vàng, thuộc bộ đồ thờ DT026.",
                        "bat-huong-phuc-loc-lien-hoa-ve-vang-1", 0, CategoryType.LUC_BINH_GOM_SU),
                product("Bát hương Phúc lộc liên hoa vẽ vàng (mẫu 2)", "assets/images/products/product-image-thumb.png",
                        "VG-DT026-03", 2_000_000L, 2_500_000L, 12, false,
                        "Bát hương Phúc lộc liên hoa vẽ vàng, thuộc bộ đồ thờ DT026.",
                        "bat-huong-phuc-loc-lien-hoa-ve-vang-2", 0, CategoryType.LUC_BINH_GOM_SU),
                comboProduct());
    }

    private List<ProductEntity> buildAltarSetProducts() {
        List<ProductEntity> products = new ArrayList<>(AltarProductCatalog.ALTAR_ITEMS.size());
        for (AltarProductCatalog.AltarSeedItem item : AltarProductCatalog.ALTAR_ITEMS) {
            products.add(altarProduct(item));
        }
        return products;
    }

    /**
     * Builds one altar-set {@link ProductEntity} on top of {@link #product}, then attaches the
     * altar item group (always) and altar style ({@code styleSlug() == null} for the 3
     * accessories). {@code compareAtPrice} is left {@code null} for every altar-set row, same as
     * several rows in the original 12.
     */
    private ProductEntity altarProduct(AltarProductCatalog.AltarSeedItem item) {
        ProductEntity draft = product(item.name(), item.thumbPath(), item.sku(), item.price(), null,
                item.soldCount(), item.featured(), item.description(), item.slug(), item.priority(),
                CategoryType.BO_DO_THO);
        draft.setAltarItemGroup(altarItemGroupPlaceholder(item.groupSlug()));
        if (item.styleSlug() != null) {
            draft.setAltarStyle(altarStylePlaceholder(item.styleSlug()));
        }
        return draft;
    }

    private ProductEntity product(String name, String thumb, String sku, Long price, Long compareAtPrice,
            int soldCount, boolean isFeatured, String descriptionText, String slug, int priority,
            CategoryType categoryType) {
        return ProductEntity.builder()
                .name(name)
                .thumb(thumb)
                .sku(sku)
                .type(ProductType.SINGLE)
                .price(price)
                .compareAtPrice(compareAtPrice)
                .soldCount(soldCount)
                .isFeatured(isFeatured)
                .status(ProductStatus.PUBLISHED)
                .description(simpleDescription(descriptionText))
                .detailSections(singleDetailSections(name))
                .slug(slug)
                .priority(priority)
                .productCategory(categoryPlaceholder(categoryType))
                .seoTitle(name)
                .seoDescription(descriptionText)
                .seoImage(thumb)
                .build();
    }

    /**
     * {@code [{title, description, image}]} rendered by {@code ProductDetail} (type=SINGLE
     * only) — every seeded single previously left this {@code null}, so the storefront
     * silently fell back to its hardcoded {@code DEFAULT_SECTIONS}, the same 3 generic
     * blurbs on every product page regardless of which item it was. Glaze copy is picked
     * from the product name (men rạn vs men lam) so the 2nd section still matches the
     * real product instead of repeating one canned paragraph everywhere. No altar-catalog
     * product is men rạn, so every altar row naturally takes the men-lam branch — correct,
     * since the real photo catalog is exclusively men lam / men lam vẽ vàng.
     */
    private String singleDetailSections(String productName) {
        boolean isMenRan = productName.contains("Men rạn") || productName.contains("men rạn");
        String glazeTitle = isMenRan ? "Chất Men Rạn Cổ Kính" : "Chất Men Lam Truyền Thống";
        String glazeText = isMenRan
                ? "Men rạn được tạo nên nhờ sự chênh lệch độ co giãn giữa men và xương gốm khi nung, hình thành các đường rạn chân chim tự nhiên - không sản phẩm nào giống sản phẩm nào, mang vẻ đẹp hoài cổ, trầm mặc riêng có."
                : "Men lam sử dụng oxit coban vẽ họa tiết xanh lam trên nền men trắng, nét vẽ tinh tế, màu sắc trong trẻo. Sản phẩm được nung ở nhiệt độ trên 1.200 độ C giúp màu men bám chắc, bền đẹp theo thời gian.";

        ArrayNode sections = objectMapper.createArrayNode();
        sections.addObject()
                .put("title", "Nghệ Thuật Chế Tác Thủ Công")
                .put("description", productName + " được nghệ nhân làng gốm Bát Tràng tạo hình và chạm khắc hoàn toàn thủ công, từng đường nét hoa văn đều mang dấu ấn bàn tay tài hoa, không hai sản phẩm nào hoàn toàn giống nhau.")
                .put("image", "assets/images/product-detail/chi-tiet-sp-thumb-1.png");
        sections.addObject()
                .put("title", glazeTitle)
                .put("description", glazeText)
                .put("image", "assets/images/product-detail/chi-tiet-sp-thumb-2.png");
        sections.addObject()
                .put("title", "Giá Trị Phong Thủy & Tâm Linh")
                .put("description", "Mỗi họa tiết trên sản phẩm đều chứa đựng những ý nghĩa phong thủy tốt lành, giúp gia chủ tụ khí, đón tài lộc và thể hiện lòng thành kính hướng về nguồn cội gia tiên.")
                .put("image", "assets/images/product-detail/chi-tiet-sp-thumb-3.png");
        return writeJson(sections);
    }

    private ProductEntity comboProduct() {
        return ProductEntity.builder()
                .name("Bộ đồ thờ Phật vẽ hoa sen men rạn cổ đơn giản DT026")
                .thumb("assets/images/products/product-image-thumb.png")
                .sku("VG001")
                .type(ProductType.COMBO)
                .price(2_000_000L)
                .compareAtPrice(2_500_000L)
                .soldCount(12)
                .isFeatured(true)
                .status(ProductStatus.PUBLISHED)
                .description(comboDescription())
                .functions(comboFunctions())
                .comboGallery(comboGalleryImages())
                .slug("bo-do-tho-phat-ve-hoa-sen-men-ran-co-dt026")
                .priority(10)
                .productCategory(categoryPlaceholder(CategoryType.BINH_PHONG_THUY))
                .seoTitle("Bộ đồ thờ Phật vẽ hoa sen men rạn cổ DT026")
                .seoDescription("Bộ đồ thờ Phật men rạn cổ vẽ hoa sen Bát Tràng, chế tác thủ công, bảo hành men trọn đời.")
                .seoImage("assets/images/products/product-image-thumb.png")
                .build();
    }

    /**
     * {@code [{name, quantity, unit, usage}]} rendered by {@code ProductSpecifications}
     * (type=COMBO only). Same 12 line items as {@link #comboDescription()}'s embedded
     * "specifications" array (kept for backward display compatibility), minus the "stt"
     * key — the table component derives row numbers from array index instead.
     */
    private String comboFunctions() {
        ArrayNode rows = objectMapper.createArrayNode();
        addFunctionRow(rows, "Bát hương", "3", "Chiếc", "Dùng cắm hương, thờ Thần linh - Gia tiên");
        addFunctionRow(rows, "Bát thờ", "10", "Chiếc", "Dùng dâng cơm trắng và lễ vật");
        addFunctionRow(rows, "Chóe thờ", "3", "Chiếc", "Dùng đựng gạo, muối và nước");
        addFunctionRow(rows, "Bát sâm", "1 - 2", "Chiếc", "Dùng dâng nước, trà hoặc sâm");
        addFunctionRow(rows, "Bộ kỷ chén", "3 hoặc 5", "Chén", "Dùng đựng nước sạch hoặc rượu");
        addFunctionRow(rows, "Nậm rượu", "1 - 2", "Chiếc", "Dùng đựng và dâng rượu cúng");
        addFunctionRow(rows, "Bộ ấm chén thờ (1 ấm - 5 chén)", "1 - 2", "Bộ", "Dùng pha và dâng trà lên bàn thờ");
        addFunctionRow(rows, "Ống cắm hương", "1", "Chiếc", "Dùng cắm nhang chưa sử dụng");
        addFunctionRow(rows, "Mâm bồng", "3", "Chiếc", "Dùng bày hoa quả và lễ vật");
        addFunctionRow(rows, "Lọ hoa", "2", "Chiếc", "Dùng cắm hoa trang trí bàn thờ");
        addFunctionRow(rows, "Đèn thờ", "2", "Chiếc", "Dùng thắp sáng và tạo sự trang nghiêm");
        addFunctionRow(rows, "Chân nến", "2", "Chiếc", "Dùng thắp sáng và tạo sự trang nghiêm");
        return writeJson(rows);
    }

    private void addFunctionRow(ArrayNode rows, String name, String quantity, String unit, String usage) {
        rows.addObject().put("name", name).put("quantity", quantity).put("unit", unit).put("usage", usage);
    }

    /** {@code [{url}]} full-width slider rendered by {@code ProductSpecifications} (type=COMBO only). */
    private String comboGalleryImages() {
        ArrayNode images = objectMapper.createArrayNode();
        images.addObject().put("url", "assets/images/product-detail/product-detail-big-thumb.png");
        images.addObject().put("url", "assets/images/nha-xuong/slider-image-1.png");
        images.addObject().put("url", "assets/images/nha-xuong/slider-image-2.png");
        images.addObject().put("url", "assets/images/nha-xuong/slider-image-3.png");
        images.addObject().put("url", "assets/images/nha-xuong/slider-image-4.png");
        return writeJson(images);
    }

    private String simpleDescription(String text) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode blocks = root.putArray("blocks");
        blocks.addObject().put("type", "paragraph").put("text", text);
        return writeJson(root);
    }

    private String comboDescription() {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode blocks = root.putArray("blocks");
        blocks.addObject().put("type", "paragraph").put("text",
                "Bộ đồ thờ Phật vẽ hoa sen men rạn cổ Bát Tràng được chế tác thủ công tinh xảo, chất men rạn cổ kính trang nghiêm, thích hợp cho không gian thờ cúng gia đình.");
        blocks.addObject().put("type", "paragraph").put("text",
                "Đồ thờ cúng Bát Tràng vẽ sen men rạn cổ là sự kết hợp hài hòa giữa nét vẽ tay mộc mạc và sắc men rạn truyền thống độc bản. Họa tiết hoa sen được khắc họa sống động, uyển chuyển, biểu trưng cho sự thanh cao, thuần khiết và lòng tôn kính vô hạn hướng về nguồn cội gia tiên.");
        blocks.addObject().put("type", "paragraph").put("text",
                "Từng vật phẩm từ bát hương, bát thờ, mâm bồng đến chóe thờ đều trải qua quá trình nung luyện nhiệt độ cao nghiêm ngặt trên 1200 độ C. Nhờ đó, chất gốm trở nên đanh chắc, có độ bền cơ học cao và giữ màu sắc vĩnh cửu theo thời gian.");

        ObjectNode attributes = root.putObject("attributes");
        attributes.put("classification", "Men rạn");
        attributes.put("size", "120x120");
        attributes.put("material", "Gốm sứ");

        ArrayNode specifications = root.putArray("specifications");
        addSpec(specifications, 1, "Bát hương", "3", "Chiếc", "Dùng cắm hương, thờ Thần linh - Gia tiên");
        addSpec(specifications, 2, "Bát thờ", "10", "Chiếc", "Dùng dâng cơm trắng và lễ vật");
        addSpec(specifications, 3, "Chóe thờ", "3", "Chiếc", "Dùng đựng gạo, muối và nước");
        addSpec(specifications, 4, "Bát sâm", "1 - 2", "Chiếc", "Dùng dâng nước, trà hoặc sâm");
        addSpec(specifications, 5, "Bộ kỷ chén", "3 hoặc 5", "Chén", "Dùng đựng nước sạch hoặc rượu");
        addSpec(specifications, 6, "Nậm rượu", "1 - 2", "Chiếc", "Dùng đựng và dâng rượu cúng");
        addSpec(specifications, 7, "Bộ ấm chén thờ (1 ấm - 5 chén)", "1 - 2", "Bộ", "Dùng pha và dâng trà lên bàn thờ");
        addSpec(specifications, 8, "Ống cắm hương", "1", "Chiếc", "Dùng cắm nhang chưa sử dụng");
        addSpec(specifications, 9, "Mâm bồng", "3", "Chiếc", "Dùng bày hoa quả và lễ vật");
        addSpec(specifications, 10, "Lọ hoa", "2", "Chiếc", "Dùng cắm hoa trang trí bàn thờ");
        addSpec(specifications, 11, "Đèn thờ", "2", "Chiếc", "Dùng thắp sáng và tạo sự trang nghiêm");
        addSpec(specifications, 12, "Chân nến", "2", "Chiếc", "Dùng thắp sáng và tạo sự trang nghiêm");

        return writeJson(root);
    }

    private void addSpec(ArrayNode specifications, int stt, String name, String quantity, String unit, String usage) {
        specifications.addObject()
                .put("stt", stt)
                .put("name", name)
                .put("quantity", quantity)
                .put("unit", unit)
                .put("usage", usage);
    }

    /** {@code [{productId, sortOrder}]} — the real seed data never includes a "quantity" key here. */
    private String comboProducts(Long subItem1Id, Long subItem2Id, Long subItem3Id) {
        ArrayNode array = objectMapper.createArrayNode();
        array.addObject().put("productId", subItem1Id).put("sortOrder", 1);
        array.addObject().put("productId", subItem2Id).put("sortOrder", 2);
        array.addObject().put("productId", subItem3Id).put("sortOrder", 3);
        return writeJson(array);
    }

    /** Images for the original 12 rows (indexes 0-11) — untouched, still index-based. */
    private List<ProductImageEntity> buildOriginalImages(List<ProductEntity> saved) {
        List<ProductImageEntity> images = new ArrayList<>();
        String[] singleImageFiles = {
                "product-card-image-1.png", "product-card-image-2.png", "product-card-image-1.png",
                "product-card-image-1.png", "product-card-image-2.png", "product-card-image-1.png",
                "product-card-image-1.png", "product-card-image-1.png", "product-card-image-1.png",
                "product-card-image-2.png", "product-card-image-1.png"
        };
        for (int i = 0; i < singleImageFiles.length; i++) {
            images.add(image(saved.get(i), singleImageFiles[i], 0));
        }
        // Combo #12 (index 11): 4 gallery images.
        String[] comboImageFiles = {
                "product-card-image-1.png", "product-card-image-1.png",
                "product-card-image-2.png", "product-card-image-1.png"
        };
        for (int priority = 0; priority < comboImageFiles.length; priority++) {
            images.add(image(saved.get(11), comboImageFiles[priority], priority));
        }
        return images;
    }

    /**
     * Images for the 28 appended altar-set rows: walks {@link AltarProductCatalog#ALTAR_ITEMS}
     * — the same source of truth {@link #buildAltarSetProducts()} reads — so a product can
     * never drift out of sync with its images. Each item's {@code 01.png} lands at
     * {@code priority = 0} (decision D5); accessories emit exactly one row at their borrowed path.
     */
    private List<ProductImageEntity> buildAltarImages(Map<String, ProductEntity> altarBySlug) {
        List<ProductImageEntity> images = new ArrayList<>();
        for (AltarProductCatalog.AltarSeedItem item : AltarProductCatalog.ALTAR_ITEMS) {
            ProductEntity product = altarBySlug.get(item.slug());
            for (int n = 1; n <= item.imageCount(); n++) {
                images.add(imageAtPath(product, item.imagePath(n), n - 1));
            }
        }
        return images;
    }

    private ProductImageEntity image(ProductEntity product, String fileName, int priority) {
        return imageAtPath(product, "assets/images/product-detail/" + fileName, priority);
    }

    /** Same shape as {@link #image}, but takes the full bare-relative path (not just a filename
     * under {@code assets/images/product-detail/}) — needed for the altar-customizer assets,
     * which live under their own {@code assets/images/altar-customizer/} folder. */
    private ProductImageEntity imageAtPath(ProductEntity product, String fullPath, int priority) {
        return ProductImageEntity.builder()
                .url(fullPath)
                .priority(priority)
                .product(product)
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
