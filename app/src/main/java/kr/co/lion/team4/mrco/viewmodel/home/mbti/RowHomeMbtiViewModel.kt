package kr.co.lion.team4.mrco.viewmodel.home.mbti

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RowHomeMbtiViewModel: ViewModel() {
    val textViewRowHomeMbtiProductMbti = MutableLiveData<String>("ISTJ")
    val textViewRowHomeMbtiProductName = MutableLiveData<String>("홍길동 코디이름")
    val textViewRowHomeMbtiProductDiscountPercent = MutableLiveData<String>("30%")
    val textViewRowHomeMbtiProductPrice = MutableLiveData<String>("178,000")
}