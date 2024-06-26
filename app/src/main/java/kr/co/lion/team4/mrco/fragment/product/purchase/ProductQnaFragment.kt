package kr.co.lion.team4.mrco.fragment.product.purchase

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.transition.MaterialSharedAxis
import kr.co.lion.team4.mrco.MainActivity
import kr.co.lion.team4.mrco.MainFragmentName
import kr.co.lion.team4.mrco.R
import kr.co.lion.team4.mrco.SubFragmentName
import kr.co.lion.team4.mrco.databinding.FragmentProductQnaBinding
import kr.co.lion.team4.mrco.databinding.RowProductQnaBinding
import kr.co.lion.team4.mrco.fragment.productQna.RegisterProductQnaFragment
import kr.co.lion.team4.mrco.viewmodel.product.ProductQnaViewModel
import kr.co.lion.team4.mrco.viewmodel.product.RowProductQnaViewModel

/* 상품페이지 - 상품 문의 탭 */
class ProductQnaFragment : Fragment() {
    lateinit var fragmentProductQnaBinding: FragmentProductQnaBinding
    lateinit var mainActivity: MainActivity
    lateinit var productQnaViewModel: ProductQnaViewModel

    var oldFragment: Fragment? = null
    var newFragment: Fragment? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentProductQnaBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_qna, container, false)
        productQnaViewModel = ProductQnaViewModel()
        fragmentProductQnaBinding.productQnaViewModel = productQnaViewModel
        fragmentProductQnaBinding.lifecycleOwner = this

        mainActivity = activity as MainActivity

        settingQuestionUploadButton()
        settingProductQnaRecyclerView()

        return fragmentProductQnaBinding.root
    }

    fun settingQuestionUploadButton(){
        // 상품 문의하기 버튼
        fragmentProductQnaBinding.productQuestionUploadButton.setOnClickListener {
            replaceFragment(SubFragmentName.REGISTER_PRODUCT_QNA_FRAGMENT, true, true, null)
        }
    }

    fun settingProductQnaRecyclerView(){
        fragmentProductQnaBinding.productQuestionRecycler.apply {
            adapter = ProductQnaRecyclerViewAdapter()
            layoutManager = LinearLayoutManager(mainActivity)
            val deco = MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
            addItemDecoration(deco)

        }
    }

    // 문의 내역 리사이클러뷰
    inner class ProductQnaRecyclerViewAdapter : RecyclerView.Adapter<ProductQnaRecyclerViewAdapter.RowProductQnaViewHolder>(){
        inner class RowProductQnaViewHolder(rowProductQnaBinding: RowProductQnaBinding) : RecyclerView.ViewHolder(rowProductQnaBinding.root) {
            val rowProductQnaBinding : RowProductQnaBinding
            init {
                this.rowProductQnaBinding = rowProductQnaBinding
                this.rowProductQnaBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowProductQnaViewHolder {
            val rowProductQnaBinding = DataBindingUtil.inflate<RowProductQnaBinding>(
                layoutInflater, R.layout.row_product_qna, parent, false
            )
            val rowProductQnaViewModel = RowProductQnaViewModel()
            rowProductQnaBinding.rowProductQnaViewModel = rowProductQnaViewModel
            rowProductQnaBinding.lifecycleOwner = this@ProductQnaFragment

            val rowProductViewHolder = RowProductQnaViewHolder(rowProductQnaBinding)

            return rowProductViewHolder
        }

        override fun onBindViewHolder(holder: RowProductQnaViewHolder, position: Int) {
            holder.rowProductQnaBinding.apply {
                // 문의 답변 상태
                if(position%2 == 1){ // 답변이 등록된 경우
                    itemProductQnaStatusText.setTextColor(Color.parseColor("#152F7E"))
                    rowProductQnaViewModel?.item_product_qna_status_text?.value = "답변 완료"
                }else{ // 아직 답변이 등록되지 않은 경우
                    rowProductQnaViewModel?.item_product_qna_status_text?.value = "답변 대기 중"
                }

                // 문의 내용
                if(position==0){ //비밀글인 경우 - 질문 text앞에 '아이콘'을 붙여준다.
                    // xml -> app:drawableStartCompat="@drawable/outline_lock_24"
                    rowProductQnaViewModel?.item_product_question_content?.value = "비밀글입니다"
                    itemProductQuestionContent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_lock_24, 0,0,0)
                }else{
                    rowProductQnaViewModel?.item_product_question_content?.value = "질문 내용은 여기에 lorem ipsum dolor sit amet 질문 내용은 여기에 lorem ipsum dolor sit amet"
                }

                // 작성자 닉네임
                rowProductQnaViewModel?.item_product_question_owner?.value = "qwe**"
                // 작성일
                rowProductQnaViewModel?.item_product_question_date?.value = "2024.04.12"

                // 답변 상태 - 답변 완료인 경우, 답변 내용 setting
                // to do - if문 변수명은 데이터에 맞춰서 변경한 뒤 if문 블럭 주석해제
                //if(qnaState == ANSWERED){
                    rowProductQnaViewModel?.item_product_answer_content?.value = "답변내용입니다. 작성자만 확인 가능한"
                    rowProductQnaViewModel?.item_product_answer_owner?.value = "아이유"
                    rowProductQnaViewModel?.item_product_question_answer_date?.value = "2024.04.12"
                //}
            }

            // 답변이 등록 된 문의인 경우 (to do - 데이터 연결 후, line 109, 112 수정하고, if문 블럭 주석 해제)
            // if( qnaState == ANSWERED ){
                // 답변이 등록되어있으면서 비밀글이 아닌 문의 or 비밀글 작성자와 로그인 사용자가 일치하는 경우
                // if( qnaSecret == TRUE || qnaWriterIdx == LoginUserIdx ){ // to do - 데이터 연결 후 line 112 삭제
                    if(position%2 == 1 || position == 4){
                        holder.rowProductQnaBinding.layoutProductQnaQuestion.setOnClickListener {
                            if(holder.rowProductQnaBinding.layoutProductQnaAnswer.visibility == View.VISIBLE){
                                holder.rowProductQnaBinding.layoutProductQnaAnswer.visibility = View.GONE
                            }else{
                                holder.rowProductQnaBinding.layoutProductQnaAnswer.visibility = View.VISIBLE
                            }
                        }
                    }
                // }
            // }
        }

        override fun getItemCount(): Int {
            return 10
        }
    }

    fun replaceFragment(
        name: SubFragmentName,
        addToBackStack: Boolean,
        isAnimate: Boolean,
        data: Bundle?
    ) {

        // Fragment를 교체할 수 있는 객체를 추출한다.
        val fragmentTransaction = mainActivity.supportFragmentManager.beginTransaction()

        // oldFragment에 newFragment가 가지고 있는 Fragment 객체를 담아준다.
        if (newFragment != null) {
            oldFragment = newFragment
        }

        when (name) {
            SubFragmentName.PRODUCT_SHIPPING_FRAGMENT -> newFragment = ProductShippingFragment()

            SubFragmentName.PRODUCT_REVIEW_FRAGMENT -> newFragment = ProductReviewFragment()

            SubFragmentName.PRODUCT_QNA_FRAGMENT -> newFragment = ProductQnaFragment()

            SubFragmentName.REVIEW_IMAGE_MORE_FRAGMENT -> newFragment = ReviewImageMoreFragment()

            SubFragmentName.REGISTER_PRODUCT_QNA_FRAGMENT -> newFragment = RegisterProductQnaFragment()
        }

        // 새로운 Fragment에 전달할 객체가 있다면 arguments 프로퍼티에 넣어준다.
        if (data != null) {
            newFragment?.arguments = data
        }

        if (newFragment != null) {

            // 애니메이션 설정
            if (isAnimate == true) {

                if (oldFragment != null) {
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
            fragmentTransaction.replace(R.id.fragmentContainerViewProduct, newFragment!!)

            // addToBackStack 변수의 값이 true면 새롭게 보여질 Fragment를 BackStack에 포함시켜 준다.
            if (addToBackStack == true) {
                // BackStack 포함 시킬때 이름을 지정해주면 원하는 Fragment를 BackStack에서 제거할 수 있다.
                fragmentTransaction.addToBackStack(name.str)
            }
            // Fragment 교체를 확정한다.
            fragmentTransaction.commit()
        }
    }


}