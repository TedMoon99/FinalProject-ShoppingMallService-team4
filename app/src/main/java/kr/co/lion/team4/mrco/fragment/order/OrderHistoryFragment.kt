package kr.co.lion.team4.mrco.fragment.order

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.team4.mrco.InquiryPeriod
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.OrderState
import kr.co.lion.team4.mrco.viewmodel.order.OrderHistoryItemViewModel
import kr.co.lion.team4.mrco.viewmodel.order.OrderHistoryProductViewModel
import kr.co.lion.team4.mrco.viewmodel.order.OrderHistoryViewModel
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.ShippingState
import kr.co.lion.team4.mrco.Tools
import kr.co.lion.team4.mrco.dao.OrderHistoryDao
import kr.co.lion.team4.mrco.databinding.FragmentOrderHistoryBinding
import kr.co.lion.team4.mrco.databinding.ItemOrderhistoryItemBinding
import kr.co.lion.team4.mrco.databinding.ItemOrderhistoryProductBinding
import kr.co.lion.team4.mrco.model.OrderModel
import kr.co.lion.team4.mrco.model.OrderedProductInfoModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/* (구매자) 주문 내역 화면 */
class OrderHistoryFragment : Fragment() {
    lateinit var fragmentOrderHistoryBinding: FragmentOrderHistoryBinding
    lateinit var orderHistoryViewModel : OrderHistoryViewModel

    lateinit var mainActivity: MainActivity

    // 전체 주문내역을 담을 리스트
    var orderList = mutableListOf<OrderModel>()
    // 주문 상태별로 구분하여 구성하기 위한 주문 내역 리스트
    var filteredOrderList = mutableListOf<OrderModel>()

    // 주문 상품의 상품 정보를 담을 리스트
    var orderedProductInfoList = mutableListOf<ArrayList<OrderedProductInfoModel>>()
    // 주문 상태별로 구분된 상품들의 정보를 담을 리스트
    var filteredOrderedProductInfoList = mutableListOf<ArrayList<OrderedProductInfoModel>>()

    // 검색 화면의 RecyclerView 구성을 위한 리스트
    var searchResultOrderList = mutableListOf<OrderModel>()
    // 검색 화면의 RecyclerView 구성을 위한 상품들의 정보를 담을 리스트
    var searchResultProductInfoList = mutableListOf<ArrayList<OrderedProductInfoModel>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentOrderHistoryBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_order_history, container, false)
        orderHistoryViewModel = OrderHistoryViewModel()
        fragmentOrderHistoryBinding.orderHistoryViewModel = orderHistoryViewModel
        fragmentOrderHistoryBinding.lifecycleOwner = this

        mainActivity = activity as MainActivity

        // 툴바 설정
        settingToolbarOrderHistory()

        // 주문 내역 목록 리사이클러뷰
        settingOrderHistoryRecyclerView()
        settingOrderHistoryPeriodButtonClickListener()

        // 기본 조회기간 설정 및 조회기간에 맞는 주문내역 불러오기
        settingOrderHistoryPeriod(InquiryPeriod.ONE_MONTH)

        // Tab Layout 세팅
        settingOrderHistoryTab()

        // 검색어 입력창 세팅
        settingOrderHistorySearchView()

        return fragmentOrderHistoryBinding.root
    }

    fun settingToolbarOrderHistory(){
        fragmentOrderHistoryBinding.toolbarOrderHistory.apply {
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                mainActivity.removeFragment(MainFragmentName.ORDER_HISTORY_FRAGMENT)
            }
        }
    }

    // 주문 내역 추가하기 위한 임시 메서드
