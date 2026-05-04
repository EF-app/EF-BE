-- ============================================================================
-- ShedLock 테이블 (분산 스케줄러 락) — JPA 관리 외 DDL.
-- defer-datasource-initialization=true 라 JPA DDL 이후 이 파일이 실행됨.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)   NOT NULL,
    lock_until TIMESTAMP(3)  NOT NULL,
    locked_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255)  NOT NULL,
    PRIMARY KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- code_nickname_word 시드 (v2.0: 형용사 50 + 동물 66 + 음식 40 + 자연물 30 = 186개)
-- Spring Boot 의 sql.init 로 부트 시 자동 실행. INSERT IGNORE 로 재실행 안전.
-- ============================================================================

INSERT IGNORE INTO code_nickname_word (word, type, is_active) VALUES
    ('귀여운', 'ADJ', TRUE), ('사랑스러운', 'ADJ', TRUE), ('행복한', 'ADJ', TRUE),
    ('신난', 'ADJ', TRUE), ('설레는', 'ADJ', TRUE), ('두근거리는', 'ADJ', TRUE),
    ('기분좋은', 'ADJ', TRUE), ('상냥한', 'ADJ', TRUE), ('다정한', 'ADJ', TRUE),
    ('따뜻한', 'ADJ', TRUE), ('포근한', 'ADJ', TRUE), ('부드러운', 'ADJ', TRUE),
    ('온화한', 'ADJ', TRUE), ('싱그러운', 'ADJ', TRUE), ('상큼한', 'ADJ', TRUE),
    ('향긋한', 'ADJ', TRUE), ('달콤한', 'ADJ', TRUE), ('달달한', 'ADJ', TRUE),
    ('말랑한', 'ADJ', TRUE), ('보드라운', 'ADJ', TRUE), ('폭신한', 'ADJ', TRUE),
    ('몽글몽글한', 'ADJ', TRUE), ('말캉한', 'ADJ', TRUE), ('촉촉한', 'ADJ', TRUE),
    ('반짝이는', 'ADJ', TRUE), ('빛나는', 'ADJ', TRUE), ('영롱한', 'ADJ', TRUE),
    ('예쁜', 'ADJ', TRUE), ('고운', 'ADJ', TRUE), ('단정한', 'ADJ', TRUE),
    ('배고픈', 'ADJ', TRUE), ('졸린', 'ADJ', TRUE), ('나른한', 'ADJ', TRUE),
    ('꾸벅꾸벅', 'ADJ', TRUE), ('방긋방긋', 'ADJ', TRUE), ('생글생글', 'ADJ', TRUE),
    ('방실방실', 'ADJ', TRUE), ('아장아장', 'ADJ', TRUE), ('오물오물', 'ADJ', TRUE),
    ('쫑긋쫑긋', 'ADJ', TRUE), ('똑똑한', 'ADJ', TRUE), ('씩씩한', 'ADJ', TRUE),
    ('용감한', 'ADJ', TRUE), ('친절한', 'ADJ', TRUE), ('수줍은', 'ADJ', TRUE),
    ('해맑은', 'ADJ', TRUE), ('명랑한', 'ADJ', TRUE), ('활발한', 'ADJ', TRUE),
    ('장난스러운', 'ADJ', TRUE), ('호기심많은', 'ADJ', TRUE);

