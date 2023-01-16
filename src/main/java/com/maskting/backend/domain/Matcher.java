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
public class Matcher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User activeMatcher;

    @ManyToOne(fetch = FetchType.LAZY)
    private User passiveMatcher;

    public void updateMatchers() {
        activeMatcher.getActiveMatcher().add(this);
        passiveMatcher.getPassiveMatcher().add(this);
    }
}
