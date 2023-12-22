package com.fillingsnap.server.domain.test.vo

class AnalysisResultVo(

    val document: Document,

    val sentences: List<Sentence>

) {

    class Document(

        val sentiment: String,

        val confidence: Confidence

    )

    class Sentence(

        val content: String,

        val offset: Int,

        val length: Int,

        val sentiment: String,

        val confidence: Confidence,

        val highlights: List<Highlight>,

        val negativeSentiment: NegativeSentimentVo?

    )

    class Confidence(

        val neutral: Double,

        val positive: Double,

        val negative: Double

    )

    class Highlight(

        val offset: Int,

        val length: Int

    )

    class NegativeSentimentVo(

        val sentiment: String,

        val confidence: Double

    )

}