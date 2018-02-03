import io.vertx.core.http.{ClientAuth, HttpMethod}
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.VertxOptions
import io.vertx.scala.core.http.{HttpConnection, HttpServerOptions, HttpServerRequest}
import io.vertx.scala.ext.web.client.WebClient
import io.vertx.scala.ext.web.{Router, RoutingContext}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main {
  var vertx = Vertx.vertx(
    VertxOptions().setWorkerPoolSize(20).setEventLoopPoolSize(1)
  )

  var server = vertx.createHttpServer(
    HttpServerOptions().setClientAuth(
      ClientAuth.NONE
    )
      .setPort(8080)
  )

//  server.connectionHandler((conn : HttpConnection) => {
//    println("aaaaa")
//  })
//
//  server.exceptionHandler((exception : Throwable) => {
//    exception.printStackTrace()
//  })
//
//  server.requestHandler((request: HttpServerRequest) => {
//    request.response().end("hello world")
//  })

  var router = Router.router(vertx)

  var client = WebClient.create(vertx)

  router.exceptionHandler((exception : Throwable) => {
        exception.printStackTrace()
      })

  router.route("/test").method(HttpMethod.GET)
    .blockingHandler((ctx : RoutingContext) => {

      client.get("www.baidu.com", "").sendFuture().onComplete {
        case Success(result) => {
          ctx.request().response().end(result.body().get)
        }
        case Failure(result) => {
          ctx.request().response().end("net error")
          println(result.getMessage)
        }
      }

    })

  router.route("/quick").method(HttpMethod.GET)
    .handler((ctx : RoutingContext) => {
      ctx.request().response().end("okokokok")
    })

  router.route("/exception").method(HttpMethod.GET)
    .handler((ctx : RoutingContext) => {
      ctx.request().response().setStatusCode(404)
      ctx.fail(404)
    })

  router.route().failureHandler((ctx: RoutingContext) => {
    if(ctx.statusCode() == 404) {
      ctx.reroute("/error")
    } else {
      ctx.next()
    }
  })

  router.route("/error").handler((ctx: RoutingContext) => {
    ctx.response().end("error!!!")
  })

  def main(args: Array[String]): Unit = server.requestHandler(router.accept _).listen()

}