INSERT IGNORE INTO code_nickname_word (word, type, is_active) VALUES
    ('토끼', 'ANIMAL', TRUE), ('다람쥐', 'ANIMAL', TRUE), ('청설모', 'ANIMAL', TRUE),
    ('햄스터', 'ANIMAL', TRUE), ('기니피그', 'ANIMAL', TRUE), ('친칠라', 'ANIMAL', TRUE),
    ('고양이', 'ANIMAL', TRUE), ('강아지', 'ANIMAL', TRUE), ('새끼고양이', 'ANIMAL', TRUE),
    ('새끼강아지', 'ANIMAL', TRUE), ('푸들', 'ANIMAL', TRUE), ('포메라니안', 'ANIMAL', TRUE),
    ('시바견', 'ANIMAL', TRUE), ('코숏', 'ANIMAL', TRUE), ('곰', 'ANIMAL', TRUE),
    ('아기곰', 'ANIMAL', TRUE), ('북극곰', 'ANIMAL', TRUE), ('판다', 'ANIMAL', TRUE),
    ('레서판다', 'ANIMAL', TRUE), ('코알라', 'ANIMAL', TRUE), ('캥거루', 'ANIMAL', TRUE),
    ('사슴', 'ANIMAL', TRUE), ('아기사슴', 'ANIMAL', TRUE), ('얼룩말', 'ANIMAL', TRUE),
    ('코끼리', 'ANIMAL', TRUE), ('아기코끼리', 'ANIMAL', TRUE), ('기린', 'ANIMAL', TRUE),
    ('사자', 'ANIMAL', TRUE), ('아기사자', 'ANIMAL', TRUE), ('아기호랑이', 'ANIMAL', TRUE),
    ('돌고래', 'ANIMAL', TRUE), ('고래', 'ANIMAL', TRUE), ('아기고래', 'ANIMAL', TRUE),
    ('바다사자', 'ANIMAL', TRUE), ('물개', 'ANIMAL', TRUE), ('해달', 'ANIMAL', TRUE),
    ('수달', 'ANIMAL', TRUE), ('거북이', 'ANIMAL', TRUE), ('아기거북이', 'ANIMAL', TRUE),
    ('문어', 'ANIMAL', TRUE), ('해마', 'ANIMAL', TRUE), ('병아리', 'ANIMAL', TRUE),
    ('오리', 'ANIMAL', TRUE), ('아기오리', 'ANIMAL', TRUE), ('펭귄', 'ANIMAL', TRUE),
    ('아기펭귄', 'ANIMAL', TRUE), ('앵무새', 'ANIMAL', TRUE), ('참새', 'ANIMAL', TRUE),
    ('부엉이', 'ANIMAL', TRUE), ('올빼미', 'ANIMAL', TRUE), ('백조', 'ANIMAL', TRUE),
    ('양', 'ANIMAL', TRUE), ('아기양', 'ANIMAL', TRUE), ('송아지', 'ANIMAL', TRUE),
    ('망아지', 'ANIMAL', TRUE), ('아기돼지', 'ANIMAL', TRUE), ('고슴도치', 'ANIMAL', TRUE),
    ('너구리', 'ANIMAL', TRUE), ('여우', 'ANIMAL', TRUE), ('아기여우', 'ANIMAL', TRUE),
    ('알파카', 'ANIMAL', TRUE), ('라마', 'ANIMAL', TRUE), ('카피바라', 'ANIMAL', TRUE),
    ('미어캣', 'ANIMAL', TRUE), ('두더지', 'ANIMAL', TRUE), ('나무늘보', 'ANIMAL', TRUE);

INSERT IGNORE INTO code_nickname_word (word, type, is_active) VALUES
    ('마카롱', 'FOOD', TRUE), ('쿠키', 'FOOD', TRUE), ('도넛', 'FOOD', TRUE),
    ('푸딩', 'FOOD', TRUE), ('젤리', 'FOOD', TRUE), ('마시멜로', 'FOOD', TRUE),
    ('솜사탕', 'FOOD', TRUE), ('카스텔라', 'FOOD', TRUE), ('식빵', 'FOOD', TRUE),
    ('크로와상', 'FOOD', TRUE), ('베이글', 'FOOD', TRUE), ('마들렌', 'FOOD', TRUE),
    ('타르트', 'FOOD', TRUE), ('컵케이크', 'FOOD', TRUE), ('와플', 'FOOD', TRUE),
    ('팬케이크', 'FOOD', TRUE), ('크레페', 'FOOD', TRUE), ('롤케이크', 'FOOD', TRUE),
    ('찰떡파이', 'FOOD', TRUE), ('호떡', 'FOOD', TRUE), ('붕어빵', 'FOOD', TRUE),
    ('찹쌀떡', 'FOOD', TRUE), ('경단', 'FOOD', TRUE), ('꿀떡', 'FOOD', TRUE),
    ('호빵', 'FOOD', TRUE), ('만두', 'FOOD', TRUE), ('팥빙수', 'FOOD', TRUE),
    ('아이스크림', 'FOOD', TRUE), ('슈크림', 'FOOD', TRUE), ('딸기', 'FOOD', TRUE),
    ('복숭아', 'FOOD', TRUE), ('귤', 'FOOD', TRUE), ('포도', 'FOOD', TRUE),
    ('블루베리', 'FOOD', TRUE), ('체리', 'FOOD', TRUE), ('망고', 'FOOD', TRUE),
    ('참외', 'FOOD', TRUE), ('수박', 'FOOD', TRUE), ('사과', 'FOOD', TRUE),
    ('앵두', 'FOOD', TRUE);

