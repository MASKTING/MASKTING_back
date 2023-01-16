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
public class Exclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User activeExclusioner;

    @ManyToOne(fetch = FetchType.LAZY)
    private User passiveExclusioner;

    public void updateExclusions() {
        activeExclusioner.getActiveExclusioner().add(this);
        passiveExclusioner.getPassiveExclusioner().add(this);
    }
}
