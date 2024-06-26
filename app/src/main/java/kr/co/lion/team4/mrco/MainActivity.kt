package kr.co.lion.team4.mrco

import android.Manifest
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.team4.mrco.dao.LikeDao
import kr.co.lion.team4.mrco.databinding.ActivityMainBinding
import kr.co.lion.team4.mrco.fragment.JoinCoordinatorFragment
import kr.co.lion.team4.mrco.fragment.JoinCoordinatorNextFragment
import kr.co.lion.team4.mrco.fragment.JoinFragment
import kr.co.lion.team4.mrco.fragment.LoginFragment
import kr.co.lion.team4.mrco.fragment.ModifyCoordinatorFragment
import kr.co.lion.team4.mrco.fragment.ModifyUserFragment
import kr.co.lion.team4.mrco.fragment.appNotice.AppNoticeFragment
import kr.co.lion.team4.mrco.fragment.cart.CartFragment
import kr.co.lion.team4.mrco.fragment.category.CategorySearchFragment
import kr.co.lion.team4.mrco.fragment.category.CategoryMainFragment
import kr.co.lion.team4.mrco.fragment.home.coordinator.CoordinatorInfoFragment
import kr.co.lion.team4.mrco.fragment.coordinatormain.CoordinatorMainFragment
import kr.co.lion.team4.mrco.fragment.customerService.CustomerInquiryFragment
import kr.co.lion.team4.mrco.fragment.home.coordinator.CoordinatorRankFragment
import kr.co.lion.team4.mrco.fragment.customerService.CustomerServiceFragment
import kr.co.lion.team4.mrco.fragment.home.coordinator.HomeCoordinatorFragment
import kr.co.lion.team4.mrco.fragment.home.coordinator.HomeMainFullFragment
import kr.co.lion.team4.mrco.fragment.home.coordinator.LikeFragment
import kr.co.lion.team4.mrco.fragment.home.mbti.HomeMbtiFragment
import kr.co.lion.team4.mrco.fragment.home.recommend.HomeRecommendFragment
import kr.co.lion.team4.mrco.fragment.like.LikeCoordinatorFragment
import kr.co.lion.team4.mrco.fragment.like.LikeProductFragment
import kr.co.lion.team4.mrco.fragment.mbtiproductmain.MbtiProductMainFragment
import kr.co.lion.team4.mrco.fragment.mypage.CoordinatorMyPageFragment
import kr.co.lion.team4.mrco.fragment.mypage.UserMyPageFragment
import kr.co.lion.team4.mrco.fragment.order.OrderDetailFragment
import kr.co.lion.team4.mrco.fragment.order.OrderFragment
import kr.co.lion.team4.mrco.fragment.order.OrderHistoryFragment
import kr.co.lion.team4.mrco.fragment.product.purchase.ProductReviewFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoAccessoryFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoAllFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoBottomFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoShoesFragment
import kr.co.lion.team4.mrco.fragment.product.codi.CodiProductInfoTopFragment
import kr.co.lion.team4.mrco.fragment.product.individual.IndividualProductInfoFragment
import kr.co.lion.team4.mrco.fragment.product.management.CodiProductMangementFragment
import kr.co.lion.team4.mrco.fragment.product.management.IndividualProductManagementFragment
import kr.co.lion.team4.mrco.fragment.product.management.ProductManagementFragment
import kr.co.lion.team4.mrco.fragment.product.purchase.ProductDetailFragment
import kr.co.lion.team4.mrco.fragment.product.purchase.ProductFragment
import kr.co.lion.team4.mrco.fragment.product.purchase.ReviewImageMoreFragment
import kr.co.lion.team4.mrco.fragment.productManagement.AddProductFragment
import kr.co.lion.team4.mrco.fragment.productQna.ProductQnaListFragment
import kr.co.lion.team4.mrco.fragment.productQna.RegisterProductQnaFragment
import kr.co.lion.team4.mrco.fragment.productQna.RegisterQnaAnswerFragment
import kr.co.lion.team4.mrco.fragment.review.CreateReviewFragment
import kr.co.lion.team4.mrco.fragment.review.MyReviewFragment
import kr.co.lion.team4.mrco.fragment.review.ReviewCreatedFragment
import kr.co.lion.team4.mrco.fragment.salesManagement.SalesManagementCalendarFragment
import kr.co.lion.team4.mrco.fragment.salesManagement.SalesManagementFragment
import kr.co.lion.team4.mrco.fragment.review.WriteReviewFragment
import kr.co.lion.team4.mrco.fragment.salesManagement.ManageShipmentsFragment
import kr.co.lion.team4.mrco.fragment.salesManagement.SalesListFragment
import kr.co.lion.team4.mrco.fragment.salesManagement.SalesManagementInvoiceReportFragment

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding

    lateinit var bundle: Bundle

    var  loginUserIdx = -1
    lateinit var loginUserId: String
    lateinit var loginUserName: String
    lateinit var loginUserMbti: String
    var  loginUserGender = -1

    // 프레그먼트의 주소 값을 담을 프로퍼티
    var oldFragment: Fragment? = null
    var newFragment: Fragment? = null

    // 확인할 권한 목록
    val permissionList = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        bundle = Bundle()

        // 권한 확인
        requestPermissions(permissionList, 0)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 자동 로그인 관련
        val sharedPreferences = getSharedPreferences("AutoLogin", AppCompatActivity.MODE_PRIVATE)
        loginUserIdx = sharedPreferences.getInt("loginUserIdx", -1)
        loginUserId = (sharedPreferences.getString("loginUserId", null)).toString()
        loginUserName = (sharedPreferences.getString("loginUserName", null)).toString()
        loginUserMbti = (sharedPreferences.getString("loginUserMbti", null)).toString()
        loginUserGender = sharedPreferences.getInt("loginUserGender", -1)

        // 자동 로그인시 저장된 사용자 인덱스값이 없다면(자동로그인을 체크하지 않았다면)
        Log.d("test1234", "MainActivity - 자동 로그인 여부(-1 빼고 전부 자동로그인): $loginUserIdx")
        if(loginUserIdx == -1) {
            // 첫 화면을 띄워준다.
            replaceFragment(MainFragmentName.LOGIN_FRAGMENT, false, false, null)
        }
        else {
            // 로그인한 사용자의 정보를 전달해준다.
            bundle.putInt("loginUserIdx", loginUserIdx)
            bundle.putString("loginUserId", loginUserId)
            bundle.putString("loginUserName", loginUserName)
            bundle.putString("loginUserMbti", loginUserMbti)
            bundle.putInt("loginUserGender", loginUserGender)

            replaceFragment(MainFragmentName.HOME_MAIN_FULL, false, false, bundle)
        }

        // 로그인부터 시작
        // replaceFragment(MainFragmentName.LOGIN_FRAGMENT, false, false, null)
        
        // 메인화면부터 시작
        // replaceFragment(MainFragmentName.HOME_MAIN_FULL, false, false, null)
    }

    // 지정한 Fragment를 보여주는 메서드
    fun replaceFragment(name: MainFragmentName, addToBackStack: Boolean, isAnimate: Boolean, data: Bundle?) {

        // SystemClock.sleep(50)

        // Fragment를 교체할 수 있는 객체를 추출한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // oldFragment에 newFragment가 가지고 있는 Fragment 객체를 담아준다.
        if(newFragment != null){
            oldFragment = newFragment
        }

        when(name){
            // 로그인, 회원가입(사용자, 코디네이터 신청)
            MainFragmentName.LOGIN_FRAGMENT -> newFragment = LoginFragment()
            MainFragmentName.JOIN_FRAGMENT -> newFragment = JoinFragment()
            MainFragmentName.JOIN_COORDINATOR_FRAGMENT -> newFragment = JoinCoordinatorFragment()
            MainFragmentName.JOIN_COORDINATOR_NEXT_FRAGMENT -> newFragment = JoinCoordinatorNextFragment()

            // 회원 정보 수정 (사용자, 코디네이터)
            MainFragmentName.MODIFY_USER_FRAGMENT -> newFragment = ModifyUserFragment()
            MainFragmentName.MODIFY_COORDINATOR_FRAGMENT -> newFragment = ModifyCoordinatorFragment()

            // 알림 화면
            MainFragmentName.APP_NOTICE_FRAGMENT -> newFragment = AppNoticeFragment()

            // 장바구니 화면
            MainFragmentName.CART_FRAGMENT -> newFragment = CartFragment()

            // 결제 화면
            MainFragmentName.ORDER_FRAGMENT -> newFragment = OrderFragment()

            // 홈 메인 (첫 화면 - 추천, MBTI 별 코디, 인기 코디네이터, 코디네이터 소개) 순서
            MainFragmentName.HOME_MAIN_FULL -> newFragment = HomeMainFullFragment()
            MainFragmentName.HOME_RECOMMEND -> newFragment = HomeRecommendFragment()
            MainFragmentName.HOME_MBTI -> newFragment = HomeMbtiFragment()
            MainFragmentName.HOME_COORDINATOR -> newFragment = HomeCoordinatorFragment()
            MainFragmentName.HOME_COORDINATOR_RANK -> newFragment = CoordinatorRankFragment()
            MainFragmentName.HOME_COORDINATOR_INFO -> newFragment = CoordinatorInfoFragment()

            // 메인 MBTI 상품 페이지 와 메인 코디네이터 상세 페이지
            MainFragmentName.MBTI_PRODUCT_MAIN -> newFragment = MbtiProductMainFragment()
            MainFragmentName.COORDINATOR_MAIN -> newFragment = CoordinatorMainFragment()

            // 상품 구매 페이지
            MainFragmentName.PRODUCT_FRAGMENT -> newFragment = ProductFragment()
            MainFragmentName.PRODUCT_DETAIL_FRAGMENT -> newFragment = ProductDetailFragment()
            MainFragmentName.REVIEW_IMAGE_MORE_FRAGMENT -> newFragment = ReviewImageMoreFragment()


            // 카테고리 (카테고리 선택, 메인) 화면
            MainFragmentName.CATEGORY_SEARCH_FRAGMENT -> newFragment = CategorySearchFragment()
            MainFragmentName.CATEGORY_MAIN_FRAGMENT -> newFragment = CategoryMainFragment()

            // 좋아요 화면 (코디, 코디네이터)
            MainFragmentName.LIKE -> newFragment = LikeFragment()
            MainFragmentName.LIKE_PRODUCT -> newFragment = LikeProductFragment()
            MainFragmentName.LIKE_COORDINATOR -> newFragment = LikeCoordinatorFragment()

            // 마이페이지 (사용자, 코디네이터) 화면
            MainFragmentName.USER_MYPAGE_FRAGMENT -> newFragment = UserMyPageFragment()
            MainFragmentName.COORDINATOR_MYPAGE_FRAGMENT -> newFragment = CoordinatorMyPageFragment()

            // 주문/배송 조회 및 주문 상세 정보
            MainFragmentName.ORDER_HISTORY_FRAGMENT -> newFragment = OrderHistoryFragment()
            MainFragmentName.ORDER_DETAIL -> newFragment = OrderDetailFragment()

            // 상품 리뷰, 리뷰 작성 화면
            MainFragmentName.MY_REVIEW -> newFragment = MyReviewFragment()
            MainFragmentName.WRITE_REVIEW -> newFragment = WriteReviewFragment()

            // 상품 리뷰 페이지 안에, 리뷰 작성, 작성한 리뷰 페이지, 리뷰 리사이클러
            MainFragmentName.FRAGMENT_PRODUCT_REVIEW -> newFragment = ProductReviewFragment()
            MainFragmentName.FRAGMENT_CREATE_REVIEW_FRAGMENT -> newFragment = CreateReviewFragment()
            MainFragmentName.FRAGMENT_REVIEW_CREATED -> newFragment = ReviewCreatedFragment()

            // 고객센터 화면 + 1:1 문의 작성 화면
            MainFragmentName.CUSTOMER_SERVICE_FRAGMENT -> newFragment = CustomerServiceFragment()
            MainFragmentName.CUSTOMER_INQUIRY_FRAGMENT -> newFragment = CustomerInquiryFragment()

            // 상품 1:1문의 작성화면
            MainFragmentName.REGISTER_PRODUCT_QNA_FRAGMENT -> newFragment = RegisterProductQnaFragment()

            // 판매자 - 매출관리(리포트, 캘린더, 내역) 화면
            MainFragmentName.SALES_MANAGEMENT -> newFragment = SalesManagementFragment()

            // 판매자 - 배송관리 화면
            MainFragmentName.MANAGE_SHIPMENT_FRAGMENT -> newFragment = ManageShipmentsFragment()

            // 판매자 - 판매 내역 관리 화면
            MainFragmentName.SALES_LIST_FRAGMENT -> newFragment = SalesListFragment()

            // 판매자 - 상품 문의 내역 화면
            MainFragmentName.PRODUCT_QNA_LIST_FRAGMENT -> newFragment = ProductQnaListFragment()

            // 상품 문의 답변 등록 화면
            MainFragmentName.REGISTER_QNA_ANSWER_FRAGMENT -> newFragment = RegisterQnaAnswerFragment()

            // 판매자 - 코디 상품등록 화면
            MainFragmentName.ADD_PRODUCT_FRAGMENT -> newFragment = AddProductFragment()

            // 판매자 - 등록상품 관리
            MainFragmentName.FRAGMENT_PRODUCT_MANAGEMENT -> newFragment = ProductManagementFragment()

            // 코디 상품 관리, 개별 상품 관리 탭
            MainFragmentName.FRAGMENT_CODI_PRODUCT_MANAGEMENT -> newFragment = CodiProductMangementFragment()
            MainFragmentName.FRAGMENT_INDIVIDUAL_PRODUCT_MANAGEMENT -> newFragment = IndividualProductManagementFragment()

            // 코디 상품 관리 - 상세
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO -> newFragment = CodiProductInfoFragment()
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO_ALL -> newFragment = CodiProductInfoAllFragment()
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO_TOP -> newFragment = CodiProductInfoTopFragment()
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO_BOTTOM -> newFragment = CodiProductInfoBottomFragment()
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO_SHOES -> newFragment = CodiProductInfoShoesFragment()
            MainFragmentName.FRAGMENT_CODI_PRODUCT_INFO_ACCESSORY -> newFragment = CodiProductInfoAccessoryFragment()

            // 개별 상품 관리 - 상세
            MainFragmentName.FRAGMENT_INDIVIDUAL_PRODUCT_INFO -> newFragment = IndividualProductInfoFragment()

            // 상품리뷰 화면
            MainFragmentName.FRAGMENT_MY_REVIEW -> newFragment = MyReviewFragment()
        }

        // 새로운 Fragment에 전달할 객체가 있다면 arguments 프로퍼티에 넣어준다.
        if(data != null){
            newFragment?.arguments = data
        }

        if(newFragment != null){

            // 애니메이션 설정
            if(isAnimate == true){

                if(oldFragment != null){
                    // old에서 new가 새롭게 보여질 때 old의 애니메이션
                    oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                    // new에서 old로 되돌아갈때 old의 애니메이션
                    oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

                    oldFragment?.enterTransition = null
                    oldFragment?.returnTransition = null
                }

                // old에서 new가 새롭게 보여질 때 new의 애니메이션
                newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                // new에서 old로 되돌아갈때 new의 애니메이션
                newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

                newFragment?.exitTransition = null
                newFragment?.reenterTransition = null
            }

            // Fragment를 교체한다.(이전 Fragment가 없으면 새롭게 추가하는 역할을 수행한다)
            fragmentTransaction.replace(R.id.containerMain, newFragment!!)

            // addToBackStack 변수의 값이 true면 새롭게 보여질 Fragment를 BackStack에 포함시켜 준다.
            if(addToBackStack == true){
                // BackStack 포함 시킬때 이름을 지정해주면 원하는 Fragment를 BackStack에서 제거할 수 있다.
                fragmentTransaction.addToBackStack(name.str)
            }
            // Fragment 교체를 확정한다.
            fragmentTransaction.commit()
        }
    }


    // BackStack에서 Fragment를 제거한다.
    fun removeFragment(name: MainFragmentName){
        SystemClock.sleep(50)

        // 지정한 이름으로 있는 Fragment를 BackStack에서 제거한다.
        supportFragmentManager.popBackStack(name.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutContent)

            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers()
            } else {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
    }
}