INSERT IGNORE INTO code_nickname_word (word, type, is_active) VALUES
    ('별', 'NATURE', TRUE), ('구름', 'NATURE', TRUE), ('달', 'NATURE', TRUE),
    ('햇살', 'NATURE', TRUE), ('눈송이', 'NATURE', TRUE), ('이슬', 'NATURE', TRUE),
    ('무지개', 'NATURE', TRUE), ('별똥별', 'NATURE', TRUE), ('달빛', 'NATURE', TRUE),
    ('별빛', 'NATURE', TRUE), ('노을', 'NATURE', TRUE), ('새벽', 'NATURE', TRUE),
    ('봄바람', 'NATURE', TRUE), ('민들레', 'NATURE', TRUE), ('튤립', 'NATURE', TRUE),
    ('해바라기', 'NATURE', TRUE), ('벚꽃', 'NATURE', TRUE), ('장미', 'NATURE', TRUE),
    ('수국', 'NATURE', TRUE), ('새싹', 'NATURE', TRUE), ('나뭇잎', 'NATURE', TRUE),
    ('도토리', 'NATURE', TRUE), ('솔방울', 'NATURE', TRUE), ('단풍', 'NATURE', TRUE),
    ('들꽃', 'NATURE', TRUE), ('꽃잎', 'NATURE', TRUE), ('조약돌', 'NATURE', TRUE),
    ('조개', 'NATURE', TRUE), ('진주', 'NATURE', TRUE), ('풍선', 'NATURE', TRUE);


-- ============================================================================
-- 개발용 시드 데이터 (local 프로파일에서만 사용 권장)
-- - SecurityUtil 폴백이 user_id=1 이므로 id=1 유저가 반드시 있어야 전 기능 테스트 가능
-- - 카테고리 / 구독 플랜 / 아이템 마스터 등 최소 세트
-- ============================================================================

-- 테스트용 유저 (id=1 은 SecurityUtil 폴백과 매칭, id=2 는 상대역)
INSERT IGNORE INTO users (id, uuid, login_id, password, phone, scode, nickname, age, job, is_withdraw, area_id, role)
VALUES
    (1, '00000000-0000-0000-0000-000000000001', 'alice', '$2a$10$dev', '+821011110001', '0001', 'alice',  25, 'OFFICE_WORKER', FALSE, 1, 'ROLE_USER'),
    (2, '00000000-0000-0000-0000-000000000002', 'bob',   '$2a$10$dev', '+821011110002', '0002', 'bob',    27, 'OFFICE_WORKER', FALSE, 1, 'ROLE_USER'),
    (9, '00000000-0000-0000-0000-000000000009', 'admin', '$2a$10$dev', '+821011110009', '0009', 'admin',  30, 'ETC',           FALSE, 1, 'ROLE_ADMIN');

-- 공통 카테고리
INSERT IGNORE INTO categories (code, name, type) VALUES
    ('BAL_LOVE',    '연애 밸런스',   'BALANCE'),
    ('BAL_DAILY',   '일상 밸런스',   'BALANCE'),
    ('POST_DAILY',  '일상 포스트잇', 'POST_IT'),
    ('POST_LIGHTN', '번개',          'POST_IT');

-- 구독 플랜 (v1.2: subscription_plan → code_subscription)
INSERT IGNORE INTO code_subscription (plan_code, name, price, duration_days, is_active) VALUES
    ('BASIC',   '무료',     0,     0, TRUE),
    ('PREMIUM', '프리미엄', 9900, 30, TRUE);

-- 아이템 마스터 (v1.2: item_catalog → code_item, 서비스 훅에서 CODE 기반으로 조회)
INSERT IGNORE INTO code_item (item_code, name, star_cost, category, effect_duration_min, is_active) VALUES
    ('POST_PIN',       '글 상단 고정',        3, 'POST_IT', 60,   TRUE),
    ('PROFILE_BOOST',  '프로필 부스터',       8, 'MATCH',   30,   TRUE),
    ('SUPER_LIKE',     '강조 좋아요',         3, 'MATCH',   NULL, TRUE),
    ('PRE_MESSAGE',    '메시지 먼저 보내기',  5, 'MATCH',   NULL, TRUE),
    ('UNDO',           '되돌리기',            1, 'MATCH',   NULL, TRUE);

-- 테스트 유저 별 잔액 초기화 (1000 별)
INSERT IGNORE INTO user_star_balance (user_id, balance, total_charged, total_used) VALUES
    (1, 1000, 1000, 0),
    (2, 1000, 1000, 0);

