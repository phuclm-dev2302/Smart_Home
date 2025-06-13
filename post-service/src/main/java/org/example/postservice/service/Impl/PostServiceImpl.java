package org.example.postservice.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commonevent.common.event.CreatePostDocumentEvent;
import org.example.commonevent.common.event.CreatePostEvent;
import org.example.commonevent.common.event.DeleteAmenitiesEvent;
import org.example.postservice.dto.request.CreateAmenityListRequest;
import org.example.postservice.dto.request.CreateAmenityRequest;
import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.request.UpdatePostRequest;
import org.example.postservice.dto.response.AmenityResponse;
import org.example.postservice.dto.response.PostResponse;
import org.example.postservice.enums.StatusEnums;
import org.example.postservice.model.Post;
import org.example.postservice.model.PostDetail;
import org.example.postservice.repository.PostDetailRepository;
import org.example.postservice.repository.PostRepository;
import org.example.postservice.service.PostService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostDetailRepository postDetailRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, CreatePostEvent> kafkaTemplate;
    private final KafkaTemplate<String, CreatePostDocumentEvent> postDocumentKafkaTemplate;
    private final KafkaTemplate<String, DeleteAmenitiesEvent> deletePostEventKafkaTemplate;

    @Override
    public Mono<String> getUserIdFromToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> jwt.getSubject())
                .switchIfEmpty(Mono.error(new RuntimeException("No user ID found in token")))
                .onErrorMap(e -> new RuntimeException("Failed to get userId: " + e.getMessage()));
    }

    public Mono<String> getTokenFromSecurityContext() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    JwtAuthenticationToken authentication = (JwtAuthenticationToken) securityContext.getAuthentication();
                    return authentication.getToken().getTokenValue();
                });
    }

    @Override
    @Transactional
    public Mono<PostResponse> createPost(PostRequest request) {
        log.info("Received PostRequest: {}", request);

        return getUserIdFromToken().zipWith(getTokenFromSecurityContext())
                .flatMap(tuple -> {
                    String userId = tuple.getT1();
                    String token = tuple.getT2();

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
                                .headers(headers -> headers.setBearerAuth(token))
                                .bodyValue(amenityListRequest)
                                .retrieve()
                                .bodyToFlux(AmenityResponse.class)
                                .collectList();
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

                    // Gửi Kafka
                    kafkaTemplate.send("notificationTopic", new CreatePostEvent(post.getId(), post.getUserId()));
                    postDocumentKafkaTemplate.send("searchTopic", CreatePostDocumentEvent.builder()
                            .id(post.getId())
                            .token(token)
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .city(post.getCity())
                            .district(post.getDistrict())
                            .address(post.getAddress())
                            .ward(post.getWard())
                            .price(postDetail.getPrice().toString())
                            .area(postDetail.getArea().toString())
                            .bedRoom(Integer.toString(postDetail.getBedRoom()))
                            .bathRoom(Integer.toString(postDetail.getBathRoom()))
                            .floor(Integer.toString(postDetail.getFloor()))
                            .legalPapers(Boolean.toString(postDetail.isLegalPapers()))
                            .postType(post.getPostType().toString())
                            .status(post.getStatus().toString())
                            .createAt(post.getCreateAt().toString())
                            .amenities(amenities != null ? amenities.stream()
                                            .map(CreateAmenityRequest::getName)
                                            .collect(Collectors.toList()) : List.of())
                            .build());

                    log.info("Đã tạo topic");
                    // 4. Gộp lại thành PostResponse
                    return amenityMono.map(amenityList -> PostResponse.toDto(post, postDetail, amenityList));
                });
    }


    @Override
    public Mono<PostResponse> getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        PostDetail postDetail = postDetailRepository.findById(post.getPostDetailId())
                .orElseThrow(() -> new RuntimeException("PostDetail not found for postId: " + id));

        Mono<List<AmenityResponse>> amenityMono = webClientBuilder.build()
                .get()
                .uri("http://amenity-service/api/v1/amenities/post-detail/{id}", postDetail.getId())
                .retrieve()
                .bodyToFlux(AmenityResponse.class)
                .collectList()
                .onErrorReturn(List.of()); // fallback nếu gọi amenity-service lỗi

        return amenityMono.map(amenities -> PostResponse.toDto(post, postDetail, amenities));
    }

    @Override
    public Mono<List<PostResponse>> getAllPost(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Post> posts = postRepository.findAll(pageable).getContent();

        List<Mono<PostResponse>> responseMonos = posts.stream()
                .map(post -> {
                    PostDetail postDetail = postDetailRepository.findById(post.getPostDetailId())
                            .orElseThrow(() -> new RuntimeException("PostDetail not found for postId: " + post.getId()));

                    Mono<List<AmenityResponse>> amenityMono = webClientBuilder.build()
                            .get()
                            .uri("http://amenity-service/api/v1/amenities/post-detail/{id}", postDetail.getId())
                            .retrieve()
                            .bodyToFlux(AmenityResponse.class)
                            .collectList()
                            .onErrorReturn(List.of());

                    return amenityMono.map(amenities -> PostResponse.toDto(post, postDetail, amenities));
                })
                .toList();

        return Mono.zip(responseMonos, results ->
                Arrays.stream(results)
                        .map(obj -> (PostResponse) obj)
                        .toList()
        );
    }
    @Override
    @Transactional
    public Mono<PostResponse> updatePost(UUID id, UpdatePostRequest request){
        log.info("Update Post with id: ", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        PostDetail postDetail = postDetailRepository.findById(post.getPostDetailId())
                .orElseThrow(() -> new IllegalArgumentException("PostDetail not found"));

        postDetail.setPrice(request.getUpdatePostDetailRequest().getPrice());
        postDetail.setArea(request.getUpdatePostDetailRequest().getArea());
        postDetail.setLength(request.getUpdatePostDetailRequest().getLength());
        postDetail.setBathRoom(postDetail.getBathRoom());
        postDetail.setBedRoom(request.getUpdatePostDetailRequest().getBedRoom());
        postDetail.setBathRoom(request.getUpdatePostDetailRequest().getBathRoom());
        postDetail.setFloor(request.getUpdatePostDetailRequest().getFloor());
        postDetail.setLegalPapers(request.getUpdatePostDetailRequest().isLegalPapers());
        PostDetail savedPostDetail = postDetailRepository.save(postDetail);
        log.info("PostDetail has been saved");

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setAddress(request.getAddress());
        post.setCity(request.getCity());
        post.setDistrict(request.getDistrict());
        post.setWard(request.getWard());
        post.setPostType(request.getPostType());
        post.setStatus(request.getStatus());

        Post savedPost = postRepository.save(post);
        log.info("Post has been saved");

        return Mono.just(PostResponse.toDto(savedPost, savedPostDetail, List.of())); // dang de aminities rong
    }

    @Override
    @Transactional
    public void deletePost(UUID id) {
        String token = getTokenFromSecurityContext().toString();
        if (token == null){
            throw new RuntimeException("token not found");
        }

        Post post = postRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Post not found with ID:" +id));

        PostDetail postDetail = postDetailRepository.findById(post.getPostDetailId())
                        .orElseThrow(() -> new IllegalArgumentException("PostDetail not found with ID:" +post.getPostDetailId()));

        Mono<List<UUID>> idsMono = webClientBuilder.build()
                .get()
                .uri("http://amenity-service/api/v1/amenities/post-detail/{id}", postDetail.getId())
                .retrieve()
                .bodyToFlux(AmenityResponse.class)
                .map(AmenityResponse::getId)
                .collectList();

        idsMono.flatMap(ids -> {
                    deletePostEventKafkaTemplate.send("delete-amenities-topic", new DeleteAmenitiesEvent(ids));
                    log.debug("delete-amenities-topic send successfully !");

                    return Mono.fromRunnable(() -> {
                                postRepository.delete(post);
                                postDetailRepository.delete(postDetail);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .subscribe();   // khởi động chuỗi (vì phương thức trả về void)

    }



}