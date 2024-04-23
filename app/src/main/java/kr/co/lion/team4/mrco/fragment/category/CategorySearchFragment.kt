package kr.co.lion.team4.mrco.fragment.category

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.databinding.FragmentCategorySearchBinding
import kr.co.lion.team4.mrco.databinding.RowCategorySemiCategory2Grid4Binding
import kr.co.lion.team4.mrco.databinding.RowCategorySemiCategoryBinding
import kr.co.lion.team4.mrco.viewmodel.category.CategorySearchViewModel

class CategorySearchFragment : Fragment() {

    lateinit var fragmentCategorySearchBinding: FragmentCategorySearchBinding
    lateinit var mainActivity: MainActivity
    lateinit var categorySearchViewModel: CategorySearchViewModel

    var mbtiData = mutableListOf<String>(
        "ENFJ", "ENFP", "ENTJ", "ENTP", "ESFJ", "ESFP", "ESTJ", "ESTP",
        "INFJ", "INFP", "INTJ", "INTP", "ISFJ", "ISFP", "ISTJ", "ISTP"
    )
    var tpoData = mutableListOf<String>("여행", "데이트", "카페", "출근", "데일리", "캠퍼스", "바다", "결혼식")
    var seasonData = mutableListOf<String>("봄", "여름", "가을", "겨울")
    var moodData = mutableListOf<String>(
        "미니멀",
        "비즈니스 캐주얼",
        "원마일웨어",
        "아메카지",
        "시티보이",
        "스트릿",
        "스포티",
        "레트로",
        "러블리",
        "모던캐주얼"
    )
    var allData = mutableListOf<String>(
        "ENFJ",
        "ENFP",
        "ENTJ",
        "ENTP",
        "ESFJ",
        "ESFP",
        "ESTJ",
        "ESTP",
        "INFJ",
        "INFP",
        "INTJ",
        "INTP",
        "ISFJ",
        "ISFP",
        "ISTJ",
        "ISTP",
        "여행",
        "데이트",
        "카페",
        "출근",
        "데일리",
        "캠퍼스",
        "바다",
        "결혼식",
        "봄",
        "여름",
        "가을",
        "겨울",
        "미니멀",
        "비즈니스\n캐주얼",
        "원마일\n웨어",
        "아메카지",
        "시티보이",
        "스트릿",
        "스포티",
        "레트로",
        "러블리",
        "모던\n캐주얼"
    )

    var checkMbtiCategorySearch = true
    var checkTpoCategorySearch = false
    var checkSeasonCategorySearch = false
    var checkMoodCategorySearch = false
    var checkSearchCategory = false

