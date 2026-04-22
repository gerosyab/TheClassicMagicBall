# UI 구조 & 스케일 전략 — 모바일(세로) / 태블릿(세로·가로)

메인 화면(`MainFragment` = Front 볼 + 힌트 pill)과 메시지 화면(`MsgFragment` = Msg 볼 + 답변 + 액션 바)은 **같은 세로 섹션 구조**를 공유한다. 반지름 계산식은 `LAYOUT_AND_SCALING.md` 참고.

---

## 1. 설계 원칙

| 축 | 원칙 |
|----|-----|
| **타이틀** | **가로 폭 구동**(버킷별 %: 폰 95% / 태블릿 세로 85% / 태블릿 가로 60%), 세로는 이미지 비율대로. |
| **하단 크롬** | **고정 dp 높이**(`magic_bottom_chrome_height=120dp`). Main/Msg 동일. |
| **매직볼** | **버킷별 percent** + **Front 상한 지름(cap)**. 세로 공간이 부족한 상황(가로 모드)은 percent를 낮춤. |
| **Main ↔ Msg 관계** | Msg 볼은 Main 볼 대비 **"유지 또는 확대"만**. 축소 금지. |
| **레이아웃 뼈대** | 폰은 오버레이(`FrameLayout`), 태블릿은 3단 분리(`LinearLayout vertical`). |
| **광고 슬롯** | `activity_main`에서 **50dp 고정 예약**. `BuildConfig.ADS_ENABLED=false`면 `GONE`. |

---

## 2. 버킷 전략 (현재 목표치)

화면 방향·비율에 따라 **타이틀 / 볼 / 하단**이 적절한 비중을 차지하도록 한 목표치. 실제 리소스 값과 함께 표기.

### 2-1. 모바일 세로 (`default` — sw < 600dp)

| 요소 | 전략 | 리소스/값 |
|------|------|-----------|
| 루트 레이아웃 | **오버레이**(볼이 전체 폭/높이, 타이틀·하단 Z-위로) | `layout/main_fragment.xml`, `layout/msg_fragment.xml` |
| 타이틀 | 부모 너비의 **95%**, 상단 `title_top_margin=16dp` | `title_image_weight=0.95`, `title_side_weight=0.025` (`values/dimens.xml`) |
| 볼(Front) | **100%** (cap 사실상 없음 → minDim의 ~38%까지 자유) | `integers: front_ball_radius_percent=100`, `dimens: magic_ball_max_diameter=6000dp` |
| 볼(Msg) | **≥ Front**: **118%** (폰에서는 크롭·겹침이 극적 연출) | `integers: msg_ball_radius_percent=118` |
| 하단 | 120dp, 좌우 인셋 **28dp** (3버튼 레이아웃의 끝 정렬 여유) | `magic_bottom_chrome_horizontal_inset=28dp` |
| 광고 | 하단 50dp 고정 슬롯 | `ad_banner_slot_height=50dp` |

> 폰은 가로 모드를 고려하지 않음(세로 전용 UX). 필요해지면 `layout-land/`에서 별도 분기.

### 2-2. 태블릿 세로 (`sw600dp` — sw ≥ 600dp, 세로 비율 >= 1)

| 요소 | 전략 | 리소스/값 |
|------|------|-----------|
| 루트 레이아웃 | **3단 분리**(`[타이틀][볼(weight=1)][하단]`) | `layout-sw600dp/main_fragment.xml`, `layout-sw600dp/msg_fragment.xml` |
| 타이틀 | **85%** (폰보다 약간 좁게) | `values-sw600dp/dimens.xml: title_image_weight=0.85, title_side_weight=0.075` |
| 볼(Front) | 중간 밴드에서 **115%** | `values-sw600dp/integers.xml: front_ball_radius_percent=115` |
| 볼(Msg) | **Front 이상**: **130%** (클로즈업 강화) | `values-sw600dp/integers.xml: msg_ball_radius_percent=130` |
| Front cap | **440dp** — 과도한 확대 방지 | `values-sw600dp/dimens.xml: magic_ball_max_diameter=440dp` |
| 하단 | 120dp, **인셋 0dp** (넓은 폭에서 28dp 인셋은 어색) | `values-sw600dp/dimens.xml: magic_bottom_chrome_horizontal_inset=0dp` |
| 광고 | 동일 50dp 슬롯 | (공통) |

