package vn.springboot.service;

import vn.springboot.dto.request.gallery.GalleryImageCreateRequest;
import vn.springboot.dto.request.gallery.GalleryImageSearchRequest;
import vn.springboot.dto.request.gallery.GalleryImageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.gallery.GalleryImageResponse;

public interface GalleryImageService {

    PageResponse<GalleryImageResponse> search(GalleryImageSearchRequest request);

    GalleryImageResponse getById(Long id);

    GalleryImageResponse create(GalleryImageCreateRequest request);

    GalleryImageResponse update(Long id, GalleryImageUpdateRequest request);

    void delete(Long id);
}
