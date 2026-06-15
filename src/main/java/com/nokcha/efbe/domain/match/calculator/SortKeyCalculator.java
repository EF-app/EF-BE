package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.ImportantPoint;
import com.nokcha.efbe.domain.match.model.UserContext;
import org.springframework.stereotype.Component;

/**
 * sortKey 산출.
 *  영역 점수 × 가중치 가중합.
 *  뷰어 me 의 중요 포인트가 가리키는 영역의 가중치에 영역별 차등 가산
 *  (bumpKeyword/Ideal/Lifestyle/Location) → 합 1 로 정규화 → 가중평균.
 *  뉴비 부스트는 sortKey 에 가산하지 않고 슬롯(FeedSelector)에서 별도 처리.
 */
@Component
public class SortKeyCalculator {

    public double calc(UserContext me,
                       double keyword,
                       double idealBidir,
                       double lifestyle,
                       double location,
                       MatchingConfig cfg) {
        double wK   = cfg.getWeightKeyword();
        double wI   = cfg.getWeightIdeal();
        double wL   = cfg.getWeightLifestyle();
        double wLoc = cfg.getWeightLocation();

        // 중요 포인트 차등 가산 — 영역별로 다른 bump 값 적용
        for (ImportantPoint ip : me.importantPoints()) {
            switch (ip) {
                case KEYWORD   -> wK   += cfg.getBumpKeyword();
                case IDEAL     -> wI   += cfg.getBumpIdeal();
                case LIFESTYLE -> wL   += cfg.getBumpLifestyle();
                case LOCATION  -> wLoc += cfg.getBumpLocation();
            }
        }

        // 합 1 로 정규화
        double sum = wK + wI + wL + wLoc;
        wK   /= sum;
        wI   /= sum;
        wL   /= sum;
        wLoc /= sum;

        return keyword * wK + idealBidir * wI + lifestyle * wL + location * wLoc;
    }
}
