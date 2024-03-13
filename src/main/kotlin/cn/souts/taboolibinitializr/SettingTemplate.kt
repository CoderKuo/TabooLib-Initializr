package cn.souts.taboolibinitializr

data class SettingTemplate(
    var group: String? = null,
    var name: String? = null,
    var description: Description? = null,
    var env: Env? = null
)


data class Env(
    var modules: MutableList<Module>? = null
)

data class Module(var name: String? = null, var desc: String? = null, var dependency: List<String>? = null)

data class Description(
    var contributors: List<Contributor>? = null,
    var links: List<Link>? = null, var name: String? = null
)

data class Contributor(var name: String? = null, var description: String? = null)

data class Link(var name: String? = null, var url: String? = null)


data class Template(var name: String? = null, var path: String? = null)