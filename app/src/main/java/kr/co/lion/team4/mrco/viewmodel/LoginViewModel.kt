package kr.co.lion.team4.mrco.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    // 아이디
    val textFieldLoginUserId = MutableLiveData<String>()
    // 비밀번호
    val textFieldLoginUserPw = MutableLiveData<String>()
    // 자동로그인
    val checkBoxLoginAutoLogin = MutableLiveData<Boolean>()
}