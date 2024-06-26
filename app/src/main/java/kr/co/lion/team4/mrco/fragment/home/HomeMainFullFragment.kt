package kr.co.lion.team4.mrco.fragment.home.coordinator

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.dao.ProductDao
import kr.co.lion.team4.mrco.dao.UserDao
import kr.co.lion.team4.mrco.databinding.FragmentHomeMainFullBinding
import kr.co.lion.team4.mrco.fragment.home.mbti.HomeMbtiFragment
import kr.co.lion.team4.mrco.fragment.home.recommend.HomeRecommendFragment
import kr.co.lion.team4.mrco.model.ProductModel
import kr.co.lion.team4.mrco.model.UserModel

class HomeMainFullFragment : Fragment() {

    // 원빈 - 인기 코디네이터 화면
    lateinit var fragmentHomeMainFullBinding: FragmentHomeMainFullBinding
    lateinit var mainActivity: MainActivity

    // 생성자에서 받은 Bundle을 저장할 변수
    lateinit var bundle: Bundle

    // 모든 상품 정보를 담고 있을 리스트
    var productList = mutableListOf<ProductModel>()
    // 신규 상품 정보를 담고 있을 리스트
    var newProductList = mutableListOf<ProductModel>()
    // 모든 사용자 정보를 담고 있을 리스트
    var userList = mutableListOf<UserModel>()

    // 초기 값 로그인 한 사용자 정보 : [인덱스, 아이디, 이름, MBTI, 성별]
    var loginUserIdx = 0
    lateinit var loginUserId: String
    lateinit var loginUserName: String
    lateinit var loginUserMbti: String
    var loginUserGender = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        fragmentHomeMainFullBinding = FragmentHomeMainFullBinding.inflate(inflater)

        mainActivity = activity as MainActivity

        // getArguments() 메서드를 사용하여 Bundle 가져오기
        val getBundle = arguments
        if (getBundle != null) {
            loginUserIdx = getBundle.getInt("loginUserIdx")
            loginUserId = (getBundle.getString("loginUserId")).toString()
            loginUserName = (getBundle.getString("loginUserName")).toString()
            loginUserMbti = (getBundle.getString("loginUserMbti")).toString()
            loginUserGender = getBundle.getInt("loginUserGender")
            Log.d("test1234", "메인(홈) 페이지 - 로그인 사용자 Idx: $loginUserIdx, Name: $loginUserName")
        }
        // 기본 세팅(로그인 정보 MainActivity 변수 값에 저장)
        settingMainInit()
        // (로그인 정보 MainActivity Bundle에 저장)
        settingMainBundle()
        // 툴바, 하단바, 탭 관련
        viewPagerActiviation()
        settingToolbar()
        bottomSheetSetting()
        settingBottomTabs()
        //
        gettingMainData()