    // 검색 화면의 RecyclerView 구성을 위한 리스트
    var searchList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentCategorySearchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_category_search, container, false)
        categorySearchViewModel = CategorySearchViewModel()
        fragmentCategorySearchBinding.categorySearchViewModel = categorySearchViewModel
        fragmentCategorySearchBinding.lifecycleOwner = this

        mainActivity = activity as MainActivity

        // 툴바, 하단바, 탭 관련
        bottomSheetSetting()
        settingBottomTabs()

        // 리사이클러 뷰
        settingRecyclerViewCategorySearch()

        // 카테고리 클릭 시
        categoryTextViewClick()

        return fragmentCategorySearchBinding.root
    }


    // SearchView 설정
    fun settingSearch() {
        fragmentCategorySearchBinding.apply {
            searchViewCategorySearch?.apply {
                // 검색시 사용하는 키보드의 엔터키를 누르면 동작하는 리스너
                editText.setOnEditorActionListener { v, actionId, event ->
                    if (event != null && event.action == KeyEvent.ACTION_DOWN) {
                        // 사이드 카테고리 탭을 비활성화
                        resetBackground()
                        checkSearchCategory = true

                        // 검색 결과를 가져와 보여주는 메서드를 호출한다.
                        showSearchData()
                    }
                    false
                }
            }
        }
    }

    fun showSearchData() {
        // 검색어를 가져온다.
        // SearchView에 있는 입력 요소(editText)를 추출하여 사용자가 입력한 내용을 가져온다
        val keyword =
            fragmentCategorySearchBinding.searchViewCategorySearch?.editText?.text.toString()

        // 검색 결과를 가지고 있는 리스트를 비워준다.
        searchList.clear()

        allData.forEach {
            if (it.contains(keyword)) {
                searchList.add(it)
            }
        }
        // 리사이클러 뷰를 갱신한다.
        fragmentCategorySearchBinding.recyclerViewCategorySearch?.adapter?.notifyDataSetChanged()
    }


    // 하단 바 설정
    fun bottomSheetSetting() {
        fragmentCategorySearchBinding.apply {
            mainBottomNavi.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.main_bottom_navi_home -> {
                        mainActivity.replaceFragment(
                            MainFragmentName.HOME_MAIN_FULL,
                            false,
                            false,
                            mainActivity.bundle
                        )
                    }

                    R.id.main_bottom_navi_category -> {
                        mainActivity.replaceFragment(
                            MainFragmentName.CATEGORY_MAIN_FRAGMENT,
                            false,
                            false,
                            null
                        )
                    }

                    R.id.main_bottom_navi_like -> {
                        mainActivity.replaceFragment(MainFragmentName.LIKE, false, false, null)
                    }

                    else -> {
                        mainActivity.replaceFragment(
                            MainFragmentName.USER_MYPAGE_FRAGMENT,
                            false,
                            false,
                            null
                        )
                    }
                }
                true
            }
        }
    }

    // 하단 바 홈으로 체크 표시 설정
    fun settingBottomTabs() {
        fragmentCategorySearchBinding.apply {
            val menuItemId = R.id.main_bottom_navi_category
            fragmentCategorySearchBinding.mainBottomNavi.menu.findItem(menuItemId)?.isChecked = true
        }
    }

    // 리사이클러 뷰 설정
    fun settingRecyclerViewCategorySearch() {
        fragmentCategorySearchBinding.apply {
            recyclerViewCategorySearch?.apply {
                if (checkMbtiCategorySearch) {
                    // 어뎁터 및 레이아웃 매니저 설정
                    adapter = Category2RecyclerViewAdapter()
                    layoutManager = GridLayoutManager(mainActivity, 4)
                } else {
                    // 어뎁터 및 레이아웃 매니저 설정
                    adapter = CategorySearchRecyclerViewAdapter()
                    layoutManager = GridLayoutManager(mainActivity, 2)
                }
            }
        }
    }

    // 리사이클러 뷰 어뎁터
    inner class CategorySearchRecyclerViewAdapter :
        RecyclerView.Adapter<CategorySearchRecyclerViewAdapter.CategorySearchViewHolder>() {
        inner class CategorySearchViewHolder(rowCategorySemiCategoryBinding: RowCategorySemiCategoryBinding) :
            RecyclerView.ViewHolder(rowCategorySemiCategoryBinding.root) {
            val rowCategorySemiCategoryBinding: RowCategorySemiCategoryBinding

            init {
                this.rowCategorySemiCategoryBinding = rowCategorySemiCategoryBinding

                this.rowCategorySemiCategoryBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CategorySearchViewHolder {
            val rowCategorySemiCategoryBinding =
                RowCategorySemiCategoryBinding.inflate(layoutInflater)
            val categorySearchViewHolder = CategorySearchViewHolder(rowCategorySemiCategoryBinding)

            return categorySearchViewHolder
        }

        override fun getItemCount(): Int {
            if (checkTpoCategorySearch) {
                return tpoData.size
            } else if (checkSeasonCategorySearch) {
                return seasonData.size
            } else if (checkMoodCategorySearch) {
                return moodData.size
            } else { // 검색결과를 보여주는 경우
                return searchList.size
            }
        }

        override fun onBindViewHolder(holder: CategorySearchViewHolder, position: Int) {
            if (checkTpoCategorySearch) {
                holder.rowCategorySemiCategoryBinding.textViewSemiCategory.text = tpoData[position]
            } else if (checkSeasonCategorySearch) {
                holder.rowCategorySemiCategoryBinding.textViewSemiCategory.text =
                    seasonData[position]
            } else if (checkMoodCategorySearch) {
                holder.rowCategorySemiCategoryBinding.textViewSemiCategory.text = moodData[position]
            } else { // 검색결과를 보여주는 경우
                holder.rowCategorySemiCategoryBinding.textViewSemiCategory.text =
                    searchList[position]
            }

            // 이미지 클릭시
            holder.rowCategorySemiCategoryBinding.imageViewRowCategorySemiCategory.setOnClickListener {
                mainActivity.replaceFragment(
                    MainFragmentName.CATEGORY_MAIN_FRAGMENT,
                    false,
                    false,
                    null
                )
            }
        }
    }

    // MBTI 리사이클러 뷰 어뎁터
    inner class Category2RecyclerViewAdapter :
        RecyclerView.Adapter<Category2RecyclerViewAdapter.CategorySearchViewHolder>() {
        inner class CategorySearchViewHolder(rowCategorySemiCategory2Grid4Binding: RowCategorySemiCategory2Grid4Binding) :
            RecyclerView.ViewHolder(rowCategorySemiCategory2Grid4Binding.root) {
            val rowCategorySemiCategory2Grid4Binding: RowCategorySemiCategory2Grid4Binding

            init {
                this.rowCategorySemiCategory2Grid4Binding = rowCategorySemiCategory2Grid4Binding

                this.rowCategorySemiCategory2Grid4Binding.root.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CategorySearchViewHolder {
            val rowCategorySemiCategory2Grid4Binding =
                RowCategorySemiCategory2Grid4Binding.inflate(layoutInflater)
            val categorySearchViewHolder =
                CategorySearchViewHolder(rowCategorySemiCategory2Grid4Binding)

            return categorySearchViewHolder
        }

        override fun getItemCount(): Int {
            return mbtiData.size
        }

        override fun onBindViewHolder(holder: CategorySearchViewHolder, position: Int) {
            holder.rowCategorySemiCategory2Grid4Binding.textViewSemiCategory.text =
                mbtiData[position]

            // 이미지 클릭시
            holder.rowCategorySemiCategory2Grid4Binding.imageViewRowCategorySemiCategory.setOnClickListener {
                mainActivity.replaceFragment(
                    MainFragmentName.CATEGORY_MAIN_FRAGMENT,
                    false,
                    false,
                    null
                )
            }
        }
    }

    // 카테고리 사이드 TextView 클릭 시
    fun categoryTextViewClick() {
        fragmentCategorySearchBinding.apply {

            textViewMbtiCategorySearch?.apply {
                setOnClickListener {
                    resetBackground()
                    setBackgroundColor(Color.parseColor("#80CDCDCD"))
                    setTextColor(Color.BLACK)
                    checkMbtiCategorySearch = true
                    settingRecyclerViewCategorySearch()
                    fragmentCategorySearchBinding.recyclerViewCategorySearch?.adapter?.notifyDataSetChanged()
                }
            }

            textViewTpoCategorySearch?.apply {
                setOnClickListener {
                    resetBackground()
                    setBackgroundColor(Color.parseColor("#80CDCDCD"))
                    setTextColor(Color.BLACK)
                    checkTpoCategorySearch = true
                    settingRecyclerViewCategorySearch()
                    fragmentCategorySearchBinding.recyclerViewCategorySearch?.adapter?.notifyDataSetChanged()
                }
            }


            textViewSeasonCategorySearch?.apply {
                setOnClickListener {
                    resetBackground()
                    setBackgroundColor(Color.parseColor("#80CDCDCD"))
                    setTextColor(Color.BLACK)
                    checkSeasonCategorySearch = true
                    settingRecyclerViewCategorySearch()
                    fragmentCategorySearchBinding.recyclerViewCategorySearch?.adapter?.notifyDataSetChanged()
                }
            }

            textViewMoodCategorySearch?.apply {
                setOnClickListener {
                    resetBackground()
                    setBackgroundColor(Color.parseColor("#80CDCDCD"))
                    setTextColor(Color.BLACK)
                    checkMoodCategorySearch = true
                    settingRecyclerViewCategorySearch()
                    fragmentCategorySearchBinding.recyclerViewCategorySearch?.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    // 카테고리 선택 백그라운드 초기화
    fun resetBackground() {
        fragmentCategorySearchBinding.apply {
            textViewMbtiCategorySearch?.setBackgroundColor(Color.WHITE)
            textViewMbtiCategorySearch?.setTextColor(Color.parseColor("#656565"))
            textViewTpoCategorySearch?.setBackgroundColor(Color.WHITE)
            textViewTpoCategorySearch?.setTextColor(Color.parseColor("#656565"))
            textViewSeasonCategorySearch?.setBackgroundColor(Color.WHITE)
            textViewSeasonCategorySearch?.setTextColor(Color.parseColor("#656565"))
            textViewMoodCategorySearch?.setBackgroundColor(Color.WHITE)
            textViewMoodCategorySearch?.setTextColor(Color.parseColor("#656565"))

            checkMbtiCategorySearch = false
            checkTpoCategorySearch = false
            checkSeasonCategorySearch = false
            checkMoodCategorySearch = false
            checkSearchCategory = false
        }
    }
}
