package com.example.consumerservice.repository
import com.example.consumerservice.model.DataEventDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
@Repository
interface DataEventRepository : MongoRepository<DataEventDocument, String>
