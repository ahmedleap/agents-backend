# Response DTOs

This folder contains Data Transfer Objects (DTOs) for HTTP response payloads.

## Purpose
Response DTOs define the structure of outgoing HTTP responses from the API. They map entity data to client-friendly JSON structures.

## Examples (to be implemented)
- `AdminResponse` - Admin entity as JSON
- `ClientResponse` - Client entity as JSON (excludes password hash)
- `AccountResponse` - Account entity as JSON
- `OrderResponse` - Order entity as JSON
- `PortfolioResponse` - Portfolio valuation summary
- `ErrorResponse` - Standardized error format

## Pattern
Response DTOs typically include:
- All publicly visible fields from entities
- Exclude sensitive fields (password hashes, tokens)
- Include related entity IDs (for API linking)
- Clear naming convention: `<Entity>Response`, `<Entity>DTO`, or `<Entity>Summary`

## Best Practices
- Use `@JsonProperty(access = JsonProperty.Access.READ_ONLY)` for read-only fields
- Null fields should be documented or use `@JsonInclude(Include.NON_NULL)`
- Include timestamps in responses for audit trails
- Flatten nested objects when appropriate (e.g., include `clientId` instead of full `client` object)

## Related
- See `requests/` folder for Request DTOs
- Controllers return these DTOs via `ResponseEntity<ResponseDTO>`
- Services convert entities to DTOs before returning to controllers
