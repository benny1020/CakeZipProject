package com.example.cakezip.controller

import com.example.cakezip.domain.cake.Cake
import com.example.cakezip.domain.cake.CakeStatusType
import com.example.cakezip.domain.member.Customer
import com.example.cakezip.domain.member.User
import com.example.cakezip.domain.member.UserType
import com.example.cakezip.dto.Message
import com.example.cakezip.service.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpSession

@Controller
class ReviewController(
    private val customerService: CustomerService,
    private val reviewService: ReviewService,
    private val cakeService: CakeService,
    private val cakeTaskService: CakeTaskService,
    private val cakeOptionListService: CakeOptionListService,
) {

    val noAccessMessage: Message = Message("접근할 수 없는 페이지입니다.", "/")

    @GetMapping("/reviews/customer/{customerId}")
    fun getMyReviews(model: Model, @PathVariable("customerId") customerId: Long): String {
        model.addAttribute("review", reviewService.getCustomerAllReviews(customerId))
        println("해당 사용자의 리뷰 전체 목록")
        return "myreview";
    }

    @GetMapping("/reviews/shop/{shopId}")
    fun getShopReviews(model: Model, @PathVariable("shopId") shopId: Long): String {
        model.addAttribute("review", reviewService.getShopAllReviews(shopId))
        println("해당 가게의 리뷰 전체 목록")
        return "myreview";
    }

    @GetMapping("/reviews/{cakeId}")
    fun directAddProduct(@PathVariable cakeId: Long, model: Model, session: HttpSession): String {
        model.addAttribute("cake", cakeService.getCakeOptionList(cakeService.findByCakeId(cakeId)))
        val user: User = session.getAttribute("user") as User
        if (user.userType == UserType.CUSTOMER) {
            val customer: Customer = session.getAttribute("customer") as Customer
            if (cakeService.findByCakeId(cakeId).customer.customerId == customer.customerId) {
                if (cakeService.findByCakeId(cakeId).cakeStatus != CakeStatusType.COMPLETE) {
                    if (cakeService.findByCakeId(cakeId).cakeStatus == CakeStatusType.REVIEW) {
                        model.addAttribute("data", Message("이미 작성된 리뷰입니다.", "/"))
                    } else {
                        model.addAttribute("data", Message("픽업 완료한 케이크만 리뷰를 작성할 수 있습니다.", "/"))
                    }
                } else {
                    model.addAttribute("data", Message("", ""))
                }
            } else {
                model.addAttribute("data", noAccessMessage)
            }
        } else {
            model.addAttribute("data", noAccessMessage)
        }

        return "review"
    }

    @PostMapping("/reviews/{cakeId}")
    fun addReview(
        @PathVariable cakeId: Long, model: Model,
        reviewTitle: String, reviewContent: String, reviewScore: Int
    ): String {
        val cake = cakeService.findByCakeId(cakeId)
        model.addAttribute("cakeId", cakeId)
        reviewService.addReview(reviewTitle, reviewContent, reviewScore, cake)
        cakeService.updateCakeStatus(cake.cakeId!!, CakeStatusType.REVIEW)
        return "redirect:/"
    }
}
