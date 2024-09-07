package com.codersee.s3.controller

import io.awspring.cloud.s3.S3Template
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.S3Client
import java.util.*
import kotlin.text.Charsets.UTF_8

@RestController
@RequestMapping("/buckets")
class BucketController(
  private val s3Template: S3Template,
  private val s3Client: S3Client,
) {

  @GetMapping
  fun listBuckets(): List<String> {
    val response = s3Client.listBuckets()

    return response.buckets()
      .mapIndexed { index, bucket ->
        "Bucket #${index + 1}: ${bucket.name()}"
      }
  }

  @PostMapping
  fun createBucket(@RequestBody request: BucketRequest) {
    s3Template.createBucket(request.bucketName)
  }

//  @PostMapping("/{bucketName}/objects")
//  fun createObject(@PathVariable bucketName: String, @RequestBody request: ObjectRequest) {
//
//    s3Template.store(bucketName, request.objectName, request.content)
//  }

  @PostMapping("/{bucketName}/objects")
  fun createExampleObject(@PathVariable bucketName: String): Example {
    val example = Example(id = UUID.randomUUID(), name = "Some name")

    s3Template.store(bucketName, "example.json", example)

    return s3Template.read(bucketName, "example.json", Example::class.java)
  }

  @GetMapping("/{bucketName}/objects")
  fun listObjects(@PathVariable bucketName: String): List<String> =
    s3Template.listObjects(bucketName, "")
      .map { s3Resource -> s3Resource.filename }

  @GetMapping("/{bucketName}/objects/{objectName}")
  fun getObject(@PathVariable bucketName: String, @PathVariable objectName: String): String =
    s3Template.download(bucketName, objectName).getContentAsString(UTF_8)


  @DeleteMapping("/{bucketName}")
  fun deleteBucket(@PathVariable bucketName: String) {
    s3Template.listObjects(bucketName, "")
      .forEach { s3Template.deleteObject(bucketName, it.filename) }

    s3Template.deleteBucket(bucketName)
  }

  data class BucketRequest(val bucketName: String)

  data class ObjectRequest(val objectName: String, val content: String)

  data class Example(val id: UUID, val name: String)

}

