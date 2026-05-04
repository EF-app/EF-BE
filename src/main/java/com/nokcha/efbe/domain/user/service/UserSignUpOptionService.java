package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.domain.profile.entity.CodeInterest;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Mbti;
import com.nokcha.efbe.domain.user.dto.response.SignUpOptionGroupRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpOptionItemRspDto;
import com.nokcha.efbe.domain.user.entity.Job;
import com.nokcha.efbe.domain.user.repository.InterestRepository;
import com.nokcha.efbe.domain.user.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSignUpOptionService {

    private static final List<String> LIFESTYLE_CATEGORIES = List.of("음주", "선호 주종", "흡연", "흡연 종류", "타투유무");
    private static final List<String> ABOUT_ME_CATEGORIES = List.of("종교", "이쪽 지인", "커밍아웃 정도", "머리", "체형", "키", "성향", "패션 스타일", "꾸미는 스타일");
    private static final List<String> IDEAL_CATEGORIES = List.of("머리", "체형", "키", "성향");

    private final InterestRepository interestRepository;
    private final PersonalRepository personalRepository;

    @Transactional(readOnly = true)
    public List<SignUpOptionGroupRspDto> getInterestOptions() {
        List<CodeInterest> interests = interestRepository.findAll(Sort.by(Sort.Order.asc("id")));

        Map<String, List<SignUpOptionItemRspDto>> grouped = new LinkedHashMap<>();
        for (CodeInterest interest : interests) {
            grouped.computeIfAbsent(interest.getBigCategory(), key -> new ArrayList<>())
                    .add(codeItem(interest.getId(), interest.getSmallCategory()));
        }

        return toGroups(grouped);
    }

    @Transactional(readOnly = true)
    public List<SignUpOptionGroupRspDto> getLifestyleOptions() {
        return getPersonalGroups(LIFESTYLE_CATEGORIES);
    }

    @Transactional(readOnly = true)
    public List<SignUpOptionGroupRspDto> getAboutMeOptions() {
        List<SignUpOptionGroupRspDto> groups = new ArrayList<>();
        groups.add(enumGroup("직업", getJobItems()));
        groups.addAll(getPersonalGroups(ABOUT_ME_CATEGORIES));
        groups.add(enumGroup("MBTI", getMbtiItems()));
        return groups;
    }

    @Transactional(readOnly = true)
    public List<SignUpOptionGroupRspDto> getIdealOptions() {
        List<SignUpOptionGroupRspDto> groups = new ArrayList<>(getPersonalGroups(IDEAL_CATEGORIES));
        groups.add(enumGroup("포인트 항목", getIdealPointItems()));
        return groups;
    }

    private List<SignUpOptionGroupRspDto> getPersonalGroups(List<String> categoryOrder) {
        List<CodePersonal> personals = personalRepository.findAll(Sort.by(Sort.Order.asc("id")));

        Set<String> categorySet = Set.copyOf(categoryOrder);
        Map<String, List<SignUpOptionItemRspDto>> grouped = new LinkedHashMap<>();
        for (String category : categoryOrder) {
            grouped.put(category, new ArrayList<>());
        }

        for (CodePersonal personal : personals) {
            if (!categorySet.contains(personal.getBigCategory())) {
                continue;
            }

            grouped.get(personal.getBigCategory()).add(codeItem(personal.getId(), personal.getSmallCategory()));
        }

        return toGroups(grouped);
    }

    private List<SignUpOptionGroupRspDto> toGroups(Map<String, List<SignUpOptionItemRspDto>> grouped) {
        List<SignUpOptionGroupRspDto> groups = new ArrayList<>();
        for (Map.Entry<String, List<SignUpOptionItemRspDto>> entry : grouped.entrySet()) {
            groups.add(SignUpOptionGroupRspDto.builder()
                    .category(entry.getKey())
                    .options(entry.getValue())
                    .build());
        }
        return groups;
    }

    private SignUpOptionGroupRspDto enumGroup(String category, List<SignUpOptionItemRspDto> options) {
        return SignUpOptionGroupRspDto.builder()
                .category(category)
                .options(options)
                .build();
    }

    private SignUpOptionItemRspDto codeItem(Long id, String label) {
        return SignUpOptionItemRspDto.builder()
                .id(id)
                .label(label)
                .build();
    }

    private SignUpOptionItemRspDto enumItem(Enum<?> code, String label) {
        return SignUpOptionItemRspDto.builder()
                .code(code.name())
                .label(label)
                .build();
    }

    private List<SignUpOptionItemRspDto> getJobItems() {
        return List.of(
                enumItem(Job.OFFICE_WORKER, "직장인"),
                enumItem(Job.FREELANCER, "프리랜서"),
                enumItem(Job.SELF_EMPLOYED, "자영업/사업"),
                enumItem(Job.STUDENT, "학생(대학생/대학원생)"),
                enumItem(Job.JOB_SEEKER, "취업준비생"),
                enumItem(Job.INTERNATIONAL_STUDENT, "유학생"),
                enumItem(Job.ETC, "기타")
        );
    }

    private List<SignUpOptionItemRspDto> getMbtiItems() {
        List<SignUpOptionItemRspDto> items = new ArrayList<>();
        for (Mbti mbti : Mbti.values()) {
            items.add(enumItem(mbti, mbti.name()));
        }
        return items;
    }

    private List<SignUpOptionItemRspDto> getIdealPointItems() {
        return List.of(
                enumItem(IdealPointType.INTEREST, "관심사"),
                enumItem(IdealPointType.IDEAL_TYPE, "이상형"),
                enumItem(IdealPointType.LIFE_STYLE, "생활 습관"),
                enumItem(IdealPointType.AREA, "지역")
        );
    }
}
