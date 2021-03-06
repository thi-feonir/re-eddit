package app.re_eddit.ui.state

sealed class UiState {
    object Loading : UiState()
    data class Data<T>(val data: T) : UiState()
    data class Error(val error: String) : UiState()
}