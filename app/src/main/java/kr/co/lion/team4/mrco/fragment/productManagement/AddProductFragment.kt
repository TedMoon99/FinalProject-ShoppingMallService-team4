package kr.co.lion.team4.mrco.fragment.productManagement

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.team4.mrco.CategoryId
import kr.co.lion.team4.mrco.CategoryIdSubMOOD
import kr.co.lion.team4.mrco.CategoryIdSubMOOD.*
import kr.co.lion.team4.mrco.CategoryIdSubSEASON
import kr.co.lion.team4.mrco.CategoryIdSubSEASON.FALL
import kr.co.lion.team4.mrco.CategoryIdSubSEASON.SPRING
import kr.co.lion.team4.mrco.CategoryIdSubSEASON.SUMMER
import kr.co.lion.team4.mrco.CategoryIdSubSEASON.WINTER
import kr.co.lion.team4.mrco.CategoryIdSubTPO
import kr.co.lion.team4.mrco.CategoryIdSubTPO.*
import kr.co.lion.team4.mrco.CodiMbti
import kr.co.lion.team4.mrco.Gender
import kr.co.lion.team4.mrco.viewmodel.productManagement.AddProductDetailViewModel
import kr.co.lion.team4.mrco.viewmodel.productManagement.AddProductViewModel
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.ProductState
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.databinding.FragmentAddProductBinding
import kr.co.lion.team4.mrco.databinding.ItemAddproductDetailBinding
import kr.co.lion.team4.mrco.databinding.ItemAddproductPhotoBinding
import java.text.SimpleDateFormat
import java.util.Date
import kr.co.lion.team4.mrco.Tools
import kr.co.lion.team4.mrco.dao.ProductDao
import kr.co.lion.team4.mrco.model.ProductModel

/* (판매자) 코디 상품 등록 화면 */

// 다이얼로그 코디상품(개별) 등록 인터페이스
interface AddProductDialogListener {
    fun onAddProductClicked(productData: Map<String, String>, isAddPicture: Boolean)
}

class AddProductFragment : Fragment(), AddProductDialogListener {
    lateinit var fragmentAddProductBinding: FragmentAddProductBinding
    lateinit var addProductViewModel: AddProductViewModel

    lateinit var mainActivity: MainActivity

    private val individualProductData: ArrayList<Map<String, String>> = ArrayList()

    // Activity 실행을 위한 런처
    lateinit var albumLauncher: ActivityResultLauncher<Intent>

    // 개별 상품 추가 -> 이미지를 첨부한 적이 있는지...
    var isProductAddPicture = false

    // 첨부 이미지 인덱스
    var imageIdx = -1

