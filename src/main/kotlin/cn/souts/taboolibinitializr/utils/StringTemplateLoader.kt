package cn.souts.taboolibinitializr.utils

import freemarker.cache.TemplateLoader
import java.io.Reader
import java.io.StringReader

class StringTemplateLoader : TemplateLoader {
    override fun findTemplateSource(p0: String?): Any {
        return p0.toString()
    }

    override fun getLastModified(p0: Any?): Long {
        return System.currentTimeMillis()
    }

    override fun getReader(p0: Any?, p1: String?): Reader {
        return StringReader(p0.toString().replace("_zh_CN_#Hans", ""))
    }

    override fun closeTemplateSource(p0: Any?) {
    }
}