### 2-3. 태블릿 가로 (`sw600dp-land` — sw ≥ 600dp, 가로 모드)

**제약:** 세로 공간이 급격히 줄어듦 → 볼이 타이틀·하단과 충돌할 위험 최대.

| 요소 | 전략 | 리소스/값 |
|------|------|-----------|
| 레이아웃 | 세로와 **동일 XML 상속**(`layout-sw600dp/`). 볼만 percent로 조정. | - |
| 타이틀 | **60%** — 가로 모드에서 타이틀이 과하게 커지는 것을 방지 | `values-sw600dp-land/dimens.xml: title_image_weight=0.60, title_side_weight=0.20` |
| 볼(Front) | **82%** — 세로 공간 부족에 맞춰 축소 | `values-sw600dp-land/integers.xml: front_ball_radius_percent=82` |
| 볼(Msg) | **Front 이상**: **110%** (Front 82 → Msg 110, 즉 **+28%p 확대**) | `values-sw600dp-land/integers.xml: msg_ball_radius_percent=110` |
| Front cap | 440dp 상속 | - |
| 하단 | 120dp, 인셋 0dp 상속 | - |

> 향후 가로에서 타이틀 상단 여백을 더 줄이고 싶으면 `values-sw600dp-land/dimens.xml`에 `title_top_margin`을 따로 두면 됨(현재는 공통 16dp 사용).

---

## 3. Main ↔ Msg 관계 정책

**원칙: Msg는 Main 대비 유지 또는 확대.**

| 버킷 | Front | Msg | Δ | 해석 |
|------|------|-----|----|------|
| 모바일 세로 | 100 | **118** | +18 | 오버레이 구조라 Msg 등장 시 뷰 전체가 볼로 채워지는 느낌 강화 |
| 태블릿 세로 | 115 | **130** | +15 | 분리 레이아웃이라 밴드가 크다 → 확대 여유 |
| 태블릿 가로 | 82 | **110** | +28 | Front를 작게 유지하고 Msg에서 본격 확대 |

모든 케이스에서 **Msg ≥ Front** 조건을 만족. 축소하는 버킷 없음.

---

## 4. 레이아웃 뼈대

### 4-1. 공통 `activity_main`

```
FrameLayout (root)
├─ StarfieldView  (match_parent, 배경)
└─ LinearLayout (vertical, mother_linear)
   ├─ FrameLayout   id=content_frame   (weight=1)   ← Fragment 교체
   └─ FrameLayout   id=ad_banner_slot  (h=50dp)
      └─ AdView (BANNER)
```

### 4-2. 폰(세로) — `layout/*_fragment.xml`

```
FrameLayout (clipChildren=false)
├─ FrontView / MsgView              (match_parent × match_parent)
├─ Title wrapper                    (top|center_horizontal, wrap_content)
│   └─ include_magic_title          (95%)
└─ Bottom chrome                    (bottom|center_horizontal, 120dp)
    ├─ (Main) hint pill  or  (Msg) [back][pill][capture]
    └─ secondary TextSwitcher
```

### 4-3. 태블릿(세로·가로) — `layout-sw600dp/*_fragment.xml`

```
LinearLayout (vertical)
├─ Title wrapper                    (wrap_content)
│   └─ include_magic_title          (95%)
├─ FrontView / MsgView              (match_parent, 0dp, weight=1)
└─ Bottom chrome                    (120dp)
    ├─ (Main) hint pill  or  (Msg) [back][pill][capture]
    └─ secondary TextSwitcher
```

---

## 5. 하단 버튼 영역 (Main / Msg 공통 치수)

