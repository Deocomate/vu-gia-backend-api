package vn.springboot.service;

import vn.springboot.dto.request.newsletter.NewsletterSubscribeRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberSearchRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;

public interface NewsletterSubscriberService {

    PageResponse<NewsletterSubscriberResponse> search(NewsletterSubscriberSearchRequest request);

    NewsletterSubscriberResponse getById(Long id);

    NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request);

    NewsletterSubscriberResponse update(Long id, NewsletterSubscriberUpdateRequest request);

    void delete(Long id);
}
