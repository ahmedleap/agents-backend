# Request DTOs

This folder contains Data Transfer Objects (DTOs) for HTTP request payloads.

## Purpose
Request DTOs define the structure of incoming HTTP POST/PUT requests to the API. They map client input to Spring controllers and services.

## Examples (to be implemented)
- `CreateAdminRequest` - Admin creation endpoint payload
- `CreateClientRequest` - Client registration payload
- `CreateAccountRequest` - Account opening payload
- `PlaceOrderRequest` - Order submission payload
- `DepositRequest` - Cash deposit request

## Pattern
Request DTOs typically include:
- Request-specific fields (may differ from entity structure)
- Validation annotations (`@NotNull`, `@Size`, `@Email`, etc.)
- No getters/setters needed with Lombok (when enabled)
- Clear naming convention: `Create<Entity>Request`, `Update<Entity>Request`, `<Action>Request`

## Related
- See `response/` folder for Response DTOs
- Controllers map requests to these DTOs
- Services convert DTOs to entities
