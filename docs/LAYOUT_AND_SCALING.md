# Magic Ball UI — Layout & scaling reference  
# 매직볼 UI — 레이아웃·스케일 참고

This document describes how **title bar**, **main (Front) ball**, and **msg (MsgView) ball** sizes are determined for **phone**, **tablet portrait**, and **tablet landscape**.  
이 문서는 **폰**, **태블릿 세로**, **태블릿 가로**에서 **상단 타이틀**, **메인(앞면) 볼**, **메시지(Msg) 볼** 크기가 어떻게 정해지는지 정리한다.

---

## 1. Title bar / 상단 타이틀바

**Layout:** `app/src/main/res/layout/include_magic_title.xml`

**Width rule / 가로 기준:**  
- Horizontal `LinearLayout`: weighted spacers **2.5% + 95% + 2.5%** of the **parent width** (same on phone and tablet).  
- 가로 `LinearLayout`: 부모 **너비** 대비 **2.5% + 95% + 2.5%** 가중치.  
- The `ImageView` sits in the **95%** slot: `layout_width="0dp"`, `layout_weight="0.95"`, `adjustViewBounds="true"`, `layout_height="wrap_content"`, `scaleType="fitCenter"`.  
- 이미지는 **95%** 슬롯에 두고, **가로가 부모 너비의 95%**가 되도록 한 뒤 비율에 맞춰 **세로는 intrinsic** (`wrap_content` + `adjustViewBounds`).

**Height rule / 세로:**  
- **Not** a fixed dp row height. The title row height follows the **scaled drawable height** at 95% width (aspect ratio preserved).  
- **고정 dp 행 높이 없음.** 타이틀 행 세로는 **너비 95%로 스케일된 이미지 높이**에 따름.

**Outer wrapper / 바깥 래퍼:**  
- `FrameLayout` with `layout_height="wrap_content"`, `paddingTop="@dimen/title_top_margin"` (**12dp**).  
- `FrameLayout`은 `wrap_content` + `title_top_margin` (**12dp**) 패딩.

| Setting / 항목 | Value / 값 |
|----------------|------------|
| `title_top_margin` | **16dp** — `values/dimens.xml` |
| Image horizontal share / 이미지 가로 비율 | **95%** of fragment (column) width |
| Side margins (implicit) / 좌우 여백 | **2.5%** each via `Space` weights |

**Fragment layouts:**  
- Phone: `layout/main_fragment.xml`, `layout/msg_fragment.xml` (overlay ball + title wrapper).  
- Tablet: `layout-sw600dp/main_fragment.xml`, `layout-sw600dp/msg_fragment.xml` (column: title row → ball `weight=1` → bottom chrome).

**Tuning / 조정:** change **0.025 / 0.95 / 0.025** weights in `include_magic_title.xml`, or `title_top_margin`, `screen_top_padding`.  
**튜닝:** `include_magic_title.xml`의 weight 비율, `title_top_margin`, `screen_top_padding`.

---

## 2. Main ball — `FrontView` / 메인 볼 (FrontView)

**Code:** `app/src/main/java/net/gerosyab/magicball/ui/view/FrontView.kt`  
**Center / 중심:** `cx = w/2`, `cy = h/2` (view-local coordinates / 뷰 로컬 좌표).

**Radius pipeline / 반지름 계산:**

1. `minDim = min(w, h)` — allocated view width × height.  
2. `maxRadiusCap = magic_ball_max_diameter / 2` (dp → px via `getDimension`).  
3. `rBase = min(minDim * 0.4f, maxRadiusCap, minDim * 0.38f)` → usually `min(minDim * 0.38f, maxRadiusCap)` because `0.38 < 0.4`.  
4. `rAfterPct = rBase * front_ball_radius_percent / 100` (integer clamped 70–200 in code).  
5. **`outerRadius = min(rAfterPct, maxRadiusCap)`** — no `maxFit = min(w/2, h/2)`; the circle may extend past the view if `%` and cap allow (parents use `clipChildren=false` where needed).  
   **폰에서 ~410px 같은 값:** `rBase`가 `minDim*0.38`과 `cap` 중 작은 쪽이고, 폰은 `magic_ball_max_diameter=6000dp`라 사실상 **cap 무시** → `rBase ≈ 0.38 * min(w,h)`. 예: `minDim≈1080`이면 `rBase≈410.4`, `front_pct=100`이면 `outerRadius≈410.4`. `%`를 올리면 `outerRadius`는 `rBase*(pct/100)`까지 커지다가 `maxRadiusCap`에 걸리면 멈춤.

