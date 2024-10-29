import _root_.controllers._
import _root_.controllers.andarbahar._

import com.softwaremill.macwire._

import scala.concurrent.Future
import akka.actor.ActorSystem
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.mvc.DefaultControllerComponents
import play.api.routing.Router
import router.Routes
import scalikejdbc.config.DBs
import services._
import dao._
import play.filters.cors.CORSFilter
import play.api.http.DefaultHttpFilters
import play.api.http.EnabledFilters


class AppLoader extends ApplicationLoader {

  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents
    with AssetsComponents {

  val log: Logger = Logger(this.getClass)

  lazy val default: Default = wire[Default]

  lazy val router: Routes = {
    val prefix = "/"
    wire[Routes] //replace it with constructor if you do manual DI
  }
  
  override lazy val actorSystem: ActorSystem = ActorSystem("baccaratActorSystem")
  lazy val maybeRouter: Option[Router] = Option(router)
  override lazy val httpErrorHandler: ProdErrorHandler = wire[ProdErrorHandler]
  override lazy val httpFilters: Seq[Nothing] = Seq()

  //Common Action builders
//  lazy val userAwareAction: UserAwareAction = wire[UserAwareAction]

  //Common Controllers
  override lazy val controllerComponents: DefaultControllerComponents = wire[DefaultControllerComponents]


  //Micro Service 0  - Game Service
  lazy val appController: AppController = wire[AppController]
  lazy val apiController: AppApiController = wire[AppApiController]
  lazy val gameService: GameService = wire[GameService]
  lazy val logDao: LogDao = wire[LogDao]
  lazy val playerDao: PlayerDao = wire[PlayerDao]
  lazy val lobbyDao: LobbyDao = wire[LobbyDao]


  //Micro Service 1 - User Account Service
//  lazy val UserAccountsService: UserAccountsService = wire[UserAccountsService]
//  lazy val authService: AuthService = wire[AuthService]
//  lazy val userDao: UserDao = wire[UserDao]
//  lazy val sessionDao: SessionDao = wire[SessionDao]

  //Micro Service 2 - Config Service
//  lazy val configService: ConfigService = wire[ConfigService]
//  lazy val configDao: ConfigDao = wire[ConfigDao]

  //Micro Service 5 - AB Stadium Seater Service
  lazy val abStadiumSeaterController: AndarBaharSSeaterController = wire[AndarBaharSSeaterController]
  lazy val abSSeaterTableService: AndarBaharSSeaterTableService = wire[AndarBaharSSeaterTableService]
  lazy val abDao: AndarBaharDao = wire[AndarBaharDao]

  applicationLifecycle.addStopHook { () =>
    DBs.closeAll()
    Future.successful(())
  }

  val onStart: Unit = {
    log.logger.info("AppLoader onStart")

    gameService.init()
    abSSeaterTableService.init()
  }
}
