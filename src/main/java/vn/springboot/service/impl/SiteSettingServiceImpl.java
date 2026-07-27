package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.setting.SiteSettingUpdateRequest;
import vn.springboot.dto.response.setting.SiteSettingResponse;
import vn.springboot.entity.setting.SiteSettingEntity;
import vn.springboot.repository.SiteSettingRepository;
import vn.springboot.service.SiteSettingService;

/**
 * Operates on the singleton settings row (id=1), guaranteed to exist by
 * {@link vn.springboot.seed.SiteSettingSeeder} before any traffic is served — so
 * {@code findById(1L).orElseThrow(...)} here is a defensive check, not a fallback
 * create path (that lazy-create approach would race under concurrent first requests).
 */
@Service
@RequiredArgsConstructor
public class SiteSettingServiceImpl implements SiteSettingService {

    private static final Long SETTINGS_ID = 1L;

    private final SiteSettingRepository siteSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public SiteSettingResponse getSettings() {
        return toResponse(findOrThrow());
    }

    @Override
    @Transactional
    public SiteSettingResponse updateSettings(SiteSettingUpdateRequest request) {
        SiteSettingEntity entity = findOrThrow();
        entity.setCartEnabled(request.getCartEnabled());
        return toResponse(siteSettingRepository.save(entity));
    }

    private SiteSettingEntity findOrThrow() {
        return siteSettingRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private SiteSettingResponse toResponse(SiteSettingEntity entity) {
        return SiteSettingResponse.builder()
                .cartEnabled(entity.isCartEnabled())
                .build();
    }
}