        return fragmentHomeMainFullBinding.root
    }

    // 로그인 된 정보를 mainActivity 변수 값에 넣어준다.
    fun settingMainInit(){
        mainActivity.loginUserIdx = loginUserIdx
        mainActivity.loginUserId = loginUserId
        mainActivity.loginUserName = loginUserName
        mainActivity.loginUserMbti = loginUserMbti
        mainActivity.loginUserGender = loginUserGender
    }

    // 로그인 된 정보를 mainActivity Bundle에 넣어준다.
    fun settingMainBundle(){
        mainActivity.bundle.putInt("loginUserIdx", loginUserIdx)
        mainActivity.bundle.putString("loginUserId", loginUserId)
        mainActivity.bundle.putString("loginUserName", loginUserName)
        mainActivity.bundle.putString("loginUserMbti", loginUserMbti)
        mainActivity.bundle.putInt("loginUserGender", loginUserGender)
    }

    // 툴바 세팅(메인 / 검색, 알림, 장바구니)
    fun settingToolbar() {
        fragmentHomeMainFullBinding.apply {
            toolbarMain.apply {
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        // 검색 클릭 시
                        R.id.home_toolbar_search -> {
                            mainActivity.replaceFragment(MainFragmentName.CATEGORY_SEARCH_FRAGMENT, true, false, null)
                        }
                        // 알람 클릭 시
                        R.id.home_toolbar_notification -> {
                            mainActivity.replaceFragment(MainFragmentName.APP_NOTICE_FRAGMENT, true, true, null)
                        }
                        // 장바구니 클릭 시
                        R.id.home_toolbar_shopping -> {
                            mainActivity.replaceFragment(MainFragmentName.CART_FRAGMENT, true, true, null)
                        }
                    }
                    true
                }
            }
        }
    }

    // 하단 바 설정
    fun bottomSheetSetting() {
        fragmentHomeMainFullBinding.apply {
            mainBottomNavi.setOnItemSelectedListener { item ->
                when(item.itemId) {
                    R.id.main_bottom_navi_home -> {
                        mainActivity.replaceFragment(MainFragmentName.HOME_MAIN_FULL, false, false, mainActivity.bundle)
                    }
                    R.id.main_bottom_navi_category -> {
                        mainActivity.replaceFragment(MainFragmentName.CATEGORY_MAIN_FRAGMENT, false, false, null)
                    }
                    R.id.main_bottom_navi_like -> {
                        mainActivity.replaceFragment(MainFragmentName.LIKE, false, false, null)
                    }
                    else -> {
                        mainActivity.replaceFragment(MainFragmentName.USER_MYPAGE_FRAGMENT, false, false, null)
                    }
                }
                true
            }
        }
    }

    // 하단 바 홈으로 체크 표시 설정
    fun settingBottomTabs() {
        fragmentHomeMainFullBinding.apply {
            val menuItemId = R.id.main_bottom_navi_home
            fragmentHomeMainFullBinding.mainBottomNavi.menu.findItem(menuItemId)?.isChecked = true
        }
    }

    // 상단 탭 viewPager 설정
    private fun viewPagerActiviation(){
        fragmentHomeMainFullBinding.apply {

            // 1. 페이지 데이터를 로드
            val list = listOf(
                HomeRecommendFragment(),
                HomeMbtiFragment(),
                HomeCoordinatorFragment())
            // 2. Adapter 생성
            val pagerAdapter = FragmentPagerAdapter(list, mainActivity)
            // 3. Adapater와 Pager연결
            viewPagerHomeMainFull.adapter = pagerAdapter
            // 4. 탭 메뉴의 갯수만큼 제목을 목록으로 생성
            val titles = listOf("추천", "MBTI별 코디", "코디네이터")
            // 5. 탭 레이아웃과 뷰페이저 연결
            TabLayoutMediator(tabsMain, viewPagerHomeMainFull) { tab, position ->
                tab.text = titles.get(position)
            }.attach()

            // ViewPager 드래그 비활성화
            viewPagerHomeMainFull.isUserInputEnabled = false
        }
    }

    // 상단 탭 viewPager inner class
    private inner class FragmentPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity):
        FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = fragmentList[position]

            // 각 프래그먼트에 전달할 데이터 설정
            val bundle = Bundle().apply {
                putInt("loginUserIdx", loginUserIdx)
            }
            fragment.arguments = bundle

            return fragment
        }
    }

    // 현재 게시판의 데이터를 가져와 메인 화면의 RecyclerView를 갱신한다.
    fun gettingMainData(){
        CoroutineScope(Dispatchers.Main).launch {
            // 상품의 정보를 가져온다. (연동 On)
            // productList = ProductDao.gettingProductAll()
            // Log.d("test1234", "메인(홈) 페이지 - productList: $productList")

            // MBTI와 성별에 맞는 상품의 정보를 가져온다. (연동 On)
            // productList = ProductDao.gettingProductMBTIList(loginUserMbti, loginUserGender)
            // MRCO 신규 코디 최대 20개
            // newProductList = ProductDao.gettingNewProductList(loginUserGender)
            /*
            if (loginUserGender == 1) {
                Log.d("test1234", "메인(홈) 페이지 - (남자, $loginUserMbti) 상품 ${productList.size}개, 신상품 ${newProductList.size}개")
            } else {
                Log.d("test1234", "메인(홈) 페이지 - (여자, $loginUserMbti) 상품 ${productList.size}개, 신상품 ${newProductList.size}개")
            }
            */
            // 사용자 정보를 가져온다. (연동 On)
            // userList = UserDao.getUserAll()
            // Log.d("test1234", "메인(홈) 페이지 - productList: $userList")
        }
    }
}