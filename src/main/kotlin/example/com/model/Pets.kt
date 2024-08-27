package example.com.model

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Pets : IntIdTable("pets") {
    val name = varchar("name", 100)
    val type = varchar("type", 50)
    val age = integer("age")
}

class Pet(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Pet>(Pets)
    var name by Pets.name
    var type by Pets.type
    var age by Pets.age

    fun toDTO(): PetDTO = PetDTO(id.value, name, type, age)
}

@Serializable
data class PetDTO(val id: Int, val name: String, val type: String, val age: Int)

@Serializable
data class PetCreateDTO(val name: String, val type: String, val age: Int)

@Serializable
data class PetUpdateDTO(val name: String? = null, val type: String? = null, val age: Int? = null)

class PetService(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Pets)
        }
    }

    suspend fun create(petCreateDTO: PetCreateDTO): PetDTO = dbQuery {
        Pet.new {
            name = petCreateDTO.name
            type = petCreateDTO.type
            age = petCreateDTO.age
        }.toDTO()
    }

    suspend fun getPetById(id: Int): PetDTO? = dbQuery {
        Pet.findById(id)?.toDTO()
    }
    suspend fun getAllPets(): List<PetDTO> = dbQuery {
        Pet.all().map { it.toDTO() }
    }

    suspend fun updatePet(id: Int, petUpdateDTO: PetUpdateDTO): PetDTO? = dbQuery {
        val pet = Pet.findById(id)
        pet?.apply {
            petUpdateDTO.name?.let { name = it }
            petUpdateDTO.type?.let { type = it }
            petUpdateDTO.age?.let { age = it }
        }?.toDTO()
    }

    suspend fun deletePet(id: Int): Boolean = dbQuery {
        Pet.findById(id)?.delete() != null
    }

    suspend fun getPetsByType(type: String): List<PetDTO> = dbQuery {
        Pet.find { Pets.type eq type }.map { it.toDTO() }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
