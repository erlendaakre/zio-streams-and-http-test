# zio-streams-and-http-test

Simple app for trying out zio-streams and zio-http

## Usage
- run `Application`
  
## Endpoints
- `http://localhost:8090/` to count all events
- `http://localhost:8090/before/$timestamp` to count events before $timestamp
- `http://localhost:8090/after/$timestamp` to count events after $timestamp
- `http://localhost:8090/between/$timestamp1/$timestamp2` to count events between $timestamp1 and $timestamp2

## TODO
- Fix broken use of REF!
- Optimise memory usage of stored events (ZStream, ZTransducer?) 
- Use zlayers for separating http / event processing and handling dependencies
- improve `webApp`, Option use looks ugly, deduplicate param handling?