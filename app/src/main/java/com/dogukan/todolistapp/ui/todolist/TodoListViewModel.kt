package com.dogukan.todolistapp.ui.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dogukan.todolistapp.data.Todo
import com.dogukan.todolistapp.data.TodoRepository
import com.dogukan.todolistapp.di.Routes
import com.dogukan.todolistapp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository,
    application: Application

): AndroidViewModel(application){


    val todos = repository.getTodos()
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private var deletedTodo : Todo? = null

    fun onEvent(event: TodoListEvent){
        when(event){
            is TodoListEvent.OnTodoClick ->{
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=${event.todo.id}"))
            }
            is TodoListEvent.OnAddTodoClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO))
            }
            is TodoListEvent.OnUndoDeleteClick ->{
                deletedTodo.let {
                    viewModelScope.launch {
                        repository.insertTodo(it!!)
                    }
                }

            }
            is TodoListEvent.OnDoneChange -> {
                viewModelScope.launch {
                    repository.insertTodo(
                        event.todo.copy(
                            isDone = event.isDone
                        )
                    )
                }
            }
            is TodoListEvent.OnDeleteTodoClick ->{
                viewModelScope.launch {
                    deletedTodo = event.todo
                    repository.deleteTodo(event.todo)
                    sendUiEvent(
                        UiEvent.ShowSnackBar(
                            message = "Todo Deleted",
                            action = "Undo"
                        )
                    )
                }
            }
        }
    }
    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

}