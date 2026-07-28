package vn.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.response.altar.AltarCustomizerItemResponse;
import vn.springboot.service.AltarCustomizerService;

import java.util.List;

/** Public storefront feed for the altar customizer canvas. See {@code /api/altar-customizer/**}
 * in {@code SecurityConfig.PUBLIC_GET_ENDPOINTS}. */
@RestController
@RequestMapping("/api/altar-customizer")
@RequiredArgsConstructor
public class AltarCustomizerController {

    private final AltarCustomizerService altarCustomizerService;

    @GetMapping("/items")
    public ApiResponse<List<AltarCustomizerItemResponse>> items(
            @RequestParam(required = false) Long altarItemGroupId,
            @RequestParam(required = false) Long altarStyleId) {
        return ApiResponse.success(altarCustomizerService.getItems(altarItemGroupId, altarStyleId));
    }
}