| Mode | `magic_ball_max_diameter` | `front_ball_radius_percent` |
|------|---------------------------|-----------------------------|
| Phone (sw &lt; 600) / 폰 | **6000dp** (effectively no cap) | **100** — `values/integers.xml` |
| Tablet portrait / 태블릿 세로 | **440dp** | **115** — `values-sw600dp/integers.xml` |
| Tablet landscape / 태블릿 가로 | **440dp** | **82** — `values-sw600dp-land/integers.xml` |

**View size `w × h`:** Phone overlay (full fragment); tablet middle band only.  
**뷰 크기:** 폰은 겹침 전체, 태블릿은 중간 밴드.

**Tuning / 조정:** `front_ball_radius_percent`, `magic_ball_max_diameter`, or coefficients in `FrontView.kt`.

---

## 3. Msg ball — `MsgView` / 메시지 볼 (MsgView)

**Code:** `app/src/main/java/net/gerosyab/magicball/ui/view/MsgView.kt`  
**Center / 중심:** `cx = w/2`, `cy = h/2`.

**Radius pipeline / 반지름 계산:**

1. `minDim = min(w, h)`.
2. `radiusFromWidth = minDim * 0.75f * tabletScaleFactor` (**1f** from `MainActivity`).
3. `maxRadiusFromHeight` = if `swDp < 600`: **`w * 0.75f`**; if `swDp >= 600`: **`minDim * 0.42f`**.
4. **`rBase = min(radiusFromWidth, maxRadiusFromHeight)`** — **no** `magic_ball_max_diameter` in this path (Msg has no diameter cap).
5. **`outerRadius = rBase * msg_ball_radius_percent / 100`** (clamped 70–220) — **no** `maxFit`, **no** final cap; ball can be larger than half the view (partial crop / overlap by design).

| Mode | `msg_ball_radius_percent` | `maxRadiusFromHeight` |
|------|---------------------------|------------------------|
| Phone / 폰 | **100** — `values/integers.xml` | **`w * 0.75`** |
| Tablet portrait / 태블릿 세로 | **130** — `values-sw600dp/integers.xml` | **`minDim * 0.42`** |
| Tablet landscape / 태블릿 가로 | **110** — `values-sw600dp-land/integers.xml` | **`minDim * 0.42`** |

**Tuning / 조정:** `msg_ball_radius_percent` and coefficients in `MsgView.kt` (`0.75`, `0.42`, `tabletScaleFactor`).

---

## 4. Other chrome dimens / 기타 크롬 dimen

| Resource | Typical use / 용도 |
|----------|-------------------|
| `screen_top_padding` | Root fragment top inset |
| `magic_bottom_chrome_height` | Fixed bottom block |
| `magic_pill_row_height` | Pill / three-button row |
| `magic_secondary_text_block_height` | Secondary line |
| `magic_footer_row_height` | Copyright row |
| `main_bottom_block_padding` | Padding inside bottom chrome |
| `ad_banner_slot_height` | Banner slot in `activity_main` |
| `magic_ball_max_diameter` | **FrontView only** — diameter cap; phone `values/dimens.xml` (6000dp ≈ uncapped), tablet `values-sw600dp/dimens.xml` (440dp) |

**Note / 참고:** `magic_chrome_title_block_height` was removed; title height is width-driven (95%) + intrinsic aspect.  
**참고:** `magic_chrome_title_block_height`는 제거됨. 타이틀 세로는 가로 95% + 비율 기반.

---

## 5. Activity / 액티비티

`activity_main.xml`: `StarfieldView`; `content_frame`; `ad_banner_slot` + `AdView`.

---

## 6. Debug logging / 디버그 로그

Filter Logcat by tag **`MagicBallScale`** (level **Info**).  
- **FrontView:** `LIMITER` is either `maxRadiusCap(...)` or `rBase*percent`; **headroom_pct_to_hit_cap** = percent at which `rAfterPct` would hit the diameter cap.  
- **MsgView:** one line with `rBase`, `%`, `outerRadius` (no maxFit / no diameter cap).  
Logcat 필터 **`MagicBallScale`**.

---

*Title: 95% width, intrinsic height. Msg ball %: tablet portrait 130, tablet landscape 110. Policy: Msg ≥ Front.*  
*타이틀: 가로 95%, 세로 intrinsic. Msg 볼 %: 태블릿 세로 130, 가로 110. 정책: Msg ≥ Front.*
