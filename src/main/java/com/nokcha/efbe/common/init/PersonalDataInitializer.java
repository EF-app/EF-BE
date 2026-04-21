package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.profile.entity.Personal;
import com.nokcha.efbe.domain.user.repository.PersonalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalDataInitializer {

    private final PersonalRepository personalRepository;

    @PostConstruct
    public void initialize() {
        registerCategory("음주", List.of(
                "아예 안 마심", "가끔 마심", "꽤 마심", "자주 마심", "금주 중"
        ));
        registerCategory("선호 주종", List.of(
                "소주", "맥주", "와인", "위스키/하이볼", "칵테일", "전통주", "가리지 않아요"
        ));
        registerCategory("흡연", List.of(
                "비흡연자", "아주 가끔 피움", "때때로 피움", "흡연자", "금연 중"
        ));
        registerCategory("흡연 종류", List.of(
                "액상 전자담배", "궐련형 전자담배", "연초", "해당없음"
        ));
        registerCategory("타투유무", List.of(
                "타투가 많이 있어요(아주 많아요)",
                "타투가 여러개 있어요",
                "작은 포인트 타투 하나/소수 있어요",
                "지금은 없지만 관심 있어요",
                "없어요"
        ));
        registerCategory("머리", List.of(
                "선택 안함", "숏컷", "단발~중단발", "긴머리"
        ));
        registerCategory("체형", List.of(
                "선택 안함", "슬림", "보통", "통통", "통통 이상"
        ));
        registerCategory("키", List.of(
                "선택 안함", "150이하", "151~155", "156~160", "160~165", "166~170", "171이상"
        ));
        registerCategory("성향", List.of(
                "선택 안함", "온깁", "깁선호", "텍선호", "온텍", "플라토닉"
        ));
    }

    // 대분류 기준 성향 데이터 등록
    private void registerCategory(String bigCategory, List<String> smallCategories) {
        for (String smallCategory : smallCategories) {
            if (personalRepository.findByBigCategoryAndSmallCategory(bigCategory, smallCategory).isPresent()) {
                continue;
            }

            personalRepository.save(Personal.builder()
                    .bigCategory(bigCategory)
                    .smallCategory(smallCategory)
                    .build());
        }
    }
}