/*    fun sendTempData(){
        val product = OrderProduct(1, 4, "셔츠 pink M / 바지 red S / 아우터 white L / 가방 black",
            1, null, null, 4, null, 0)
        val product2 = OrderProduct(1, 4, "셔츠 blue L / 바지 skyblue S / 아우터 grey M / 가방 white",
            1, null, null, 2, null, 0)

        val orderedItem = arrayListOf<OrderProduct>(product, product2)

        val orderModel = OrderModel(_, 1, Timestamp.now(), orderedItem, "카드", 396000, 396000, 0, 0, "@@시 @@구", "@@아파트 @@동 @@호"
        , "홍길동", "01000000000", "부재시 경비실에 맡겨주세요")

        CoroutineScope(Dispatchers.Main).launch {
            OrderHistoryDao.insertOrderData(orderModel)
        }
    }*/

    // 상단 탭 선택 설정
    fun settingOrderHistoryTab(){
        fragmentOrderHistoryBinding.apply { 
            val tabLayout = tabOrderHistoryState
            
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // 탭의 카테고리에 맞는 데이터만 담아서 보여준다.
                    when(tab?.position){
                        0 -> {  // 전체
                            if(orderList.size>0){
                                hideNotice()
                                // 목록 초기화
                                filteredOrderList.clear()
                                filteredOrderedProductInfoList.clear()
                                // 전체 주문내역, 상품정보 목록을 담아준다.
                                filteredOrderList.addAll(orderList) // 전체 주문내역 목록을 담아준다.
                                filteredOrderedProductInfoList.addAll(orderedProductInfoList)
                                fragmentOrderHistoryBinding.recyclerviewOrderHistory.adapter?.notifyDataSetChanged()
                            }
                        }
                        1 -> { filterOrderListByTab(OrderState.ORDERED.num, OrderState.ORDERED.num) } // 주문접수 (0)
                        2 -> { filterOrderListByTab(OrderState.IN_TRANSIT.num, OrderState.IN_TRANSIT.num) } // 배송중 (1,
                        3 -> { filterOrderListByTab(OrderState.DELIVERED.num, OrderState.ORDER_CONFIRM.num) } // 배송완료, 구매 확정 (2 or 3)
                        4 -> { filterOrderListByTab(OrderState.RETURN.num, OrderState.CANCEL.num) } // 취소/교환/반품 (4,5,6)
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    //"Not yet implemented"
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                    //"Not yet implemented"
                }
            })
        }
    }


    fun filterOrderListByTab( state : Int, stateAdditional : Int ){
        // 리스트 초기화
        filteredOrderList.clear()
        filteredOrderedProductInfoList.clear()
        orderList.forEachIndexed { index, orderModel ->
            if(orderModel.order_state >= state && orderModel.order_state <= stateAdditional ){
                filteredOrderList.add(orderModel)
                // 상품 정보도 동일하게 상품 정보 리스트에 추가해준다.
                val orderedProductInfo = orderedProductInfoList[index]
                filteredOrderedProductInfoList.add(orderedProductInfo)
            }
        }
        if(filteredOrderList.size>0){
            hideNotice()
            // 리사이클러뷰 갱신
            fragmentOrderHistoryBinding.recyclerviewOrderHistory.adapter?.notifyDataSetChanged()
        }else{
            noticeNoOrderList()
        }
    }
    
    // 조회기간 버튼 클릭 이벤트
    fun settingOrderHistoryPeriodButtonClickListener(){
        fragmentOrderHistoryBinding.apply {
            // 조회 기간 1개월
            buttonOrderHistoryOneMonth.setOnClickListener {
                settingOrderHistoryPeriod(InquiryPeriod.ONE_MONTH)
            }
            buttonOrderHistoryThreeMonths.setOnClickListener {
                settingOrderHistoryPeriod(InquiryPeriod.THREE_MONTHS)
            }
            buttonOrderHistorySixMonths.setOnClickListener {
                settingOrderHistoryPeriod(InquiryPeriod.SIX_MONTHS)
            }
            buttonOrderHistorySetPeriod.setOnClickListener {
                // 날짜는 최대 오늘까지 선택할 수 있도록 선택 가능한 기간 설정
                val calendarConstraints = CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
                val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("조회기간을 설정해주세요")
                    .setCalendarConstraints(calendarConstraints)
                    .build()
                dateRangePicker.show(mainActivity.supportFragmentManager, "orderHistoryPeriod")
                dateRangePicker.addOnPositiveButtonClickListener {
                    val startDate = Date(it.first)
                    val endDate = Date(it.second)
                    // 시작일
                    orderHistoryViewModel?.periodStart?.value = Timestamp(startDate)
                    // 종료일
                    orderHistoryViewModel?.periodEnd?.value = Timestamp(endDate)

                    // 조회 기간에 맞는 주문 배송 내역 불러오기
                    gettingOrderList()
                }
            }
        }
    }

    // dateRangePicker에서 선택된 날짜의 Long 값을 날짜로 변환