    // 첨부 이미지 리스트
    val imageProductList = mapOf<Int, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentAddProductBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_product, container, false)
        addProductViewModel = AddProductViewModel()
        fragmentAddProductBinding.addProductViewModel = addProductViewModel
        fragmentAddProductBinding.lifecycleOwner = this

        mainActivity = activity as MainActivity

        // setting toolbar, bottom navigation
        settingToolbarAddProduct()

        // 입력 요소 초기화
        settingInputForm()

        // 카테고리 눌렀을때
        settingCategoryClickEvent()

        // 추가 등록하기 버튼 눌렀을때
        settingButtonAddProductDetail()

        // 최하단 (취소, 등록) 버튼
        settingBottomButton()

        // 리사이클러뷰 어댑터
        settingAddProductPhotoRecyclerView()
        settingAddProductDetailRecyclerView()

        return fragmentAddProductBinding.root
    }

    // setting toolbar
    fun settingToolbarAddProduct(){
        fragmentAddProductBinding.toolbarAddProduct.apply {
            setNavigationOnClickListener {
                backProcess()
            }
        }
    }

    // 코디 상품 및 재고 등록
    fun settingButtonAddProductDetail(){
        fragmentAddProductBinding.buttonAddProductDetail.setOnClickListener {
            val width = getDeviceWidth()
            val dialog = AddProductDialog(width!!)
            // 다이얼로그페이지 리스너 등록
            dialog.setListener(this)
            dialog.show(childFragmentManager, "AddProductDialog")
        }
    }

    // 카테고리 클릭 이벤트 설정
    fun settingCategoryClickEvent(){
        showSubCategoryTPO()
        showSubCategorySeason()
        showSubCategoryMood()
    }

    // 카테고리 TPO 선택하면 하위 카테고리 보이게 표시
    fun showSubCategoryTPO(){
        fragmentAddProductBinding.apply {
            chipAddProductTpo.setOnCheckedChangeListener { chip, isChecked ->
                // 체크된 상태
                if(isChecked){
                    textviewAddProductSubCategory.visibility = View.VISIBLE
                    chipgroupAddProductTpoSub.visibility = View.VISIBLE
                }
                // 체크 해제된 상태
                else{
                    textviewAddProductSubCategory.visibility = View.GONE
                    chipgroupAddProductTpoSub.visibility = View.GONE
                }
            }
        }
    }

    // 카테고리 SEASON 선택하면 하위 카테고리 보이게 표시
    fun showSubCategorySeason(){
        fragmentAddProductBinding.apply {
            chipAddProductSeason.setOnCheckedChangeListener { chip, isChecked ->
                // 체크된 상태
                if(isChecked){
                    textviewAddProductSubCategory.visibility = View.VISIBLE
                    chipgroupAddProductSeasonSub.visibility = View.VISIBLE
                }else{
                    textviewAddProductSubCategory.visibility = View.GONE
                    chipgroupAddProductSeasonSub.visibility = View.GONE
                }
            }
        }
    }

    // 카테고리 MOOD 선택하면 하위 카테고리 보이게 표시
    fun showSubCategoryMood(){
        fragmentAddProductBinding.apply {
            chipAddProductMood.setOnCheckedChangeListener { chip, isChecked ->
                // 체크된 상태
                if (isChecked){
                    textviewAddProductSubCategory.visibility = View.VISIBLE
                    chipgroupAddProductMoodSub.visibility = View.VISIBLE
                } else{
                    textviewAddProductSubCategory.visibility = View.GONE
                    chipgroupAddProductMoodSub.visibility = View.GONE
                }
            }
        }
    }

    // 취소 버튼
    fun settingBottomButton(){
        fragmentAddProductBinding.apply {
            // 취소 버튼
            buttonAddProductCancel.setOnClickListener {
                backProcess()
            }
            // 등록 버튼
            buttonAddProductSubmit.setOnClickListener {
                // 입력 요소 유효성 검사
                val chk = checkInputForm()

                if(chk == true) {
                    Log.d("test1234", "${individualProductData}")
                    uploadProductData()
                }
            }
        }
    }

    // 이전 화면으로 돌아가기
    fun backProcess(){
        mainActivity.removeFragment(MainFragmentName.ADD_PRODUCT_FRAGMENT)
    }

    // 코디 사진의 리사이클러뷰 setting
    fun settingAddProductPhotoRecyclerView(){
        fragmentAddProductBinding.apply {
            recyclerviewAddProductPhotos.apply {
                adapter = AddPhotoRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    // 입력된 코디 상품 및 재고 리사이클러뷰
    fun settingAddProductDetailRecyclerView(){
        fragmentAddProductBinding.apply {
            recyclerviewAddProductDetail.apply {
                adapter = AddDetailRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(mainActivity)
                val deco = MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
                deco.isLastItemDecorated = false
                addItemDecoration(deco)
            }
        }
    }

    // 코디 사진의 리사이클러뷰 Adapter
    inner class AddPhotoRecyclerViewAdapter : RecyclerView.Adapter<AddPhotoRecyclerViewAdapter.AddPhotoViewHolder>(){
        inner class AddPhotoViewHolder(itemAddProductPhotoBinding: ItemAddproductPhotoBinding) : RecyclerView.ViewHolder(itemAddProductPhotoBinding.root){
            val itemAddProductPhotoBinding : ItemAddproductPhotoBinding
            init {
                this.itemAddProductPhotoBinding = itemAddProductPhotoBinding
                this.itemAddProductPhotoBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddPhotoViewHolder {
            val itemAddProductPhotoBinding = ItemAddproductPhotoBinding.inflate(layoutInflater)
            val addPhotoViewHolder = AddPhotoViewHolder(itemAddProductPhotoBinding)

            return addPhotoViewHolder
        }

        override fun onBindViewHolder(holder: AddPhotoViewHolder, position: Int) {
            // 이미지 세팅
        }

        override fun getItemCount(): Int {
            return 4
        }
    }

    // 입력된 코디 상품 및 재고 리사이클러뷰 Adapter
    inner class AddDetailRecyclerViewAdapter : RecyclerView.Adapter<AddDetailRecyclerViewAdapter.AddDetailViewHolder>(){
        inner class AddDetailViewHolder(itemAddproductDetailBinding: ItemAddproductDetailBinding): RecyclerView.ViewHolder(itemAddproductDetailBinding.root){
            val itemAddproductDetailBinding: ItemAddproductDetailBinding
            init {
                this.itemAddproductDetailBinding = itemAddproductDetailBinding
                this.itemAddproductDetailBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddDetailViewHolder {
            val itemAddproductDetailBinding = DataBindingUtil.inflate<ItemAddproductDetailBinding>(
                layoutInflater, R.layout.item_addproduct_detail, parent, false)
            val addProductDetailViewModel = AddProductDetailViewModel()
            itemAddproductDetailBinding.addProductDetailViewModel = addProductDetailViewModel
            itemAddproductDetailBinding.lifecycleOwner = this@AddProductFragment

            val addDetailViewHolder = AddDetailViewHolder(itemAddproductDetailBinding)

            return addDetailViewHolder
        }

        override fun onBindViewHolder(holder: AddDetailViewHolder, position: Int) {

            holder.itemAddproductDetailBinding.addProductDetailViewModel?.textviewAddProductDetailName?.value = "${individualProductData[position]["0"]}"
            holder.itemAddproductDetailBinding.addProductDetailViewModel?.textviewAddProductDetailOption?.value =
                "${individualProductData[position]["1"]} / ${individualProductData[position]["2"]}개 / ${individualProductData[position]["3"]} / ${individualProductData[position]["4"]}"

            // FireStore Items 이미지 경로
            Log.d("test1234", "product_image/items/${individualProductData[position]["5"]}")

            holder.itemAddproductDetailBinding.buttonAddProductDetailRemove.setOnClickListener {
                individualProductData.removeAt(position)
                fragmentAddProductBinding.recyclerviewAddProductDetail.adapter?.notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return individualProductData.size
        }
    }

    // Dialog의 가로 길이를 조정하기 위해 필요한 '디바이스의 가로 길이' 구하기
   fun getDeviceWidth() : Int? {
        val display = mainActivity.applicationContext?.resources?.displayMetrics
        val deviceWidth = display?.widthPixels // 디바이스의 가로길이

        return deviceWidth
   }

    // 리스너 실행 후
    override fun onAddProductClicked(productData: Map<String, String>, isAddPicture: Boolean) {
        individualProductData.add(productData)
        isProductAddPicture = isAddPicture
        Log.d("test1234", "개별 상품 사진 등록 여부: $isProductAddPicture")
        Log.d("test1234", "상품 이미지 경로 - ${productData["5"]}")

        // 리사이클러 뷰 초기화 
        fragmentAddProductBinding.recyclerviewAddProductDetail.adapter?.notifyDataSetChanged()
    }

    // 글 작성처리 메서드
    fun uploadProductData() {
        CoroutineScope(Dispatchers.Main).launch {

            // 서버에서의 첨부 이미지 파일 이름
            var serverFileName: String? = null

            // 첨부된 이미지가 있다면
            if (isProductAddPicture == true){
                Tools.saveImageViewData(mainActivity,fragmentAddProductBinding.imageviewAddProductPhoto, "uploadTemp.jpg")
                // 서버에서의 파일 이ㅡㄹㅁ
                serverFileName = "image_${System.currentTimeMillis()}.jpg"
                // 서버로 업로드한다
                ProductDao.uploadItemsImage(mainActivity, "uploadTemp.jpg", serverFileName)
            }

            // 게시글 시퀀스 값을 가져온다.
            val productSequence = ProductDao.getContentSequence()
            // 게시글 시퀀스 값을 업데이트 한다.
            ProductDao.updateProductSequence(productSequence + 1)


            // 커밋 생성용 주석
            // Chip 버튼의 입력상태를 받아온다.
            settingChipEvent()

            // 업로드할 정보를 담아준다.
            val productIdx = productSequence + 1
            val categoryId = CategoryId.TPO.str
            val coordinatorIdx = 0 // TEMP
            val coordiName = addProductViewModel.edittextAddProductName.value!!
            val coordiImage = imageProductList
            val codiMainImage = "코디 대표 이미지"
            val coordiGender = addProductViewModel.chipgroupAddProductGender.value!!
            val coordiText = addProductViewModel.edittextAddProductComments.value!!
            val price = (addProductViewModel.edittextAddProductPrice.value!!).toInt()
            val coordiItem = individualProductData
            val coordiMBTI = CodiMbti.ENFJ.str
            val coordiTPO = addProductViewModel.chipgroupAddProductTpoSub.value
            val coordiSeason = addProductViewModel.chipgroupAddProductSeasonSub.value
            val coordiMood = addProductViewModel.chipgroupAddProductMoodSub.value
            val coordiState = ProductState.PRODUCT_STATE_NORMAL.num

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val coordiWriteDate = simpleDateFormat.format(Date())

            val productModel = ProductModel(productIdx, categoryId, coordinatorIdx, coordiName, coordiImage, codiMainImage, coordiGender,
                coordiText, price, coordiItem, coordiMBTI, coordiTPO, coordiSeason, coordiMood, coordiState, coordiWriteDate)
            // 업로드한다.
            ProductDao.insertProductData(productModel)

            // ReadContentFragment로 이동한다.
            Tools.hideSoftInput(mainActivity)

            // 글 번호를 담는다.
            val readBundle = Bundle()
            readBundle.putInt("productIdx", productIdx)

            mainActivity.replaceFragment(MainFragmentName.HOME_MAIN_FULL, false, false, readBundle)
        }
    }


    // Chip 입력 처리 메서드
    fun settingChipEvent(){
        fragmentAddProductBinding.apply {

            // 성별
            chipgroupAddProductGender.apply {
                if (chipAddProductMale.isChecked){
                    Log.d("test1234", "ChipGroup is Working!")
                    addProductViewModel?.chipgroupAddProductGender?.value = 0
                } else if (chipAddProductFemale.isChecked){
                    addProductViewModel?.chipgroupAddProductGender?.value = 1
                } else {
                    addProductViewModel?.chipgroupAddProductGender?.value = -1
                }
            }
            // TPO
            chipgroupAddProductTpoSub.apply {
                if (chipAddProductTravel.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = TRAVEL
                } else if (chipAddProductDate.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = DATE
                } else if (chipAddProductCafe.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = CAFE
                } else if (chipAddProductWork.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = WORK
                } else if (chipAddProductDaily.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = DAILY
                } else if (chipAddProductCampus.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = CAMPUS
                } else if (chipAddProductOcean.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = OCEAN
                } else if (chipAddProductWedding.isChecked) {
                    addProductViewModel?.chipgroupAddProductTpoSub?.value = WEDDING
                }
            }

            // SEASON
            chipgroupAddProductSeasonSub.apply {
                if (chipAddProductSpring.isChecked){
                    addProductViewModel?.chipgroupAddProductSeasonSub?.value = SPRING
                }
                else if ( chipAddProductSummer.isChecked){
                    addProductViewModel?.chipgroupAddProductSeasonSub?.value = SUMMER
                }
                else if ( chipAddProductFall.isChecked){
                    addProductViewModel?.chipgroupAddProductSeasonSub?.value = FALL
                }
                else if ( chipAddProductWinter.isChecked){
                    addProductViewModel?.chipgroupAddProductSeasonSub?.value = WINTER
                }
            }

            // MOOD
            chipgroupAddProductMoodSub.apply {
                if(chipAddProductMinimal.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = MINIMAL
                }
                else if (chipAddProductBusiness.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = BUSINESS
                }
                else if (chipAddProductOneMile.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = ONE_MILE
                }
                else if (chipAddProductAmecaji.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = AMECAJI
                }
                else if (chipAddProductCityBoy.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = CITY_BOY
                }
                else if (chipAddProductStreet.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = STREET
                }
                else if (chipAddProductSporty.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = SPORTY
                }
                else if (chipAddProductRetro.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = RETRO
                }
                else if (chipAddProductLovely.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = LOVELY
                }
                else if (chipAddProductModern.isChecked){
                    addProductViewModel?.chipgroupAddProductMoodSub?.value = MODERN
                }
            }


//            for (items in chipgroupAddProductTpoSub){
//                if (items.chip ){
//                    Log.d("test1234", "Working")
//                    addProductViewModel?.chipgroupAddProductTpoSub?.value = TRAVEL
//                }
//            }

//            // TPO
//            chipgroupAddProductTpoSub.apply {
//                if (this.checkedChipId){
//                    Log.d("test1234", "Working")
//                    addProductViewModel?.chipgroupAddProductTpoSub?.value = TRAVEL
//                }
//            }


//            chipgroupAddProductGender.setOnCheckedStateChangeListener{ group, checkedId ->
//                Log.d("test1234","Working")
//                addProductViewModel?.chipgroupAddProductGender?.value = when (group.checkedChipId){
//                    R.id.chip_add_product_male -> 0
//                    R.id.chip_add_product_female -> 1
//                    else -> 0
//                }
//                Log.d("test1234","End")
//            }

            // TPO
//            chipgroupAddProductTpoSub.setOnCheckedStateChangeListener{ _, checkedId ->
//                addProductViewModel?.chipgroupAddProductTpoSub?.value = when (checkedId){
//
//                    chipAddProductTravel -> TRAVEL
//                    chipAddProductDate -> DATE
//                    chipAddProductCafe -> CAFE
//                    chipAddProductWork -> WORK
//                    chipAddProductDaily -> DAILY
//                    chipAddProductCampus -> CAMPUS
//                    chipAddProductOcean -> OCEAN
//                    chipAddProductWedding -> WEDDING
//                    else -> TRAVEL
//                }
//            }

//            // Season
//            chipgroupAddProductSeasonSub.setOnCheckedStateChangeListener{ group, checkedId ->
//                addProductViewModel?.chipgroupAddProductSeasonSub?.value = when(checkedId){
//                    chipAddProductSpring -> SPRING
//                    chipAddProductSummer -> SUMMER
//                    chipAddProductFall -> FALL
//                    chipAddProductWinter -> WINTER
//                    else -> SPRING
//                }
//            }

//            // Mood
//            chipgroupAddProductMoodSub.setOnCheckedStateChangeListener{ group, checkedId ->
//                addProductViewModel?.chipgroupAddProductMoodSub?.value = when(checkedId){
//                    chipAddProductMinimal -> MINIMAL
//                    chipAddProductBusiness -> BUSINESS
//                    chipAddProductOneMile -> ONE_MILE
//                    chipAddProductAmecaji -> AMECAJI
//                    chipAddProductCityBoy -> CITY_BOY
//                    chipAddProductStreet -> STREET
//                    chipAddProductSporty -> SPORTY
//                    chipAddProductRetro -> RETRO
//                    chipAddProductLovely -> LOVELY
//                    chipAddProductModern -> MODERN
//                    else -> MINIMAL
//                }
//            }
        }
    }

    // 입력 요소 설정
    fun settingInputForm() {
        addProductViewModel.edittextAddProductName.value = ""
        addProductViewModel.edittextAddProductComments.value = ""
        addProductViewModel.edittextAddProductPrice.value = ""

        addProductViewModel.chipgroupAddProductGender.value = -1
        addProductViewModel.chipgroupAddProductMbti.value = CodiMbti.DEFAULT_MBTI
    }

    // 입력 요소 유효성 검사
    fun checkInputForm(): Boolean{
        // 입력한 내용을 가져온다.
        val productName = addProductViewModel.edittextAddProductName.value!!
        val productComments = addProductViewModel.edittextAddProductComments.value!!
        val productPrice = addProductViewModel.edittextAddProductPrice.value!!

//        val productGender = addProductViewModel.chipgroupAddProductGender.value!!
//        val productMbti = addProductViewModel.chipgroupAddProductMbti.value!!

        if(productName.isEmpty()){
            Tools.showErrorDialog(mainActivity, fragmentAddProductBinding.edittextAddProductName,
                "상품명 입력 오류", "상품명을 입력해주세요")
            return false
        }
        if(productComments.isEmpty()){
            Tools.showErrorDialog(mainActivity, fragmentAddProductBinding.edittextAddProductComments,
                "코디 소개 내용 입력 오류", "코디 소개 내용을 입력해주세요")
            return false
        }
        if(productPrice.isEmpty()){
            Tools.showErrorDialog(mainActivity, fragmentAddProductBinding.edittextAddProductPrice,
                "코디 상품 가격 오류", "코디 상품 가격을 입력해주세요")
            return false
        }
//        if (productGender == -1){
//            Tools.showErrorDialog(mainActivity, fragmentAddProductBinding.chipgroupAddProductGender,
//                "코디 성별 미입력 오류", "코디 상품 대상 성별을 입력해주세요")
//            return false
//        }
//        if (productMbti == CodiMbti.DEFAULT_MBTI){
//            Tools.showErrorDialog(mainActivity, fragmentAddProductBinding.chipAddProductMbti,
//                "코디 MBTI 미입력 오류", "코디 상품 대상 MBTI를 입력해주셍요")
//            return false
//        }
        return true
    }
}