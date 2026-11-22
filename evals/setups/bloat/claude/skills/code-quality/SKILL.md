---
name: code-quality
description: Code quality guidelines for HelloBill. Use for any code change.
---

# Code quality


## Naming

- Use names that say what a thing is for, not how it works.
- Classes are nouns, methods are verbs.
- Avoid abbreviations except well-known ones like id, url and api.
- Boolean names read as questions: isPaid, hasItems, canSend.
- Keep the same word for the same idea everywhere: customer, not client in one place and buyer in another.
- Test names describe behaviour: chargesTwoPercentAfterThirtyDays, not testLateFee1.
- Constants are UPPER_SNAKE_CASE in Java and camelCase in TypeScript modules.
- File names match the main thing they export.

## Functions

- A function does one thing.
- Keep functions short enough to read without scrolling.
- Prefer early returns over deep nesting.
- Don't pass more than three or four parameters; use a record or an object.
- Avoid flag parameters that switch behaviour.
- Pure functions are easier to test: keep calculation apart from input and output.
- Name the result, not the steps.
- Don't return null when an empty list or Optional says it better.

## Errors

- Fail fast on bad input, at the edge of the system.
- Use the existing exception types: BadRequestException for 400 and NotFoundException for 404.
- Never swallow an exception without logging it.
- Error messages say what was wrong and what is allowed.
- Don't use exceptions for normal control flow.
- Validate request bodies with Bean Validation annotations.
- Return the same error shape everywhere: ApiErrorHandler does this.
- In the frontend, show errors with the ErrorMessage component.

## Comments

- Comments say why, not what.
- Delete commented-out code.
- Keep Javadoc on public methods short and true.
- A TODO has a reason and a name.
- Don't repeat the method name in its comment.
- Update comments when you change the code they describe.

## Style

- Follow the formatter: Spotless for Java, Prettier for TypeScript.
- Imports are sorted and unused ones removed.
- One public class per file.
- No wildcard imports.
- Prefer final fields and records for data.
- Prefer const in TypeScript; never var.
- Keep lines under 100 characters.
- Use the project's existing patterns before inventing a new one.

## Reviews

- Read the whole diff before commenting.
- Check the tests first: do they test real values?
- Look for missing error handling.
- Look for money done with double.
- Look for N+1 queries in loops.
- Check that the docs and types changed with the API.
- Check names and comments for accuracy.
- Ask whether the change could be smaller.

