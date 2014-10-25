# Welcome to the Scynapse 

Scynapse enables the use of Axon with Scala

This version (0.2.8) works with Axon version 2.3.2 

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

5) Update the state (if required) in the aggregate root

      @EventHandler
      def on(e: MyCommandHappened) {
        someState = Some(e.otherParam)
      }

6) Have event handlers in views build up specific state. 


## Make use of akka actors for event listening



## Make use of scalatest to test your domain logic

It's possible to make use of the Axon given -> when -> then test logic in scalatest and have matchers that work in scala style. 
The test in the scynapse-test package shows best in what way this works. 

# Dependencies

In order to make use of the the scynapse framework, you need to include in your build.sbt

For Scynapse core:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-core"           % 0.2.8
    )
    
For Scynapse akka:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-akka"           % 0.2.8
    )

For Scynapse test:

    libraryDependencies ++= Seq(
        "org.axonframework.scynapse"        %% "scynapse-test"           % 0.2.8 % "test"
    )


