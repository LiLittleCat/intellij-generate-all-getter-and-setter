package com.github.lilittlecat.intellijgenerateallgetter.services

import com.intellij.openapi.project.Project
import com.github.lilittlecat.intellijgenerateallgetter.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
