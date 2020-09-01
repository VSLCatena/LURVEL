package nl.vslcatena.lurvel.controllers

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest


@ControllerAdvice
class ErrorController {

    @ExceptionHandler(Exception::class)
    fun handleError(request: HttpServletRequest, e: Exception): ModelAndView {
        return ModelAndView("error")
    }
}