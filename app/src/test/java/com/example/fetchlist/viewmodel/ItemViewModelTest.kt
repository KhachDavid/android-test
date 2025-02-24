package com.example.fetchlist.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.fetchlist.model.Item
import com.example.fetchlist.repository.ItemRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import retrofit2.Response

@ExperimentalCoroutinesApi
class ItemViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: ItemViewModel

    @Mock
    private lateinit var repository: ItemRepository

    @Mock
    private lateinit var observer: Observer<Result>

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ItemViewModel(repository)
        viewModel.items.observeForever(observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetch items successfully`() = runTest {
        // Use captor to monitor observer calls & debug behavior
        val captor = argumentCaptor<Result>()

        `when`(repository.fetchItems()).thenReturn(Response.success(listOf(
            Item(1, 1, "Item 1"),
            Item(2, 1, "Item 2"),
            Item(3, 2, "Item 3"),
            Item(4, 2, null)
        )))

        viewModel.fetchItems()
        advanceUntilIdle()

        verify(observer, atLeastOnce()).onChanged(captor.capture())

        val observedStates = captor.allValues
        println("Observed LiveData states: $observedStates")

        assertTrue(observedStates.contains(Result.Loading))
    }

    @Test
    fun `fetch items with error`() = runTest {
        // Captor to track emissions
        val captor = argumentCaptor<Result>()

        `when`(repository.fetchItems()).thenReturn(
            Response.error(404, okhttp3.ResponseBody.create(null, ""))
        )

        viewModel.fetchItems()
        advanceUntilIdle()

        verify(observer, atLeastOnce()).onChanged(captor.capture())

        // Capture actual emitted states
        val observedStates = captor.allValues
        println("Observed LiveData states (Error Case): $observedStates")

        assertTrue(observedStates.any { it is Result.Loading })
        assertTrue(observedStates.any { it is Result.Error && it.message.contains("404") })
    }

    @Test
    fun `fetch items with exception`() = runTest {
        // Captor to track emissions
        val captor = argumentCaptor<Result>()

        `when`(repository.fetchItems()).thenThrow(RuntimeException("Network Error"))

        viewModel.fetchItems()
        advanceUntilIdle()

        verify(observer, atLeastOnce()).onChanged(captor.capture())

        // Capture and print observed changes
        val observedStates = captor.allValues
        println("Observed LiveData states (Exception Case): $observedStates")

        assertTrue(observedStates.contains(Result.Loading))
        assertTrue(observedStates.contains(Result.Error("Exception: Network Error")))
    }
}
