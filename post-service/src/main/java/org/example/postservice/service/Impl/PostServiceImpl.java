package org.example.postservice.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.postservice.config.WebClientConfig;
import org.example.postservice.dto.request.CreateAmenityListRequest;
import org.example.postservice.dto.request.CreateAmenityRequest;
import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.response.AmenityResponse;
import org.example.postservice.dto.response.PostResponse;
import org.example.postservice.enums.StatusEnums;
import org.example.postservice.model.Post;
import org.example.postservice.model.PostDetail;
import org.example.postservice.repository.PostDetailRepository;
import org.example.postservice.repository.PostRepository;
import org.example.postservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostDetailRepository postDetailRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @Override
    public Mono<String> getUserIdFromToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> jwt.getSubject())
                .switchIfEmpty(Mono.error(new RuntimeException("No user ID found in token")))
                .onErrorMap(e -> new RuntimeException("Failed to get userId: " + e.getMessage()));
    }

    @Override
    public Mono<PostResponse> createPost(PostRequest request) {
        log.info("Received PostRequest: {}", request);
        return getUserIdFromToken().flatMap(userId -> {
            // 1. Lưu PostDetail
            PostDetail postDetail = PostDetail.builder()
                    .price(request.getPostDetailRequest().getPrice())
                    .area(request.getPostDetailRequest().getArea())
                    .length(request.getPostDetailRequest().getLength())
                    .horizontal(request.getPostDetailRequest().getHorizontal())
                    .bedRoom(request.getPostDetailRequest().getBedRoom())
                    .bathRoom(request.getPostDetailRequest().getBathRoom())
                    .floor(request.getPostDetailRequest().getFloor())
                    .legalPapers(request.getPostDetailRequest().isLegalPapers())
                    .build();
            postDetailRepository.save(postDetail);

            // 2. Gửi request tạo amenity
            List<CreateAmenityRequest> amenities = request.getPostDetailRequest().getAmenities();
            Mono<List<AmenityResponse>> amenityMono = Mono.just(List.of());

            if (amenities != null && !amenities.isEmpty()) {
                CreateAmenityListRequest amenityListRequest = CreateAmenityListRequest.builder()
                        .postDetailId(postDetail.getId())
                        .amenities(amenities)
                        .build();

                amenityMono = webClientBuilder.build()
                        .post()
                        .uri("http://amenity-service/api/v1/amenities")
                        .bodyValue(amenityListRequest)
                        .retrieve() // gửi request và lấy response
                        .bodyToFlux(AmenityResponse.class).collectList();
            }

            // 3. Tạo Post
            Post post = Post.builder()
                    .postDetailId(postDetail.getId())
                    .userId(UUID.fromString(userId))
                    .imageId(request.getImageId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .district(request.getDistrict())
                    .ward(request.getWard())
                    .postType(request.getPostType())
                    .status(StatusEnums.EMPTY)
                    .createAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            postRepository.save(post);

            // 4. Gộp lại thành PostResponse
            return amenityMono.map(amenityList -> PostResponse.toDto(post, postDetail, amenityList));
        });
    }


}