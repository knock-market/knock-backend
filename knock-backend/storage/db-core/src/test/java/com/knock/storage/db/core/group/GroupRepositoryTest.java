package com.knock.storage.db.core.group;

import com.knock.storage.db.core.config.QueryDslConfig;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("그룹 저장 및 조회 성공")
    void saveAndFindGroup() {
        // given
        Member owner = Member.create("owner@test.com", "Owner", "Password", "Nickname", "LOCAL");
        memberRepository.save(owner);
        Group group = Group.create("Test Group", "Description", owner);

        // when
        Group savedGroup = groupRepository.save(group);
        Optional<Group> foundGroup = groupRepository.findById(savedGroup.getId());

        // then
        assertThat(foundGroup).isPresent();
        assertThat(foundGroup.get().getName()).isEqualTo("Test Group");
        assertThat(foundGroup.get().getOwner().getId()).isEqualTo(owner.getId());
    }
}
