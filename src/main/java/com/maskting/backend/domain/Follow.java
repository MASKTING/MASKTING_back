package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User following;

    @ManyToOne(fetch = FetchType.LAZY)
    private User follower;

    public void updateUser(User sender, User receiver) {
        sender.getFollowing().add(this);
        receiver.getFollower().add(this);
    }
}
