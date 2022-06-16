package com.dogukan.todolistapp.ui.add_edit_todo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogukan.todolistapp.data.Todo
import com.dogukan.todolistapp.data.TodoRepository
import com.dogukan.todolistapp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var todo by mutableStateOf<Todo?>(null)
        private set

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        val todoId = savedStateHandle.get<Int>("todoId")
        if (todoId != -1){
            viewModelScope.launch {
                if (todoId != null) {
                    repository.getTodoById(todoId)?.let {
                        title = it.title
                        description = it.description ?: ""
                        this@AddEditTodoViewModel.todo = it
                    }
                }

            }
        }
    }
    fun onEvent(event : AddEditTodoEvent){
        when(event){
            is AddEditTodoEvent.OnTitleChange -> {
                title = event.title
            }
            is AddEditTodoEvent.OnDescriptionChange ->{
                description = event.description
            }
            is AddEditTodoEvent.OnSaveTodoClick ->{
                viewModelScope.launch {
                    if (title.isBlank()){
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "The title can't be empty"
                        ))
                        return@launch

                    }
                    repository.insertTodo(
                        Todo(
                            title = title,
                            description = description,
                            isDone = todo?.isDone ?: false,
                            id = todo?.id

                        )

                    )
                    sendUiEvent(UiEvent.PopBackStack)
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