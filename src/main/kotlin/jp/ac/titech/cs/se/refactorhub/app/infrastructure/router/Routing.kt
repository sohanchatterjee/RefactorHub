package jp.ac.titech.cs.se.refactorhub.app.infrastructure.router

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import jp.ac.titech.cs.se.refactorhub.app.interfaces.controller.UserController
import org.koin.ktor.ext.inject

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Routing.api() {

    route("/api") {

        val userController: UserController by inject()
        get<UserController.GetUser> {
            call.respond(userController.get(it))
        }
        get<UserController.GetDrafts> {
            call.respond(userController.getDrafts(it))
        }
        get<UserController.GetRefactorings> {
            call.respond(userController.getRefactorings(it))
        }
        get<UserController.GetMe> {
            call.respond(userController.getMe())
        }

    }
}
