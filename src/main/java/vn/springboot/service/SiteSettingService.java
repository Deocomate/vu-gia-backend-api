package vn.springboot.service;

import vn.springboot.dto.request.setting.SiteSettingUpdateRequest;
import vn.springboot.dto.response.setting.SiteSettingResponse;

public interface SiteSettingService {

    SiteSettingResponse getSettings();

    SiteSettingResponse updateSettings(SiteSettingUpdateRequest request);
}
