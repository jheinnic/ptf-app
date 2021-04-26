package name.jchein.demo.lagom.hello_worldstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import name.jchein.demo.lagom.hello_worldstream.api.HelloWorldStreamService
import name.jchein.demo.lagom.hello_world.api.HelloWorldService

import scala.concurrent.Future

/**
  * Implementation of the HelloWorldStreamService.
  */
class HelloWorldStreamServiceImpl(helloWorldService: HelloWorldService) extends HelloWorldStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(helloWorldService.hello(_).invoke()))
  }
}
