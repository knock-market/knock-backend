package com.knock.core.domain.review;

import com.knock.core.domain.review.enums.ReputationPolicy;
import com.knock.storage.db.core.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReputationService {

	@Transactional
	public void changeReputation(Member seller, int score) {
		double change = ReputationPolicy.calculateChange(score);
		double currentScore = seller.getMannerTemperature();

		double newScore = Math.max(0, Math.min(100, currentScore + change));
		seller.updateMannerTemperature(newScore);
	}

}
