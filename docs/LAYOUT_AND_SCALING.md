# Magic Ball UI — Layout & scaling reference  
# 매직볼 UI — 레이아웃·스케일 참고

This document describes how **title bar**, **main (Front) ball**, and **msg (MsgView) ball** sizes are determined for **phone**, **tablet portrait**, and **tablet landscape**.  
이 문서는 **폰**, **태블릿 세로**, **태블릿 가로**에서 **상단 타이틀**, **메인(앞면) 볼**, **메시지(Msg) 볼** 크기가 어떻게 정해지는지 정리한다.

---

## 1. Title bar / 상단 타이틀바

**Layout:** `app/src/main/res/layout/include_magic_title.xml`  
- Horizontal `LinearLayout` with weighted spacers: **7.5% + 85% + 7.5%** of the row width.  
- 가로 `LinearLayout` + 가중치: 행 너비의 **7.5% + 85% + 7.5%**.  
- `ImageView` uses `scaleType="fitCenter"` inside the **85%** slot.  
- 이미지는 **85%** 슬롯 안에서 `fitCenter`.

**Row height** comes from `magic_chrome_title_block_height` + inner `paddingTop` = `title_top_margin` on the wrapping `FrameLayout` (phone) or the same dimen on tablet column layouts.  
**행 높이**는 `magic_chrome_title_block_height`이고, 바깥 `FrameLayout`에 `title_top_margin` 패딩이 있음.

| Mode | `magic_chrome_title_block_height` | `title_top_margin` | Image width |
|------|-----------------------------------|--------------------|-------------|
| Phone / 폰 | `values/dimens.xml` → **92dp** | **12dp** | **85%** of row |
| Tablet portrait / 태블릿 세로 (sw ≥ 600) | `values-sw600dp/dimens.xml` → **140dp** | **12dp** | **85%** |
| Tablet landscape / 태블릿 가로 (sw600dp-land) | `values-sw600dp-land/dimens.xml` → **100dp** | **12dp** | **85%** |

**Fragment layouts:**  
- Phone: `layout/main_fragment.xml`, `layout/msg_fragment.xml` (overlay ball + title `FrameLayout`).  
- 폰: `layout/main_fragment.xml`, `layout/msg_fragment.xml` (볼 전체 겹침 + 타이틀 `FrameLayout`).  
- Tablet: `layout-sw600dp/main_fragment.xml`, `layout-sw600dp/msg_fragment.xml` (column: title row → ball `weight=1` → bottom chrome).  
- 태블릿: `layout-sw600dp/` — 세로 컬럼: 타이틀 행 → 볼 `weight=1` → 하단.

**Tuning / 조정:** change weights in `include_magic_title.xml`, or dimens `magic_chrome_title_block_height`, `title_top_margin`, `screen_top_padding`.  
**튜닝:** `include_magic_title.xml`의 weight, 또는 `magic_chrome_title_block_height`, `title_top_margin`, `screen_top_padding`.

---

## 2. Main ball — `FrontView` / 메인 볼 (FrontView)

**Code:** `app/src/main/java/net/gerosyab/magicball/ui/view/FrontView.kt`  
**Center / 중심:** `cx = w/2`, `cy = h/2` (view-local coordinates / 뷰 로컬 좌표).

**Radius pipeline / 반지름 계산:**

1. `minDim = min(w, h)` — allocated view width × height.  
   `minDim = min(w, h)` — 뷰에 배정된 가로·세로.
2. `maxRadiusCap = magic_ball_max_diameter / 2` (dp → px).  
   `maxRadiusCap = magic_ball_max_diameter / 2` (dp→px).
3. `r0 = min(minDim * 0.4f, maxRadiusCap, minDim * 0.38f)` → effectively often `min(minDim * 0.38f, maxRadiusCap)`.  
   보통 `min(minDim×0.38, cap)`.
4. `r1 = r0 * front_ball_radius_percent / 100` (clamped 70–200 in code).  
   `r1 = r0 × front_ball_radius_percent / 100` (코드에서 70–200 클램프).
5. `outerRadius = min(min(r1, maxRadiusCap), minDim * 0.38f)`.  
   `outerRadius = min(min(r1, cap), minDim×0.38)`.

| Mode | `magic_ball_max_diameter` | `front_ball_radius_percent` |
|------|---------------------------|-----------------------------|
| Phone (sw &lt; 600) / 폰 | **6000dp** (effectively no cap / 사실상 무캡) | **100** — `values/integers.xml` |
| Tablet portrait / 태블릿 세로 | **440dp** (radius cap ≈ 220dp / 반지름 상한 ≈ 220dp) | **115** — `values-sw600dp/integers.xml` |
| Tablet landscape / 태블릿 가로 | **440dp** | **82** — `values-sw600dp-land/integers.xml` |

