# AGENT.md

## Development Guide – Elixir Anti-patterns

This document curates the official Elixir anti-pattern catalogue so every contributor can quickly reference common “code smells” and their remedies when working on Votis Wallet services.

Source (always up-to-date): <https://hexdocs.pm/elixir/what-anti-patterns.html>

### Why it matters
Anti-patterns are recurring mistakes that negatively affect readability, maintainability, performance, or reliability. Recognising them early helps us:
• keep the codebase approachable for newcomers
• avoid regressions and subtle bugs
• spend less time on refactors later

### Categories covered by the guide
1. **Code-related** – mis-use of language features or idioms (deep nesting, complex pattern matches, unnecessary macros).
2. **Design-related** – modules / functions that take on too many responsibilities, large parameter lists, etc.
3. **Process-related** – sub-optimal use of processes/OTP abstractions (one process per request without need, message storms, etc.).
4. **Meta-programming** – macro overuse that obscures control flow or hinders tooling.

Each documented anti-pattern contains:
• **Name** – concise identifier (e.g., “God Module”).
• **Problem** – why the smell harms the code.
• **Example** – minimal snippet illustrating the issue.
• **Refactoring** – step-by-step improvement.
• **Additional remarks** – legitimate exceptions.

### How we use this list
1. While coding, skim the category headers and ask: _does my change introduce any smell?_  
2. In PR reviews, reference the specific anti-pattern when suggesting improvements:  
   “This looks like the **Duplicated Code** anti-pattern – can we extract a helper?”
3. When we must accept an anti-pattern (e.g., pragmatic optimisation), add a brief code comment with rationale and a FIXME/NOLINT tag if appropriate.
4. During quarterly refactor sessions, prioritise modules with the heaviest concentration of anti-patterns.

### Quick links to detailed sections
• Overview: <https://hexdocs.pm/elixir/what-anti-patterns.html>  
• Code-related: <https://hexdocs.pm/elixir/code-related-anti-patterns.html>  
• Design-related: <https://hexdocs.pm/elixir/design-related-anti-patterns.html>  
• Process-related: <https://hexdocs.pm/elixir/process-related-anti-patterns.html>  
• Meta-programming: <https://hexdocs.pm/elixir/meta-programming-anti-patterns.html>

---
_Last updated: 2025-08-23_

