package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.profile.entity.CodePersonal;
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
        registerCategory("종교", List.of(
                "무교", "불교", "개신교", "천주교", "기타"
        ));
        registerCategory("이쪽 지인", List.of(
                "거의 없어요 (0~3명)", "손에 꼽을 정도예요", "꽤 있는 편이에요", "주변에 많아요"
        ));
        registerCategory("커밍아웃 정도", List.of(
                "완전 벽장", "가까운 친구 몇 명만", "친구들 대부분에게", "가족까지", "거의 모두 오픈", "완전 오픈"
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
        registerCategory("패션 스타일", List.of(
                "캐주얼", "스트릿", "미니멀", "댄디", "스포티", "빈티지", "기타"
        ));
        registerCategory("꾸미는 스타일", List.of(
                "꾸미는 걸 좋아해요", "자연스러운 꾸안꾸", "깔끔하게 신경 써요", "편한 게 좋아요", "상황에 따라 달라요", "기타"
        ));
    }

    // 대분류 기준 성향 데이터 등록
    private void registerCategory(String bigCategory, List<String> smallCategories) {
        for (String smallCategory : smallCategories) {
            if (personalRepository.findByBigCategoryAndSmallCategory(bigCategory, smallCategory).isPresent()) {
                continue;
            }

            personalRepository.save(CodePersonal.builder()
                    .bigCategory(bigCategory)
                    .smallCategory(smallCategory)
                    .build());
        }
    }
}