/*    fun getDateFromLongValue(date: Long) : String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(calendar.time)

        return date
    }*/

    // 현재 날짜로부터 1개월, 3개월, 6개월 조회 기간을 설정
    fun settingOrderHistoryPeriod(periodType: InquiryPeriod){
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        //val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        // 종료일 (현재)
        orderHistoryViewModel.periodEnd.value = Timestamp(calendar.time)

        when(periodType){
            // 조회기간 1개월
            InquiryPeriod.ONE_MONTH -> {
                calendar.add(Calendar.MONTH, InquiryPeriod.ONE_MONTH.num)
                // 1개월 전의 날짜로 설정
                orderHistoryViewModel.periodStart.value = Timestamp(calendar.time)
            }
            // 조회기간 3개월
            InquiryPeriod.THREE_MONTHS -> {
                calendar.add(Calendar.MONTH, InquiryPeriod.THREE_MONTHS.num)
                // 3개월 전의 날짜로 설정
                orderHistoryViewModel.periodStart.value = Timestamp(calendar.time)
            }
            // 조회기간 6개월
            InquiryPeriod.SIX_MONTHS -> {
                calendar.add(Calendar.MONTH, InquiryPeriod.SIX_MONTHS.num)
                // 6개월 전의 날짜로 설정
                orderHistoryViewModel.periodStart.value = Timestamp(calendar.time)
            }
        }
        // 조회 기간에 맞는 주문 배송 내역 불러오기
        gettingOrderList()
    }

    // 서버에서 로그인한 유저의 전체 주문 내역을 불러온다.
    fun gettingOrderList(){
        CoroutineScope(Dispatchers.Main).launch {
            orderList.clear()
            filteredOrderList.clear()

            // 조회 시작일
            val startDateTimestamp = orderHistoryViewModel.periodStart.value
            // 조회 종료일
            val enddDateTimestamp = orderHistoryViewModel.periodEnd.value

            orderList = OrderHistoryDao.gettingOrderList(mainActivity.loginUserIdx, startDateTimestamp!!, enddDateTimestamp!!)

            if(orderList.size>0){ // 주문 내역이 존재하는 경우
                filteredOrderList.addAll(orderList)
                // 주문한 상품들의 정보를 불러온 뒤, 리사이클러뷰를 갱신해준다.
                hideNotice()
                gettingOrderItemInfoFromOrderList()
            }
            // 주문 내역이 존재하지 않는 경우
            else{
                noticeNoOrderList()
            }
        }
    }

    // 주문 내역이 없을 경우, 리사이클러뷰를 숨기고 주문내역이 없다는 안내문구를 띄워준다.
    fun noticeNoOrderList(){
        fragmentOrderHistoryBinding.apply {
            recyclerviewOrderHistory.visibility = View.GONE
            textviewOrderHistoryEmpty.visibility = View.VISIBLE
        }
    }

    fun hideNotice(){
        fragmentOrderHistoryBinding.apply {
            recyclerviewOrderHistory.visibility = View.VISIBLE
            textviewOrderHistoryEmpty.visibility = View.GONE
        }
    }

    // 주문 내역의 상품 정보(상품명, 상품 이미지 파일명, 판매가)를 불러온다
    fun gettingOrderItemInfoFromOrderList(){
        CoroutineScope(Dispatchers.Main).launch {
            filteredOrderedProductInfoList.clear() // 목록 초기화
            orderedProductInfoList = OrderHistoryDao.gettingOrderedItemInfo(orderList)
            filteredOrderedProductInfoList.addAll(orderedProductInfoList)
            fragmentOrderHistoryBinding.recyclerviewOrderHistory.adapter?.notifyDataSetChanged()
        }
    }

    // SearchView에서 검색어 입력후 키보드 엔터키를 누르면 검색되도록 설정
    fun settingOrderHistorySearchView(){
        fragmentOrderHistoryBinding.apply {
            searchviewOrderHistory.apply {
                // 검색 시 사용하는 키보드의 엔터키를 누르면 동작하는 리스너
                editText.setOnEditorActionListener { view, actionId, event ->
                    if(event!=null && event.action == KeyEvent.ACTION_DOWN){
                        // 검색 결과 목록 초기화
                        searchResultOrderList.clear()
                        searchResultProductInfoList.clear()

                        val keyword = editText.text.toString()
                        if(keyword.isEmpty()){
                            Tools.showErrorDialog(mainActivity, searchviewOrderHistory, "검색어 입력 오류", "검색어를 입력해주세요.")
                        }else{ // 검색어가 입력되었다면
                            // 검색결과가 없다는 TextView는 숨겨주고,
                            fragmentOrderHistoryBinding.textviewOrderHistorySearchEmpty.visibility = View.GONE
                            // 검색 결과를 가져와 보여주는 메서드를 호출한다.
                            gettingOrderSearchList(keyword)
                        }
                    }
                    false
                }
            }
        }
    }

    // 검색 결과 불러오기
    fun gettingOrderSearchList(keyword: String){
        orderedProductInfoList.forEachIndexed { index, orderedProductInfoModels ->
            orderedProductInfoModels.forEach {
                if(it.ordered_product_name.contains(keyword)){
                    searchResultOrderList.add(orderList[index])
                    searchResultProductInfoList.add(orderedProductInfoModels)
                    return@forEachIndexed
                }
            }
        }

        // 검색결과 없으면
        if(searchResultOrderList.size < 1){
            fragmentOrderHistoryBinding.textviewOrderHistorySearchEmpty.visibility = View.VISIBLE
        }
    }

    fun settingOrderHistoryRecyclerView(){
        // 주문 / 배송 내역 목록
        fragmentOrderHistoryBinding.recyclerviewOrderHistory.apply {
            adapter = OrderHistoryRecyclerViewAdapter(filteredOrderList, filteredOrderedProductInfoList)
            layoutManager = LinearLayoutManager(mainActivity)
        }

        // 검색 결과 목록
        fragmentOrderHistoryBinding.recyclerviewOrderHistorySearch.apply {
            adapter = OrderHistoryRecyclerViewAdapter(searchResultOrderList, searchResultProductInfoList)
            layoutManager = LinearLayoutManager(mainActivity)
        }
    }

    // 주문 내역 목록의 RecyclerView Adapter
    inner class OrderHistoryRecyclerViewAdapter(var orders: MutableList<OrderModel>, var productsInfo: MutableList<ArrayList<OrderedProductInfoModel>>) : RecyclerView.Adapter<OrderHistoryRecyclerViewAdapter.OrderHistoryItemViewHolder>(){
        inner class OrderHistoryItemViewHolder(itemOrderhistoryItemBinding:ItemOrderhistoryItemBinding):
            RecyclerView.ViewHolder(itemOrderhistoryItemBinding.root){
                val itemOrderhistoryItemBinding : ItemOrderhistoryItemBinding
                init {
                    this.itemOrderhistoryItemBinding = itemOrderhistoryItemBinding
                    this.itemOrderhistoryItemBinding.root.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryItemViewHolder {
            val itemOrderHistoryItemBinding = DataBindingUtil.inflate<ItemOrderhistoryItemBinding>(
                layoutInflater, R.layout.item_orderhistory_item, parent, false
            )
            val orderHistoryItemViewModel = OrderHistoryItemViewModel()
            itemOrderHistoryItemBinding.orderHistoryItemViewModel = orderHistoryItemViewModel
            itemOrderHistoryItemBinding.lifecycleOwner = this@OrderHistoryFragment

            val orderHistoryItemViewHolder = OrderHistoryItemViewHolder(itemOrderHistoryItemBinding)

            return orderHistoryItemViewHolder
        }

        override fun onBindViewHolder(holder: OrderHistoryItemViewHolder, position: Int) {
            val orderedDate = orders[position].order_date.toDate()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val orderedDateParse = simpleDateFormat.format(orderedDate) // 년-월-일 형태로 날짜만 표기해준다.

            holder.itemOrderhistoryItemBinding.orderHistoryItemViewModel?.textViewOrderedItemDate?.value = orderedDateParse

            // 내부 리사이클러뷰 (주문 상품 목록)
            val productRecyclerView = holder.itemOrderhistoryItemBinding.recyclerviewOrderedItems
            productRecyclerView.apply {
                adapter = OrderHistoryProductRecyclerViewAdapter(position, orders, productsInfo)
                layoutManager = LinearLayoutManager(mainActivity)
                val deco = MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
                deco.isLastItemDecorated = false // 마지막 아이템에는 MaterialDividerItemDecoration 제거
                addItemDecoration(deco)
            }

            // 주문상세 > 이거 눌렀을 때
            holder.itemOrderhistoryItemBinding.buttonOrderHistory.setOnClickListener {
                val orderHistoryBundle = Bundle()
                // 주문번호를 전달
                orderHistoryBundle.putInt("orderIdx", orders[position].order_idx)
                mainActivity.replaceFragment(MainFragmentName.ORDER_DETAIL, true, true, orderHistoryBundle)
            }
        }

        override fun getItemCount(): Int {
            return orders.size
        }
    }

    // 주문 내역 목록에 표시되는 주문 상품 목록의 RecyclerView Adapter
    inner class OrderHistoryProductRecyclerViewAdapter(var parentPosition: Int, var orders: MutableList<OrderModel>, var productsInfo: MutableList<ArrayList<OrderedProductInfoModel>>) : RecyclerView.Adapter<OrderHistoryProductRecyclerViewAdapter.OrderHistoryProductViewHolder>(){
        inner class OrderHistoryProductViewHolder(itemOrderhistoryProductBinding: ItemOrderhistoryProductBinding) :
            RecyclerView.ViewHolder(itemOrderhistoryProductBinding.root){
                val itemOrderhistoryProductBinding : ItemOrderhistoryProductBinding
                init {
                    this.itemOrderhistoryProductBinding = itemOrderhistoryProductBinding
                    this.itemOrderhistoryProductBinding.root.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryProductViewHolder {
            val itemOrderhistoryProductBinding = DataBindingUtil.inflate<ItemOrderhistoryProductBinding>(
                layoutInflater, R.layout.item_orderhistory_product, parent, false)
            val orderHistoryProductViewModel = OrderHistoryProductViewModel()
            itemOrderhistoryProductBinding.orderHistoryProductViewModel = orderHistoryProductViewModel
            itemOrderhistoryProductBinding.lifecycleOwner = this@OrderHistoryFragment

            val orderHistoryProductViewHolder = OrderHistoryProductViewHolder(itemOrderhistoryProductBinding)

            return orderHistoryProductViewHolder
        }

        override fun onBindViewHolder(holder: OrderHistoryProductViewHolder, position: Int) {
            // 주문 상품의 정보
            val orderedProduct = orders[parentPosition].order_product[position]
            val orderedProductInfo = productsInfo[parentPosition][position]
            // 주문 상태
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.textViewOrderedItemState?.value = Tools.getOrderItemStateStringValue(orderedProduct.tracking_state)
            // 주문 상품명
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.textViewOrderedItemCoordiName?.value = orderedProductInfo.ordered_product_name
            // 주문 옵션
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.textViewOrderedItemOption?.value = orderedProduct.product_coordi_option
            // 주문 가격
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.textViewOrderedItemPrice?.value = Tools.gettingPriceDecimalFormat(orderedProductInfo.ordered_product_price)

            if(orderedProduct.tracking_state < ShippingState.DELIVERED.num){ // 아직 배송 진행 중인 경우
                // 주문 상태 텍스트 아이콘 + 프로그레스바 설정
                settingOrderState(orderedProduct.tracking_state, holder)
            }else{ // 배송 완료된 경우
                holder.itemOrderhistoryProductBinding.layoutOrderedItemStates.visibility = View.INVISIBLE
                holder.itemOrderhistoryProductBinding.buttonOrderedItemReview.visibility = View.VISIBLE
            }

            /*// 주문 상품 썸네일(대표사진)
            CoroutineScope(Dispatchers.Main).launch {
                ProductDao.gettingProductImage(mainActivity, orderedProductInfo.ordered_product_thumbnail_filename,
                    holder.itemOrderhistoryProductBinding.imageviewOrderedItemThumbnail)
            }*/

        }

        override fun getItemCount(): Int {
            return orders[parentPosition].order_product.size
        }

        // 각 상품의 배송 상태를 표기해주기 위한 메서드
        fun settingOrderState(state: Int, holder: OrderHistoryProductViewHolder){
            // to do 데이터 연결 후 state 값은 enum class로 수정할 것

            // 주문 상태의 텍스트, 아이콘 -> 상태 초기화
            initOrderState(holder)

            when(state){
                // 발송 준비 상태
                0 -> {
                    // 텍스트 아이콘으로 현재 상태 표기
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateReady?.value = true
                    // progressBar로 현재 상태 표시
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.progressbarOrderedItemState?.value = 2
                }
                // 배송 시작 상태
                1 -> {
                    // 텍스트 아이콘으로 현재 상태 표기
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateShipped?.value = true
                    // progressBar로 현재 상태 표시
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.progressbarOrderedItemState?.value = 34
                }
                // 배송중 상태
                2 -> {
                    // 텍스트 아이콘으로 현재 상태 표기
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateInTransit?.value = true
                    // progressBar로 현재 상태 표시
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.progressbarOrderedItemState?.value = 67

                }
                // 배송 완료 상태
                3 -> {
                    // 텍스트 아이콘으로 현재 상태 표기
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateDelivered?.value = true
                    // progressBar로 현재 상태 표시
                    holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.progressbarOrderedItemState?.value = 100
                }
            }
        }

        fun initOrderState(holder: OrderHistoryProductViewHolder){
            // 배송 상태 표기해주는 텍스트 아이콘 상태 초기화
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateReady?.value = false
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateShipped?.value = false
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateInTransit?.value = false
            holder.itemOrderhistoryProductBinding.orderHistoryProductViewModel?.radiobuttonOrderedItemStateDelivered?.value = false
        }
    }
}