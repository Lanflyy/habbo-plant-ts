# Plants

A Java G-Earth extension for treating and composting monster plants in the current room.

## Commands

* `:plants` - Treat all living plants
* `:plants compost` - Compost all dead plants
* `:plants abort` - Abort plant processing

## Build

```bash
mvn -DskipTests package
```

The built extension jar is created in `target/bin`.
