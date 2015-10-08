# Welcome to the Scynapse

Scynapse enables the use of Axon with Scala

This version (0.4.2) works with Axon version 2.4.2

## A quick start in using scynapse (core)

1) Setup a structure with an event store that makes use of the XStreamSerializer found in scynapse-core

2) Create your aggregate root as

    class MyAggregateRoot extends AbstractAnnotatedAggregateRoot[MyIdentifier]

      @AggregateIdentifier
      private var id : MyIdentifier = _

3) Create your Commands and Events

Note that Commands need an annotation in order to route them to the proper AggregateRoot instance using
the @aggregateId annotation

    case class MyCommand(@aggregateId myId: MyIdentifier, otherParam: String)

4) Have the Aggregate Root handle the commands that results in events

    @CommandHandler
      def handle(cmd: MyCommand) {
        apply(MyCommandHappened(id))
      }

  Creation of a new aggregate root works with a command and event handler like this:
  
     @CommandHandler
      def this(cmd: CreateMyAggregate) = {
        this()
        apply(MyAggregateCreated(cmd.myId, more values))
      }
    
      @EventHandler
      def on(e: MyAggregateCreated) = {
        id = e.myId 
      }

5) Update the state (if required) in the aggregate root

      @EventHandler
      def on(e: MyCommandHappened) {
        someState = Some(e.otherParam)
      }

6) Have event handlers in views build up specific state.


## Integrate with Akka

`scynapse-akka` module provides facilities that make it easier to
integrate Axon components with Akka.

### Subscribing actors to events

`AxonEventBusExtension` allows to subscribe Akka actors to events
published on Axon event bus.  It is implemented as an
[Akka extension](http://doc.akka.io/docs/akka/2.3.12/scala/extending-akka.html)
that manages event bus subscriptions.

In order to subscribe an `ActorRef` to the event bus you should first
initialize an `AxonAkkaBridge`. Here is an example(using Spring):

    import org.axonframework.eventhandling.EventBus
    import com.thenewmotion.scynapse.akka.AxonEventBusExtension

    val eventBus = getBean("eventBus", classOf[EventBus])
    val axonAkkaBridge = AxonEventBusExtension(actorSystem) forEventBus eventBus

Then `axonAkkaBridge` can be used to subscribe actors to the Event bus:

    val eventListener: ActorRef = context.actorOf(...)
    axonAkkaBridge subscribe eventListener

After that `eventListener` actor will receive all events published to
the event bus as simple messages.

To unsubscribe actor from the event bus:

    axonAkkaBridge unsubscribe eventListener

If actor is terminated unsubscription occurs automatically.


### Sending commands from actors

In order to make sending domain commands from Akka components easier a
`CommandGatewayActor` was introduced. It is just a simple actor
interface for the Axon `CommandBus` that dispatches all messages it
receives to a command bus. A result returned by the command handler
(if any) is sent back to the original command sender.

Example usage (again, we're using Spring context here):

    import org.axonframework.commandhandling.CommandBus
    import com.thenewmotion.scynapse.akka.CommandGatewayActor

    val commandBus = getBean("commandBus", classOf[CommandBus])
    val cmdGateway = actorSystem.actorOf(CommandGatewayActor.props(commandBus))

    ...

    cmdGateway ! CreateOrder(...)



## Make use of scalatest to test your domain logic

It's possible to make use of the Axon given -> when -> then test logic in scalatest and have matchers that work in scala style.
The test in the scynapse-test package shows best in what way this works.

# Dependencies

In order to make use of the the scynapse framework, you need to include in your build.sbt

For Scynapse core:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-core"           % 0.4.2
    )

For Scynapse akka:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-akka"           % 0.4.2
    )

For Scynapse test:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-test"           % 0.4.2 % "test"
    )


# Development of Scynapse

For the development of scynapse, you need [SBT](http://www.scala-sbt.org)
The build is setup in the project folder and with

    sbt publishLocal

You will build and publish the packages to your local machine.
