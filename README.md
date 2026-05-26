# Messengr

- Repository contains code implementing inter process and distributed messaging patterns to enable building of distributed applications.
- But more than that currently its a playground for these concepts.
- The idea is to provide Java applications with similar mechanisms to modern C# frameworks such as MediatR and Wolverine.
- Java ecosystem falls behind in use cases for event driven architectures so this is a way to demonstrate the concepts inside Java

## Messengr messaging
- lightweight library allowing for in process messaging via mediators for the messaging
- this approach decouples implementation from message routing which is beneficial in bigger codebases because it encourages better splitting and testing of code

### Commands
- CommandBus interface allows to invoke commands
- Provides InProcessCommandBus implementation that invokes registered Command Handlers
- Command.Handler implementors are the endpoint, command handling is one to one, meaning a command is always received by a single handler

### Events
- EventBus interface allows publishing of events
- Provides InProcessEventBus implementation that routes the published Event to all Event Handlers
- Event.Handler implementors receive events of their specified type and are allowed to execute an action, if Event type is not specified the handler will receive ALL events

## Messengr outbox
- support for outboxing events produced by Messengr messaging
- useful when application transitions from Modular monolith to a Distributed application - Microservices

### Outboxing
- OutboxStoragePort needs to be configured, which is a database where the message can be stored, to work properly the storing of the event will run in transaction that publishes it, this would usually be done with a database
- OutboxRelayBackendPort needs to be configured so that stored messages can be relayed to a broker - Kafka, Redis, RabbitMQ...

## Messengr inbox
- Not started yet
- deduplication of incoming messages to achieve processing of the messages exactly once
- in conjuction with an outbox which ensures that a message is published at least once the inbox deduplicates and ensures the message is processed exactly once in case outbox published it multiple times due to a crash for example
