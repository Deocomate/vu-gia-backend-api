package vn.springboot.service;

import vn.springboot.dto.request.showroom.ShowroomCreateRequest;
import vn.springboot.dto.request.showroom.ShowroomSearchRequest;
import vn.springboot.dto.request.showroom.ShowroomUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.showroom.ShowroomResponse;

public interface ShowroomService {

    PageResponse<ShowroomResponse> search(ShowroomSearchRequest request);

    ShowroomResponse getById(Long id);

    ShowroomResponse create(ShowroomCreateRequest request);

    ShowroomResponse update(Long id, ShowroomUpdateRequest request);

    void delete(Long id);
}
