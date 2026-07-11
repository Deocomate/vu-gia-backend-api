package vn.springboot.service;

import vn.springboot.dto.request.contact.ContactRequestCreateRequest;
import vn.springboot.dto.request.contact.ContactRequestSearchRequest;
import vn.springboot.dto.request.contact.ContactRequestUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.contact.ContactRequestResponse;

public interface ContactRequestService {

    PageResponse<ContactRequestResponse> search(ContactRequestSearchRequest request);

    ContactRequestResponse getById(Long id);

    ContactRequestResponse create(ContactRequestCreateRequest request);

    ContactRequestResponse update(Long id, ContactRequestUpdateRequest request);

    void delete(Long id);
}
