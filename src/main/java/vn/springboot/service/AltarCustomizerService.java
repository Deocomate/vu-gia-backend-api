package vn.springboot.service;

import vn.springboot.dto.response.altar.AltarCustomizerItemResponse;

import java.util.List;

/** Public feed backing the storefront altar customizer canvas (Phase 4). */
public interface AltarCustomizerService {

    /**
     * Published {@code BO_DO_THO} products with their first image's placement joined inline,
     * optionally narrowed by altar item group and/or glaze style.
     */
    List<AltarCustomizerItemResponse> getItems(Long altarItemGroupId, Long altarStyleId);
}