**View size `w × h` / 뷰 크기:**  
- **Phone:** `FrontView` is `match_parent` under a `FrameLayout`; it draws **behind** title and bottom overlays. `h` is almost full fragment height (minus root `screen_top_padding`).  
- **폰:** `FrontView`가 `match_parent`라 타이틀·하단 **뒤까지** 그림. `h`는 프래그먼트 높이 거의 전체.  
- **Tablet:** `FrontView` sits in the **middle band** only (`layout_weight=1` between title row and bottom chrome); `h` is **much smaller** than on phone for the same device.  
- **태블릿:** 볼은 타이틀과 하단 사이 **중간 밴드**만 사용 → 같은 기기에서도 `h`가 폰 레이아웃보다 **훨씬 작음**.

**Tuning / 조정:** `values*/integers.xml` → `front_ball_radius_percent`; `values*/dimens.xml` → `magic_ball_max_diameter`; or edit coefficients `0.4f`, `0.38f` in `FrontView.kt`.  
**튜닝:** `front_ball_radius_percent`, `magic_ball_max_diameter`, 또는 `FrontView.kt`의 `0.4`, `0.38` 계수.

---

## 3. Msg ball — `MsgView` / 메시지 볼 (MsgView)

**Code:** `app/src/main/java/net/gerosyab/magicball/ui/view/MsgView.kt`  
**Center / 중심:** `cx = w/2`, `cy = h/2`.

**Radius pipeline / 반지름 계산:**

1. `minDim = min(w, h)`.
2. `maxRadiusCap = magic_ball_max_diameter / 2`.
3. `radiusFromWidth = minDim * 0.75f * tabletScaleFactor` (`tabletScaleFactor` is **1f** from `MainActivity` today).  
   `tabletScaleFactor`는 현재 **1f**.
4. `maxRadiusFromHeight` =  
   - if `smallestScreenWidthDp < 600` / **폰:** `w * 0.75f` (full view width / 뷰 가로 전체)  
   - if `swDp >= 600` / **태블릿:** `minDim * 0.42f`
5. `r0 = min(radiusFromWidth, maxRadiusCap, maxRadiusFromHeight)`.
6. `r1 = r0 * msg_ball_radius_percent / 100` (clamped 70–220).  
7. `outerRadius = min(min(r1, maxRadiusCap), maxRadiusFromHeight)`.

| Mode | `magic_ball_max_diameter` | `msg_ball_radius_percent` | `maxRadiusFromHeight` |
|------|---------------------------|----------------------------|------------------------|
| Phone / 폰 | 6000dp | **100** — `values/integers.xml` | **`w * 0.75`** |
| Tablet portrait / 태블릿 세로 | 440dp | **115** — `values-sw600dp/integers.xml` | **`minDim * 0.42`** |
| Tablet landscape / 태블릿 가로 | 440dp | **82** — `values-sw600dp-land/integers.xml` | **`minDim * 0.42`** |

**View size / 뷰 크기:** Same pattern as `FrontView` — phone overlay vs tablet middle band.  
**뷰 크기:** `FrontView`와 동일 — 폰은 겹침, 태블릿은 중간 밴드.

**Tuning / 조정:** `msg_ball_radius_percent`, `magic_ball_max_diameter`, `tabletScaleFactor` (constructor args), or coefficients `0.75f`, `0.42f` / phone `w*0.75` branch in `MsgView.kt`.  
**튜닝:** `msg_ball_radius_percent`, `magic_ball_max_diameter`, `MsgView.kt`의 `0.75`, `0.42`, 폰 분기 `w×0.75`.

---

## 4. Other chrome dimens / 기타 크롬 dimen

| Resource | Typical use / 용도 |
|----------|-------------------|
| `screen_top_padding` | Root fragment top inset / 루트 상단 패딩 |
| `magic_bottom_chrome_height` | Fixed bottom block (pill + secondary + footer) / 하단 고정 블록 |
| `magic_pill_row_height` | Pill or three-button row height / 알약·3버튼 행 높이 |
| `magic_secondary_text_block_height` | Secondary line (e.g. BORING cycle) / 보조 문구 |
| `magic_footer_row_height` | Copyright row / 저작권 행 |
| `main_bottom_block_padding` | Padding inside bottom chrome / 하단 블록 내부 패딩 |
| `ad_banner_slot_height` | Reserved height for banner in `activity_main` / 액티비티 하단 광고 슬롯 |

Files / 파일: `app/src/main/res/values/dimens.xml`, `values-sw600dp/dimens.xml`, `values-sw600dp-land/dimens.xml`.

---

## 5. Activity / 액티비티

`activity_main.xml`: `StarfieldView` full screen; `content_frame` for fragments; `ad_banner_slot` fixed height (`ad_banner_slot_height`) with `AdView` inside.  
`activity_main.xml`: 별 배경 전체, `content_frame`에 프래그먼트, `ad_banner_slot` 고정 높이 안에 `AdView`.

---

*Last aligned with codebase structure for layout-sw600dp split and include_magic_title 85% width.*  
*코드 기준: layout-sw600dp 분리, include_magic_title 85% 가로 반영.*
