package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.profile.entity.Interest;
import com.nokcha.efbe.domain.user.repository.InterestRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InterestDataInitializer {

    private final InterestRepository interestRepository;

    @PostConstruct
    public void initialize() {
        registerCategory("라이프스타일", List.of(
                "아침형 인간", "저녁형 인간", "집순이", "활동적", "반려동물", "미니멀라이프", "혼밥/혼영", "워라밸", "친환경"
        ));
        registerCategory("취미", List.of(
                "유튜브", "영화", "드라마", "연예인", "애니메이션", "웹툰", "웹소설", "요리", "베이킹", "홈카페",
                "드로잉", "양재", "소잉", "필사", "캘리그라피", "뜨개질", "향수", "악기연주", "밴드", "춤",
                "사주/타로", "메이크업", "네일아트", "청소", "인테리어", "물건 수집", "주식", "비트코인", "반려식물"
        ));
        registerCategory("외부 여가 활동", List.of(
                "맛집 탐방", "카페 투어", "전시회", "연극", "뮤지컬", "사진 찍기", "필름사진", "스포츠 경기 직관", "페스티벌", "방탈출",
                "코인노래방", "볼링", "당구", "포켓볼", "쇼핑", "피크닉", "놀이공원", "산책", "외출", "국내여행",
                "해외여행", "드라이브", "캠핑", "바비큐", "등산", "봉사활동", "플로깅", "낚시"
        ));
        registerCategory("자기계발", List.of(
                "재테크", "자격증 취득", "외국어 공부", "커리어 개발", "독서", "신문", "뉴스", "카공", "명상", "미라클모닝",
                "식단", "건강 관리", "작문", "SNS 키우기"
        ));
        registerCategory("음식", List.of(
                "비건", "한식", "중식", "일식", "양식", "브런치", "베트남/태국", "멕시코 음식", "매운 음식", "채소파",
                "육식파", "해산물", "분식", "패스트푸드", "베이커리", "간식류"
        ));
        registerCategory("운동", List.of(
                "풋살", "러닝", "마라톤", "자전거", "헬스", "요가", "필라테스", "발레", "홈트", "클라이밍",
                "크로스핏", "배드민턴", "탁구", "골프", "테니스", "수영", "서핑", "스쿠버다이빙", "스키", "스노우보드",
                "스케이트보드", "롤러스케이트", "유도", "태권도", "복싱/주짓수", "야구", "축구", "농구", "배구"
        ));
        registerCategory("음악", List.of(
                "K-POP", "J-POP", "팝송", "인디음악", "시티팝", "알앤비", "EDM", "힙합", "락", "발라드", "재즈", "클래식", "골고루"
        ));
        registerCategory("게임", List.of(
                "닌텐도", "보드게임", "오버워치", "배틀그라운드", "LOL", "서든어택", "메이플스토리", "발로란트", "플스", "스팀 게임"
        ));
    }

    // 대분류 기준 관심사 데이터 등록
    private void registerCategory(String bigCategory, List<String> smallCategories) {
        for (int index = 0; index < smallCategories.size(); index++) {
            String smallCategory = smallCategories.get(index);

            if (interestRepository.findByBigCategoryAndSmallCategory(bigCategory, smallCategory).isPresent()) {
                continue;
            }

            interestRepository.save(Interest.builder()
                    .bigCategory(bigCategory)
                    .smallCategory(smallCategory)
                    .sortOrder(index + 1)
                    .build());
        }
    }
}
