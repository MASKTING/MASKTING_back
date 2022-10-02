package com.maskting.backend.service;

import com.maskting.backend.common.exception.ExceedFeedLimitException;
import com.maskting.backend.common.exception.NoFeedException;
import com.maskting.backend.domain.Feed;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.repository.FeedRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final S3Uploader s3Uploader;
    private final FeedRepository feedRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional
    public Feed addFeed(HttpServletRequest request, FeedRequest feedRequest) throws IOException {
        User user = getUserByProviderId(request);
        if (user.getFeeds().size() == 6)
            throw new ExceedFeedLimitException();
        if (feedRequest.getFeed().isEmpty())
            throw new NoFeedException();
        Feed feed = buildFeed(upload(feedRequest));
        feed.updateUser(user);
        return feedRepository.save(feed);
    }

    private User getUserByProviderId(HttpServletRequest request) {
        return userRepository.findByProviderId(getProviderId(request));
    }

    private String getProviderId(HttpServletRequest request) {
        return jwtUtil.getSubject(getAccessToken(request));
    }

    private String getAccessToken(HttpServletRequest request) {
        return jwtUtil.resolveToken(request);
    }

    private S3Response upload(FeedRequest feedRequest) throws IOException {
        return s3Uploader.upload(feedRequest.getFeed(), "feed");
    }

    private Feed buildFeed(S3Response uploadFile) {
        return Feed.builder()
                .name(uploadFile.getName())
                .path(uploadFile.getPath())
                .build();
    }

    @Transactional
    @Scheduled(cron = "0 0 12 * * *")
    public void updateAllUser() {
        userRepository.updateAllUserLatest(false);
    }
}
