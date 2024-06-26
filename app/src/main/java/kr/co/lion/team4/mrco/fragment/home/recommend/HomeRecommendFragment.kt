package kr.co.lion.team4.mrco.fragment.home.recommend

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.Tools
import kr.co.lion.team4.mrco.dao.CoordinatorDao
import kr.co.lion.team4.mrco.dao.LikeDao
import kr.co.lion.team4.mrco.dao.ProductDao
import kr.co.lion.team4.mrco.databinding.FragmentHomeRecommendBinding
import kr.co.lion.team4.mrco.databinding.RowHomeRecommendBannerBinding
import kr.co.lion.team4.mrco.databinding.RowHomeRecommendBinding
import kr.co.lion.team4.mrco.databinding.RowHomeRecommendNewCoordiBinding
import kr.co.lion.team4.mrco.fragment.home.coordinator.HomeMainFullFragment
import kr.co.lion.team4.mrco.model.CoordinatorModel
import kr.co.lion.team4.mrco.model.LikeModel
import kr.co.lion.team4.mrco.model.ProductModel
import kr.co.lion.team4.mrco.viewmodel.coordinator.CoordinatorRankViewModel
import kr.co.lion.team4.mrco.viewmodel.home.recommend.HomeRecommendViewModel
import kr.co.lion.team4.mrco.viewmodel.home.recommend.RowHomeRecommendBannerViewModel
import kr.co.lion.team4.mrco.viewmodel.home.recommend.RowHomeRecommendNewCoordiViewModel
import kr.co.lion.team4.mrco.viewmodel.home.recommend.RowHomeRecommendViewModel
import java.text.NumberFormat
import java.util.Locale

class HomeRecommendFragment : Fragment() {

    lateinit var fragmentHomeRecommendBinding: FragmentHomeRecommendBinding
    lateinit var mainActivity: MainActivity

    lateinit var homeRecommendViewModel: HomeRecommendViewModel

    lateinit var snapHelper: SnapHelper

    // MBTI 추천 상품 정보를 담고 있을 리스트
    var recommendProductList = mutableListOf<ProductModel>()
    // 신규 상품 정보를 담고 있을 리스트
    var newProductList = mutableListOf<ProductModel>()
    // 코디네이터 인덱스와 이름 정보를 담고 있을 맵
    var coordinatorMap = mutableMapOf<Int, String>()

