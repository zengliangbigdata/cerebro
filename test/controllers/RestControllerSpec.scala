package controllers

import elastic.ElasticResponse
import models.ElasticServer
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

object RestControllerSpec extends MockedServices {

  def is =
    s2"""
    RestController should                               ${step(play.api.Play.start(application))}
      invoke refreshIndex                               $executeRequest
      should throw exception if method param is missing $missingMethod
      should throw exception if path param is missing   $missingPath
                                                         ${step(play.api.Play.stop(application))}
      """

  def executeRequest = {
    val expectedResponse = Json.parse(
      """
        |{
        |  "_shards": {
        |    "total": 10,
        |    "successful": 5,
        |    "failed": 0
        |  }
        |}
      """.stripMargin
    )
    val body = Json.obj("host" -> "somehost", "method" -> "GET", "path" -> "/someesapi")
    client.executeRequest("GET", "/someesapi", None, ElasticServer("somehost", None)) returns Future.successful(ElasticResponse(200, expectedResponse))
    val response = route(FakeRequest(POST, "/rest/request").withBody(body)).get
    contentAsJson(response) mustEqual expectedResponse and
      (status(response) mustEqual 200)
  }

  def missingPath = {
    val body = Json.obj("host" -> "somehost", "method" -> "GET")
    val response = route(FakeRequest(POST, "/rest/request").withBody(body)).get
    contentAsJson(response) mustEqual Json.obj("error" -> "Missing required parameter path") and
      (status(response) mustEqual 400)

  }

  def missingMethod = {
    val body = Json.obj("host" -> "somehost", "path" -> "GET")
    val response = route(FakeRequest(POST, "/rest/request").withBody(body)).get
    contentAsJson(response) mustEqual Json.obj("error" -> "Missing required parameter method") and
      (status(response) mustEqual 400)
  }

}
