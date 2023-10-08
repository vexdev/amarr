package amarr.amule.model

import com.iukonline.amule.ec.ECSearchFile

data class SearchFile(
    val query: String,
    val fileName: String,
    val hash: String,
    val sizeFull: Long,
    val sourceCount: Int,
    val sourceXfer: Int,
    val aaa: Byte,
    val raw: ECSearchFile
)