    // 모든 코디네이터의 팔로우 정보를 담고 있을 리스트
    var coordinatorsFollowList = mutableListOf<LikeModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        fragmentHomeRecommendBinding = FragmentHomeRecommendBinding.inflate(inflater)
        fragmentHomeRecommendBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_recommend, container, false)
        homeRecommendViewModel = HomeRecommendViewModel()
        fragmentHomeRecommendBinding.homeRecommendViewModel = HomeRecommendViewModel()
        fragmentHomeRecommendBinding.lifecycleOwner = this

        mainActivity = activity as MainActivity

        // 초기 세팅
        gettingCoordinatorName()
        gettingRecommendProductData(mainActivity.loginUserGender)
        gettingNewProductData(mainActivity.loginUserGender)
        gettingCoordinatorsFollowData()

        // 버튼
        settingButton()

        // 리사이클러 뷰
        settingRecyclerViewHomeRecommendBanner()
        settingRecyclerViewHomeRecommend()
        settingRecyclerViewHomeRecommendNewCoordi()
        settingInit()

        // 추천 코디 상품(첫번째) TextView 관찰
        homeRecommendViewModel.textViewHomeRecommendContent1.observe(viewLifecycleOwner) { text ->
            // MBTI TextView 업데이트
            fragmentHomeRecommendBinding.homeRecommendContent1.text = text
        }

        return fragmentHomeRecommendBinding.root
    }

    fun settingInit() {
        if (mainActivity.loginUserIdx != -1){
            homeRecommendViewModel.textViewHomeRecommendContent1.value = "${mainActivity.loginUserName}(${mainActivity.loginUserMbti}"
        } else {
            homeRecommendViewModel.textViewHomeRecommendContent1.value = "ENFP"
            fragmentHomeRecommendBinding.textViewSideContent1.setText(" 추천코디")
        }
    }

    fun settingButton(){
        fragmentHomeRecommendBinding.apply {
            homeRecommendAllButton.apply {
                setOnClickListener {
                    mainActivity.replaceFragment(MainFragmentName.CATEGORY_MAIN_FRAGMENT, true, false, null)
                }
            }
        }
    }

    // 홈(추천) - 배너 리사이클러 뷰 설정
    fun settingRecyclerViewHomeRecommendBanner() {
        fragmentHomeRecommendBinding.apply {
            homeRecommendBanner.apply {
                // 어뎁터 및 레이아웃 매니저 설정
                adapter = HomeRecommendBannerRecyclerViewAdapter()

                snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)
            }
        }
    }

    // 홈(추천) - 추천 코디 리사이클러 뷰 설정
    fun settingRecyclerViewHomeRecommend() {
        fragmentHomeRecommendBinding.apply {
            homeRecommendRecycler.apply {
                // 어뎁터 및 레이아웃 매니저 설정
                adapter = HomeRecommendRecyclerViewAdapter()
            }
        }
    }

    // 홈(추천) - 신규 코디 리사이클러 뷰 설정
    fun settingRecyclerViewHomeRecommendNewCoordi() {
        fragmentHomeRecommendBinding.apply {
            homeRecommendNewRecycler.apply {
                // 어뎁터 및 레이아웃 매니저 설정
                adapter = HomeRecommendNewCoordiRecyclerViewAdapter()
            }
        }
    }

    // 홈(추천) - 배너 리사이클러 뷰 어뎁터
    inner class HomeRecommendBannerRecyclerViewAdapter: RecyclerView.Adapter<HomeRecommendBannerRecyclerViewAdapter.HomeRecommendBannerViewHolder>(){
        inner class HomeRecommendBannerViewHolder(rowHomeRecommendBannerBinding: RowHomeRecommendBannerBinding): RecyclerView.ViewHolder(rowHomeRecommendBannerBinding.root){
            val rowHomeRecommendBannerBinding: RowHomeRecommendBannerBinding

            init {
                this.rowHomeRecommendBannerBinding = rowHomeRecommendBannerBinding

                this.rowHomeRecommendBannerBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecommendBannerViewHolder {
            val rowHomeRecommendBannerBinding = DataBindingUtil.inflate<RowHomeRecommendBannerBinding>(
                layoutInflater, R.layout.row_home_recommend_banner, parent, false
            )
            val rowHomeRecommendBannerViewModel = RowHomeRecommendBannerViewModel()
            rowHomeRecommendBannerBinding.rowHomeRecommendBannerViewModel = rowHomeRecommendBannerViewModel
            rowHomeRecommendBannerBinding.lifecycleOwner = this@HomeRecommendFragment

            val homeRecommendBannerViewHolder = HomeRecommendBannerViewHolder(rowHomeRecommendBannerBinding)

            return homeRecommendBannerViewHolder
        }

        override fun getItemCount(): Int {
            return 6
        }

        override fun onBindViewHolder(holder: HomeRecommendBannerViewHolder, position: Int) {
            holder.rowHomeRecommendBannerBinding.textViewBannerPage.text = "${position+1}/6"
        }
    }

    // 홈(추천) - 추천 코디 리사이클러 뷰 어뎁터
    inner class HomeRecommendRecyclerViewAdapter: RecyclerView.Adapter<HomeRecommendRecyclerViewAdapter.HomeRecommendViewHolder>(){
        inner class HomeRecommendViewHolder(rowHomeRecommendBinding: RowHomeRecommendBinding): RecyclerView.ViewHolder(rowHomeRecommendBinding.root){
            val rowHomeRecommendBinding: RowHomeRecommendBinding

            init {
                this.rowHomeRecommendBinding = rowHomeRecommendBinding

                this.rowHomeRecommendBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecommendViewHolder {
            val rowHomeRecommendBinding = DataBindingUtil.inflate<RowHomeRecommendBinding>(
                layoutInflater, R.layout.row_home_recommend, parent, false
            )
            val rowHomeRecommendViewModel = RowHomeRecommendViewModel()
            rowHomeRecommendBinding.rowHomeRecommendViewModel = rowHomeRecommendViewModel
            rowHomeRecommendBinding.lifecycleOwner = this@HomeRecommendFragment

            val homeRecommendViewHolder = HomeRecommendViewHolder(rowHomeRecommendBinding)

            return homeRecommendViewHolder
        }

        override fun getItemCount(): Int {
            return recommendProductList.size
        }

        override fun onBindViewHolder(holder: HomeRecommendViewHolder, position: Int) {
            // position 값에 따라 다른 이미지 설정
            val imageResource = when (position % 4) {
                0 -> R.drawable.iu_image4
                1 -> R.drawable.iu_image5
                2 -> R.drawable.iu_image6
                else -> R.drawable.iu_image7
            }
            holder.rowHomeRecommendBinding.itemMainProductThumbnail.setImageResource(imageResource)

            // 좋아요 상태 초기 세팅
            for (i in 0 until coordinatorsFollowList.size) {
                for (j in 0 until (coordinatorsFollowList[i].like_product_idx).size) {
                    if (coordinatorsFollowList[i].like_product_idx[j] == recommendProductList[position].productIdx) {
                        holder.rowHomeRecommendBinding.itemMainProductPickButton1.apply {
                            isChecked = true
                        }
                    }
                }
            }
            // 하트 모양(좋아요) 버튼 클릭 시
            holder.rowHomeRecommendBinding.itemMainProductPickButton1.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    CoroutineScope(Dispatchers.Main).launch {
                        LikeDao.insertLikeProductData(mainActivity.loginUserIdx, recommendProductList[position].productIdx)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        LikeDao.deleteLikeProductData(mainActivity.loginUserIdx, recommendProductList[position].productIdx)
                    }
                }
            }

            holder.rowHomeRecommendBinding.textViewRowHomeRecommendMBTI.setBackgroundColor(Color.parseColor(
                Tools.mbtiColor(recommendProductList[position].coordiMBTI)))
            holder.rowHomeRecommendBinding.textViewRowHomeRecommendMBTI.text = "${recommendProductList[position].coordiMBTI}"
            // 해당 코디네이터의 이름
            holder.rowHomeRecommendBinding.itemMainCoordinatorName.text = "${coordinatorMap[recommendProductList[position].coordinatorIdx]}"
            // 해당 코디 상품의 이름
            holder.rowHomeRecommendBinding.itemMainProductName.text = "${recommendProductList[position].coordiName}"
            // 해당 코디 상품의 가격
            holder.rowHomeRecommendBinding.itemMainProductPrice.text =
                "￦${NumberFormat.getNumberInstance(Locale.getDefault()).format(recommendProductList[position].price)}"

            // 해당 코디 상품의 할인률 0이면 표시안함
            if (recommendProductList[position].productDiscoutPrice == 0) {
                holder.rowHomeRecommendBinding.itemMainProductDiscountPercent.text = ""
            } else {
                holder.rowHomeRecommendBinding.itemMainProductDiscountPercent.text = "${recommendProductList[position].productDiscoutPrice}%  "
            }

            holder.rowHomeRecommendBinding.itemMainProductThumbnail.setOnClickListener {
                mainActivity.replaceFragment(MainFragmentName.PRODUCT_FRAGMENT,true,true,null)
            }
        }
    }

    // 홈(추천) - 신규 코디 리사이클러 뷰 어뎁터
    inner class HomeRecommendNewCoordiRecyclerViewAdapter: RecyclerView.Adapter<HomeRecommendNewCoordiRecyclerViewAdapter.HomeRecommendNewCoordiViewHolder>(){
        inner class HomeRecommendNewCoordiViewHolder(rowHomeRecommendNewCoordiBinding: RowHomeRecommendNewCoordiBinding): RecyclerView.ViewHolder(rowHomeRecommendNewCoordiBinding.root){
            val rowHomeRecommendNewCoordiBinding: RowHomeRecommendNewCoordiBinding

            init {
                this.rowHomeRecommendNewCoordiBinding = rowHomeRecommendNewCoordiBinding

                this.rowHomeRecommendNewCoordiBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecommendNewCoordiViewHolder {
            // val rowCoordinatorInfoBinding = RowCoordinatorInfoBinding.inflate(layoutInflater)

            val rowHomeRecommendNewCoordiBinding = DataBindingUtil.inflate<RowHomeRecommendNewCoordiBinding>(
                layoutInflater, R.layout.row_home_recommend_new_coordi, parent, false
            )
            val rowHomeRecommendNewCoordiViewModel = RowHomeRecommendNewCoordiViewModel()
            rowHomeRecommendNewCoordiBinding.rowHomeRecommendNewCoordiViewModel = rowHomeRecommendNewCoordiViewModel
            rowHomeRecommendNewCoordiBinding.lifecycleOwner = this@HomeRecommendFragment


            val homeRecommendNewCoordiViewHolder = HomeRecommendNewCoordiViewHolder(rowHomeRecommendNewCoordiBinding)

            return homeRecommendNewCoordiViewHolder
        }

        override fun getItemCount(): Int {
            return newProductList.size
        }

        override fun onBindViewHolder(holder: HomeRecommendNewCoordiViewHolder, position: Int) {
            // position 값에 따라 다른 이미지 설정
            val imageResource = when (position % 4) {
                0 -> R.drawable.iu_image2
                1 -> R.drawable.iu_image3
                2 -> R.drawable.iu_image5
                else -> R.drawable.iu_image8
            }
            holder.rowHomeRecommendNewCoordiBinding.itemMainProductThumbnail3.setImageResource(imageResource)

            // 좋아요 상태 초기 세팅
            for (i in 0 until coordinatorsFollowList.size) {
                for (j in 0 until (coordinatorsFollowList[i].like_product_idx).size) {
                    if (coordinatorsFollowList[i].like_product_idx[j] == newProductList[position].productIdx) {
                        holder.rowHomeRecommendNewCoordiBinding.itemMainProductPickButton3.apply {
                            isChecked = true
                        }
                    }
                }
            }
            // 하트 모양(좋아요) 버튼 클릭 시
            holder.rowHomeRecommendNewCoordiBinding.itemMainProductPickButton3.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    CoroutineScope(Dispatchers.Main).launch {
                        LikeDao.insertLikeProductData(mainActivity.loginUserIdx, newProductList[position].productIdx)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        LikeDao.deleteLikeProductData(mainActivity.loginUserIdx, newProductList[position].productIdx)
                    }
                }
            }

            holder.rowHomeRecommendNewCoordiBinding.itemMainProductMbti3.setBackgroundColor(Color.parseColor(
                Tools.mbtiColor(newProductList[position].coordiMBTI)))
            holder.rowHomeRecommendNewCoordiBinding.itemMainProductMbti3.text = "${newProductList[position].coordiMBTI}"
            // 해당 코디네이터의 이름
            holder.rowHomeRecommendNewCoordiBinding.itemMainCoordinatorName3.text =
                "${coordinatorMap[newProductList[position].coordinatorIdx]}"
            // 해당 코디 상품의 이름
            holder.rowHomeRecommendNewCoordiBinding.itemMainProductName3.text = "${newProductList[position].coordiName}"
            // 해당 코디 상품의 가격
            holder.rowHomeRecommendNewCoordiBinding.itemMainProductPrice3.text =
                "￦${NumberFormat.getNumberInstance(Locale.getDefault()).format(newProductList[position].price)}"

            // 해당 코디 상품의 할인률 0이면 표시안함
            if (newProductList[position].productDiscoutPrice == 0) {
                holder.rowHomeRecommendNewCoordiBinding.itemMainProductDiscountPercent3.text = ""
            } else {
                holder.rowHomeRecommendNewCoordiBinding.itemMainProductDiscountPercent3.text = "${newProductList[position].productDiscoutPrice}%  "
            }

            holder.rowHomeRecommendNewCoordiBinding.itemMainProductThumbnail3.setOnClickListener {
                mainActivity.replaceFragment(MainFragmentName.PRODUCT_FRAGMENT,true,true,null)
            }
        }
    }

    // 인기 상품의 데이터를 가져와 메인 화면의 RecyclerView를 갱신한다.
    fun gettingRecommendProductData(gender: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            // MBTI와 성별에 맞는 상품의 정보를 가져온다. (연동 On)
            recommendProductList = ProductDao.gettingRecommendProductList(gender)
            if (mainActivity.loginUserGender == 1) {
                Log.d("test1234", "메인(홈) 페이지 - (남자)추천 코디: ${recommendProductList.size}개")
            } else {
                Log.d("test1234", "메인(홈) 페이지 - (여자)추천 코디: ${recommendProductList.size}개")
            }
            fragmentHomeRecommendBinding.homeRecommendRecycler.adapter?.notifyDataSetChanged()
        }
    }

    // 신규 상품의 데이터를 가져와 메인 화면의 RecyclerView를 갱신한다.
    fun gettingNewProductData(gender: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            // MBTI와 성별에 맞는 상품의 정보를 가져온다. (연동 On)
            newProductList = ProductDao.gettingNewProductList(gender)
            if (mainActivity.loginUserGender == 1) {
                Log.d("test1234", "메인(홈) 페이지 - (남자)신규 코디: ${newProductList.size}개")
            } else {
                Log.d("test1234", "메인(홈) 페이지 - (여자)신규 코디: ${newProductList.size}개")
            }
            fragmentHomeRecommendBinding.homeRecommendNewRecycler.adapter?.notifyDataSetChanged()
        }
    }

    // 코디네이터의 이름을 가져온다.
    fun gettingCoordinatorName() {
        CoroutineScope(Dispatchers.Main).launch {
            // MBTI와 성별에 맞는 상품의 정보를 가져온다. (연동 On)
            coordinatorMap = CoordinatorDao.getCoordinatorName()
            resetRecyclerView()
        }
    }

    // 모든 코디네이터의 팔로우 상태 데이터를 가져와 메인 화면의 RecyclerView를 갱신한다.
    fun gettingCoordinatorsFollowData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 모든 코디네이터의 팔로우 상태 정보를 가져온다. (연동 On)
            coordinatorsFollowList = LikeDao.getLikeData(mainActivity.loginUserIdx)
            // Log.d("test1234", "메인(홈) 페이지 - coordinatorsFollowList: ${coordinatorsFollowList[0].like_coordinator_idx.size}명")
            resetRecyclerView()
        }
    }

    // 상품 리사이클러 뷰 2개 다 갱신
    fun resetRecyclerView(){
        fragmentHomeRecommendBinding.apply {
            homeRecommendRecycler.adapter?.notifyDataSetChanged()
            homeRecommendNewRecycler.adapter?.notifyDataSetChanged()
        }
    }
}