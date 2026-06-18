package com.knock.core.enums;

public enum ReportReason {

	PROHIBITED_ITEM("금지 품목"), SUSPECTED_FRAUD("사기 의심"), OFF_PLATFORM_PAYMENT("외부 결제 유도"),
	PERSONAL_INFO_OR_CODE_REQUEST("개인정보 또는 인증코드 요구"), HARASSMENT_OR_THREAT("괴롭힘 또는 위협"),
	NO_SHOW("노쇼"), COUNTERFEIT_OR_STOLEN_SUSPECTED("위조품 또는 도난품 의심"), OTHER("기타");

	private final String description;

	ReportReason(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