| 요소 | 값 |
|------|-----|
| `magic_bottom_chrome_height` | **120dp** |
| `magic_pill_row_height` | **58dp** |
| `magic_circle_button_size` (back/capture) | **58dp** |
| `magic_hint_pill_width` | **168dp** (폭 고정, 텍스트 ellipsize) |
| `magic_pill_to_secondary_gap` | **8dp** |
| `magic_secondary_text_block_height` | **38dp** |
| `main_bottom_block_padding` (하단 여백) | **16dp** |
| 좌우 인셋 (폰/태블릿) | **28dp / 0dp** |

Main(pill 1개) ↔ Msg(버튼 3개)에서 **pill 중심선과 블록 총 높이가 동일**하므로 전환 시 깜빡임 없음.

---

## 6. 광고 & 스크린샷 모드

- **개발/릴리즈 기본**: `ADS_ENABLED=true` → `ad_banner_slot`에 `AdView` 로드.
- **스토어 스크린샷용**: `./gradlew assembleDebug -PADS_ENABLED=false` → `MainActivity`가 `adBannerSlot.visibility = GONE`.
- **비밀 ID**: `secrets.properties`(gitignore) / `-PADMOB_*` / `env`. 없으면 빌드 실패(`GradleException`).

---

## 7. 조정 포인트 요약

| 목적 | 파일 / 키 |
|------|-----------|
| 타이틀 폭 | `values*/dimens.xml: title_image_weight` & `title_side_weight` (float via `<item type="dimen" format="float">`). image + 2*side = 1.0 |
| 타이틀 상단 여백 | `dimens/title_top_margin` |
| 볼 크기(버킷별) | `values*/integers.xml: front_ball_radius_percent`, `msg_ball_radius_percent` |
| 볼 상한 지름(Front 전용) | `values*/dimens.xml: magic_ball_max_diameter` |
| 하단 블록 높이 | `dimens/magic_bottom_chrome_height`, `magic_pill_row_height`, `magic_secondary_text_block_height` |
| 하단 좌우 인셋 | `dimens/magic_bottom_chrome_horizontal_inset` (폰 28dp, 태블릿 0dp) |
| 광고 슬롯 높이 | `dimens/ad_banner_slot_height` |
| 광고 on/off | `-PADS_ENABLED=true|false` → `BuildConfig.ADS_ENABLED` |

---

## 8. 리소스 버킷 매트릭스

| 버킷 | 레이아웃 | dimens | integers |
|------|----------|--------|----------|
| **default** (폰 세로) | `layout/` | `values/dimens.xml` | `values/integers.xml` |
| **sw600dp** (태블릿 세로) | `layout-sw600dp/` | `values-sw600dp/dimens.xml` (+상속) | `values-sw600dp/integers.xml` |
| **sw600dp-land** (태블릿 가로) | `layout-sw600dp/` (상속) | `values-sw600dp/dimens.xml` (상속) | `values-sw600dp-land/integers.xml` |

태블릿 가로는 **전용 레이아웃 없이** percent만 다르게 덮어쓰는 전략. 타이틀/하단이 고정 dp + 이미지 비율이라 가로에서도 비례 축소가 자연스러움.

---

## 9. 요구사항 vs 현재 구현 (체크리스트)

| 요구사항 | 상태 |
|----------|------|
| 타이틀 폭 버킷별 차등(폰 95 / 태블릿 세로 85 / 태블릿 가로 60) | ✅ 리소스화 완료 |
| Main ↔ Msg 볼: **Msg가 Front 이상** | ✅ 모든 버킷에서 충족 |
| 폰: 오버레이 구조 | ✅ |
| 태블릿 세로: 3단 분리, 여유 있는 볼 | ✅ |
| 태블릿 가로: 세로 공간 부족 대응(Front %↓) | ✅ |
| Main/Msg 하단 블록 치수 동일 | ✅ |
| 광고 슬롯 고정 50dp + 스샷 모드 GONE | ✅ |
| AdMob ID 레포 제외 + 빌드 시 주입 | ✅ |

---

*Title width: phone 95% / tablet portrait 85% / tablet landscape 60%. Msg ≥ Front 정책.*
