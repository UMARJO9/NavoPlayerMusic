package tj.umar.navoplayer.core.domain.model

enum class RepeatMode {
    Off,
    All,
    One,
    ;

    fun next(): RepeatMode = when (this) {
        Off -> All
        All -> One
        One -> Off
    }
}
