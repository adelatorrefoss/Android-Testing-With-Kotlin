package com.example.unittesting.presenter.login

import android.content.res.Resources
import com.example.unittesting.domain.ResourceProvider
import com.example.unittesting.domain.SchedulersFactory
import com.example.unittesting.domain.login.LoginUseCase
import com.example.unittesting.entity.login.LoginCredentials
import com.example.unittesting.entity.login.LoginRepository
import com.example.unittesting.entity.login.LoginValidator
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*

class LoginPresenterTest {

    val loginViewMock: LoginView = mock(LoginView::class.java)
    val resourcesStub: Resources = mock(Resources::class.java)
    val schedulersFactoryStub: SchedulersFactory = mock(SchedulersFactory::class.java)
    val loginRepositoryStub = mock(LoginRepository::class.java)

    val objectUnderTest = LoginPresenter(ResourceProvider(resourcesStub), LoginValidator(), LoginUseCase(loginRepositoryStub), schedulersFactoryStub)

    @Before
    fun setUp() {
        removeObserveOnMainThreadScheduler()
        objectUnderTest.createView(loginViewMock)
    }

    @Test
    fun `login with correct data`() {
        //given
        given(loginRepositoryStub.login(any(), any())).willReturn(Observable.just(true))
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin("correct").withPassword("correct"))
        //then
        verify(loginViewMock).onLoginSuccessful()
    }

    @Test
    fun `login with correct data with progress indication`() {
        //given
        given(loginRepositoryStub.login(any(), any())).willReturn(Observable.just(true))
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin("correct").withPassword("correct"))
        //then
        val ordered = inOrder(loginViewMock)
        ordered.verify(loginViewMock).showProgress()
        ordered.verify(loginViewMock).hideProgress()
    }

    @Test
    fun `login with valid but incorrect data`() {
        //given
        given(resourcesStub.getString(anyInt())).willReturn("error")
        given(loginRepositoryStub.login(any(), any())).willReturn(Observable.just(false))
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin("valid").withPassword("incorrectPassword"))
        //then
        val ordered = inOrder(loginViewMock)
        ordered.verify(loginViewMock).showLoginError(null)
        ordered.verify(loginViewMock).showPasswordError("error")
    }

    @Test
    fun `show validation error for empty email`() {
        //given
        given(resourcesStub.getString(anyInt())).willReturn("error")
        val login = ""
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin(login).withPassword("validPassword"))
        //then
        verify(loginViewMock).showLoginError("error")
        verify(loginViewMock).showPasswordError(null)
    }

    @Test
    fun `show validation error for empty email and too short password`() {
        //given
        given(resourcesStub.getString(anyInt())).willReturn("error")
        val login = ""
        val password = "short"
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin(login).withPassword(password))
        //then
        verify(loginViewMock).showLoginError("error")
        verify(loginViewMock).showPasswordError("error")
    }

    @Test
    fun `show validation error for too short password`() {
        //given
        given(resourcesStub.getString(anyInt())).willReturn("error")
        val password = "short"
        //when
        objectUnderTest.attemptLogin(LoginCredentials().withLogin("valid").withPassword(password))
        //then
        verify(loginViewMock).showLoginError(null)
        verify(loginViewMock).showPasswordError("error")
    }

    private fun removeObserveOnMainThreadScheduler() {
        given(schedulersFactoryStub.createMainThreadSchedulerTransformer<Boolean>()).willReturn(ObservableTransformer { it })
    }
}
