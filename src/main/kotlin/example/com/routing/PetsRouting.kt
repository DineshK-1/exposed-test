package example.com.routing

import example.com.model.*
import example.com.plugins.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configurePetRoutes() {
    val db = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )

    val petService = PetService(db);

    routing {
        route("/pets") {
            get {
                val pets = petService.getAllPets()
                call.respond(pets)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@get
                }
                val pet = petService.getPetById(id)
                if (pet == null) {
                    call.respond(HttpStatusCode.NotFound, "Pet not found")
                } else {
                    call.respond(pet)
                }
            }

            post {
                val petCreateDTO = call.receive<PetCreateDTO>()
                val pet = petService.create(petCreateDTO)
                call.respond(HttpStatusCode.Created, pet)
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@put
                }
                val petUpdateDTO = call.receive<PetUpdateDTO>()
                val updatedPet = petService.updatePet(id, petUpdateDTO)
                if (updatedPet == null) {
                    call.respond(HttpStatusCode.NotFound, "Pet not found")
                } else {
                    call.respond(updatedPet)
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                    return@delete
                }
                val deleted = petService.deletePet(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Pet not found")
                }
            }

            get("/type/{type}") {
                val type = call.parameters["type"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Type is required")
                val pets = petService.getPetsByType(type)
                call.respond(pets)
            }
        }
    }
}