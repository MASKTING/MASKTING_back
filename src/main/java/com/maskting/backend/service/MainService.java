package com.maskting.backend.service;

import com.maskting.backend.common.exception.ExceedFeedLimitException;
import com.maskting.backend.common.exception.ExistLikeException;
import com.maskting.backend.common.exception.NoFeedException;
import com.maskting.backend.common.exception.NoNicknameException;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.dto.response.*;
import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.repository.*;
import com.maskting.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final S3Uploader s3Uploader;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatUserService chatUserService;
    private final ChatService chatService;
    private final FollowRepository followRepository;
    private final MatcherRepository matcherRepository;
    private final ExclusionRepository exclusionRepository;

    @Transactional
    public Feed addFeed(org.springframework.security.core.userdetails.User userDetail, FeedRequest feedRequest) throws IOException {
        User user = getUserByProviderId(userDetail);
        if (user.getFeeds().size() == 6)
            throw new ExceedFeedLimitException();
        if (feedRequest.getFeed().isEmpty())
            throw new NoFeedException();
        Feed feed = buildFeed(upload(feedRequest));
        feed.updateUser(user);
        return feedRepository.save(feed);
    }

    private User getUserByProviderId(org.springframework.security.core.userdetails.User userDetail) {
        return userRepository.findByProviderId(userDetail.getUsername());
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
    public List<User> matchPartner(org.springframework.security.core.userdetails.User userDetail) {
        User user = getUserByProviderId(userDetail);
        List<User> matches = new ArrayList<>();
        
        if (!user.isLatest()) {
            deleteExistingMatcher(user);
            addExclusions(user);
            List<User> partners = getPartners(user);
            List<PartnerInfo> partnerInfos = calculateScore(user, partners);

            for (int i = 0; i < partnerInfos.size() && i < 2; i++) {
                matches.add(partners.get(getIndex(partnerInfos, i)));
            }
            updateUserMatching(user, matches);
            return matches;
        }
        matches.addAll(getMatchers(user));
        return matches;
    }

    private void deleteExistingMatcher(User user) {
        List<Matcher> existingMatchers = user.getActiveMatcher();
        for (Matcher matcher : existingMatchers) {
            matcherRepository.delete(matcher);
        }
    }

    private List<User> getMatchers(User user) {
        return user.getActiveMatcher().stream().map(Matcher::getPassiveMatcher).collect(Collectors.toList());
    }

    private void addExclusions(User user) {
        List<Matcher> matchers = user.getActiveMatcher();
        if (!matchers.isEmpty()) {
            updateExclusions(matchers);
        }
    }

    private void updateExclusions(List<Matcher> matchers) {
        for (Matcher matcher : matchers) {
            Exclusion exclusion = buildExclusion(matcher);
            exclusion.updateExclusions();
            exclusionRepository.save(exclusion);
        }
    }

    private Exclusion buildExclusion(Matcher matcher) {
        return Exclusion.builder()
                .activeExclusioner(matcher.getActiveMatcher())
                .passiveExclusioner(matcher.getPassiveMatcher())
                .build();
    }

    private void updateUserMatching(User user, List<User> partners) {
        List<Matcher> matchers = getPartners(user, partners);
        for (Matcher matcher : matchers) {
            matcher.updateMatchers();
            matcherRepository.save(matcher);
        }
        user.updateLatest();
    }

    private List<Matcher> getPartners(User user, List<User> partners) {
        List<Matcher> matchers = new ArrayList<>();
        for (User partner : partners) {
            matchers.add(buildMatch(user, partner));
        }
        return matchers;
    }

    private Matcher buildMatch(User user, User partner) {
        return Matcher.builder()
                .activeMatcher(user)
                .passiveMatcher((User) Hibernate.unproxy(partner))
                .build();
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
                        , user.getActiveExclusioner().stream()
                                .map(Exclusion::getPassiveExclusioner)
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
        return new PartnerResponse(partner.getNickname(), getProfile(partner), partner.getBio(),
                getFeeds(partner));
    }

    private String getProfile(User partner) {
        return partner.getProfiles().get(ProfileType.MASK_PROFILE.getValue()).getPath();
    }

    private List<String> getFeeds(User partner) {
        return partner.getFeeds().stream()
                .map(Feed::getPath)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendLike(org.springframework.security.core.userdetails.User userDetail, String nickname) {
        User sender = getUserByProviderId(userDetail);
        User receiver = userRepository.findByNickname(nickname).orElseThrow(NoNicknameException::new);

        if (existLike(sender, receiver)) {
            throw new ExistLikeException();
        }
        processLike(sender, receiver);
        if (isChatable(sender, receiver)) {
            openChat(sender, receiver);
            deleteFollow(sender, receiver);
            deleteFollow(receiver, sender);
            addExclusion(sender, receiver);
            addExclusion(receiver, sender);
        }

    }

    private void deleteFollow(User sender, User receiver) {
        followRepository.delete(followRepository.findByFollowingAndFollower(sender.getId(), receiver.getId()).get());
    }

    private void addExclusion(User sender, User receiver) {
        Exclusion exclusion = buildExclusion(sender, receiver);
        exclusion.updateExclusions();
        exclusionRepository.save(exclusion);
    }

    private Exclusion buildExclusion(User sender, User receiver) {
        return Exclusion.builder()
                .activeExclusioner(sender)
                .passiveExclusioner(receiver)
                .build();
    }

    private void processLike(User sender, User receiver) {
        Follow follow = buildLike(sender, receiver);
        follow.updateUser(sender, receiver);
        followRepository.save(follow);
        addExclusion(sender, receiver);
    }

    private Follow buildLike(User sender, User receiver) {
        return Follow.builder()
                .following(sender)
                .follower(receiver)
                .build();
    }

    private void openChat(User sender, User receiver) {
        ChatRoom chatRoom = chatRoomService.createRoom();
        chatRoom.addUser(chatUserService.createChatUser(sender, chatRoom), chatUserService.createChatUser(receiver, chatRoom));
        ChatMessageRequest message = new ChatMessageRequest(chatRoom.getId(), "System", "앞으로 72시간 대화를 나누며 서로를 알아갈 수 있어요.");
        chatRoom.updateResult(ChatRoomResult.STILL);
        chatService.saveChatMessage(message);
    }

    private boolean existLike(User sender, User receiver) {
        return followRepository.findByFollowingAndFollower(sender.getId(), receiver.getId()).isPresent();
    }

    private boolean isChatable(User sender, User receiver) {
        return followRepository.findByFollowingAndFollower(receiver.getId(), sender.getId()).isPresent();
    }

    public UserResponse getUser(org.springframework.security.core.userdetails.User userDetail) {
        User user = getUserByProviderId(userDetail);
        return new UserResponse(getProfile(user), user.getNickname());
    }

    public FeedResponse getFeed(org.springframework.security.core.userdetails.User userDetail) {
        User user = getUserByProviderId(userDetail);
        return new FeedResponse(user.getBio(), getFeeds(user));
    }
}
