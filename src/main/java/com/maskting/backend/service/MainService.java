package com.maskting.backend.service;

import com.maskting.backend.common.exception.ExceedFeedLimitException;
import com.maskting.backend.common.exception.ExistLikeException;
import com.maskting.backend.common.exception.NoFeedException;
import com.maskting.backend.common.exception.NoNicknameException;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.response.PartnerInfo;
import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.response.PartnerResponse;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public List<User> matchPartner(HttpServletRequest request) {
        User user = getUserByProviderId(request);
        List<User> matches = new ArrayList<>();
        
        if (!user.isLatest()) {
            addExclusions(user);
            List<User> partners = getPartners(user);
            List<PartnerInfo> partnerInfos = calculateScore(user, partners);

            for (int i = 0; i < partnerInfos.size() && i < 2; i++) {
                matches.add(partners.get(getIndex(partnerInfos, i)));
            }
            updateUserMatching(user, matches);
            return matches; 
        }
        matches.addAll(user.getMatches());
        return matches;
    }

    private void addExclusions(User user) {
        if (!user.getMatches().isEmpty()) {
            user.updateExclusions(user.getMatches());
        }
    }

    private void updateUserMatching(User user, List<User> matches) {
        user.updateMatches(matches);
        user.updateLatest();
    }

    private List<PartnerInfo> calculateScore(User user, List<User> partners) {
        List<PartnerInfo> result = new ArrayList<>();
        HashMap<String, Integer> check = new HashMap<>();
        initCheck(user, check);

        for (int i = 0; i < partners.size(); i++) {
            double score = 0;
            User partner = partners.get(i);

            score += getAgeScore(user, partner) * 5;
            score += getInterestScore(check, partner) * 10;
            score *= getDrinkingScore(user, partner);
            score *= getHeightScore(user, partner);
            if (notEqualSmoking(user, partner))
                score *= 0.3;
            if (notEqualDuty(user, partner))
                score *= 0.5;
            if (notEqualReligion(user, partner))
                score *= 0.5;
            if (notEqualBodyType(user, partner))
                score *= 0.8;
            result.add(new PartnerInfo(i, score));
        }
        sortResult(result);
        return result;
    }

    private int getIndex(List<PartnerInfo> result, int i) {
        return result.get(i).getIndex();
    }

    private void sortResult(List<PartnerInfo> result) {
        Collections.sort(result, new Comparator<PartnerInfo>() {
            @Override
            public int compare(PartnerInfo o1, PartnerInfo o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });
    }

    private double getHeightScore(User user, User partner) {
        double partnerMaxHeight = user.getPartner().getPartnerMaxHeight();
        double partnerMinHeight = user.getPartner().getPartnerMinHeight();
        return 1 - ((Math.abs(partner.getHeight() - (partnerMaxHeight + partnerMinHeight) / 2)
                - (partnerMaxHeight - partnerMinHeight) / 2)) / 10;
    }

    private boolean notEqualBodyType(User user, User partner) {
        return !user.getPartnerBodyTypes().stream()
                .map(PartnerBodyType::getVal)
                .collect(Collectors.toList())
                .contains(partner.getBodyType());
    }

    private boolean notEqualReligion(User user, User partner) {
        return !user.getPartnerReligions()
                .stream()
                .map(PartnerReligion::getName)
                .collect(Collectors.toList())
                .contains(partner.getName());
    }

    private int getInterestScore(HashMap<String, Integer> check, User partner) {
        int cnt = 0;
        for (int j = 0; j < partner.getInterests().size(); j++) {
            if (equalInterest(check, partner.getInterests().get(j).getName()))
                cnt++;
        }
        return cnt;
    }

    private double getDrinkingScore(User user, User partner) {
        return 1 - Math.abs((user.getDrinking() - 1) * 0.25 - (partner.getDrinking() - 1) * 0.25);
    }

    private boolean notEqualDuty(User user, User partner) {
        return user.getGender().equals("female")
                && user.getPartner().getPartnerDuty().equals("true") && !partner.isDuty();
    }

    private boolean notEqualSmoking(User user, User partner) {
        return user.getPartner().getPartnerSmoking().equals("false") && partner.isSmoking();
    }

    private boolean equalInterest(HashMap<String, Integer> check, String interest) {
        return check.containsKey(interest);
    }

    private int getAgeScore(User user, User partner) {
        return (10 - Math.abs(Integer.parseInt(user.getBirth().substring(0, 4))
                - Integer.parseInt(partner.getBirth().substring(0, 4)) - 2));
    }

    private List<User> getPartners(User user) {
        return userRepository
                .findByLocationsAndGender(
                        user.getPartnerLocations()
                            .stream()
                            .map(PartnerLocation::getName)
                            .collect(Collectors.toList())
                        , user.getGender()
                        , user.getExclusions()
                                .stream()
                                .map(User::getId)
                                .collect(Collectors.toList())
                );
    }

    private void initCheck(User user, HashMap<String, Integer> check) {
        for (int i = 0; i < user.getInterests().size(); i++) {
            check.put(user.getInterests().get(i).getName(), 1);
        }
    }

    public List<PartnerResponse> getPartnerResponse(List<User> partners) {
        List<PartnerResponse> partnerResponses = new ArrayList<>();
        for (User partner : partners) {
            partnerResponses.add(getPartnerResponse(partner));
        }

        return partnerResponses;
    }

    private PartnerResponse getPartnerResponse(User partner) {
        return new PartnerResponse(getProfile(partner), partner.getBio(),
                getFeeds(partner));
    }

    private String getProfile(User partner) {
        return partner.getProfiles().get(0).getPath();
    }

    private List<String> getFeeds(User partner) {
        return partner.getFeeds().stream()
                .map(Feed::getPath)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendLike(HttpServletRequest request, String nickname) {
        User sender = getUserByProviderId(request);
        User receiver = userRepository.findByNickname(nickname).orElseThrow(NoNicknameException::new);

        if (existLike(sender, receiver))
            throw new ExistLikeException();
        sender.addLike(receiver);

        //TODO 채팅방 개설
        if (isChatable(sender, receiver))
            System.out.println("Chat Open");
    }

    private boolean existLike(User sender, User receiver) {
        return sender.getLikes().contains(receiver);
    }

    private boolean isChatable(User sender, User receiver) {
        return receiver.getLikes().contains(sender);
    }
}
