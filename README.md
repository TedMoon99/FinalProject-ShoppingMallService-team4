# FinalProject-ShoppingMallService-team4
## 테킷 앱스쿨:안드로이드2기 최종 프로젝트(쇼핑몰 서비스 개발) - 4팀

### Intro
<hr>

   -  프로젝트명  : MRCO (MBTI만 알아와, 코디해줄게!)
   -  기획기간    : 2024.03.13 ~ 2024.03.27
   -  개발기간    : 2024.03.28 ~ 2024.04.26
   -  개발인원    : 5명
   -  개발환경    : Android Studio
   -  개발언어    : Kotlin
   -  데이터베이스: Firebase

<hr>

## 👥 Member 👥
<h4>🧑 박현준</h4>
<a href="https://github.com/pakkyunn">
    <img src="http://img.shields.io/badge/pakkyunn-gray?logo=github"/>
</a>
<h4>👧 이보람</h4>
<a href="https://github.com/dev-worthwhile">
    <img src="http://img.shields.io/badge/dev--worthwhile-gray?logo=github"/>
</a>
<h4>🧑 김원빈</h4>
<a href="https://github.com/diffngood">
    <img src="http://img.shields.io/badge/diffngood-gray?logo=github"/>
</a>
<h4>🧑 문태진</h4>
<a href="https://github.com/TedMoon99">
    <img src="http://img.shields.io/badge/TedMoon99-gray?logo=github"/>
</a>
<h4>🧑 송상민</h4>
<a href="https://github.com/wtoshm">
    <img src="http://img.shields.io/badge/wtoshm-gray?logo=github"/>
</a>

<hr>

📓 노션 링크 : <a href="https://www.notion.so/likelion/4-MRCO-80163a8706e848bdbeef5efccf54fd42">Notion</a>

</br>

📓 피그마 링크 : <a href="https://www.figma.com/file/193zUmGJRnDWxupngbFmW9/4-Team-(MRCO)?type=design&node-id=54795%3A1646&mode=design&t=i2DvXl4SiNKQK6qb-1">Figma</a>

</br>

📓 시연 영상 링크 : <a href="https://youtu.be/1sHPeT0WGFo">Youtube</a>

<hr>

## 1. 프로젝트명 및 주제소개

MRCO는 `"MBTI만 알아와, 코디해줄게"`의 줄임말이며, **MBTI별 선호도**를 기반으로 **맞춤 스타일링 코디 상품**을 판매하는 **쇼핑몰 플랫폼 서비스**입니다. 전문 코디네이터를 통해 특정 MBTI인 사람에게 **잘 어울리는 스타일 코디** 상품과 **특정 MBTI인 사람이 좋아하는 스타일링**을 코디 상품으로 판매합니다.
<img width="1255" alt="스크린샷 2024-09-27 오후 2 15 16" src="https://github.com/user-attachments/assets/7f091227-fc2d-4edc-87ce-626b5d380927" />


## 2. 기획의도

- MBTI, 카테고리 기반의 코디 상품 판매 앱
최근 증가하는 **개인화**에 대한 수요와 **MBTI**를 결합하여, MBTI별 선호하는 스타일에 대한 수요를 충족시키기 위하여 MRCO 앱을 기획하였습니다.
<img width="1205" alt="스크린샷 2024-09-27 오후 2 18 30" src="https://github.com/user-attachments/assets/3130b329-7ba1-4686-b118-4775f13ba3d4" />

|<img width="533" alt="스크린샷 2024-09-27 오후 2 26 46" src="https://github.com/user-attachments/assets/13498221-0af5-4e21-93ba-956c66eb7c32" />|<img width="533" alt="스크린샷 2024-09-27 오후 2 26 08" src="https://github.com/user-attachments/assets/ea463ac9-7219-47f7-9605-9ddf8962e878" />|
|-----------------------------|-----------------------------|
|<img width="706" alt="스크린샷 2024-09-27 오후 2 27 48" src="https://github.com/user-attachments/assets/efa7a679-31ed-4052-a28f-355740d59404" />|<img width="531" alt="스크린샷 2024-09-27 오후 2 28 14" src="https://github.com/user-attachments/assets/6938c5d6-6c2a-47ad-b285-ebf468c1326b" />|
|<img width="696" alt="스크린샷 2024-09-27 오후 2 28 44" src="https://github.com/user-attachments/assets/288f9f8f-8fcd-45ee-a0b8-67d34b32f908" />|<img width="536" alt="스크린샷 2024-09-27 오후 2 29 00" src="https://github.com/user-attachments/assets/0b320bfe-f443-4d80-92b1-97be8ff76a47" />|
|<img width="678" alt="스크린샷 2024-09-27 오후 2 29 25" src="https://github.com/user-attachments/assets/c4b5d4d8-8675-41dd-a77e-209e67ff2ce3" />|<img width="678" alt="스크린샷 2024-09-27 오후 2 29 39" src="https://github.com/user-attachments/assets/ec03bd59-3ee3-4e54-9733-36d791010874" />|

## 3. 개발목표

