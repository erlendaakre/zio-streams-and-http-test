# zio-streams-and-http-test

Simple app for trying out zio-streams and zio-http

## Usage
- run `Application`
- browse to  `http://localhost:8090/` to count all events
- browse to  `http://localhost:8090/after/<unix time>` to count events from that timestamp

## TODO
- Fix broken use of REF with mutable collection
- Use zlayers to separate http and event processing
- improve `webApp`, Option use looks ugly, deduplicate param handling?