package dev.hoyeon.socket

class NicknameGenerator {

    private val prefixes = arrayOf("귀여운", "공부하는", "날으는", "달리는", "흥미로운", "친절한", "영리한", "잘생긴", "아름다운", "기분 좋은", "배고픈", "수줍은", "용감한", "나른한", "활발한", "신비로운", "재미있는", "애교있는", "사랑스러운", "똑똑한", "착한", "예쁜", "멋진", "행복한", "놀라운", "신나는", "상냥한", "빛나는", "따뜻한", "부드러운", "매력적인", "모험적인", "끈기있는", "유쾌한", "다정한", "센스있는", "평화로운")
    private val suffixes = arrayOf("고라니", "사자", "호랑이", "표범", "늑대", "여우", "너구리", "고슴도치", "두더지", "땃쥐", "산양", "사향노루", "멧돼지", "수달", "반달가슴곰", "청설모", "날다람쥐", "하늘다람쥐", "관박쥐", "긴가락박쥐", "박쥐", "뿔박쥐", "스라소니", "오소리", "산달", "족제비", "제주족제비", "토끼")

    fun getRandomNickname(): String {
        return prefixes.random() + " " + suffixes.random()
    }
}