- 사용자에게 제시하는 목표
사용자에게는 **사용자 맞춤 코디 제안**, **세분화된 검색 및 필터링**, 그리고 **판매 전략 수립을 위한 분석 리포트**의 기능을 이용할 수 있습니다.

i. 구매자는 **사용자 맞춤 코디 제안**을 받을 수 있고, **세분화된 검색 및 필터링 기능**을 통해 찾는 상품이나 스타일 또는 관련된 MBTI에게 추천되는 상품을 빠르고 효율적으로 찾아볼 수 있습니다.

ii. 판매다즌 **판매 전략 수립을 위한 분석 리포트**를 통해 자신의 판매 현황 및 인기 상품 등을 시각화된 자료를 통해 확인하고 다음 판매 전략을 수립할 수 있습니다 
<img width="1005" alt="스크린샷 2024-09-27 오후 2 38 01" src="https://github.com/user-attachments/assets/10235bc8-7da3-4dca-9703-8c7d9fc95da9" />

- 앱 구현에 필요한 기술적 목표
회원가입, 상품 관리, 회원 관리, 코디네이터 관리, 각종 문의 관리, 리뷰 및 댓글 관리, 주문 관리 등을 구현하기 위해 **Firebase Realtime Database**, **Firebase Storage** 사용
분석 리포트 제공을 위한 glide의 **AAChartCore** Library 사용
<img width="1225" alt="스크린샷 2024-09-27 오후 2 40 31" src="https://github.com/user-attachments/assets/c6e56568-a256-4243-a61f-f2f458028f64" />

## 4. 프로젝트 아키텍처

- **MVVM** 구조 사용
<img width="1291" alt="스크린샷 2024-09-27 오후 2 41 21" src="https://github.com/user-attachments/assets/fdb83de6-64bb-4fd8-95c6-262e2472b28c" />


## 5. 앱 기능 소개

|`주문/배송 내역`|`자주 묻는 질문`|
|-----|-----|
|상품명, 주문일자, 주문 상태로 필터링|FAQ 카테고리별 필터링, 제목으로 검색|
|<img width="451" alt="스크린샷 2024-09-27 오후 2 45 02" src="https://github.com/user-attachments/assets/e6b3e23a-a685-4b3e-8128-81cbf2494d13" />|<img width="451" alt="스크린샷 2024-09-27 오후 2 43 50" src="https://github.com/user-attachments/assets/18fbf3d4-451b-46d2-80e4-3eff781974ef" />|

- 코디네이터 상품 등록 CRUD
Firebase Firestore Database를 이용하여 코디네이터가 상품을 **등록**, **조회**, **수정**, **삭제** 할 수 있다

|`상품 등록`|`상품 보기`|
|-----|-----|
|<img width="189" alt="스크린샷 2024-09-27 오후 2 46 39" src="https://github.com/user-attachments/assets/c4423eaa-4b76-488f-a840-5784f9eff1ee" />|<img width="264" alt="스크린샷 2024-09-27 오후 2 46 50" src="https://github.com/user-attachments/assets/5b55483d-bb14-4e9d-94dc-b62692011b57" />|

|`카테고리 검색`|`스타일 필터`||
|-----|-----|-----|
|<img width="245" alt="스크린샷 2024-09-27 오후 2 51 55" src="https://github.com/user-attachments/assets/5f6503f7-78ae-40a4-a1dc-27a873a67639" />|<img width="242" alt="스크린샷 2024-09-27 오후 2 52 27" src="https://github.com/user-attachments/assets/6e24a7c4-fdeb-40e2-b897-326ac065fffd" />|<img width="242" alt="스크린샷 2024-09-27 오후 2 52 58" src="https://github.com/user-attachments/assets/e4a8a7de-36a4-4472-9915-0510b0a25895" />|

- `코디네이터 코디상품 관리`
Firebase Firestore Database를 이용하여 코디네이터가 자신이 등록한 코디상품 확인 가능
<img width="1279" alt="스크린샷 2024-09-27 오후 2 58 47" src="https://github.com/user-attachments/assets/6284a77f-c79e-4169-a936-11fbcb5c873b" />


## 6. 회고

- git을 이용한 협업을 할 때 다른 사람의 코드를 지워버리거나 프로젝트를 완전 먹통으로 만들어 버릴 수 있기에 항상 두려움이 있었는데 이번 팀 프로젝트를 통해 이러한 두려움을 많이 극복한는 계기가 되었다
- Coroutine을 통한 비동기 처리를 통해 DB와 통신하면서 좀 더 자세히 공부하게 되는 계기가 되었고, 비동기 처리에도 다양한 방식이 있다는 점을 알게 되어서 앞으로 새로운 방식으로 비동기 처리를 할 수 있는 시발점이 되었다
- 처음에 MVVM을 공부하면서 이 디자인 패턴에 대해서 제대로 이해를 못 했으나 이번 프로젝트를 통해 확실히 알게 되었다. 앞으로 MVVM을 통한 개발을 더 수월하게 할 수 있다고 생각한다.
